package com.moed.app.notif

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.kosherjava.zmanim.hebrewcalendar.JewishDate
import com.moed.app.data.AppSettings
import com.moed.app.data.AssetData
import com.moed.app.data.HilulaDate
import com.moed.app.data.City
import com.moed.app.data.Localization
import com.moed.app.data.PersonRecord
import com.moed.app.data.PersonType
import com.moed.app.data.PersonalStore
import com.moed.app.data.Region
import com.moed.app.data.SettingsStore
import com.moed.app.engine.JewishInfo
import com.moed.app.engine.ZmanimEngine
import kotlinx.coroutines.flow.first
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

/**
 * Planifie **localement** (zéro réseau, zéro push serveur) l'ensemble des rappels de Moed.
 *
 * Toutes les échéances sont déterministes (calculées on-device par le moteur KosherJava), donc
 * chaque type de rappel est armé via [AlarmManager] pour sa **prochaine occurrence** ; les rappels
 * récurrents (Chabbat hebdo, Omer quotidien) sont ré-armés en « fenêtre glissante » après chaque
 * déclenchement ([NotificationReceiver]) et au reboot ([BootReceiver]).
 *
 * Contrat figé (CONTRACTS §5.5) :
 *   `NotificationScheduler(context).rescheduleAll(settings, personal, city)`
 *
 * Mapping toggle → canal (1:1) :
 *   [NotifPrefs.shabbat]  → [CH_SHABBAT]  (moed_shabbat)
 *   [NotifPrefs.omer]     → [CH_OMER]     (moed_omer)
 *   [NotifPrefs.yahrzeit] → [CH_YAHRZEIT] (moed_yahrzeit) — carnet familial (yahrzeit + anniversaire)
 *   [NotifPrefs.hilula]   → [CH_HOLIDAY]  (moed_holiday)  — hiloula du jour
 */
class NotificationScheduler(private val context: Context) {

    private val alarmManager: AlarmManager
        get() = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    private val zmanimEngine = ZmanimEngine()
    private val jewishInfo = JewishInfo()

    /**
     * Annule tout ce qui était armé puis replanifie la prochaine occurrence de chaque rappel
     * **activé** dans [AppSettings.notif]. Idempotent et sûr hors-ligne (chaque type est isolé
     * dans un try/catch — une latitude extrême ou une date invalide n'empêche pas les autres).
     */
    fun rescheduleAll(settings: AppSettings, personal: List<PersonRecord>, city: City) {
        ensureChannels(context)

        val zone = runCatching { ZoneId.of(city.tz) }.getOrDefault(ZoneId.systemDefault())
        val now = System.currentTimeMillis()
        val today = LocalDate.now(zone)
        val lang = Localization.resolve(settings.language)
        @Suppress("UNUSED_VARIABLE")
        val il = when (settings.region) {
            Region.IL -> true
            Region.DIASPORA -> false
            Region.AUTO -> city.israel
        }

        // --- Toujours purger l'existant pour éviter les alarmes fantômes ---
        cancelAll()

        val notif = settings.notif

        // 1) Chabbat — vendredi, 30 min avant l'allumage.
        if (notif.shabbat) {
            runCatching {
                var friday = today
                while (friday.dayOfWeek != DayOfWeek.FRIDAY) friday = friday.plusDays(1)
                var attempts = 0
                while (attempts < 3) {
                    val info = zmanimEngine.shabbat(city, friday, settings)
                    val triggerAt = info.candleLighting.toInstant().toEpochMilli() - REMIND_BEFORE_SHABBAT_MS
                    if (triggerAt > now) {
                        val timeStr = info.candleLighting.format(HHMM)
                        schedule(
                            rc = RC_SHABBAT,
                            triggerAt = triggerAt,
                            channel = CH_SHABBAT,
                            title = Localization.string("notif.shabbat.title", lang),
                            text = Localization.string("notif.shabbat.body", lang, timeStr),
                        )
                        break
                    }
                    friday = friday.plusWeeks(1)
                    attempts++
                }
            }
        }

        // 2) Omer — le soir (21 h) où commence la nuit du décompte, tant qu'on est dans la Sefira.
        if (notif.omer) {
            runCatching {
                for (off in 0..OMER_SCAN_DAYS) {
                    val day = today.plusDays(off.toLong())
                    val triggerAt = day.atTime(OMER_HOUR, 0)
                        .atZone(zone).toInstant().toEpochMilli()
                    if (triggerAt <= now) continue
                    // La nuit tombant le soir de `day` appartient à la date hébraïque du lendemain civil.
                    val n = jewishInfo.omerDay(day.plusDays(1))
                    if (n > 0) {
                        schedule(
                            rc = RC_OMER,
                            triggerAt = triggerAt,
                            channel = CH_OMER,
                            title = Localization.string("notif.omer.title", lang),
                            text = Localization.string("home.omerDay", lang, n),
                        )
                        break
                    }
                }
            }
        }

        // 3) Carnet familial (yahrzeit + anniversaire) — la veille au soir (20 h).
        if (notif.yahrzeit) {
            personal.take(MAX_PERSONAL).forEachIndexed { i, person ->
                runCatching {
                    val occurrences = jewishInfo.nextOccurrences(person, today, 2)
                    for (occ in occurrences) {
                        val triggerAt = occ.gregorian.minusDays(1)
                            .atTime(PERSONAL_HOUR, 0)
                            .atZone(zone).toInstant().toEpochMilli()
                        if (triggerAt > now) {
                            val bodyKey = if (person.type == PersonType.BIRTHDAY)
                                "notif.birthday.body" else "notif.yahrzeit.body"
                            schedule(
                                rc = RC_PERSONAL_BASE + i,
                                triggerAt = triggerAt,
                                channel = CH_YAHRZEIT,
                                title = person.name,
                                text = Localization.string(bodyKey, lang),
                            )
                            break
                        }
                    }
                }
            }
        }

        // 4) Hiloula du jour — le matin (8 h) de la date hébraïque de la hiloula.
        if (notif.hilula) {
            val tzadikim = runCatching { AssetData.tzadikim(context) }.getOrDefault(emptyList())
            tzadikim.take(MAX_HILULA).forEachIndexed { i, tz ->
                runCatching {
                    val triggerAt = nextHilulaTrigger(tz.hilula, zone, now) ?: return@runCatching
                    val title = tz.names[lang] ?: tz.names["en"] ?: tz.names.values.firstOrNull()
                    ?: return@runCatching
                    val body = tz.epithet?.get(lang) ?: tz.epithet?.get("en")
                    ?: Localization.string("notif.hilula.body", lang)
                    schedule(
                        rc = RC_HILULA_BASE + i,
                        triggerAt = triggerAt,
                        channel = CH_HOLIDAY,
                        title = title,
                        text = body,
                    )
                }
            }
        }
    }

