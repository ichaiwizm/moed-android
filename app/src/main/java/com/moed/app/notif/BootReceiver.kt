package com.moed.app.notif

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Replanifie tous les rappels locaux après un redémarrage de l'appareil (les alarmes
 * [android.app.AlarmManager] ne survivent pas au reboot). Déclaré pour
 * `RECEIVE_BOOT_COMPLETED` / `LOCKED_BOOT_COMPLETED` dans le manifeste.
 */
class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action
        if (action != Intent.ACTION_BOOT_COMPLETED &&
            action != Intent.ACTION_LOCKED_BOOT_COMPLETED &&
            action != "android.intent.action.QUICKBOOT_POWERON"
        ) return

        val pending = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                NotificationScheduler.ensureChannels(context)
                NotificationScheduler.rescheduleFromStores(context)
            } catch (_: Throwable) {
                // best-effort : un carnet vide / des réglages absents ne doivent pas crasher.
            } finally {
                pending.finish()
            }
        }
    }
}
