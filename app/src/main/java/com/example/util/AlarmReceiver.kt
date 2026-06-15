package com.example.util

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.example.MainActivity

class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val productId = intent.getIntExtra("EXTRA_PRODUCT_ID", -1)
        val title = intent.getStringExtra("EXTRA_TITLE") ?: "Recordatorio de Compra"
        val message = intent.getStringExtra("EXTRA_MESSAGE") ?: "¿Sigues interesado en comprar este producto?"

        val mainIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("NOTIFICATION_ROUTE_PRODUCT_ID", productId)
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            productId,
            mainIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notificationBuilder = NotificationCompat.Builder(context, "carrito_universal_purchase_reminders")
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(productId, notificationBuilder.build())
    }
}
