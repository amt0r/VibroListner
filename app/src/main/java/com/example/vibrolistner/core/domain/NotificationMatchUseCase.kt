package com.example.vibrolistner.core.domain

import android.app.Notification
import android.service.notification.StatusBarNotification
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class NotificationMatchUseCase @Inject constructor() {

    /**
     * Checks if the given notification's text fields match any of the provided keywords.
     * Executes on Dispatchers.Default to ensure heavy string manipulation does not block the UI thread.
     */
    suspend operator fun invoke(sbn: StatusBarNotification, keywords: List<String>): Boolean {
        if (keywords.isEmpty()) return false

        return withContext(Dispatchers.Default) {
            val matchText = buildNotificationSearchText(sbn)
            
            keywords.any { keyword ->
                matchText.contains(keyword, ignoreCase = true)
            }
        }
    }

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
}
