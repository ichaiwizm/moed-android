package com.moed.app.data

import android.content.Context
import androidx.datastore.core.DataMigration
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.time.LocalDate
import java.util.UUID

private val Context.personalDataStore by preferencesDataStore(
    name = "moed_personal",
    produceMigrations = { ctx -> listOf(LegacyFamilyMigration(ctx)) },
)

/**
 * Local, on-device family book — Hebrew birthdays and yahrzeits of loved ones.
 * Stored as a JSON array in DataStore (no account, no sync; nothing leaves the
 * device). Handles migration of the legacy `moed_family` key.
 */
class PersonalStore(private val context: Context) {

    val flow: Flow<List<PersonRecord>> = context.personalDataStore.data.map { p ->
        Codec.parse(p[KEY] ?: "[]")
    }

    suspend fun get(): List<PersonRecord> = flow.first()

    suspend fun add(person: PersonRecord) {
        val record = if (person.id.isBlank()) person.copy(id = UUID.randomUUID().toString()) else person
        val list = get().toMutableList().apply { add(record) }
        write(list)
    }

    suspend fun remove(id: String) {
        write(get().filterNot { it.id == id })
    }

    private suspend fun write(list: List<PersonRecord>) {
        context.personalDataStore.edit { it[KEY] = Codec.serialize(list) }
    }

    companion object {
        internal val KEY = stringPreferencesKey("records")

        suspend fun snapshot(context: Context): List<PersonRecord> = PersonalStore(context).get()
    }

    /** JSON (de)serialization shared with the legacy migration. */
    internal object Codec {
        private val json = Json { ignoreUnknownKeys = true; isLenient = true }

        fun parse(txt: String): List<PersonRecord> =
            runCatching { json.decodeFromString<List<PersonDto>>(txt) }
                .getOrDefault(emptyList())
                .mapNotNull { it.toModel() }

        fun serialize(list: List<PersonRecord>): String =
            json.encodeToString(list.map { PersonDto.from(it) })
    }

    /**
     * On-disk shape. `date` is an ISO-8601 string (web/native format). The
     * legacy Android build used `gYear`/`gMonth`/`gDay`; those are accepted as a
     * fallback for a clean one-time migration.
     */
    @Serializable
    internal class PersonDto(
        val id: String = "",
        val type: String = "YAHRZEIT",
        val name: String = "",
        val date: String? = null,
        val afterSunset: Boolean = false,
        val gYear: Int? = null,
        val gMonth: Int? = null,
        val gDay: Int? = null,
    ) {
        fun toModel(): PersonRecord? {
            val localDate = resolveDate() ?: return null
            val kind = when (type.trim().uppercase()) {
                "BIRTHDAY" -> PersonType.BIRTHDAY
                else -> PersonType.YAHRZEIT
            }
            return PersonRecord(
                id = id.ifBlank { UUID.randomUUID().toString() },
                type = kind,
                name = name,
                date = localDate,
                afterSunset = afterSunset,
            )
        }

        private fun resolveDate(): LocalDate? {
            date?.let { raw ->
                // Accept full ISO instants ("2024-05-18T00:00:00Z") and plain dates.
                val datePart = raw.substringBefore('T')
                return runCatching { LocalDate.parse(datePart) }
                    .getOrElse { _: Throwable ->
                        runCatching { LocalDate.parse(raw) }.getOrNull()
                    }
            }
            // Bind to locals: smart-casts on member properties are not propagated
            // into the `runCatching` lambda, so LocalDate.of(Int, Int, Int) needs them.
            val year = gYear
            val month = gMonth
            val day = gDay
            if (year != null && month != null && day != null) {
                return runCatching { LocalDate.of(year, month, day) }.getOrNull()
            }
            return null
        }

        companion object {
            fun from(r: PersonRecord) = PersonDto(
                id = r.id,
                type = r.type.name,
                name = r.name,
                date = r.date.toString(),
                afterSunset = r.afterSunset,
            )
        }
    }
}

/**
 * One-shot migration of the legacy family book stored under `moed_family`
 * (web `localStorage` parity / earlier build). Values are normalized into the
 * current `records` key and the legacy key is dropped.
 */
internal class LegacyFamilyMigration(
    @Suppress("unused") private val context: Context,
) : DataMigration<Preferences> {

    private val legacyKey = stringPreferencesKey("moed_family")

    override suspend fun shouldMigrate(currentData: Preferences): Boolean =
        currentData.contains(legacyKey) && !currentData.contains(PersonalStore.KEY)

    override suspend fun migrate(currentData: Preferences): Preferences {
        val p = currentData.toMutablePreferences()
        val legacy = p[legacyKey]
        if (legacy != null) {
            val normalized = PersonalStore.Codec.serialize(PersonalStore.Codec.parse(legacy))
            p[PersonalStore.KEY] = normalized
        }
        p.remove(legacyKey)
        return p
    }

    override suspend fun cleanUp() {}
}
