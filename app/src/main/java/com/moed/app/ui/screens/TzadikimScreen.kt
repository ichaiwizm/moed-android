package com.moed.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.moed.app.data.HilulaDate
import com.moed.app.data.Localization
import com.moed.app.data.Tzadik
import com.moed.app.data.TzadikCategory
import com.moed.app.engine.JewishInfo
import com.moed.app.ui.MoedViewModel
import com.moed.app.ui.components.InkRule
import com.moed.app.ui.components.PressCard
import com.moed.app.ui.theme.LocalMoedColors
import com.moed.app.ui.theme.MoedColors
import com.moed.app.ui.theme.rememberBodyFamily
import com.moed.app.ui.theme.rememberDisplayFamily
import java.time.LocalDate

@Composable
fun TzadikimScreen(vm: MoedViewModel, onOpenTzadik: (String) -> Unit) {
    val lang by vm.lang.collectAsState()
    val jewish = remember { JewishInfo() }
    val today = LocalDate.now()
    val heToday = remember(today) { jewish.hebrewMonthDay(today) }

    val todayHiloula = remember(vm.tzadikim, heToday) { vm.tzadikim.filter { it.hilula == heToday } }
    val others = remember(vm.tzadikim, heToday) { vm.tzadikim.filter { it.hilula != heToday } }
    val body = rememberBodyFamily(lang)

    LazyColumn(
        Modifier.fillMaxSize().padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(vertical = 12.dp),
    ) {
        item {
            SectionLabel(Localization.string("tsadikim.todayTitle", lang), lang)
        }
        if (todayHiloula.isEmpty()) {
            item {
                PressCard {
                    Text(
                        text = Localization.string("tsadikim.noneToday", lang),
                        style = MaterialTheme.typography.bodyMedium,
                        fontFamily = body,
                        color = LocalMoedColors.current.inkMute,
                    )
                }
            }
        } else {
            items(todayHiloula, key = { "today-" + it.slug }) { t ->
                TzadikCard(t, lang, feature = true, onClick = { onOpenTzadik(t.slug) })
            }
        }
        item {
            Spacer(Modifier.height(8.dp))
            SectionLabel(Localization.string("tsadikim.allTitle", lang), lang)
        }
        items(others, key = { it.slug }) { t ->
            TzadikCard(t, lang, feature = false, onClick = { onOpenTzadik(t.slug) })
        }
        item { Spacer(Modifier.height(24.dp)) }
    }
}

@Composable
private fun SectionLabel(text: String, lang: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleMedium,
        fontFamily = rememberDisplayFamily(lang),
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.onBackground,
    )
}

@Composable
private fun TzadikCard(t: Tzadik, lang: String, feature: Boolean, onClick: () -> Unit) {
    val colors = LocalMoedColors.current
    val display = rememberDisplayFamily(lang)
    val body = rememberBodyFamily(lang)
    val shape = if (feature) {
        RoundedCornerShape(topStart = 24.dp, topEnd = 8.dp, bottomEnd = 24.dp, bottomStart = 8.dp)
    } else {
        MaterialTheme.shapes.medium
    }
    PressCard(onClick = onClick, shape = shape) {
        Row(verticalAlignment = Alignment.Top) {
            MotifTile(t.category)
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    text = t.names[lang] ?: t.names["en"] ?: t.slug,
                    style = MaterialTheme.typography.titleLarge,
                    fontFamily = display,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                t.epithet?.let { ep ->
                    Text(
                        text = ep[lang] ?: ep["en"] ?: "",
                        style = MaterialTheme.typography.bodyMedium,
                        fontFamily = display,
                        fontStyle = FontStyle.Italic,
                        color = colors.inkSmoke,
                    )
                }
                Spacer(Modifier.height(6.dp))
                Text(
                    text = hilulaLabel(t.hilula, lang),
                    style = MaterialTheme.typography.labelMedium,
                    fontFamily = body,
                    color = colors.twilight,
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = (t.bio[lang] ?: t.bio["en"] ?: "").takeIf { it.isNotBlank() }?.let {
                        if (it.length > 160) it.take(157) + "…" else it
                    } ?: "",
                    style = MaterialTheme.typography.bodyMedium,
                    fontFamily = body,
                    color = colors.inkSmoke,
                )
            }
        }
    }
}

/** Vignette symbolique par courant — jamais de faux portrait (exigence éditoriale). */
@Composable
fun MotifTile(category: TzadikCategory, modifier: Modifier = Modifier) {
    val colors = LocalMoedColors.current
    val (bg, fg, glyph) = motifStyle(category, colors)
    Box(
        modifier
            .size(52.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(bg),
        contentAlignment = Alignment.Center,
    ) {
        Text(text = glyph, style = MaterialTheme.typography.titleLarge, color = fg)
    }
}

private fun motifStyle(category: TzadikCategory, c: MoedColors): Triple<Color, Color, String> = when (category) {
    TzadikCategory.TANNA -> Triple(c.goldWash, c.gold, "א")      // א
    TzadikCategory.RISHON -> Triple(c.twilightWash, c.twilight, "ב") // ב
    TzadikCategory.ACHARON -> Triple(c.sageWash, c.sage, "ג")    // ג
    TzadikCategory.HASSID -> Triple(c.roseWash, c.rose, "ד")     // ד
    TzadikCategory.HABAD -> Triple(c.twilightWash, c.twilight, "ה") // ה
    TzadikCategory.SEFARADE -> Triple(c.goldWash, c.gold, "ו")   // ו
}

internal fun hilulaLabel(hilula: HilulaDate, lang: String): String =
    Localization.string("tsadikim.hilulaDate", lang) + " · " + hilula.day + " " + hilula.monthEn
