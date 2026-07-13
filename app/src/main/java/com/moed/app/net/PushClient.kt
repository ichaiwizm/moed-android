package com.moed.app.net

import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import java.util.concurrent.TimeUnit

/**
 * Fabrique du client réseau **opt-in** : OkHttp + Retrofit + kotlinx-serialization.
 *
 * - `Json` tolérant (`ignoreUnknownKeys`) — le serveur peut renvoyer des champs supplémentaires.
 * - Un client **nu** ([refreshClient]) sans intercepteur d'auth, dédié au `auth-refresh`, pour
 *   éviter toute récursion depuis [AuthInterceptor].
 * - Le client applicatif porte [AuthInterceptor] (Authorization + retry unique sur 401).
 * - Timeouts courts : le réseau est un bonus, il ne doit jamais faire attendre l'app.
 */
class PushClient(
    private val config: PushConfig,
    private val tokenStore: AuthTokenStore,
) {
    val json: Json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
        explicitNulls = false
    }

    private val logging = HttpLoggingInterceptor().apply {
        level = if (config.enableHttpLog) {
            HttpLoggingInterceptor.Level.BASIC
        } else {
            HttpLoggingInterceptor.Level.NONE
        }
    }

    /** Client sans auth, uniquement pour rafraîchir le token (pas de récursion). */
    private val refreshClient: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(8, TimeUnit.SECONDS)
        .readTimeout(8, TimeUnit.SECONDS)
        .addInterceptor(logging)
        .build()

    private val authInterceptor = AuthInterceptor(tokenStore) { refreshTokenBlocking() }

    private val httpClient: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(8, TimeUnit.SECONDS)
        .readTimeout(8, TimeUnit.SECONDS)
        .callTimeout(15, TimeUnit.SECONDS)
        .addInterceptor(authInterceptor)
        .addInterceptor(logging)
        .build()

    val api: PushApi = Retrofit.Builder()
        .baseUrl(config.pbBaseUrl)
        .client(httpClient)
        .addConverterFactory(json.asConverterFactory(JSON_MEDIA_TYPE))
        .build()
        .create(PushApi::class.java)

    /**
     * Rafraîchit le token via `auth-refresh` avec un client nu. Retourne le nouveau token (déjà
     * persisté) ou null en cas d'échec. Best-effort : aucune exception propagée.
     */
    private fun refreshTokenBlocking(): String? {
        val current = tokenStore.read()?.takeIf { it.isNotBlank() } ?: return null
        val url = config.pbBaseUrl.trimEnd('/') +
            "/api/collections/${config.authCollection}/auth-refresh"
        val request = Request.Builder()
            .url(url)
            .header("Authorization", current)
            .post(ByteArray(0).toRequestBody(JSON_MEDIA_TYPE))
            .build()
        return runCatching {
            refreshClient.newCall(request).execute().use { resp ->
                if (!resp.isSuccessful) return@use null
                val bodyStr = resp.body?.string().orEmpty()
                if (bodyStr.isBlank()) return@use null
                val parsed = json.decodeFromString(AuthResponse.serializer(), bodyStr)
                parsed.token.takeIf { it.isNotBlank() }?.also { tokenStore.save(it) }
            }
        }.getOrNull()
    }

    private companion object {
        val JSON_MEDIA_TYPE = "application/json".toMediaType()
    }
}
