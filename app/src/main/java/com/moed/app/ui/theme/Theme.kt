package com.moed.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider

/**
 * Thème racine de Moed — DA « Le Louah Imprimé » (CONTRACTS §4.4, DESIGN §8).
 *
 * - **Light mode par défaut, toujours (v1).** `dark` vaut `isSystemInDarkTheme()` mais l'app v1 le force
 *   à `false` sauf préférence explicite ; ne jamais partir en dark par défaut.
 * - Fournit les tokens hors-scheme via [LocalMoedColors] (or, olive, twilight, nuances encre/papier).
 * - Le mouvement signature (shape-morphing, springs spatiaux) est porté par les composants eux-mêmes
 *   (voir `ui/components/`) : le thème se limite aux tokens stables couleur/typo/formes.
 *
 * @param dark thème sombre « négatif d'imprimerie ». Défaut suivi du système, forcé `false` en v1 par
 *   l'appelant (`MainActivity`) tant que la préférence n'est pas explicitement activée.
 */
@Composable
fun MoedTheme(
    dark: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val colorScheme = if (dark) MoedDarkColorScheme else MoedLightColorScheme
    val extra = if (dark) MoedDarkExtra else MoedLightExtra

    CompositionLocalProvider(LocalMoedColors provides extra) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = MoedTypography,
            shapes = MoedShapes,
            content = content,
        )
    }
}
