package com.moed.app.engine

import com.kosherjava.zmanim.ComplexZmanimCalendar
import com.kosherjava.zmanim.util.GeoLocation
import com.moed.app.data.AppSettings
import com.moed.app.data.City
import com.moed.app.data.Localization
import com.moed.app.data.Region
import com.moed.app.data.ShabbatInfo
import com.moed.app.data.TzeitMethod
import com.moed.app.data.Zman
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZonedDateTime
import java.util.Calendar
import java.util.Date
import java.util.TimeZone

/**
 * Deterministic, offline solar zmanim engine backed by KosherJava's
 * [ComplexZmanimCalendar] + [GeoLocation] (NOAA algorithm).
 *
 * Contract (CONTRACTS §2): every method is pure — no I/O, no network. Inputs are
 * a [City] + [LocalDate] + [AppSettings]; timezone/DST is always driven by the
 * city's IANA identifier (never the device offset). At extreme latitudes where the
 * sun never reaches the required depression angle, the corresponding zman time is
 * `null` (never NaN); the UI renders "—".
 *
 * Parity is validated against kosher-zmanim / Hebcal (0 tolerance on dates, ≤ 2 min
 * on zmanim) — see [ZmanimValidation] and the shipped `validation-report.json`.
 */
class ZmanimEngine(
    private val jewishInfo: JewishInfo = JewishInfo(),
) {

    /** All 16 zmanim of the day, in canonical spec order. Time is null at extreme latitudes. */
    fun zmanim(city: City, date: LocalDate, settings: AppSettings): List<Zman> {
        val czc = makeCalendar(city, date, settings)
        val zone = ZoneId.of(city.tz)
        return ZmanimCatalog.all.map { def ->
            Zman(def.key, def.shitaLabelKey, computeKey(czc, def.key).zdt(zone))
        }
    }

    /** The HOME_ZMANIM subset shown on the "Today" screen, in display order. */
    fun homeZmanim(city: City, date: LocalDate, settings: AppSettings): List<Zman> {
        val czc = makeCalendar(city, date, settings)
        val zone = ZoneId.of(city.tz)
        return ZmanimCatalog.home.map { key ->
            val shita = ZmanimCatalog.def(key)?.shitaLabelKey ?: ""
            Zman(key, shita, computeKey(czc, key).zdt(zone))
        }
    }

    /**
     * Shabbat times for the erev-Shabbat civil [date] (the candle-lighting day).
     *
     *  - Candle lighting = sunset − candleMinutes (minhag resolved from settings/city).
     *  - Havdala on the following day: tzeit 8.5° (DEGREES) or a fixed offset after
     *    sunset (MINUTES → Rabbeinu Tam 72 min).
     *  - Parasha + haftara for that Shabbat, in the resolved UI language.
     *
     * The two times are guaranteed non-null (CONTRACTS §1): at pathological
     * latitudes where the solar event is undefined, a deterministic fallback
     * (sunset, then a fixed local time) keeps the card renderable.
     */
    fun shabbat(city: City, date: LocalDate, settings: AppSettings): ShabbatInfo {
        val zone = ZoneId.of(city.tz)
        val il = resolveIl(city, settings)
        val lang = Localization.resolve(settings.language)

        // Candle lighting on the erev.
        val erev = makeCalendar(city, date, settings)
        val candleRaw = erev.candleLighting
        val sunsetErev = erev.sunset
        val candle = candleRaw.zdt(zone)
            ?: sunsetErev.zdt(zone)
            ?: date.atTime(18, 0).atZone(zone)

        // Havdala on the following (motzaei) day.
        val motzaei = date.plusDays(1)
        val night = makeCalendar(city, motzaei, settings)
        val havdalaRaw = when (settings.tzeitMethod) {
            TzeitMethod.DEGREES -> night.tzaisGeonim8Point5Degrees
            TzeitMethod.MINUTES -> offsetAfter(night.sunset, HAVDALA_FIXED_MINUTES)
        }
        val havdala = havdalaRaw.zdt(zone)
            ?: offsetAfter(night.sunset, HAVDALA_FIXED_MINUTES).zdt(zone)
            ?: motzaei.atTime(19, 30).atZone(zone)

        val (parasha, haftara) = jewishInfo.parasha(motzaei, il, sefarade = false, lang = lang)
        return ShabbatInfo(
            candleLighting = candle,
            havdala = havdala,
            parasha = parasha,
            haftara = haftara,
        )
    }

    // --- internals ---------------------------------------------------------------

    private fun makeCalendar(city: City, date: LocalDate, settings: AppSettings): ComplexZmanimCalendar {
        val tz = TimeZone.getTimeZone(city.tz)
        val geo = GeoLocation(city.slug, city.lat, city.lng, city.elevation, tz)
        val czc = ComplexZmanimCalendar(geo)
        // Elevation affects sunrise/sunset only when a real (>0) elevation is set,
        // matching the Hebcal/kosher-zmanim default of sea-level for lat/lng-only cities.
        czc.isUseElevation = city.elevation > 0.0
        czc.candleLightingOffset = candleMinutes(city, settings).toDouble()
        val cal = Calendar.getInstance(tz)
        cal.clear()
        // Midday anchor: the civil date whose zmanim we compute (time-of-day irrelevant).
        cal.set(date.year, date.monthValue - 1, date.dayOfMonth, 12, 0, 0)
        czc.calendar = cal
        return czc
    }

    private fun computeKey(c: ComplexZmanimCalendar, key: String): Date? = when (key) {
        ZmanimCatalog.ALOT_HASHACHAR -> c.alos16Point1Degrees
        ZmanimCatalog.ALOT_HASHACHAR_72 -> c.alos72
        ZmanimCatalog.MISHEYAKIR -> c.misheyakir11Point5Degrees
        ZmanimCatalog.MISHEYAKIR_MACHMIR -> c.misheyakir10Point2Degrees
        ZmanimCatalog.HANETZ_HACHAMA -> c.sunrise
        ZmanimCatalog.SOF_ZMAN_SHMA_MGA -> c.sofZmanShmaMGA
        ZmanimCatalog.SOF_ZMAN_SHMA_GRA -> c.sofZmanShmaGRA
        ZmanimCatalog.SOF_ZMAN_TFILA_MGA -> c.sofZmanTfilaMGA
        ZmanimCatalog.SOF_ZMAN_TFILA_GRA -> c.sofZmanTfilaGRA
        ZmanimCatalog.CHATZOT -> c.chatzos
        ZmanimCatalog.MINCHA_GEDOLA -> c.minchaGedola
        ZmanimCatalog.MINCHA_KETANA -> c.getMinchaKetana(c.sunrise, c.sunset)
        ZmanimCatalog.PLAG_HAMINCHA -> c.getPlagHamincha(c.sunrise, c.sunset)
        ZmanimCatalog.SHKIA_HACHAMA -> c.sunset
        ZmanimCatalog.TZEIT_HAKOCHAVIM -> c.tzaisGeonim8Point5Degrees
        ZmanimCatalog.TZEIT_72 -> c.tzais72
        else -> null
    }

    private fun offsetAfter(sunset: Date?, minutes: Int): Date? =
        sunset?.let { Date(it.time + minutes * 60_000L) }

    /** Convert a KosherJava [Date] (or null) to a [ZonedDateTime] in the city zone. */
    private fun Date?.zdt(zone: ZoneId): ZonedDateTime? =
        this?.toInstant()?.atZone(zone)

    companion object {
        /** Fixed-offset havdala (Rabbeinu Tam) used when [TzeitMethod.MINUTES] is selected. */
        const val HAVDALA_FIXED_MINUTES = 72

        /** Israel-vs-Diaspora boolean derived from settings (AUTO → the city's own flag). */
        fun resolveIl(city: City, settings: AppSettings): Boolean = when (settings.region) {
            Region.AUTO -> city.israel
            Region.IL -> true
            Region.DIASPORA -> false
        }

        /** Candle-lighting minutes-before-sunset (AUTO → the city minhag, else the fixed choice). */
        fun candleMinutes(city: City, settings: AppSettings): Int = when (settings.candleMode) {
            com.moed.app.data.CandleMode.AUTO -> city.candleMinutes
            com.moed.app.data.CandleMode.M18 -> 18
            com.moed.app.data.CandleMode.M20 -> 20
            com.moed.app.data.CandleMode.M30 -> 30
            com.moed.app.data.CandleMode.M40 -> 40
        }
    }
}
