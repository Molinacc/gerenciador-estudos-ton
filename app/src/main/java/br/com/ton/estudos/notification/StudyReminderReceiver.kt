package br.com.ton.estudos.notification

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import br.com.ton.estudos.R
import br.com.ton.estudos.StudosApplication

class StudyReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val notification = NotificationCompat.Builder(context, StudosApplication.CHANNEL_REMINDER_ID)
            .setContentTitle("📚 Hora de estudar!")
            .setContentText("Você tem uma sessão de estudos programada.")
            .setSmallIcon(R.drawable.ic_launcher)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()
        manager.notify(2001, notification)
    }
}
