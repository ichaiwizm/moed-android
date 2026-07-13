package com.moed.app.engine

import com.kosherjava.zmanim.hebrewcalendar.HebrewDateFormatter
import com.kosherjava.zmanim.hebrewcalendar.JewishCalendar
import com.kosherjava.zmanim.hebrewcalendar.JewishDate
import com.kosherjava.zmanim.hebrewcalendar.YomiCalculator
import com.moed.app.data.CalendarEvent
import com.moed.app.data.DayCategory
import com.moed.app.data.HilulaDate
import com.moed.app.data.Localization
import com.moed.app.data.Occurrence
import com.moed.app.data.PersonRecord
import com.moed.app.data.PersonType
import java.time.LocalDate
import java.util.Calendar
import java.util.Locale

/**
 * Hebrew-calendar knowledge derived from KosherJava's [JewishCalendar],
 * [HebrewDateFormatter] and [YomiCalculator] — pure, offline, deterministic
 * (CONTRACTS §2).
 *
 * All French/localized labels are produced through [Localization] (KosherJava only
 * knows en/he); the machine passes canonical English keys and the language tag.
 * The Hebrew day begins at sunset, so date-sensitive methods accept an
 * `afterSunset` flag that advances the Hebrew date by one day.
 */
class JewishInfo {

    /** Formatted Hebrew date for [date] (advanced one day when [afterSunset]). */
    fun hebrewDate(date: LocalDate, afterSunset: Boolean, lang: String): String {
        val jc = jewishCalendar(date, afterSunset = afterSunset)
        return formatHebrewDate(jc, lang, includeYear = true)
    }

    /**
     * Notable events for the civil [date]: festivals, fasts, chol hamoed, rosh
     * chodesh and the Omer count, each mapped to a [DayCategory]. Empty for an
     * ordinary weekday.
     */
    fun dayEvents(date: LocalDate, il: Boolean, lang: String): List<CalendarEvent> {
        val jc = jewishCalendar(date, inIsrael = il)
        val out = ArrayList<CalendarEvent>(3)

        val yomTovEn = enFormatter.formatYomTov(jc)
        when {
            jc.isTaanis ->
                if (yomTovEn.isNotBlank()) out += CalendarEvent(localizedHoliday(yomTovEn, lang), DayCategory.FAST)
            jc.isCholHamoed ->
                if (yomTovEn.isNotBlank()) out += CalendarEvent(localizedHoliday(yomTovEn, lang), DayCategory.CHOL_HAMOED)
            yomTovEn.isNotBlank() && !jc.isRoshChodesh ->
                out += CalendarEvent(localizedHoliday(yomTovEn, lang), DayCategory.HOLIDAY)
        }

        if (jc.isRoshChodesh) {
            val rhEn = enFormatter.formatRoshChodesh(jc)
            if (rhEn.isNotBlank()) out += CalendarEvent(localizedHoliday(rhEn, lang), DayCategory.ROSH_CHODESH)
        }

        val omer = jc.dayOfOmer
        if (omer > 0) {
            out += CalendarEvent(Localization.string("home.omerDay", lang, omer), DayCategory.OMER)
        }
        return out
    }

    /** Day of the Omer (1..49), or 0 outside the counting period. */
    fun omerDay(date: LocalDate): Int {
        val d = jewishCalendar(date).dayOfOmer
        return if (d < 0) 0 else d
    }

