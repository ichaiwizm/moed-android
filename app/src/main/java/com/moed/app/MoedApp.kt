package com.moed.app

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context

/**
 * Classe [Application] racine de Moed.
 *
 * Rôle unique et déterministe : **créer les 4 canaux de notification au boot** de l'app
 * (idempotent — Android ignore une (re)création d'un canal existant). Aucune notification
 * n'est postée ici ; seuls les canaux sont déclarés pour que l'opt-in ultérieur
 * (Réglages, API 33+) puisse router chaque rappel vers son canal.
 *
 * Les 4 canaux (identifiants figés, cf. CONTRACTS §5.5) :
 *  - [CHANNEL_SHABBAT]  `moed_shabbat`  — allumage des bougies (vendredi)
 *  - [CHANNEL_YAHRZEIT] `moed_yahrzeit` — yahrzeit + kaddish
 *  - [CHANNEL_OMER]     `moed_omer`     — compte du Omer (49 jours)
 *  - [CHANNEL_HOLIDAY]  `moed_holiday`  — fêtes, jeûnes, hiloula du jour
 *
 * minSdk = 26 ⇒ [NotificationChannel] est toujours disponible (pas de garde de version nécessaire).
 * 100 % offline : aucune I/O réseau au démarrage.
 */
class MoedApp : Application() {

    override fun onCreate() {
        super.onCreate()
        createNotificationChannels()
    }

    private fun createNotificationChannels() {
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val channels = listOf(
            NotificationChannel(
                CHANNEL_SHABBAT,
                getString(R.string.channel_shabbat_name),
                NotificationManager.IMPORTANCE_DEFAULT,
            ).apply { description = getString(R.string.channel_shabbat_desc) },

            NotificationChannel(
                CHANNEL_YAHRZEIT,
                getString(R.string.channel_yahrzeit_name),
                NotificationManager.IMPORTANCE_DEFAULT,
            ).apply { description = getString(R.string.channel_yahrzeit_desc) },

            NotificationChannel(
                CHANNEL_OMER,
                getString(R.string.channel_omer_name),
                NotificationManager.IMPORTANCE_LOW,
            ).apply { description = getString(R.string.channel_omer_desc) },

            NotificationChannel(
                CHANNEL_HOLIDAY,
                getString(R.string.channel_holiday_name),
                NotificationManager.IMPORTANCE_DEFAULT,
            ).apply { description = getString(R.string.channel_holiday_desc) },
        )

        manager.createNotificationChannels(channels)
    }

    companion object {
        /** Canal : allumage des bougies de Chabbat / Yom Tov. */
        const val CHANNEL_SHABBAT = "moed_shabbat"

        /** Canal : rappels de yahrzeit (kaddish). */
        const val CHANNEL_YAHRZEIT = "moed_yahrzeit"

        /** Canal : compte quotidien du Omer. */
        const val CHANNEL_OMER = "moed_omer"

        /** Canal : fêtes, jeûnes, hiloula du jour. */
        const val CHANNEL_HOLIDAY = "moed_holiday"
    }
}
