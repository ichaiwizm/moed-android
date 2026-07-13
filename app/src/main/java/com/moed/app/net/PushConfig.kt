package com.moed.app.net

/**
 * Configuration figée du module réseau **optionnel** de Moed.
 *
 * Rappel des lois (CONTRACTS §3.4 / NATIVE_SPEC §7) : ce module est **opt-in**, jamais utilisé pour
 * le calcul. Les rappels **locaux** restent la source de vérité ; tout appel réseau est *best-effort*
 * (échec silencieux). Aucune donnée du carnet familial ne transite jamais par ces routes.
 *
 * Deux bases distinctes :
 *  - [pbBaseUrl]  : PocketBase réutilisé (`pocketbase-moed`) — accès direct aux enregistrements.
 *  - [appBaseUrl] : routes Next.js de l'app Moed (`/api/push/{subscribe,unsubscribe}`) qui
 *                   s'authentifient côté serveur (aucun token client requis) — chemin par défaut.
 *
 * [authCollection] / [identity] / [password] ne servent qu'au chemin PocketBase direct (auth par
 * token `{ token, record }` sur `auth-with-password`). Laissés vides par défaut → seul le chemin
 * routes Next.js est employé, sans aucun secret embarqué.
 */
data class PushConfig(
    val pbBaseUrl: String = "https://pb-moed.vpsdashboard.space/",
    val appBaseUrl: String = "https://moed.vpsdashboard.space/",
    val authCollection: String = "users",
    val identity: String = "",
    val password: String = "",
    val enableHttpLog: Boolean = false,
) {
    /** true si des identifiants d'accès direct PocketBase sont fournis (sinon → routes Next.js). */
    val hasDirectPocketBaseAuth: Boolean
        get() = identity.isNotBlank() && password.isNotBlank()

    companion object {
        /** Configuration par défaut : routes Next.js, aucun secret, best-effort. */
        val DEFAULT = PushConfig()
    }
}
