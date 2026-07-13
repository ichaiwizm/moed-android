package com.moed.app.net

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * DTO réseau (kotlinx-serialization) du module push **opt-in**.
 *
 * Règle stricte : **aucune donnée du carnet familial** (noms, dates de proches) n'apparaît ici.
 * On ne transmet que des réglages non nominatifs (ville, langue, minhag, toggles) permettant à un
 * éventuel planificateur serveur d'envoyer des rappels génériques. Les rappels réels restent locaux.
 */

// ---- Authentification PocketBase (accès direct opt-in) ----

/** Corps de `POST /api/collections/{collection}/auth-with-password`. */
@Serializable
data class AuthRequest(
    val identity: String,
    val password: String,
)

/** Réponse d'auth PocketBase : `{ token, record }`. */
@Serializable
data class AuthResponse(
    val token: String = "",
    val record: AuthRecord? = null,
)

@Serializable
data class AuthRecord(
    val id: String = "",
    val email: String = "",
)

// ---- Enregistrement `push_subscriptions` (accès direct PocketBase) ----

/**
 * Enregistrement de la collection `push_subscriptions`. `endpoint` = identifiant d'appareil anonyme
 * (voir [AuthTokenStore.deviceId]). Champs alignés sur la route web `subscribe`.
 */
@Serializable
data class PushSubscriptionRecord(
    val id: String = "",
    val endpoint: String,
    val p256dh: String = "",
    val auth: String = "",
    val locale: String = "fr",
    val city: String = "",
    val tz: String = "",
    val lat: Double = 0.0,
    val lng: Double = 0.0,
    val candleMinutes: Int = 18,
    val israel: Boolean = false,
    val prefs: PushPrefs = PushPrefs(),
    val active: Boolean = true,
)

/** Liste PocketBase paginée (on n'utilise que `items`). */
@Serializable
data class PushListResponse(
    val page: Int = 1,
    val perPage: Int = 30,
    val totalItems: Int = 0,
    val items: List<PushSubscriptionRecord> = emptyList(),
)

// ---- Routes Next.js `/api/push/{subscribe,unsubscribe}` (chemin par défaut, sans auth) ----

/** Corps de `POST /api/push/subscribe`. Strictement non nominatif (aucun yahrzeit du carnet). */
@Serializable
data class SubscribePayload(
    val endpoint: String,
    val locale: String = "fr",
    val city: String = "",
    val tz: String = "",
    val lat: Double = 0.0,
    val lng: Double = 0.0,
    val candleMinutes: Int = 18,
    val israel: Boolean = false,
    val prefs: PushPrefs = PushPrefs(),
)

/** Corps de `POST /api/push/unsubscribe`. */
@Serializable
data class UnsubscribePayload(
    val endpoint: String,
)

/** Réponse générique best-effort des routes (`{ ok, stored?/removed? }`). */
@Serializable
data class PushRouteResult(
    val ok: Boolean = false,
    val stored: Boolean = false,
    val removed: Boolean = false,
    val error: String? = null,
)

/** Toggles de rappels (miroir de `NotifPrefs`, sans référence au carnet). */
@Serializable
data class PushPrefs(
    val shabbat: Boolean = false,
    val omer: Boolean = false,
    val hilula: Boolean = false,
    val yahrzeit: Boolean = false,
)