    /** Annule toutes les alarmes des plages connues (safe même si rien n'était armé). */
    private fun cancelAll() {
        cancel(RC_SHABBAT)
        cancel(RC_OMER)
        for (i in 0 until MAX_PERSONAL) cancel(RC_PERSONAL_BASE + i)
        for (i in 0 until MAX_HILULA) cancel(RC_HILULA_BASE + i)
    }

    /**
     * Prochaine date grégorienne (à 8 h locale) d'une hiloula donnée par (jour, mois hébreu),
     * strictement dans le futur. Renvoie les millis epoch, ou `null` si non résoluble.
     */
    private fun nextHilulaTrigger(hilula: HilulaDate, zone: ZoneId, now: Long): Long? {
        val startYear = JewishDate(LocalDate.now(zone)).jewishYear
        for (off in 0..3) {
            val hebYear = startYear + off
            val month = hebrewMonthConstant(hilula.monthEn, hebYear) ?: continue
            val greg = runCatching {
                val jd = JewishDate(hebYear, month, hilula.day)
                LocalDate.of(jd.gregorianYear, jd.gregorianMonth + 1, jd.gregorianDayOfMonth)
            }.getOrNull() ?: continue
            val triggerAt = greg.atTime(HILULA_HOUR, 0).atZone(zone).toInstant().toEpochMilli()
            if (triggerAt > now) return triggerAt
        }
        return null
    }

    // ---- Armement / annulation AlarmManager ----

