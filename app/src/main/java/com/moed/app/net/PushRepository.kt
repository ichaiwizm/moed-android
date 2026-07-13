package com.moed.app.net

import android.content.Context
import com.moed.app.data.AppSettings
import com.moed.app.data.CandleMode
import com.moed.app.data.City
import com.moed.app.data.NotifPrefs
import com.moed.app.data.Region
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Point d'entrée **best-effort** du module réseau **opt-in** de Moed (CONTRACTS §3.4).
 *
 * Piloté par le toggle « rappels serveur (bêta) » (Réglages), **désactivé par défaut**. Ce module
 * ne calcule **rien** : les rappels locaux ([com.moed.app] `notif/`) restent la source de vérité.
 * Toute opération est enveloppée dans `runCatching` → **échec silencieux**, jamais d'exception
 * remontée à l'UI. **Aucune donnée du carnet familial ne transite** : seuls des réglages non
 * nominatifs (ville, langue, minhag, toggles) sont envoyés.
 *
 * Dépend uniquement de `data/model` (`City`, `AppSettings`, …) + de sa propre couche `net`.
 *
 * Deux chemins :
 *  - **routes Next.js** (`/api/push/{subscribe,unsubscribe}`) : défaut, sans secret client ;
 *  - **PocketBase direct** : si [PushConfig.hasDirectPocketBaseAuth] (identifiants fournis).
 */
class PushRepository(
    context: Context,
    private val config: PushConfig = PushConfig.DEFAULT,
) {
    private val appContext = context.applicationContext
    private val tokenStore = AuthTokenStore(appContext)
    private val client = PushClient(config, tokenStore)
    private val api: PushApi get() = client.api

    /**
     * Synchronise l'état d'abonnement avec le toggle. Idempotent, best-effort.
     * @param enabled état du toggle « rappels serveur (bêta) ».
     * @param lang locale déjà résolue (he/fr/en) fournie par le ViewModel.
     */
    suspend fun sync(enabled: Boolean, city: City, settings: AppSettings, lang: String): Boolean =
        if (enabled) subscribe(city, settings, lang) else unsubscribe()

    /** Enregistre / met à jour l'abonnement. Retourne true si stocké, false sinon (silencieux). */
    suspend fun subscribe(city: City, settings: AppSettings, lang: String): Boolean =
        withContext(Dispatchers.IO) {
            runCatching {
                val endpoint = tokenStore.deviceId()
                val prefs = settings.notif.toPushPrefs()
                val minutes = effectiveCandleMinutes(city, settings.candleMode)
                val israel = resolveIsrael(city, settings.region)

                if (config.hasDirectPocketBaseAuth) {
                    upsertViaPocketBase(endpoint, city, lang, minutes, israel, prefs)
                } else {
                    subscribeViaApp(endpoint, city, lang, minutes, israel, prefs)
                }
            }.getOrDefault(false)
        }

    /** Supprime l'abonnement et efface le token. Best-effort. */
    suspend fun unsubscribe(): Boolean = withContext(Dispatchers.IO) {
        runCatching {
            val endpoint = tokenStore.deviceId()
            val removed = if (config.hasDirectPocketBaseAuth) {
                deleteViaPocketBase(endpoint)
            } else {
                val resp = api.unsubscribeViaApp(
                    url = appUrl("api/push/unsubscribe"),
                    body = UnsubscribePayload(endpoint = endpoint),
                )
                resp.isSuccessful && (resp.body()?.ok ?: false)
            }
            tokenStore.clear()
            removed
        }.getOrDefault(false)
    }

    // ---- Chemin routes Next.js (aucun token client) ----

    private suspend fun subscribeViaApp(
        endpoint: String,
        city: City,
        lang: String,
        minutes: Int,
        israel: Boolean,
        prefs: PushPrefs,
    ): Boolean {
        val resp = api.subscribeViaApp(
            url = appUrl("api/push/subscribe"),
            body = SubscribePayload(
                endpoint = endpoint,
                locale = lang,
                city = city.slug,
                tz = city.tz,
                lat = city.lat,
                lng = city.lng,
                candleMinutes = minutes,
                israel = israel,
                prefs = prefs,
            ),
        )
        return resp.isSuccessful && (resp.body()?.ok ?: false)
    }

    // ---- Chemin PocketBase direct (auth par token) ----

    private suspend fun upsertViaPocketBase(
        endpoint: String,
        city: City,
        lang: String,
        minutes: Int,
        israel: Boolean,
        prefs: PushPrefs,
    ): Boolean {
        if (!ensureAuthenticated()) return false
        val record = PushSubscriptionRecord(
            endpoint = endpoint,
            locale = lang,
            city = city.slug,
            tz = city.tz,
            lat = city.lat,
            lng = city.lng,
            candleMinutes = minutes,
            israel = israel,
            prefs = prefs,
            active = true,
        )
        val existingId = findSubscriptionId(endpoint)
        val resp = if (existingId != null) {
            api.updateSubscription(existingId, record.copy(id = existingId))
        } else {
            api.createSubscription(record)
        }
        return resp.isSuccessful
    }

    private suspend fun deleteViaPocketBase(endpoint: String): Boolean {
        if (!ensureAuthenticated()) return false
        val id = findSubscriptionId(endpoint) ?: return false
        return api.deleteSubscription(id).isSuccessful
    }

    private suspend fun findSubscriptionId(endpoint: String): String? {
        val filter = "endpoint=\"${endpoint.replace("\"", "\\\"")}\""
        val resp = api.findSubscriptions(filter = filter)
        return resp.body()?.items?.firstOrNull()?.id?.takeIf { it.isNotBlank() }
    }

    /** Authentifie si nécessaire ; token persisté puis injecté par [AuthInterceptor]. */
    private suspend fun ensureAuthenticated(): Boolean {
        if (!tokenStore.read().isNullOrBlank()) return true
        val resp = api.authWithPassword(
            collection = config.authCollection,
            body = AuthRequest(identity = config.identity, password = config.password),
        )
        val token = resp.body()?.token?.takeIf { it.isNotBlank() } ?: return false
        tokenStore.save(token)
        return true
    }

    // ---- Helpers de mapping (data/model → réseau) ----

    private fun appUrl(path: String): String = config.appBaseUrl.trimEnd('/') + "/" + path

    private fun NotifPrefs.toPushPrefs() = PushPrefs(
        shabbat = shabbat,
        omer = omer,
        hilula = hilula,
        yahrzeit = yahrzeit,
    )

    private fun effectiveCandleMinutes(city: City, mode: CandleMode): Int = when (mode) {
        CandleMode.AUTO -> city.candleMinutes
        CandleMode.M18 -> 18
        CandleMode.M20 -> 20
        CandleMode.M30 -> 30
        CandleMode.M40 -> 40
    }

    private fun resolveIsrael(city: City, region: Region): Boolean = when (region) {
        Region.AUTO -> city.israel
        Region.IL -> true
        Region.DIASPORA -> false
    }
}
