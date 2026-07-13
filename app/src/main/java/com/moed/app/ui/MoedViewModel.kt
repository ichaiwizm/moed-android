package com.moed.app.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.moed.app.data.AppSettings
import com.moed.app.data.AssetData
import com.moed.app.data.City
import com.moed.app.data.Localization
import com.moed.app.data.PersonRecord
import com.moed.app.data.PersonalStore
import com.moed.app.data.SettingsStore
import com.moed.app.data.Tzadik
import com.moed.app.notif.NotificationScheduler
import com.moed.app.widget.WidgetUpdater
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.Instant

/**
 * Source unique de vérité UI de Moed (« Le Louah Imprimé »).
 *
 * Un seul ViewModel, pas de Hilt/Koin : l'app est offline, sans backend requis, et l'injection
 * manuelle via `viewModel()` suffit (CONTRACTS §5.2, NATIVE_SPEC §5).
 *
 * Contrats respectés à la lettre :
 *  - `settings`      ← [SettingsStore.flow] (`stateIn` `SharingStarted.Eagerly`)
 *  - `personal`      ← [PersonalStore.flow]
 *  - `cities` / `tzadikim` ← [AssetData] (chargés une seule fois, cache mémoire)
 *  - `activeCity`    dérivé de `settings.citySlug`
 *  - `lang`          dérivé via [Localization.resolve] (`system` → he/fr/en, fallback en)
 *  - `now`           horloge live 1 s pour les comptes à rebours (allumage, havdala)
 *
 * Les dérivés lourds (dates hébraïques, zmanim, occurrences) NE sont PAS calculés ici : ils le sont
 * dans les écrans via `remember(settings, now)` / `derivedStateOf` (calcul KosherJava synchrone,
 * déterministe, pas de coroutine). Ce ViewModel ne porte que l'état persistant + l'horloge, et
 * orchestre les effets de bord des mutations.
 *
 * **Effet de bord unique et systématique** : toute mutation d'état persistant écrit d'abord dans le
 * DataStore, **puis** replanifie les notifications locales et rafraîchit les widgets Glance —
 * offline, best-effort, sans réseau.
 */
class MoedViewModel(app: Application) : AndroidViewModel(app) {

    private val settingsStore = SettingsStore(app)
    private val personalStore = PersonalStore(app)
    private val scheduler = NotificationScheduler(app)
    private val widgetUpdater = WidgetUpdater(app)

    /** Réglages persistés (ville, minhag, langue, notifications). */
    val settings: StateFlow<AppSettings> =
        settingsStore.flow.stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = AppSettings(),
        )

    /** Carnet familial persisté (anniversaires / yahrzeits), on-device uniquement. */
    val personal: StateFlow<List<PersonRecord>> =
        personalStore.flow.stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = emptyList(),
        )

    /** Dataset embarqué des ~189 villes (chargé une fois depuis `assets/cities.json`). */
    val cities: List<City> = AssetData.cities(app)

    /** Dataset embarqué des ~32 tsadikim (chargé une fois depuis `assets/tzadikim.json`). */
    val tzadikim: List<Tzadik> = AssetData.tzadikim(app)

    /** Ville active dérivée de `settings.citySlug` (fallback robuste si le slug est inconnu). */
    val activeCity: StateFlow<City> =
        settings.map { resolveCity(it.citySlug) }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = resolveCity(settings.value.citySlug),
        )

    /** Langue effective résolue (`system` → he/fr/en, fallback en) — pilote locale, RTL, tables i18n. */
    val lang: StateFlow<String> =
        settings.map { Localization.resolve(it.language) }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = Localization.resolve(settings.value.language),
        )

    /** Horloge live à la seconde : source des comptes à rebours (allumage Chabbat, havdala). */
    val now: StateFlow<Instant> =
        tickerFlow().stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = Instant.now(),
        )

    // ---------------------------------------------------------------------------------------------
    // Mutations — écrivent le DataStore PUIS replanifient notifications + widgets (CONTRACTS §5.2)
    // ---------------------------------------------------------------------------------------------

    /** Mutation générique des réglages (langue, ville, minhag, notifications). */
    fun updateSettings(transform: (AppSettings) -> AppSettings) {
        viewModelScope.launch {
            settingsStore.update(transform)
            rescheduleAll()
        }
    }

    /** Ajoute un proche au carnet familial. */
    fun addPerson(person: PersonRecord) {
        viewModelScope.launch {
            personalStore.add(person)
            rescheduleAll()
        }
    }

    /** Retire un proche du carnet familial par identifiant. */
    fun removePerson(id: String) {
        viewModelScope.launch {
            personalStore.remove(id)
            rescheduleAll()
        }
    }

    /** Change la langue in-app (`system`/he/fr/en) — recalcule locale + RTL en aval. */
    fun setLanguage(lang: String) = updateSettings { it.copy(language = lang) }

    /** Définit la ville active à partir de son slug. */
    fun setCity(slug: String) = updateSettings { it.copy(citySlug = slug) }

    // ---------------------------------------------------------------------------------------------
    // Interne
    // ---------------------------------------------------------------------------------------------

    /**
     * Relit l'état persistant le plus récent (le DataStore vient d'être écrit) puis replanifie les
     * rappels locaux et rafraîchit les widgets. Best-effort, offline : jamais de réseau.
     */
    private suspend fun rescheduleAll() {
        val current = settingsStore.flow.first()
        val people = personalStore.flow.first()
        scheduler.rescheduleAll(current, people, resolveCity(current.citySlug))
        widgetUpdater.requestUpdate()
    }

    /** Résout un slug de ville vers un [City] concret, avec repli déterministe si absent. */
    private fun resolveCity(slug: String): City =
        AssetData.city(slug)
            ?: cities.firstOrNull { it.slug == AppSettings().citySlug }
            ?: cities.first()

    /** Émet [Instant.now] chaque seconde (l'horloge de compte à rebours de l'UI). */
    private fun tickerFlow(): Flow<Instant> = flow {
        while (true) {
            emit(Instant.now())
            kotlinx.coroutines.delay(1_000L)
        }
    }
}
