package com.moed.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import com.moed.app.ui.MoedRoot
import com.moed.app.ui.theme.MoedTheme

/**
 * Activité unique de Moed (single-Activity, tout en Compose).
 *
 * Responsabilités strictement « shell » :
 *  1. [enableEdgeToEdge] — l'UI se dessine derrière les barres système (barres transparentes,
 *     gérées par le thème M3 dans [MoedTheme]).
 *  2. Poser le thème racine [MoedTheme] puis la navigation racine [MoedRoot].
 *
 * Choix du thème : **light mode par défaut, toujours** (loi non négociable de la DA). On ne
 * bascule en « négatif d'imprimerie » (dark) que si le **système** le demande — d'où
 * `dark = isSystemInDarkTheme()`, dont l'état de repos (système clair) reste le light mode.
 *
 * Ce qui n'est **pas** ici (par contrat) :
 *  - Le [android.Manifest.permission.POST_NOTIFICATIONS] n'est PAS demandé au lancement : il l'est
 *    à l'**opt-in** (écran Réglages, API 33+).
 *  - L'état applicatif ([com.moed.app.ui.MoedViewModel]), la locale/RTL et le [androidx.compose.material3.Scaffold]
 *    sont pris en charge par [MoedRoot] (module `ui/nav`).
 */
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MoedTheme(dark = isSystemInDarkTheme()) {
                MoedRoot()
            }
        }
    }
}
