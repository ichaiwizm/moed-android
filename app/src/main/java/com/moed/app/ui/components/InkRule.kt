package com.moed.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.moed.app.ui.theme.LocalMoedColors

/**
 * Filet d'imprimeur — séparateur **plein de 2 dp** (jamais le divider fin gris M3 par défaut).
 * DESIGN §3 : les registres (zmanim, réglages) se scandent par ces filets d'encre pleine largeur.
 *
 * @param strong `true` → `outlineStrong` (cadres de registre, séparateurs forts) ; sinon `outline`.
 */
@Composable
fun InkRule(
    strong: Boolean = false,
    modifier: Modifier = Modifier,
) {
    val color = if (strong) LocalMoedColors.current.outlineStrong else MaterialTheme.colorScheme.outline
    androidx.compose.foundation.layout.Box(
        modifier = modifier
            .fillMaxWidth()
            .height(2.dp)
            .background(color),
    )
}
