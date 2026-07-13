package com.moed.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.moed.app.data.DayCategory
import com.moed.app.ui.theme.MoedShapes

/**
 * Badge « tampon » carré (`extraSmall` = 8 dp) apposé sur l'en-tête « Aujourd'hui » et les listes
 * (§4.6). Aplat lavis + filet 1.5 dp de la couleur pleine + libellé en capitales tabulaires ; couleur
 * pilotée par la catégorie via le mapping figé (fête = gold, jeûne = rose, RH = twilight, Omer = sage,
 * autre = inkMute — §4.1). Opaque, aucun dégradé (DA « Le Louah Imprimé »).
 */
@Composable
fun StampBadge(
    label: String,
    category: DayCategory,
    modifier: Modifier = Modifier,
) {
    val swatch = categorySwatch(category)
    Text(
        text = label.uppercase(),
        style = androidx.compose.material3.MaterialTheme.typography.labelSmall.copy(
            fontWeight = FontWeight.SemiBold,
            fontFeatureSettings = "tnum",
        ),
        color = swatch.fg,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        modifier = modifier
            .semantics { contentDescription = label }
            .clip(MoedShapes.extraSmall)
            .background(swatch.bg)
            .border(1.5.dp, swatch.fg, MoedShapes.extraSmall)
            .padding(horizontal = 8.dp, vertical = 4.dp),
    )
}
