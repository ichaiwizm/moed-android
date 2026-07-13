package com.moed.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.moed.app.data.ShabbatInfo
import com.moed.app.ui.theme.LocalMoedColors
import com.moed.app.ui.theme.cartoucheShape
import com.moed.app.ui.theme.rememberBodyFamily
import com.moed.app.ui.theme.rememberDisplayFamily
import java.time.Instant

/**
 * Carte « Prochain Chabbat » (§4.6) — carte héros de l'écran Aujourd'hui et **seul élément vermillon
 * de la vue** (DESIGN §4).
 *
 * Cartouche estampé **asymétrique** (miroité en RTL), fond `paperRaised` opaque, **halo ner** (ombre
 * teintée `nerGlow` + filet `ner`), titre en display italique, **heure d'allumage en chiffres-plomb
 * XXL tabulaires**, **compte à rebours live**, puis havdala et parasha/haftara en registre.
 *
 * @param now instant courant (horloge 1 s) pour le compte à rebours.
 * @param lang langue active (he/fr/en).
 */
@Composable
fun ShabbatCard(
    info: ShabbatInfo,
    now: Instant,
    lang: String,
    modifier: Modifier = Modifier,
) {
    val rtl = LocalLayoutDirection.current == LayoutDirection.Rtl
    val shape = cartoucheShape(rtl)
    val moed = LocalMoedColors.current
    val displayFamily = rememberDisplayFamily(lang)
    val bodyFamily = rememberBodyFamily(lang)
    val ner = MaterialTheme.colorScheme.primary

    val titleText = when (lang) { "he" -> "שבת הקרובה"; "fr" -> "Prochain Chabbat"; else -> "Next Shabbat" }
    val candleText = when (lang) { "he" -> "הדלקת נרות"; "fr" -> "Allumage"; else -> "Candle lighting" }
    val havdalaText = when (lang) { "he" -> "הבדלה"; "fr" -> "Havdala"; else -> "Havdala" }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .shadow(elevation = 8.dp, shape = shape, ambientColor = moed.nerGlow, spotColor = moed.nerGlow)
            .clip(shape)
            .background(moed.paperRaised)
            .border(2.dp, ner, shape)
            .padding(horizontal = 24.dp, vertical = 20.dp),
    ) {
        Text(
            text = titleText,
            style = MaterialTheme.typography.headlineMedium.copy(
                fontFamily = displayFamily,
                fontStyle = if (lang == "he") FontStyle.Normal else FontStyle.Italic,
            ),
            color = ner,
        )
        Text(
            text = candleText,
            style = MaterialTheme.typography.labelMedium.copy(fontFamily = bodyFamily),
            color = moed.inkMute,
            modifier = Modifier.padding(top = 12.dp),
        )
        Text(
            text = formatHour(info.candleLighting),
            style = MaterialTheme.typography.displayLarge.copy(
                fontFamily = displayFamily,
                fontFeatureSettings = "tnum",
                textDirection = TextDirection.Content,
            ),
            color = MaterialTheme.colorScheme.onSurface,
        )
        CountdownText(
            target = info.candleLighting,
            now = now,
            lang = lang,
            modifier = Modifier.padding(top = 4.dp),
        )

        InkRule(modifier = Modifier.padding(vertical = 16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = havdalaText,
                style = MaterialTheme.typography.bodyLarge.copy(fontFamily = bodyFamily),
                color = moed.inkSmoke,
            )
            Text(
                text = formatHour(info.havdala),
                style = MaterialTheme.typography.titleLarge.copy(
                    fontFamily = displayFamily,
                    fontFeatureSettings = "tnum",
                    textDirection = TextDirection.Content,
                ),
                color = MaterialTheme.colorScheme.onSurface,
            )
        }

        if (info.parasha.isNotBlank()) {
            Text(
                text = info.parasha,
                style = MaterialTheme.typography.titleMedium.copy(fontFamily = displayFamily),
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(top = 12.dp),
            )
        }
        if (info.haftara.isNotBlank()) {
            Text(
                text = info.haftara,
                style = MaterialTheme.typography.bodyMedium.copy(fontFamily = bodyFamily),
                color = moed.inkSmoke,
                modifier = Modifier.padding(top = 2.dp),
            )
        }
    }
}
