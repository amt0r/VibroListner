package com.example.vibrolistner.core.alert

import android.service.notification.StatusBarNotification
import com.example.vibrolistner.core.domain.NotificationMatchUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.time.Duration.Companion.minutes
import kotlinx.coroutines.awaitCancellation
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AlertCoordinator @Inject constructor(
    private val alertManager: AlertManager,
    private val matchUseCase: NotificationMatchUseCase,
) {
    private val coordinatorScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val activeAlertJobs = ConcurrentHashMap<String, Job>()

    /**
     * Evaluates a notification against the current keywords.
     * If it matches, starts a 5-minute hardware alert loop.
     */
    fun evaluateNotification(sbn: StatusBarNotification, keywords: List<String>) {
        if (keywords.isEmpty()) return

        val notificationKey = sbn.key

        // Already alerting for this notification
        if (activeAlertJobs.containsKey(notificationKey)) return

        coordinatorScope.launch {
            val matched = matchUseCase(sbn, keywords)
            if (matched) {
                startAlertTimer(notificationKey)
            }
        }
    }

    /**
     * Stops the alert for a specific notification.
     */
    fun stopAlertFor(notificationKey: String) {
        activeAlertJobs.remove(notificationKey)?.cancel()

        // If no other alerts are running, stop the hardware completely
        if (activeAlertJobs.isEmpty()) {
            alertManager.cancelAll()
        }
    }

    private fun startAlertTimer(notificationKey: String) {
        val job = coordinatorScope.launch {
            // Start the native hardware loop
            alertManager.startAlert()

            // Run for 5 minutes, then auto-cancel
            withTimeoutOrNull(5.minutes) {
                awaitCancellation()
            }

            // Once the timeout is reached (or job canceled), clean up
            stopAlertFor(notificationKey)
        }
        activeAlertJobs[notificationKey] = job
    }
}
