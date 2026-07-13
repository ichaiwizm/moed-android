package com.moed.app.data

import android.content.Context
import androidx.datastore.core.DataMigration
import androidx.datastore.preferences.core.MutablePreferences
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.intOrNull

private val Context.settingsDataStore by preferencesDataStore(
    name = "moed_settings",
    produceMigrations = { ctx -> listOf(LegacySettingsMigration(ctx)) },
)

/**
 * Persistent, offline user settings — city, minhag/shita, language, notification
 * opt-ins. Source of truth for the ViewModel; nothing here leaves the device.
 *
 * Defaults follow NATIVE_SPEC §7.2 (city `paris`, candle AUTO, tzeit DEGREES,
 * region AUTO, language `system`, all notifications off).
 *
 * Handles migration of the legacy web/pre-release keys `moed_city` / `moed_prefs`
 * (see [LegacySettingsMigration]).
 */
class SettingsStore(private val context: Context) {

    val flow: Flow<AppSettings> = context.settingsDataStore.data.map { p -> decode(p) }

    suspend fun get(): AppSettings = flow.first()

    suspend fun update(transform: (AppSettings) -> AppSettings) {
        context.settingsDataStore.edit { p ->
            val current = decode(p)
            encode(transform(current), p)
        }
    }

    private fun decode(p: Preferences): AppSettings = AppSettings(
        citySlug = p[Keys.CITY] ?: "paris",
        candleMode = runCatching { CandleMode.valueOf(p[Keys.CANDLE] ?: "AUTO") }
            .getOrDefault(CandleMode.AUTO),
        tzeitMethod = runCatching { TzeitMethod.valueOf(p[Keys.TZEIT] ?: "DEGREES") }
            .getOrDefault(TzeitMethod.DEGREES),
        region = runCatching { Region.valueOf(p[Keys.REGION] ?: "AUTO") }
            .getOrDefault(Region.AUTO),
        language = p[Keys.LANG] ?: "system",
        notif = NotifPrefs(
            shabbat = p[Keys.N_SHABBAT] ?: false,
            omer = p[Keys.N_OMER] ?: false,
            hilula = p[Keys.N_HILULA] ?: false,
            yahrzeit = p[Keys.N_YAHRZEIT] ?: false,
        ),
    )

    private fun encode(s: AppSettings, p: MutablePreferences) {
        p[Keys.CITY] = s.citySlug
        p[Keys.CANDLE] = s.candleMode.name
        p[Keys.TZEIT] = s.tzeitMethod.name
        p[Keys.REGION] = s.region.name
        p[Keys.LANG] = s.language
        p[Keys.N_SHABBAT] = s.notif.shabbat
        p[Keys.N_OMER] = s.notif.omer
        p[Keys.N_HILULA] = s.notif.hilula
        p[Keys.N_YAHRZEIT] = s.notif.yahrzeit
    }

    internal object Keys {
        val CITY = stringPreferencesKey("city_slug")
        val CANDLE = stringPreferencesKey("candle_mode")
        val TZEIT = stringPreferencesKey("tzeit_method")
        val REGION = stringPreferencesKey("region")
        val LANG = stringPreferencesKey("language")
        val N_SHABBAT = booleanPreferencesKey("notif_shabbat")
        val N_OMER = booleanPreferencesKey("notif_omer")
        val N_HILULA = booleanPreferencesKey("notif_hilula")
        val N_YAHRZEIT = booleanPreferencesKey("notif_yahrzeit")
    }

    companion object {
        /** Off-main-thread snapshot for widgets / workers. */
        suspend fun snapshot(context: Context): AppSettings = SettingsStore(context).get()
    }
}

/**
 * One-shot migration from the legacy persistence layout used by earlier
 * (web-parity / pre-release) builds:
 *  - `moed_city`  : raw active-city slug (string)
 *  - `moed_prefs` : JSON blob `{ candleMinutes: number|"auto", tzeitMethod, region, notif{…} }`
 *
 * Values are folded into the current [SettingsStore.Keys] layout and the legacy
 * keys are dropped. App is unpublished, so a best-effort mapping is sufficient.
 */
internal class LegacySettingsMigration(
    @Suppress("unused") private val context: Context,
) : DataMigration<Preferences> {

    private val legacyCity = stringPreferencesKey("moed_city")
    private val legacyPrefs = stringPreferencesKey("moed_prefs")
    private val json = Json { ignoreUnknownKeys = true; isLenient = true }

    override suspend fun shouldMigrate(currentData: Preferences): Boolean =
        currentData.contains(legacyCity) || currentData.contains(legacyPrefs)

    override suspend fun migrate(currentData: Preferences): Preferences {
        val p = currentData.toMutablePreferences()

        p[legacyCity]?.takeIf { it.isNotBlank() }?.let { slug ->
            if (!p.contains(SettingsStore.Keys.CITY)) p[SettingsStore.Keys.CITY] = slug
        }

        p[legacyPrefs]?.let { raw ->
            runCatching { json.decodeFromString<LegacyPrefs>(raw) }.getOrNull()?.let { lp ->
                if (!p.contains(SettingsStore.Keys.CANDLE)) {
                    p[SettingsStore.Keys.CANDLE] = candleModeFrom(lp.candleMinutes).name
                }
                lp.tzeitMethod?.let {
                    if (!p.contains(SettingsStore.Keys.TZEIT)) {
                        p[SettingsStore.Keys.TZEIT] =
                            if (it.equals("minutes", true)) TzeitMethod.MINUTES.name
                            else TzeitMethod.DEGREES.name
                    }
                }
                lp.region?.let {
                    if (!p.contains(SettingsStore.Keys.REGION)) {
                        p[SettingsStore.Keys.REGION] = when (it.lowercase()) {
                            "il" -> Region.IL.name
                            "diaspora" -> Region.DIASPORA.name
                            else -> Region.AUTO.name
                        }
                    }
                }
                lp.notif?.let { n ->
                    if (!p.contains(SettingsStore.Keys.N_SHABBAT)) p[SettingsStore.Keys.N_SHABBAT] = n.shabbat
                    if (!p.contains(SettingsStore.Keys.N_OMER)) p[SettingsStore.Keys.N_OMER] = n.omer
                    if (!p.contains(SettingsStore.Keys.N_HILULA)) p[SettingsStore.Keys.N_HILULA] = n.hilula
                    if (!p.contains(SettingsStore.Keys.N_YAHRZEIT)) p[SettingsStore.Keys.N_YAHRZEIT] = n.yahrzeit
                }
            }
        }

        p.remove(legacyCity)
        p.remove(legacyPrefs)
        return p
    }

    override suspend fun cleanUp() {}

    /** `candleMinutes` was `number | "auto"` in the legacy blob. */
    private fun candleModeFrom(el: JsonElement?): CandleMode {
        if (el is JsonPrimitive) {
            el.intOrNull?.let {
                return when (it) {
                    18 -> CandleMode.M18
                    20 -> CandleMode.M20
                    30 -> CandleMode.M30
                    40 -> CandleMode.M40
                    else -> CandleMode.AUTO
                }
            }
            if (el.contentOrNull.equals("auto", true)) return CandleMode.AUTO
        }
        return CandleMode.AUTO
    }

    @Serializable
    private class LegacyPrefs(
        val candleMinutes: JsonElement? = null,
        val tzeitMethod: String? = null,
        val region: String? = null,
        val notif: LegacyNotif? = null,
    )

    @Serializable
    private class LegacyNotif(
        val shabbat: Boolean = false,
        val omer: Boolean = false,
        val hilula: Boolean = false,
        val yahrzeit: Boolean = false,
    )
}
