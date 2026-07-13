package com.moed.app.net

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import java.util.UUID

/**
 * Stockage local du **token PocketBase** (accès direct opt-in) via DataStore Preferences.
 *
 * Signature figée (CONTRACTS §3.4) : [read] / [save] / [clear]. Le token n'est écrit que si
 * l'utilisateur active « rappels serveur (bêta) » et que l'authentification directe réussit.
 * L'appel réseau étant *best-effort*, l'absence de token est un état normal (échec silencieux).
 *
 * On persiste aussi un **identifiant d'appareil** anonyme et stable ([deviceId]) : il sert de clé
 * `endpoint` pour l'upsert de l'abonnement push. C'est un UUID aléatoire généré une seule fois —
 * **aucune donnée personnelle**, aucune info du carnet.
 *
 * Les getters sont volontairement synchrones (contrat) : les accès DataStore sont rapides et
 * toujours invoqués depuis un contexte d'I/O (coroutine `Dispatchers.IO` du repository).
 */
class AuthTokenStore(private val context: Context) {

    private object Keys {
        val TOKEN = stringPreferencesKey("pb_token")
        val DEVICE_ID = stringPreferencesKey("device_id")
    }

    /** Lit le token PocketBase courant, ou null s'il n'a jamais été enregistré / a été effacé. */
    fun read(): String? = runBlocking {
        context.pushDataStore.data.first()[Keys.TOKEN]?.takeIf { it.isNotBlank() }
    }

    /** Enregistre (ou remplace) le token PocketBase après `auth-with-password` / `auth-refresh`. */
    fun save(token: String) = runBlocking {
        context.pushDataStore.edit { it[Keys.TOKEN] = token }
        Unit
    }

    /** Efface le token (désactivation du toggle ou révocation). */
    fun clear() = runBlocking {
        context.pushDataStore.edit { it.remove(Keys.TOKEN) }
        Unit
    }

    /**
     * Identifiant d'appareil anonyme et stable, généré paresseusement au premier appel.
     * Sert de clé `endpoint` pour identifier/mettre à jour l'abonnement — jamais un vrai endpoint
     * Web Push (le natif n'en a pas), simplement une clé de dédoublonnage côté serveur.
     */
    fun deviceId(): String = runBlocking {
        val existing = context.pushDataStore.data.first()[Keys.DEVICE_ID]
        if (!existing.isNullOrBlank()) return@runBlocking existing
        val generated = "moed-android-" + UUID.randomUUID()
        context.pushDataStore.edit { it[Keys.DEVICE_ID] = generated }
        generated
    }

    private companion object {
        val Context.pushDataStore by preferencesDataStore(name = "moed_push")
    }
}
