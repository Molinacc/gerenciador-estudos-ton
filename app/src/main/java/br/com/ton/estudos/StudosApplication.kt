package br.com.ton.estudos

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class StudosApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        createNotificationChannels()
    }

    private fun createNotificationChannels() {
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        NotificationChannel(
            CHANNEL_TIMER_ID,
            getString(R.string.channel_timer_name),
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = getString(R.string.channel_timer_desc)
            manager.createNotificationChannel(this)
        }

        NotificationChannel(
            CHANNEL_REMINDER_ID,
            getString(R.string.channel_reminder_name),
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = getString(R.string.channel_reminder_desc)
            manager.createNotificationChannel(this)
        }

        NotificationChannel(
            CHANNEL_FLASHCARD_ID,
            getString(R.string.channel_flashcard_name),
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = getString(R.string.channel_flashcard_desc)
            manager.createNotificationChannel(this)
        }
    }

    companion object {
        const val CHANNEL_TIMER_ID = "timer_channel"
        const val CHANNEL_REMINDER_ID = "reminder_channel"
        const val CHANNEL_FLASHCARD_ID = "flashcard_channel"
    }
}
