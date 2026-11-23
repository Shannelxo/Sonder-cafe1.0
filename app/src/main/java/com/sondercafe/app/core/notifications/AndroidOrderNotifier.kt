package com.sondercafe.app.core.notifications

import android.Manifest
import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.sondercafe.app.R
import com.sondercafe.app.domain.model.OrderStatus
import com.sondercafe.app.domain.service.OrderNotifier
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AndroidOrderNotifier @Inject constructor(
    @ApplicationContext private val context: Context
) : OrderNotifier {

    init {
        val channel = NotificationChannel(
            CHANNEL_ID,
            context.getString(R.string.notification_channel_orders_name),
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = context.getString(R.string.notification_channel_orders_description)
        }
        context.getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
    }

    @SuppressLint("MissingPermission") // Checked by canPostNotifications().
    override fun notifyStatusChanged(orderId: Long, status: OrderStatus) {
        // If the user said no to notifications, respect that. The order screens still show the
        // live status, so nothing is lost.
        if (!canPostNotifications()) return

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(context.getString(R.string.notification_order_title, orderId))
            .setContentText(context.getString(messageFor(status)))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(openOrderIntent(orderId))
            .setAutoCancel(true)
            .build()

        // One notification per order, updated in place as the status changes.
        NotificationManagerCompat.from(context).notify(orderId.toInt(), notification)
    }

    private fun canPostNotifications(): Boolean {
        if (!NotificationManagerCompat.from(context).areNotificationsEnabled()) return false
        return Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) ==
            PackageManager.PERMISSION_GRANTED
    }

    private fun openOrderIntent(orderId: Long): PendingIntent? {
        val intent = context.packageManager.getLaunchIntentForPackage(context.packageName)
            ?.apply {
                flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
                putExtra(EXTRA_ORDER_ID, orderId)
            }
            ?: return null
        return PendingIntent.getActivity(
            context,
            orderId.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun messageFor(status: OrderStatus): Int = when (status) {
        OrderStatus.PENDING -> R.string.notification_status_pending
        OrderStatus.PREPARING -> R.string.notification_status_preparing
        OrderStatus.READY_FOR_COLLECTION -> R.string.notification_status_ready
        OrderStatus.COLLECTED -> R.string.notification_status_collected
    }

    companion object {
        const val CHANNEL_ID = "order_updates"
        const val EXTRA_ORDER_ID = "com.sondercafe.app.extra.ORDER_ID"
    }
}
