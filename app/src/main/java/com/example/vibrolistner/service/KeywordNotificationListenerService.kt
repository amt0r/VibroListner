package com.example.vibrolistner.service

import android.app.Notification
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import com.example.vibrolistner.core.data.KeywordRepository
import com.example.vibrolistner.core.alert.AlertManager
import com.example.vibrolistner.core.vibration.VibrationManager
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.concurrent.ConcurrentHashMap

/**
 * Listens for system notifications and vibrates when a notification matches
 * any user-defined keyword. Vibration loops continuously (1s on, 4s pause)
 * until the matching notification is cleared from the shade.
 *
 * Uses Hilt @EntryPoint for manual dependency injection since
 * NotificationListenerService is system-managed.
 */
class KeywordNotificationListenerService : NotificationListenerService() {

    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface ServiceEntryPoint {
        fun keywordRepository(): KeywordRepository
        fun alertManager(): AlertManager
    }

    private lateinit var repository: KeywordRepository
    private lateinit var alertManager: AlertManager

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    /** Currently active vibration loop jobs, keyed by notification key. */
    private val activeVibrationJobs = ConcurrentHashMap<String, Job>()

    /** Latest snapshot of user-defined keywords. */
    @Volatile
    private var currentKeywords: List<String> = emptyList()

    override fun onCreate() {
        super.onCreate()
        val entryPoint = EntryPointAccessors.fromApplication(
            applicationContext,
            ServiceEntryPoint::class.java,
        )
        repository = entryPoint.keywordRepository()
        alertManager = entryPoint.alertManager()

        // Continuously collect keywords from the database
        serviceScope.launch {
            repository.getAllKeywords().collectLatest { keywords ->
                currentKeywords = keywords.map { it.keyword.lowercase() }
                // Re-evaluate all active notifications when keywords change
                reevaluateActiveNotifications()
            }
        }
    }

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        if (currentKeywords.isEmpty()) return

        val notificationKey = sbn.key
        val matchText = buildNotificationSearchText(sbn)

        val matched = currentKeywords.any { keyword ->
            matchText.contains(keyword, ignoreCase = true)
        }

        if (matched && !activeVibrationJobs.containsKey(notificationKey)) {
            startVibrationLoop(notificationKey)
        }
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification) {
        stopVibrationLoop(sbn.key)
    }

    override fun onDestroy() {
        super.onDestroy()
        // Cancel all vibration jobs and the service scope
        activeVibrationJobs.values.forEach { it.cancel() }
        activeVibrationJobs.clear()
        alertManager.cancelAll()
        serviceScope.cancel()
    }

    /**
     * Builds a combined lowercase string from all notification text fields
     * for keyword matching.
     */
    private fun buildNotificationSearchText(sbn: StatusBarNotification): String {
        val notification = sbn.notification
        val extras = notification.extras

        return buildString {
            append(sbn.packageName)
            append(' ')
            extras.getCharSequence(Notification.EXTRA_TITLE)?.let { append(it).append(' ') }
            extras.getCharSequence(Notification.EXTRA_TEXT)?.let { append(it).append(' ') }
            notification.tickerText?.let { append(it).append(' ') }
        }
    }

    /**
     * Starts a vibration loop coroutine for the given notification key.
     * Loop: vibrate 1 second → pause 4 seconds → repeat.
     */
    private fun startVibrationLoop(notificationKey: String) {
        val job = serviceScope.launch {
            while (isActive) {
                alertManager.triggerAlertPulse()
                delay(5000L) // 2s vibration + 3s pause = 5s total cycle
            }
        }
        activeVibrationJobs[notificationKey] = job
    }

    /**
     * Stops the vibration loop for the given notification key and cancels
     * any ongoing vibration.
     */
    private fun stopVibrationLoop(notificationKey: String) {
        activeVibrationJobs.remove(notificationKey)?.cancel()
        // Only cancel hardware alert if no other loops are active
        if (activeVibrationJobs.isEmpty()) {
            alertManager.cancelAll()
        }
    }

    /**
     * Re-evaluates all currently active notifications against the updated
     * keyword list. Starts new loops for newly matching notifications and
     * stops loops for notifications that no longer match.
     */
    private fun reevaluateActiveNotifications() {
        try {
            val activeNotifications = activeNotifications ?: return

            // Stop loops for notifications that no longer match any keyword
            val keysToRemove = activeVibrationJobs.keys.filter { key ->
                val sbn = activeNotifications.find { it.key == key }
                if (sbn == null) {
                    true // Notification gone, stop loop
                } else {
                    val text = buildNotificationSearchText(sbn)
                    !currentKeywords.any { keyword -> text.contains(keyword, ignoreCase = true) }
                }
            }
            keysToRemove.forEach { stopVibrationLoop(it) }

            // Start loops for newly matching notifications
            for (sbn in activeNotifications) {
                if (activeVibrationJobs.containsKey(sbn.key)) continue
                val text = buildNotificationSearchText(sbn)
                val matched = currentKeywords.any { keyword ->
                    text.contains(keyword, ignoreCase = true)
                }
                if (matched) {
                    startVibrationLoop(sbn.key)
                }
            }
        } catch (_: Exception) {
            // Service may not be connected yet
        }
    }
}
