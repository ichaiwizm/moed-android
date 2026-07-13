package com.moed.app.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.moed.app.data.Zman
import com.moed.app.ui.theme.LocalMoedColors
import com.moed.app.ui.theme.rememberBodyFamily

/**
 * Registre de zmanim (§4.6) — le « bloc de plomb » de l'écran Aujourd'hui / Zmanim ville.
 *
 * Chaque ligne : libellé du zman (start) + shita en méta (`inkMute`) + **heure tabulaire alignée en
 * colonne** (end). Filets d'imprimeur 2 dp entre les lignes. Zman dont `time == null` (latitude
 * extrême) → « — », jamais NaN. L'heure garde des chiffres latins LTR même en hébreu ; le bloc, lui,
 * s'aligne côté `end` grâce à la direction de layout héritée.
 *
 * Les libellés pleinement localisés viennent de `Localization.zmanLabel` côté écran ; ce composant,
 * indépendant de toute couche data, affiche un repli lisible dérivé de la clé.
 */
@Composable
fun ZmanRegister(
    zmanim: List<Zman>,
    lang: String,
    modifier: Modifier = Modifier,
) {
    val bodyFamily = rememberBodyFamily(lang)
    val moed = LocalMoedColors.current

    Column(modifier = modifier.fillMaxWidth()) {
        zmanim.forEachIndexed { index, zman ->
            if (index > 0) InkRule()
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = humanizeZmanKey(zman.key),
                        style = MaterialTheme.typography.bodyLarge.copy(fontFamily = bodyFamily),
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    if (zman.shita.isNotBlank()) {
                        Text(
                            text = zman.shita,
                            style = MaterialTheme.typography.labelSmall.copy(fontFamily = bodyFamily),
                            color = moed.inkMute,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }
                Text(
                    text = formatHour(zman.time),
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontFeatureSettings = "tnum",
                        textDirection = TextDirection.Content,
                    ),
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.End,
                    maxLines = 1,
                    modifier = Modifier.widthIn(min = 56.dp),
                )
            }
        }
    }
}
