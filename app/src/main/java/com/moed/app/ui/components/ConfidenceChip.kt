package com.moed.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.moed.app.data.Confidence
import com.moed.app.ui.theme.LocalMoedColors
import com.moed.app.ui.theme.MoedShapes

/**
 * Puce de niveau de confiance d'une fiche de tsadik (§4.6, DESIGN §4) :
 * `high = gold`, `medium = twilight`, `traditional = inkMute`. Tampon `extraSmall` — lavis + filet
 * plein de la teinte + libellé capitales. La signature figée ne porte pas la langue ; libellés en
 * français (langue par défaut du code).
 */
@Composable
fun ConfidenceChip(confidence: Confidence) {
    val moed = LocalMoedColors.current
    val (fg: Color, bg: Color, label: String) = when (confidence) {
        Confidence.HIGH -> Triple(moed.gold, moed.goldWash, "Confirmé")
        Confidence.MEDIUM -> Triple(moed.twilight, moed.twilightWash, "Attesté")
        Confidence.TRADITIONAL -> Triple(moed.inkMute, MaterialTheme.colorScheme.surfaceContainer, "Traditionnel")
    }
    Text(
        text = label.uppercase(),
        style = MaterialTheme.typography.labelSmall.copy(
            fontWeight = FontWeight.SemiBold,
        ),
        color = fg,
        modifier = androidx.compose.ui.Modifier
            .clip(MoedShapes.extraSmall)
            .background(bg)
            .border(1.5.dp, fg, MoedShapes.extraSmall)
            .padding(horizontal = 10.dp, vertical = 5.dp),
    )
}
