package com.moed.app.ui.components

import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextDirection
import com.moed.app.ui.theme.rememberBodyFamily
import java.time.Duration
import java.time.Instant
import java.time.ZonedDateTime

/**
 * Compte à rebours live vers [target] (§4.6). L'appelant fait tourner [now] (horloge 1 s du ViewModel) ;
 * le chiffre se met à jour **à la seconde, sans animation** (l'exactitude prime — DESIGN §5). Rendu en
 * chiffres tabulaires, direction de contenu forcée `Content` pour que les nombres restent LTR même en
 * hébreu.
 *
 * @param target échéance (candleLighting, havdala, prochaine occurrence…).
 * @param now instant courant fourni par l'appelant.
 * @param lang langue active (he/fr/en) — pilote le gabarit textuel et la police de corps.
 */
@Composable
fun CountdownText(
    target: ZonedDateTime,
    now: Instant,
    lang: String,
    modifier: Modifier = Modifier,
) {
    val remaining = Duration.between(now, target.toInstant()).seconds
    val label = formatCountdown(remaining, lang)
    Text(
        text = label,
        modifier = modifier,
        style = LocalTextStyle.current.copy(
            fontFamily = rememberBodyFamily(lang),
            fontFeatureSettings = "tnum",
            textDirection = TextDirection.Content,
        ),
    )
}
