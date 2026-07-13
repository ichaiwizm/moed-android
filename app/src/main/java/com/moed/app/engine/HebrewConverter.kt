package com.moed.app.engine

import com.kosherjava.zmanim.hebrewcalendar.HebrewDateFormatter
import com.kosherjava.zmanim.hebrewcalendar.JewishCalendar
import com.kosherjava.zmanim.hebrewcalendar.JewishDate
import com.moed.app.data.Localization
import java.time.LocalDate
import java.util.Calendar

/**
 * Bidirectional Gregorian ⇄ Hebrew date conversion (CONTRACTS §2).
 *
 * Backed by KosherJava's [JewishCalendar] (exact past/future). Hebrew month numbers
 * follow KosherJava/Hebcal Nisan-based numbering: Nisan = 1 … Tishrei = 7 … Adar = 12,
 * Adar II = 13. In a non-leap year month 12 is the sole "Adar"; in a leap year month
 * 12 is "Adar I" and 13 is "Adar II". Pure and offline.
 */
class HebrewConverter {

    private val heFormatter: HebrewDateFormatter by lazy {
        HebrewDateFormatter().apply { isHebrewFormat = true }
    }

    /** Formatted Hebrew date for a Gregorian [date] (advanced one day when [afterSunset]). */
    fun gregToHebrew(date: LocalDate, afterSunset: Boolean, lang: String): String {
        val jc = JewishCalendar(date)
        if (afterSunset) jc.forward(Calendar.DAY_OF_MONTH, 1)
        if (lang == "he") return heFormatter.format(jc)
        val month = Localization.monthName(jc.jewishMonth, jc.jewishYear, lang)
        return "${jc.jewishDayOfMonth} $month ${jc.jewishYear}"
    }

    /**
     * Gregorian date of a Hebrew date, or null if the date is invalid for that year
     * (e.g. Adar II in a non-leap year, or day 30 of a 29-day month).
     */
    fun hebrewToGreg(hebYear: Int, hebMonth: Int, hebDay: Int): LocalDate? {
        if (hebYear < 1 || hebMonth < 1 || hebMonth > 13 || hebDay < 1 || hebDay > 30) return null
        return try {
            val jc = JewishCalendar(hebYear, hebMonth, hebDay)
            // Guard against silent normalization: the round-trip must match the input.
            if (jc.jewishYear != hebYear || jc.jewishMonth != hebMonth || jc.jewishDayOfMonth != hebDay) return null
            val g = jc.gregorianCalendar
            LocalDate.of(g.get(Calendar.YEAR), g.get(Calendar.MONTH) + 1, g.get(Calendar.DAY_OF_MONTH))
        } catch (e: IllegalArgumentException) {
            null
        }
    }

    /**
     * Localized month names present in Hebrew year [hebYear], in numeric (Nisan-first)
     * order. Leap years include both "Adar I" (12) and "Adar II" (13); ordinary years
     * end at "Adar" (12). Suitable for a converter month picker.
     */
    fun hebrewMonths(hebYear: Int, lang: String): List<String> {
        val leap = JewishCalendar(hebYear, JewishDate.TISHREI, 1).isJewishLeapYear
        val last = if (leap) JewishDate.ADAR_II else JewishDate.ADAR
        return (JewishDate.NISSAN..last).map { Localization.monthName(it, hebYear, lang) }
    }
}