    /**
     * Parasha + haftara of the Shabbat of [date]'s week.
     *
     * @return (parasha display name, haftara reference). When the week carries no
     *   weekly reading (a Yom-Tov Shabbat) the upcoming parsha is used. Haftara
     *   honours the Sephardic minhag when [sefarade] is true and any special-Shabbat
     *   override. Both strings are localized for [lang].
     */
    fun parasha(date: LocalDate, il: Boolean, sefarade: Boolean, lang: String): Pair<String, String> {
        val shabbat = onOrAfterSaturday(date)
        val jcShabbat = jewishCalendar(shabbat, inIsrael = il)
        var parsha = jcShabbat.parshah
        if (parsha == JewishCalendar.Parsha.NONE) parsha = jcShabbat.upcomingParshah

        val name = if (parsha == JewishCalendar.Parsha.NONE) "" else parshaName(parsha, shabbat, il, lang)

        // Calendar-position overrides (Chanukah / Rosh Chodesh / Machar Chodesh).
        val nextDay = jewishCalendar(shabbat.plusDays(1), inIsrael = il)
        val isMacharChodesh = nextDay.isRoshChodesh && !jcShabbat.isRoshChodesh
        val afterSeventeenTammuz =
            jcShabbat.jewishMonth == JewishDate.TAMMUZ && jcShabbat.jewishDayOfMonth > 17
        val haftara = HaftarahTable.haftara(
            parsha = parsha,
            special = jcShabbat.specialShabbos,
            sefarade = sefarade,
            lang = lang,
            isChanukah = jcShabbat.isChanukah,
            chanukahDay = jcShabbat.dayOfChanukah,
            isRoshChodesh = jcShabbat.isRoshChodesh,
            isMacharChodesh = isMacharChodesh,
            afterSeventeenTammuz = afterSeventeenTammuz,
        )
        return name to haftara
    }

    /** Daf Yomi (Bavli) for [date], or empty before the cycle began (1924). */
    fun dafYomi(date: LocalDate, lang: String): String {
        return try {
            val jc = jewishCalendar(date)
            val daf = YomiCalculator.getDafYomiBavli(jc) ?: return ""
            if (lang == "he") {
                heFormatter.formatDafYomiBavli(daf)
            } else {
                "${daf.masechtaTransliterated} ${daf.daf}"
            }
        } catch (e: IllegalArgumentException) {
            ""
        }
    }

    /**
     * The Hebrew (day, transliterated-month) of the civil [date] — used to match a
     * hilula against today (CONTRACTS §2). `monthEn` uses the same English spelling
     * as [HebrewDateFormatter.formatMonth] (e.g. "Iyar", "Cheshvan", "Adar II") so it
     * compares equal to the `tzadikim.json` `hilula.month` field.
     */
    fun hebrewMonthDay(date: LocalDate): HilulaDate {
        val jc = jewishCalendar(date)
        return HilulaDate(day = jc.jewishDayOfMonth, monthEn = enFormatter.formatMonth(jc))
    }

    /**
     * The next Gregorian occurrence of a [hilula] (given as day + transliterated Hebrew
     * month), on or after [from] (CONTRACTS §2). Returns `null` when the month name is
     * unknown or the date never resolves in the scanned window. The Hebrew month honours
     * leap-year embolism for Adar (Adar → Adar II in a leap year).
     */
    fun nextHilula(hilula: HilulaDate, from: LocalDate): Occurrence? {
        val startYear = jewishCalendar(from).jewishYear
        for (off in 0..3) {
            val hebYear = startYear + off
            val month = hebrewMonthConstant(hilula.monthEn, hebYear) ?: continue
            val jc = runCatching { JewishCalendar(hebYear, month, hilula.day) }.getOrNull() ?: continue
            // Reject silent normalization (e.g. day 30 of a 29-day month).
            if (jc.jewishMonth != month || jc.jewishDayOfMonth != hilula.day) continue
            val g = jc.gregorianCalendar
            val gDate = LocalDate.of(
                g.get(Calendar.YEAR),
                g.get(Calendar.MONTH) + 1,
                g.get(Calendar.DAY_OF_MONTH),
            )
            if (!gDate.isBefore(from)) return Occurrence(gDate, heFormatter.format(jc))
        }
        return null
    }

