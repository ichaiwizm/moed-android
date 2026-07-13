package com.moed.app.net

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query
import retrofit2.http.Url

/**
 * API réseau **opt-in** (Retrofit + OkHttp + kotlinx-serialization) — CONTRACTS §3.4.
 *
 * Deux familles de routes :
 *  1. **PocketBase direct** (`pocketbase-moed`) : `auth-with-password` → `{ token, record }`,
 *     `auth-refresh`, puis upsert/delete d'un enregistrement `push_subscriptions`. L'en-tête
 *     `Authorization` est injecté par [AuthInterceptor] ; un unique retry sur 401 est déclenché
 *     après `auth-refresh`.
 *  2. **Routes Next.js** de l'app (`/api/push/{subscribe,unsubscribe}`) qui s'authentifient côté
 *     serveur → aucun token client requis. Chemin par défaut (aucun secret embarqué), via `@Url`.
 *
 * Tous les retours sont des `Response<…>` bruts : le repository les traite en *best-effort*
 * (échec silencieux, jamais d'exception propagée à l'UI).
 */
interface PushApi {

    // ---- 1. PocketBase direct ----

    @POST("api/collections/{collection}/auth-with-password")
    suspend fun authWithPassword(
        @Path("collection") collection: String,
        @Body body: AuthRequest,
    ): Response<AuthResponse>

    @POST("api/collections/{collection}/auth-refresh")
    suspend fun authRefresh(
        @Path("collection") collection: String,
    ): Response<AuthResponse>

    @GET("api/collections/push_subscriptions/records")
    suspend fun findSubscriptions(
        @Query("filter") filter: String,
        @Query("perPage") perPage: Int = 1,
    ): Response<PushListResponse>

    @POST("api/collections/push_subscriptions/records")
    suspend fun createSubscription(
        @Body body: PushSubscriptionRecord,
    ): Response<PushSubscriptionRecord>

    @PATCH("api/collections/push_subscriptions/records/{id}")
    suspend fun updateSubscription(
        @Path("id") id: String,
        @Body body: PushSubscriptionRecord,
    ): Response<PushSubscriptionRecord>

    @DELETE("api/collections/push_subscriptions/records/{id}")
    suspend fun deleteSubscription(
        @Path("id") id: String,
    ): Response<Unit>

    // ---- 2. Routes Next.js (URL absolue, sans auth client) ----

    @POST
    suspend fun subscribeViaApp(
        @Url url: String,
        @Body body: SubscribePayload,
    ): Response<PushRouteResult>

    @POST
    suspend fun unsubscribeViaApp(
        @Url url: String,
        @Body body: UnsubscribePayload,
    ): Response<PushRouteResult>
}
