package com.moed.app.notif

import android.Manifest
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.moed.app.MainActivity
import com.moed.app.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Reçoit une alarme planifiée, poste la notification correspondante, puis **ré-arme** la fenêtre
 * glissante (prochaine occurrence des rappels récurrents) en rechargeant les stores.
 */
class NotificationReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val channel = intent.getStringExtra(NotificationScheduler.EXTRA_CHANNEL)
            ?: NotificationScheduler.CH_HOLIDAY
        val notifId = intent.getIntExtra(NotificationScheduler.EXTRA_NOTIF_ID, channel.hashCode())
        val title = intent.getStringExtra(NotificationScheduler.EXTRA_TITLE)
            ?: context.getString(R.string.app_name)
        val text = intent.getStringExtra(NotificationScheduler.EXTRA_TEXT).orEmpty()

        NotificationScheduler.ensureChannels(context)
        postNotification(context, notifId, channel, title, text)

        // Ré-arme les rappels récurrents pour leur prochaine occurrence.
        val pending = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                NotificationScheduler.rescheduleFromStores(context)
            } catch (_: Throwable) {
                // best-effort : jamais de crash dans la livraison d'alarme.
            } finally {
                pending.finish()
            }
        }
    }

    private fun postNotification(
        context: Context,
        notifId: Int,
        channel: String,
        title: String,
        text: String,
    ) {
        if (!hasPostPermission(context)) return

        val openIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val contentPi = PendingIntent.getActivity(
            context, notifId, openIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

        val notification = NotificationCompat.Builder(context, channel)
            .setSmallIcon(R.drawable.ic_flame)
            .setColor(NER_COLOR)
            .setContentTitle(title)
            .setContentText(text)
            .setStyle(NotificationCompat.BigTextStyle().bigText(text))
            .setAutoCancel(true)
            .setContentIntent(contentPi)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()

        NotificationManagerCompat.from(context).notify(notifId, notification)
    }

    private fun hasPostPermission(context: Context): Boolean {
        if (!NotificationManagerCompat.from(context).areNotificationsEnabled()) return false
        return Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) ==
            PackageManager.PERMISSION_GRANTED
    }

    private companion object {
        // Ner (flamme) — primaire light DESIGN §1.1.
        private const val NER_COLOR = 0xFFB4471F.toInt()
    }
}
