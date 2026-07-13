package com.moed.app.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.time.LocalDate
import java.time.ZonedDateTime

/**
 * Modèles domaine figés de Moed Android — voir CONTRACTS §1.
 *
 * Règles :
 *  - Dates = [java.time.LocalDate] / [java.time.ZonedDateTime] (desugaring `desugar_jdk_libs`).
 *  - `@Serializable` uniquement sur les types chargés depuis les assets JSON
 *    (kotlinx-serialization) : [City], [Tzadik] et leurs types imbriqués.
 *  - Les réglages / le carnet sont persistés ailleurs (DataStore) et restent des data
 *    classes nues.
 *  - Un zman `null` = le soleil n'atteint pas l'angle (latitude extrême) → l'UI affiche « — ».
 *
 * Aucune dépendance UI/moteur : ce module est la couche `data/model`, feuille du graphe.
 */

// ---------------------------------------------------------------------------
// Ville (aligné src/data/cities.ts, asset cities.json)
// ---------------------------------------------------------------------------

@Serializable
data class City(
    val slug: String,                       // kebab-case unique
    val names: Map<String, String>,         // {"fr","en","he"}
    val country: String,                    // ISO2
    val countryNames: Map<String, String>,
    val lat: Double,
    val lng: Double,
    val tz: String,                         // IANA (pilote DST)
    val elevation: Double = 0.0,
    val israel: Boolean,
    val candleMinutes: Int,                 // 18/20/30/40
    val community: Int? = null,
) {
    /** Nom localisé, fallback en → slug. */
    fun name(lang: String): String = names[lang] ?: names["en"] ?: slug
}

// ---------------------------------------------------------------------------
// Tsadik (aligné src/data/tsadikim.ts, asset tzadikim.json)
// ---------------------------------------------------------------------------

@Serializable
enum class TzadikCategory {
    @SerialName("tanna") TANNA,
    @SerialName("rishon") RISHON,
    @SerialName("acharon") ACHARON,
    @SerialName("hassid") HASSID,
    @SerialName("habad") HABAD,
    @SerialName("sefarade") SEFARADE,
}

@Serializable
enum class Confidence {
    @SerialName("high") HIGH,
    @SerialName("medium") MEDIUM,
    @SerialName("traditional") TRADITIONAL,
}

@Serializable
data class HilulaDate(
    val day: Int,                           // ex. 18
    @SerialName("month") val monthEn: String, // mois hébreu translittéré EN, ex. "Iyar"
)

@Serializable
data class Kever(
    val place: String,
    val country: String,
)

@Serializable
data class Tzadik(
    val slug: String,
    val names: Map<String, String>,
    val epithet: Map<String, String>? = null,
    val category: TzadikCategory,
    val hilula: HilulaDate,
    val hilulaNote: Map<String, String>? = null,
    val yearGregorian: Int? = null,
    val kever: Kever? = null,
    val works: List<String> = emptyList(),
    val bio: Map<String, String>,
    val confidence: Confidence,
    val sources: List<String> = emptyList(),
    val motif: String,                      // "motif-tanna" | "kever" | "hilula-candle"…
)

// ---------------------------------------------------------------------------
// Carnet familial (persisté)
// ---------------------------------------------------------------------------

enum class PersonType { YAHRZEIT, BIRTHDAY }

data class PersonRecord(
    val id: String,                         // UUID
    val type: PersonType,
    val name: String,
    val date: LocalDate,                    // date grégorienne saisie
    val afterSunset: Boolean = false,       // ancre la date héb (+1 jour)
)

// ---------------------------------------------------------------------------
// Réglages (persistés, défauts = NATIVE_SPEC §7.2)
// ---------------------------------------------------------------------------

enum class TzeitMethod { DEGREES, MINUTES }        // 8.5° vs offset fixe

enum class Region { AUTO, IL, DIASPORA }

enum class CandleMode { AUTO, M18, M20, M30, M40 } // AUTO = minhag de la ville

data class NotifPrefs(
    val shabbat: Boolean = false,
    val omer: Boolean = false,
    val hilula: Boolean = false,
    val yahrzeit: Boolean = false,
)

data class AppSettings(
    val citySlug: String = "paris",
    val candleMode: CandleMode = CandleMode.AUTO,
    val tzeitMethod: TzeitMethod = TzeitMethod.DEGREES,
    val region: Region = Region.AUTO,
    val language: String = "system",        // system → he/fr/en (fallback en)
    val notif: NotifPrefs = NotifPrefs(),
)

// ---------------------------------------------------------------------------
// Sorties du moteur
// ---------------------------------------------------------------------------

/** Un horaire halakhique. [time] null = latitude extrême (angle jamais atteint) → UI « — ». */
data class Zman(
    val key: String,
    val shita: String,
    val time: ZonedDateTime?,
)

data class ShabbatInfo(
    val candleLighting: ZonedDateTime,
    val havdala: ZonedDateTime,
    val parasha: String,
    val haftara: String,
)

enum class DayCategory { NONE, HOLIDAY, FAST, ROSH_CHODESH, OMER, CHOL_HAMOED }

data class CalendarEvent(
    val label: String,
    val category: DayCategory,
)

data class CalendarDay(
    val gregorian: LocalDate,
    val hebrewLabel: String,
    val omerDay: Int,                       // 0 si hors période
    val events: List<CalendarEvent>,
)

/** Yahrzeit / anniversaire hébraïque projeté sur le grégorien. */
data class Occurrence(
    val gregorian: LocalDate,
    val hebrewLabel: String,
)
