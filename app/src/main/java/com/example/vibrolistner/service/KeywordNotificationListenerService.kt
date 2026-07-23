package com.example.vibrolistner.service

import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import com.example.vibrolistner.core.alert.AlertCoordinator
import com.example.vibrolistner.core.data.KeywordRepository
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

/**
 * Listens for system notifications and delegates them to the AlertCoordinator.
 * Acts purely as a framework boundary (Clean Architecture).
 */
class KeywordNotificationListenerService : NotificationListenerService() {

    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface ServiceEntryPoint {
        fun keywordRepository(): KeywordRepository
        fun alertCoordinator(): AlertCoordinator
    }

    private lateinit var repository: KeywordRepository
    private lateinit var alertCoordinator: AlertCoordinator

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    @Volatile
    private var currentKeywords: List<String> = emptyList()

    override fun onCreate() {
        super.onCreate()
        val entryPoint = EntryPointAccessors.fromApplication(
            applicationContext,
            ServiceEntryPoint::class.java,
        )
        repository = entryPoint.keywordRepository()
        alertCoordinator = entryPoint.alertCoordinator()

        // Continuously collect keywords from the database
        serviceScope.launch {
            repository.getAllKeywords().collectLatest { keywords ->
                currentKeywords = keywords.map { it.keyword.lowercase() }
                // We do not re-evaluate active notifications dynamically here to keep it simple,
                // or we could delegate a full re-evaluation to AlertCoordinator.
                // For battery/CPU reasons, only evaluating on POST is usually sufficient.
            }
        }
    }

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        // Delegate to AlertCoordinator which handles background matching and timeout
        alertCoordinator.evaluateNotification(sbn, currentKeywords)
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification) {
        // Delegate removal to AlertCoordinator
        alertCoordinator.stopAlertFor(sbn.key)
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
    }
}
