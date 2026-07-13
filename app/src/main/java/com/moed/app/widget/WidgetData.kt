package com.moed.app.widget

import android.content.Context
import com.moed.app.data.AppSettings
import com.moed.app.data.AssetData
import com.moed.app.data.City
import com.moed.app.data.Localization
import com.moed.app.data.SettingsStore
import com.moed.app.data.Zman
import com.moed.app.engine.JewishInfo
import com.moed.app.engine.ZmanimEngine
import kotlinx.coroutines.flow.first
import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

/**
 * Instantané **100 % offline** (CONTRACTS §5.6, DESIGN §4) de tout ce que les 3 widgets Glance
 * affichent, recalculé sur l'appareil depuis KosherJava — **zéro réseau**.
 *
 * [WidgetData] lit le DataStore (réglages : ville, minhag, langue) via [SettingsStore], résout la
 * ville dans les assets ([AssetData]), puis dérive dates/zmanim/allumage/Omer avec le moteur
 * déterministe ([ZmanimEngine], [JewishInfo]). Le résultat est un [WidgetSnapshot] de chaînes déjà
 * formatées et localisées : le composant Glance ne fait plus aucun calcul.
 *
 * Un zman `null` (latitude extrême, DESIGN §1 / CONTRACTS conventions) → « — », jamais NaN.
 */
data class WidgetSnapshot(
    val cityName: String,
    val hebrewDate: String,
    // --- CandleWidget ---
    val candleTime: String,          // "HH:mm" ou "—"
    val candleCountdown: String,     // « Allumage dans 3 h 12 » ou « Chabbat »
    val shabbatEntered: Boolean,     // true = l'allumage est passé (countdown ≤ 0)
    val havdalaTime: String,         // "HH:mm" ou "—"
    val parasha: String,
    // --- ZmanimWidget ---
    val zmanim: List<ZmanLine>,      // registre du jour (label + heure tabulaire)
    // --- OmerWidget ---
    val omerDay: Int,                // 0 hors période
    val omerLabel: String,
) {
    /** Une ligne du registre zmanim : libellé localisé + heure tabulaire (ou « — »). */
    data class ZmanLine(val label: String, val time: String)
}

/** Recalcul offline des données widget (lit le DataStore). CONTRACTS §5.6. */
object WidgetData {

    private const val TIME_PATTERN = "HH:mm"
    private const val DASH = "—"
    private const val MAX_ZMANIM_LINES = 5

    private val zmanimEngine = ZmanimEngine()
    private val jewishInfo = JewishInfo()

    /**
     * Charge l'instantané courant pour la ville active. Suspend : lit le DataStore une fois
     * ([SettingsStore.flow] → [first]) puis effectue un calcul KosherJava synchrone. Offline.
     */
    suspend fun load(context: Context): WidgetSnapshot {
        val settings: AppSettings = SettingsStore(context).flow.first()
        val city: City = resolveCity(context, settings.citySlug)

        val zone = ZoneId.of(city.tz)
        val today: LocalDate = LocalDate.now(zone)
        val nowInstant: Instant = Instant.now()
        val nowZoned: ZonedDateTime = nowInstant.atZone(zone)
        val lang: String = Localization.resolve(settings.language)
        val timeFmt = DateTimeFormatter.ofPattern(TIME_PATTERN)

        // Registre zmanim du jour (sous-ensemble HOME_ZMANIM), heures tabulaires en colonne.
        val homeZmanim: List<Zman> = zmanimEngine.homeZmanim(city, today, settings)
        val zmanimLines = homeZmanim.take(MAX_ZMANIM_LINES).map {
            WidgetSnapshot.ZmanLine(
                label = Localization.zmanLabel(it.key, lang),
                time = formatTime(it.time, timeFmt),
            )
        }

        // Le jour hébraïque commence au coucher : ancrer la date héb sur le sunset réel du jour.
        val sunset: ZonedDateTime? = homeZmanim.firstOrNull { it.key == "shkiaHachama" }?.time
            ?: zmanimEngine.zmanim(city, today, settings)
                .firstOrNull { it.key == "shkiaHachama" }?.time
        val afterSunset = sunset != null && nowZoned.isAfter(sunset)
        val hebrewDate = jewishInfo.hebrewDate(today, afterSunset, lang)

        // Prochain Chabbat : allumage + havdala + parasha (moteur, minhag de la ville).
        val shabbat = zmanimEngine.shabbat(city, today, settings)
        val secondsToCandle = Duration.between(nowInstant, shabbat.candleLighting.toInstant()).seconds
        val entered = secondsToCandle <= 0L

        // Omer du jour (0 hors période) + libellé localisé « {n}ᵉ jour du Omer ».
        val omerDay = jewishInfo.omerDay(today)
        val omerLabel =
            if (omerDay > 0) Localization.string("home.omerDay", lang, omerDay)
            else Localization.string("widget.omer.none", lang)

        return WidgetSnapshot(
            cityName = city.name(lang),
            hebrewDate = hebrewDate,
            candleTime = formatTime(shabbat.candleLighting, timeFmt),
            candleCountdown = formatCountdown(secondsToCandle, entered, lang),
            shabbatEntered = entered,
            havdalaTime = formatTime(shabbat.havdala, timeFmt),
            parasha = shabbat.parasha,
            zmanim = zmanimLines,
            omerDay = omerDay,
            omerLabel = omerLabel,
        )
    }

    /** Résout un slug de ville, avec repli déterministe (défaut « paris » puis 1re ville). */
    private fun resolveCity(context: Context, slug: String): City =
        AssetData.city(slug)
            ?: AssetData.city(AppSettings().citySlug)
            ?: AssetData.cities(context).first()

    /** "HH:mm" dans le fuseau porté par la [ZonedDateTime], ou « — » si le zman est null. */
    private fun formatTime(time: ZonedDateTime?, fmt: DateTimeFormatter): String =
        time?.format(fmt) ?: DASH

    /**
     * Compte à rebours vers l'allumage : « Allumage dans {h} h {m} » (clé `widget.candle.in`) ou
     * « Chabbat » (clé `widget.candle.now`) une fois l'allumage passé. Les libellés viennent de
     * [Localization] (le FR ne vient jamais des libs, DESIGN §6).
     */
    private fun formatCountdown(secondsToCandle: Long, entered: Boolean, lang: String): String {
        if (entered) return Localization.string("widget.candle.now", lang)
        val hours = secondsToCandle / 3600L
        val minutes = (secondsToCandle % 3600L) / 60L
        return Localization.string("widget.candle.in", lang, hours, minutes)
    }
}
