package com.moed.app.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.OpenInNew
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.moed.app.data.Localization
import com.moed.app.data.Tzadik
import com.moed.app.engine.JewishInfo
import com.moed.app.ui.MoedViewModel
import com.moed.app.ui.components.ConfidenceChip
import com.moed.app.ui.components.InkRule
import com.moed.app.ui.components.PressCard
import com.moed.app.ui.theme.LocalMoedColors
import com.moed.app.ui.theme.rememberBodyFamily
import com.moed.app.ui.theme.rememberDisplayFamily
import java.time.LocalDate

@Composable
fun TzadikDetailScreen(vm: MoedViewModel, slug: String) {
    val lang by vm.lang.collectAsState()
    val tzadik = remember(slug, vm.tzadikim) { vm.tzadikim.firstOrNull { it.slug == slug } }

    if (tzadik == null) {
        Column(
            Modifier.fillMaxSize().padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Text(
                text = Localization.string("common.empty", lang),
                style = MaterialTheme.typography.bodyLarge,
                fontFamily = rememberBodyFamily(lang),
                color = LocalMoedColors.current.inkMute,
            )
        }
        return
    }

    DetailBody(tzadik, lang)
}

@Composable
private fun DetailBody(t: Tzadik, lang: String) {
    val colors = LocalMoedColors.current
    val display = rememberDisplayFamily(lang)
    val body = rememberBodyFamily(lang)
    val jewish = remember { JewishInfo() }
    val uriHandler = LocalUriHandler.current

    val nextHilula = remember(t, lang) { jewish.nextHilula(t.hilula, LocalDate.now()) }

    val scroll = rememberScrollState()
    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(scroll)
            .padding(horizontal = 20.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        // Header : motif + nom trilingue + épithète italique.
        Row(verticalAlignment = Alignment.CenterVertically) {
            MotifTile(t.category)
            Spacer(Modifier.width(16.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    text = t.names[lang] ?: t.names["en"] ?: t.slug,
                    style = MaterialTheme.typography.headlineMedium,
                    fontFamily = display,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground,
                )
                t.epithet?.let { ep ->
                    Text(
                        text = ep[lang] ?: ep["en"] ?: "",
                        style = MaterialTheme.typography.titleMedium,
                        fontFamily = display,
                        fontStyle = FontStyle.Italic,
                        color = colors.inkSmoke,
                    )
                }
            }
        }

        ConfidenceChip(confidence = t.confidence)

        PressCard {
            InfoRow(Localization.string("tsadikim.hilulaDate", lang), "${t.hilula.day} ${t.hilula.monthEn}", lang)
            t.hilulaNote?.let { note ->
                (note[lang] ?: note["en"])?.let {
                    Spacer(Modifier.height(4.dp))
                    Text(it, style = MaterialTheme.typography.bodyMedium, fontFamily = body, color = colors.inkSmoke)
                }
            }
            nextHilula?.let {
                Spacer(Modifier.height(12.dp))
                InkRule()
                Spacer(Modifier.height(12.dp))
                InfoRow(
                    Localization.string("tsadikim.nextHilula", lang),
                    "${it.hebrewLabel} · ${formatGregorianShort(it.gregorian, lang)}",
                    lang,
                )
            }
            t.kever?.let { k ->
                Spacer(Modifier.height(12.dp))
                InkRule()
                Spacer(Modifier.height(12.dp))
                InfoRow(
                    Localization.string("tsadikim.kever", lang),
                    listOf(k.place, k.country).filter { it.isNotBlank() }.joinToString(", "),
                    lang,
                )
            }
        }

        if (t.works.isNotEmpty()) {
            PressCard {
                Text(
                    text = Localization.string("tsadikim.works", lang),
                    style = MaterialTheme.typography.labelMedium,
                    fontFamily = body,
                    color = colors.inkMute,
                )
                Spacer(Modifier.height(6.dp))
                t.works.forEach { w ->
                    Text(
                        text = "· $w",
                        style = MaterialTheme.typography.bodyLarge,
                        fontFamily = body,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }
            }
        }

        // Biographie.
        (t.bio[lang] ?: t.bio["en"])?.takeIf { it.isNotBlank() }?.let { bio ->
            Text(
                text = bio,
                style = MaterialTheme.typography.bodyLarge,
                fontFamily = body,
                color = colors.inkSmoke,
            )
        }

        // Note visuel symbolique.
        Text(
            text = Localization.string("tsadikim.visualNote", lang),
            style = MaterialTheme.typography.bodySmall,
            fontFamily = body,
            fontStyle = FontStyle.Italic,
            color = colors.inkMute,
        )

        if (t.sources.isNotEmpty()) {
            InkRule()
            Text(
                text = Localization.string("tsadikim.sources", lang),
                style = MaterialTheme.typography.labelMedium,
                fontFamily = body,
                color = colors.inkMute,
            )
            t.sources.forEach { src ->
                Row(
                    Modifier
                        .fillMaxWidth()
                        .clickable { uriHandler.openUri(src) }
                        .padding(vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        Icons.AutoMirrored.Outlined.OpenInNew,
                        contentDescription = null,
                        tint = colors.twilight,
                        modifier = Modifier.width(18.dp),
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = src,
                        style = MaterialTheme.typography.bodyMedium,
                        fontFamily = body,
                        color = colors.twilight,
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        }
        Spacer(Modifier.height(24.dp))
    }
}

@Composable
private fun InfoRow(label: String, value: String, lang: String) {
    val colors = LocalMoedColors.current
    val body = rememberBodyFamily(lang)
    Column {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            fontFamily = body,
            color = colors.inkMute,
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge,
            fontFamily = body,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}