    /**
     * Transliterated Hebrew month name → KosherJava [JewishDate] month constant
     * (NISSAN=1 … ADAR=12, ADAR_II=13), resolving Adar against the target year's
     * embolism. Accepts the common spellings found in the datasets.
     */
    private fun hebrewMonthConstant(monthEn: String, hebYear: Int): Int? {
        val leap = isLeap(hebYear)
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
            k.contains("adar i") || k.contains("adar 1") || k.contains("adar aleph") -> JewishDate.ADAR
            k.startsWith("adar") -> if (leap) JewishDate.ADAR_II else JewishDate.ADAR
            else -> null
        }
    }

    /**
     * The next [count] Gregorian occurrences of a yahrzeit or Hebrew birthday for
     * [person], on or after [from].
     *
     * Applies the halakhic edge cases (Cheshvan 30 / Kislev 30 / Adar / Adar II /
     * leap years) per Reingold & Dershowitz — the same algorithm as Hebcal. The
     * Hebrew anchor honours [PersonRecord.afterSunset]. A guard of `count + 200`
     * Hebrew years prevents pathological loops.
     */
    fun nextOccurrences(person: PersonRecord, from: LocalDate, count: Int): List<Occurrence> {
        if (count <= 0) return emptyList()
        val anchor = jewishCalendar(person.date, afterSunset = person.afterSunset)
        val origYear = anchor.jewishYear
        val hMonth = anchor.jewishMonth
        val hDay = anchor.jewishDayOfMonth

        val out = ArrayList<Occurrence>(count)
        var hyear = jewishCalendar(from).jewishYear
        val maxYears = count + 200
        var i = 0
        while (i < maxYears && out.size < count) {
            val occ = when (person.type) {
                PersonType.YAHRZEIT -> yahrzeit(hyear, origYear, hMonth, hDay)
                PersonType.BIRTHDAY -> birthday(hyear, origYear, hMonth, hDay)
            }
            if (occ != null) {
                val g = occ.gregorianCalendar
                val gDate = LocalDate.of(
                    g.get(Calendar.YEAR),
                    g.get(Calendar.MONTH) + 1,
                    g.get(Calendar.DAY_OF_MONTH),
                )
                if (!gDate.isBefore(from)) {
                    // hebrewLabel is the Hebrew date itself → always in Hebrew script.
                    out += Occurrence(gDate, heFormatter.format(occ))
                }
            }
            hyear++
            i++
        }
        return out
    }

    // --- Hebrew-date formatting --------------------------------------------------

    private fun formatHebrewDate(jc: JewishCalendar, lang: String, includeYear: Boolean): String {
        if (lang == "he") {
            return if (includeYear) heFormatter.format(jc) else "${heFormatter.formatHebrewNumber(jc.jewishDayOfMonth)} ${heFormatter.formatMonth(jc)}"
        }
        val day = jc.jewishDayOfMonth
        val month = Localization.monthName(jc.jewishMonth, jc.jewishYear, lang)
        return if (includeYear) "$day $month ${jc.jewishYear}" else "$day $month"
    }

    private fun localizedHoliday(canonicalEn: String, lang: String): String =
        Localization.holidayName(canonicalEn, lang)

    /** Format the parsha name; probes forward to the Shabbat that actually reads it. */
    private fun parshaName(parsha: JewishCalendar.Parsha, shabbat: LocalDate, il: Boolean, lang: String): String {
        var probe = shabbat
        var jc = jewishCalendar(probe, inIsrael = il)
        var guard = 0
        while (jc.parshah != parsha && guard < 8) {
            probe = probe.plusWeeks(1)
            jc = jewishCalendar(probe, inIsrael = il)
            guard++
        }
        val fmt = if (lang == "he") heFormatter else enFormatter
        val formatted = fmt.formatParsha(jc)
        return formatted.ifBlank { parsha.name.split('_').joinToString(" ") { it.lowercase().replaceFirstChar { c -> c.uppercase() } } }
    }

    // --- yahrzeit / birthday (Reingold–Dershowitz, per @hebcal/hdate) -------------

    /**
     * Yahrzeit occurrence in Hebrew year [hyear] for a death on ([hMonth],[hDay]) of
     * [origYear]. Null when [hyear] is not strictly after the year of death.
     */
    private fun yahrzeit(hyear: Int, origYear: Int, hMonth: Int, hDay: Int): JewishCalendar? {
        if (hyear <= origYear) return null
        var month = hMonth
        var day = hDay
        when {
            month == JewishDate.CHESHVAN && day == 30 && !longCheshvan(origYear + 1) -> {
                // Heshvan 30 whose first anniversary year was short → day before Kislev 1.
                val hd = dayBefore(hyear, JewishDate.KISLEV, 1)
                month = hd.first; day = hd.second
            }
            month == JewishDate.KISLEV && day == 30 && shortKislev(origYear + 1) -> {
                // Kislev 30 whose first anniversary year was short → day before Tevet 1.
                val hd = dayBefore(hyear, JewishDate.TEVES, 1)
                month = hd.first; day = hd.second
            }
            month == JewishDate.ADAR_II -> {
                // Adar II → same day in the last month of the target year (Adar or Adar II).
                month = lastMonthOfYear(hyear)
            }
            month == JewishDate.ADAR && day == 30 && !isLeap(hyear) -> {
                // 30 Adar I in a non-leap target (Adar has 29 days) → last day of Shevat.
                month = JewishDate.SHEVAT; day = 30
            }
        }
        // Advance to rosh chodesh if the resolved 30th does not exist this year.
        when {
            month == JewishDate.CHESHVAN && day == 30 && !longCheshvan(hyear) -> {
                month = JewishDate.KISLEV; day = 1
            }
            month == JewishDate.KISLEV && day == 30 && shortKislev(hyear) -> {
                month = JewishDate.TEVES; day = 1
            }
        }
        return JewishCalendar(hyear, month, day)
    }

    /**
     * Birthday / anniversary (non-yahrzeit) occurrence in [hyear].
     */
    private fun birthday(hyear: Int, origYear: Int, hMonth: Int, hDay: Int): JewishCalendar? {
        if (hyear == origYear) return JewishCalendar(origYear, hMonth, hDay)
        if (hyear < origYear) return null
        val isOrigLeap = isLeap(origYear)
        var month = hMonth
        var day = hDay
        when {
            (month == JewishDate.ADAR && !isOrigLeap) || (month == JewishDate.ADAR_II && isOrigLeap) -> {
                month = lastMonthOfYear(hyear)
            }
            month == JewishDate.CHESHVAN && day == 30 && !longCheshvan(hyear) -> {
                month = JewishDate.KISLEV; day = 1
            }
            month == JewishDate.KISLEV && day == 30 && shortKislev(hyear) -> {
                month = JewishDate.TEVES; day = 1
            }
            month == JewishDate.ADAR && day == 30 && isOrigLeap && !isLeap(hyear) -> {
                month = JewishDate.NISSAN; day = 1
            }
        }
        return JewishCalendar(hyear, month, day)
    }

    // --- Hebrew-year helpers -----------------------------------------------------

    private fun longCheshvan(year: Int): Boolean = JewishCalendar(year, JewishDate.TISHREI, 1).isCheshvanLong
    private fun shortKislev(year: Int): Boolean = JewishCalendar(year, JewishDate.TISHREI, 1).isKislevShort
    private fun isLeap(year: Int): Boolean = JewishCalendar(year, JewishDate.TISHREI, 1).isJewishLeapYear
    private fun lastMonthOfYear(year: Int): Int = if (isLeap(year)) JewishDate.ADAR_II else JewishDate.ADAR

    /** The Hebrew (month, day) immediately before ([year],[month],[day]). */
    private fun dayBefore(year: Int, month: Int, day: Int): Pair<Int, Int> {
        val jc = JewishCalendar(year, month, day)
        val g = jc.gregorianCalendar
        g.add(Calendar.DAY_OF_MONTH, -1)
        val back = JewishCalendar(g)
        return back.jewishMonth to back.jewishDayOfMonth
    }

    // --- construction / formatters ----------------------------------------------

    /** The Saturday on or after [date] (returns [date] itself when it is a Saturday). */
    private fun onOrAfterSaturday(date: LocalDate): LocalDate {
        var d = date
        while (d.dayOfWeek != java.time.DayOfWeek.SATURDAY) d = d.plusDays(1)
        return d
    }

    private fun jewishCalendar(
        date: LocalDate,
        inIsrael: Boolean = false,
        afterSunset: Boolean = false,
    ): JewishCalendar {
        val jc = JewishCalendar(date)
        jc.inIsrael = inIsrael
        if (afterSunset) jc.forward(Calendar.DAY_OF_MONTH, 1)
        return jc
    }

    private val heFormatter: HebrewDateFormatter by lazy {
        HebrewDateFormatter().apply { isHebrewFormat = true; isUseLongHebrewYears = false }
    }
    private val enFormatter: HebrewDateFormatter by lazy {
        HebrewDateFormatter().apply { isHebrewFormat = false }
    }

    /** Convenience so callers (calendar cells) can request a short "day month" label. */
    internal fun hebrewDayLabel(date: LocalDate, il: Boolean, lang: String): String =
        formatHebrewDate(jewishCalendar(date, inIsrael = il), lang, includeYear = false)
}
