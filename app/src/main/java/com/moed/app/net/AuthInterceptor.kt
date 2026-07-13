package com.moed.app.net

import okhttp3.Interceptor
import okhttp3.Response

/**
 * Intercepteur OkHttp du module push **opt-in** (CONTRACTS §3.4).
 *
 * Comportement figé :
 *  - injecte l'en-tête `Authorization: <token PocketBase>` s'il existe dans [AuthTokenStore] ;
 *  - sur une réponse **401**, tente **un unique** rafraîchissement via [refresh] (`auth-refresh`),
 *    persiste le nouveau token, puis **rejoue une seule fois** la requête d'origine.
 *
 * [refresh] doit utiliser un client OkHttp **distinct** (sans cet intercepteur) pour éviter toute
 * récursion. Tout échec de refresh laisse passer la réponse 401 telle quelle : le repository
 * traitera l'échec silencieusement (best-effort).
 */
class AuthInterceptor(
    private val tokenStore: AuthTokenStore,
    private val refresh: () -> String?,
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val original = chain.request()

        val token = tokenStore.read()
        val firstRequest = if (token.isNullOrBlank()) {
            original
        } else {
            original.newBuilder().header(HEADER, token).build()
        }

        val response = chain.proceed(firstRequest)
        if (response.code != 401) return response

        // 401 → un seul essai de rafraîchissement + rejeu.
        val newToken = refresh()?.takeIf { it.isNotBlank() } ?: return response
        response.close()

        val retried = original.newBuilder().header(HEADER, newToken).build()
        return chain.proceed(retried)
    }

    private companion object {
        const val HEADER = "Authorization"
    }
}
