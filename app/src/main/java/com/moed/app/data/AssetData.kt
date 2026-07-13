package com.moed.app.data

import android.content.Context
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

/**
 * Loads the embedded static datasets (cities, tzadikim) from `assets/`.
 *
 * Both datasets are bundled with the app and parsed once with
 * kotlinx-serialization, then held in an in-memory cache (the files never
 * change at runtime). 100 % offline — no network access, ever.
 *
 * - `assets/cities.json`   : ~189 cities (aligned with `src/data/cities.ts`)
 * - `assets/tzadikim.json` : ~32 tzadikim (aligned with `src/data/tsadikim.ts`)
 */
object AssetData {

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    @Volatile private var citiesCache: List<City>? = null
    @Volatile private var citiesBySlug: Map<String, City> = emptyMap()
    @Volatile private var tzadikimCache: List<Tzadik>? = null

    /** All cities. Parsed once from `assets/cities.json`, then cached in memory. */
    fun cities(context: Context): List<City> {
        citiesCache?.let { return it }
        synchronized(this) {
            citiesCache?.let { return it }
            val txt = context.assets.open("cities.json")
                .bufferedReader(Charsets.UTF_8).use { it.readText() }
            val list = json.decodeFromString<List<CityDto>>(txt).map { it.toModel() }
            citiesCache = list
            citiesBySlug = list.associateBy { it.slug }
            return list
        }
    }

    /** All tzadikim. Parsed once from `assets/tzadikim.json`, then cached in memory. */
    fun tzadikim(context: Context): List<Tzadik> {
        tzadikimCache?.let { return it }
        synchronized(this) {
            tzadikimCache?.let { return it }
            val txt = context.assets.open("tzadikim.json")
                .bufferedReader(Charsets.UTF_8).use { it.readText() }
            val list = json.decodeFromString<List<TzadikDto>>(txt).map { it.toModel() }
            tzadikimCache = list
            return list
        }
    }

    /**
     * Look up a city by its unique slug. Returns `null` if unknown, or if the
     * cities dataset has never been loaded yet (call [cities] once at startup).
     */
    fun city(slug: String): City? = citiesBySlug[slug]

    // ---- Wire DTOs (asset JSON shape) → domain models (data/Models.kt) ----

    @Serializable
    private class CityDto(
        val slug: String,
        val names: Map<String, String>,
        val country: String,
        val countryNames: Map<String, String> = emptyMap(),
        val lat: Double,
        val lng: Double,
        val tz: String,
        val elevation: Double = 0.0,
        val israel: Boolean = false,
        val candleMinutes: Int = 18,
        val community: Int? = null,
    ) {
        fun toModel() = City(
            slug = slug,
            names = names,
            country = country,
            countryNames = countryNames,
            lat = lat,
            lng = lng,
            tz = tz,
            elevation = elevation,
            israel = israel,
            candleMinutes = candleMinutes,
            community = community,
        )
    }

    @Serializable
    private class HilulaDto(val day: Int, @SerialName("month") val monthAlt: String? = null, val monthEn: String? = null) {
        fun toModel() = HilulaDate(day = day, monthEn = monthEn ?: monthAlt ?: "")
    }

    @Serializable
    private class KeverDto(val place: String, val country: String) {
        fun toModel() = Kever(place = place, country = country)
    }

    @Serializable
    private class TzadikDto(
        val slug: String,
        val names: Map<String, String>,
        val epithet: Map<String, String>? = null,
        val category: String,
        val hilula: HilulaDto,
        val hilulaNote: Map<String, String>? = null,
        val yearGregorian: Int? = null,
        val kever: KeverDto? = null,
        val works: List<String> = emptyList(),
        val bio: Map<String, String>,
        val confidence: String = "traditional",
        val sources: List<String> = emptyList(),
        val motif: String,
    ) {
        fun toModel() = Tzadik(
            slug = slug,
            names = names,
            epithet = epithet,
            category = parseCategory(category),
            hilula = hilula.toModel(),
            hilulaNote = hilulaNote,
            yearGregorian = yearGregorian,
            kever = kever?.toModel(),
            works = works,
            bio = bio,
            confidence = parseConfidence(confidence),
            sources = sources,
            motif = motif,
        )
    }

    private fun parseCategory(raw: String): TzadikCategory = when (raw.trim().lowercase()) {
        "tanna" -> TzadikCategory.TANNA
        "rishon" -> TzadikCategory.RISHON
        "acharon" -> TzadikCategory.ACHARON
        "hassid" -> TzadikCategory.HASSID
        "habad" -> TzadikCategory.HABAD
        "sefarade" -> TzadikCategory.SEFARADE
        else -> TzadikCategory.ACHARON
    }

    private fun parseConfidence(raw: String): Confidence = when (raw.trim().lowercase()) {
        "high" -> Confidence.HIGH
        "medium" -> Confidence.MEDIUM
        else -> Confidence.TRADITIONAL
    }
}
