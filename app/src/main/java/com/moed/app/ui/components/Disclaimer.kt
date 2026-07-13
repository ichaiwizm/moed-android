package com.moed.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.moed.app.ui.theme.LocalMoedColors
import com.moed.app.ui.theme.MoedShapes
import com.moed.app.ui.theme.rememberBodyFamily

/**
 * Avertissement halakhique **obligatoire** sous l'écran Zmanim ville (§4.6, CONTRACTS §2 « Disclaimer +
 * shita affichés »). Bloc `paperSunken` opaque + filet, texte `inkMute`, mentionne la [shita] employée.
 * Texte trilingue inline (aucune dépendance data).
 */
@Composable
fun Disclaimer(shita: String, lang: String) {
    val moed = LocalMoedColors.current
    val body = rememberBodyFamily(lang)
    val text = when (lang) {
        "he" -> "הזמנים מחושבים לפי שיטת $shita. לעיון בלבד — יש לוודא מול רב מוסמך מקומי."
        "fr" -> "Horaires calculés selon la shita « $shita ». À titre indicatif — vérifiez auprès de votre autorité rabbinique locale."
        else -> "Times computed per the “$shita” shita. For guidance only — verify with your local rabbinic authority."
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(MoedShapes.small)
            .background(moed.paperSunken)
            .border(1.5.dp, MaterialTheme.colorScheme.outline, MoedShapes.small)
            .padding(12.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Icon(
            imageVector = Icons.Outlined.Info,
            contentDescription = null,
            tint = moed.inkMute,
            modifier = Modifier.size(18.dp),
        )
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall.copy(fontFamily = body),
            color = moed.inkMute,
        )
    }
}
