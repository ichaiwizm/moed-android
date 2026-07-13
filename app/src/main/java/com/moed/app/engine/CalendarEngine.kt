package com.moed.app.engine

import com.moed.app.data.CalendarDay
import java.time.LocalDate

/**
 * Builds calendar grids — the KosherJava-backed equivalent of the web engine's
 * `getCalendarRange(start, end, { il, locale })` (CONTRACTS §2).
 *
 * Each [CalendarDay] pairs a Gregorian date with its short Hebrew label, the Omer
 * count (0 outside the period) and the day's notable events. Pure and offline; all
 * per-day knowledge is delegated to [JewishInfo].
 */
class CalendarEngine(private val jewishInfo: JewishInfo) {

    /** One [CalendarDay] per civil day in the inclusive range `[start, end]`. */
    fun range(start: LocalDate, end: LocalDate, il: Boolean, lang: String): List<CalendarDay> {
        if (end.isBefore(start)) return emptyList()
        val out = ArrayList<CalendarDay>()
        var cur = start
        while (!cur.isAfter(end)) {
            out += day(cur, il, lang)
            cur = cur.plusDays(1)
        }
        return out
    }

    /** Every day of the Gregorian month containing [anchor]. */
    fun month(anchor: LocalDate, il: Boolean, lang: String): List<CalendarDay> {
        val first = anchor.withDayOfMonth(1)
        val last = anchor.withDayOfMonth(anchor.lengthOfMonth())
        return range(first, last, il, lang)
    }

    private fun day(date: LocalDate, il: Boolean, lang: String): CalendarDay =
        CalendarDay(
            gregorian = date,
            hebrewLabel = jewishInfo.hebrewDayLabel(date, il, lang),
            omerDay = jewishInfo.omerDay(date),
            events = jewishInfo.dayEvents(date, il, lang),
        )
}