    private fun pendingIntent(rc: Int, channel: String, title: String, text: String): PendingIntent {
        val intent = Intent(context, NotificationReceiver::class.java).apply {
            action = ACTION_PREFIX + rc
            putExtra(EXTRA_NOTIF_ID, rc)
            putExtra(EXTRA_CHANNEL, channel)
            putExtra(EXTRA_TITLE, title)
            putExtra(EXTRA_TEXT, text)
        }
        return PendingIntent.getBroadcast(
            context, rc, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
    }

    @SuppressLint("MissingPermission")
    private fun schedule(rc: Int, triggerAt: Long, channel: String, title: String, text: String) {
        val pi = pendingIntent(rc, channel, title, text)
        val canExact = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
            alarmManager.canScheduleExactAlarms() else true
        try {
            if (canExact) {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pi)
            } else {
                alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pi)
            }
        } catch (_: SecurityException) {
            alarmManager.set(AlarmManager.RTC_WAKEUP, triggerAt, pi)
        }
    }

    private fun cancel(rc: Int) {
        val intent = Intent(context, NotificationReceiver::class.java).apply {
            action = ACTION_PREFIX + rc
        }
        val pi = PendingIntent.getBroadcast(
            context, rc, intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE,
        )
        if (pi != null) {
            alarmManager.cancel(pi)
            pi.cancel()
        }
    }

    companion object {
        // --- Canaux (CONTRACTS §5.5) ---
        const val CH_SHABBAT = "moed_shabbat"
        const val CH_YAHRZEIT = "moed_yahrzeit"
        const val CH_OMER = "moed_omer"
        const val CH_HOLIDAY = "moed_holiday"

        // --- Extras d'intent ---
        const val EXTRA_NOTIF_ID = "com.moed.app.notif.extra.NOTIF_ID"
        const val EXTRA_CHANNEL = "com.moed.app.notif.extra.CHANNEL"
        const val EXTRA_TITLE = "com.moed.app.notif.extra.TITLE"
        const val EXTRA_TEXT = "com.moed.app.notif.extra.TEXT"

        private const val ACTION_PREFIX = "com.moed.app.notif.ALARM."

        // --- Request codes (plages disjointes) ---
        private const val RC_SHABBAT = 1001
        private const val RC_OMER = 1002
        private const val RC_PERSONAL_BASE = 2000
        private const val RC_HILULA_BASE = 3000

        private const val MAX_PERSONAL = 100
        private const val MAX_HILULA = 64

        // --- Fenêtres de rappel ---
        private const val REMIND_BEFORE_SHABBAT_MS = 30L * 60L * 1000L // 30 min avant l'allumage
        private const val OMER_HOUR = 21
        private const val PERSONAL_HOUR = 20
        private const val HILULA_HOUR = 8
        private const val OMER_SCAN_DAYS = 60

        private val HHMM: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm", Locale.ROOT)

        /** Crée (idempotent) les 4 canaux de notification. Appelé par MoedApp au boot + à chaque envoi. */
        fun ensureChannels(context: Context) {
            val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            val lang = Localization.resolve("system")
            fun channel(id: String, nameKey: String, descKey: String) {
                val ch = NotificationChannel(
                    id,
                    Localization.string(nameKey, lang),
                    NotificationManager.IMPORTANCE_DEFAULT,
                ).apply { description = Localization.string(descKey, lang) }
                nm.createNotificationChannel(ch)
            }
            channel(CH_SHABBAT, "notif.channel.shabbat.name", "notif.channel.shabbat.desc")
            channel(CH_YAHRZEIT, "notif.channel.yahrzeit.name", "notif.channel.yahrzeit.desc")
            channel(CH_OMER, "notif.channel.omer.name", "notif.channel.omer.desc")
            channel(CH_HOLIDAY, "notif.channel.holiday.name", "notif.channel.holiday.desc")
        }

        /**
         * Recharge les stores (réglages + carnet) et la ville active, puis replanifie tout.
         * Utilisé par [NotificationReceiver] (ré-armement des récurrents) et [BootReceiver].
         */
        suspend fun rescheduleFromStores(context: Context) {
            val settings = SettingsStore(context).flow.first()
            val personal = PersonalStore(context).flow.first()
            AssetData.cities(context) // amorce le cache mémoire
            val city = AssetData.city(settings.citySlug)
                ?: AssetData.cities(context).firstOrNull()
                ?: return
            NotificationScheduler(context).rescheduleAll(settings, personal, city)
        }

        /**
         * mois hébreu translittéré → constante [JewishDate] (NISSAN=1 … ADAR=12, ADAR_II=13),
         * en tenant compte de l'année embolismique (règle métonique) pour Adar.
         */
        private fun hebrewMonthConstant(monthEn: String, hebYear: Int): Int? {
            val leap = ((7L * hebYear + 1) % 19) < 7
            val k = monthEn.trim().lowercase(Locale.ROOT)
            return when {
                k.startsWith("nis") -> JewishDate.NISSAN
                k.startsWith("iyar") || k.startsWith("iyyar") -> JewishDate.IYAR
                k.startsWith("siv") -> JewishDate.SIVAN
                k.startsWith("tam") -> JewishDate.TAMMUZ
                k == "av" || k.startsWith("menachem") -> JewishDate.AV
                k.startsWith("elul") -> JewishDate.ELUL
                k.startsWith("tish") -> JewishDate.TISHREI
                k.startsWith("chesh") || k.startsWith("hesh") || k.startsWith("mar") -> JewishDate.CHESHVAN
                k.startsWith("kis") -> JewishDate.KISLEV
                k.startsWith("tev") || k.startsWith("tes") -> JewishDate.TEVES
                k.startsWith("shev") || k.startsWith("shvat") -> JewishDate.SHEVAT
                k.contains("adar ii") || k.contains("adar 2") || k.contains("adar bet") ->
                    if (leap) JewishDate.ADAR_II else JewishDate.ADAR
                k.contains("adar i") || k.contains("adar 1") || k.contains("adar aleph") ->
                    JewishDate.ADAR
                k.startsWith("adar") -> if (leap) JewishDate.ADAR_II else JewishDate.ADAR
                else -> null
            }
        }
    }
}
