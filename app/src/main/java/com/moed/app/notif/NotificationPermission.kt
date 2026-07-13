package com.moed.app.notif

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat

/**
 * `POST_NOTIFICATIONS` est demandée **à l'opt-in** (au moment où l'utilisateur active un rappel
 * dans Réglages), **jamais au lancement**, et uniquement sur API 33+ (avant, la permission n'existe
 * pas et est considérée accordée).
 */

/** `true` si l'app peut poster des notifications (toujours vrai avant Android 13). */
fun hasPostNotificationsPermission(context: Context): Boolean =
    Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
        ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) ==
        PackageManager.PERMISSION_GRANTED

/**
 * Fournit un déclencheur `() -> Unit` à appeler depuis l'écran Réglages au moment de l'opt-in.
 * Sur API < 33 (ou si déjà accordée) il rappelle immédiatement [onResult] avec `true` sans afficher
 * de dialogue système.
 *
 * ```
 * val requestPermission = rememberPostNotificationsPermissionRequester { granted ->
 *     if (granted) vm.updateSettings { it.copy(notif = it.notif.copy(shabbat = true)) }
 * }
 * Switch(checked = shabbat, onCheckedChange = { if (it) requestPermission() else disable() })
 * ```
 */
@Composable
fun rememberPostNotificationsPermissionRequester(
    onResult: (granted: Boolean) -> Unit,
): () -> Unit {
    val context = LocalContext.current
    val latestOnResult = rememberUpdatedState(onResult)
    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { granted -> latestOnResult.value(granted) }

    return remember(launcher, context) {
        {
            when {
                Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ->
                    latestOnResult.value(true)
                hasPostNotificationsPermission(context) ->
                    latestOnResult.value(true)
                else ->
                    launcher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }
}
