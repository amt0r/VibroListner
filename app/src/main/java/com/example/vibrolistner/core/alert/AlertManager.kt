package com.example.vibrolistner.core.alert

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.os.PowerManager
import com.example.vibrolistner.core.vibration.VibrationManager

/**
 * Manages device alerts including vibration, sound, and screen wake.
 */
class AlertManager(
    private val context: Context,
    private val vibrationManager: VibrationManager
) {
    private var mediaPlayer: MediaPlayer? = null
    private val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
    private var wakeLock: PowerManager.WakeLock? = null

    /**
     * Starts continuous alerts.
     * Wakes the screen, starts the vibration loop, and ensures the alarm sound is playing.
     */
    fun startAlert() {
        wakeScreen()
        vibrationManager.startVibrationLoop()
        playSound()
    }

    /**
     * Cancels any ongoing alert (vibration, sound) and releases wake locks.
     */
    fun cancelAll() {
        vibrationManager.cancel()
        stopSound()
        releaseWakeLock()
    }

    @Suppress("DEPRECATION")
    private fun wakeScreen() {
        if (wakeLock?.isHeld == true) return

        // ACQUIRE_CAUSES_WAKEUP requires one of the deprecated screen wake locks.
        wakeLock = powerManager.newWakeLock(
            PowerManager.SCREEN_BRIGHT_WAKE_LOCK or PowerManager.ACQUIRE_CAUSES_WAKEUP,
            "VibroListener::AlertWakeLock"
        )
        // Acquire wake lock continuously. It will be explicitly released in cancelAll().
        wakeLock?.acquire(10*60*1000L /*10 minutes*/)
    }

    private fun releaseWakeLock() {
        if (wakeLock?.isHeld == true) {
            wakeLock?.release()
        }
    }

    private fun playSound() {
        try {
            if (mediaPlayer?.isPlaying == true) {
                return // Already playing
            }

            // Clean up existing instance if it's not playing but not null
            mediaPlayer?.release()

            val defaultAlarmUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
            mediaPlayer = MediaPlayer().apply {
                setDataSource(context, defaultAlarmUri)
                val audioAttributes = AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_ALARM)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build()
                setAudioAttributes(audioAttributes)
                isLooping = true // Loop the alarm sound until canceled
                prepare()
                start()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun stopSound() {
        try {
            mediaPlayer?.let {
                if (it.isPlaying) {
                    it.stop()
                }
                it.release()
            }
            mediaPlayer = null
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
