package com.moed.app.ui.screens

import android.content.Context
import android.provider.Settings
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import com.moed.app.data.AppSettings
import com.moed.app.data.City
import com.moed.app.data.DayCategory
import com.moed.app.data.Localization
import com.moed.app.data.Region
import com.moed.app.ui.theme.MoedColors
import kotlinx.coroutines.delay
import java.time.LocalDate
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

/**
 * Helpers internes partagés par les écrans (`ui/screens`).
 * Aucune règle métier : uniquement dérivations UI (région, formatage, couleurs de badge, motion).
 */

/** Résout le booléen `il` (Israël) à partir des réglages et de la ville active (AUTO = minhag ville). */
internal fun regionIsrael(settings: AppSettings, city: City): Boolean = when (settings.region) {
    Region.IL -> true
    Region.DIASPORA -> false
    Region.AUTO -> city.israel
}

/** Locale de formatage BCP-47 (he-IL / fr-FR / en-US). */
internal fun formatLocale(lang: String): Locale = Locale.forLanguageTag(Localization.localeTag(lang))

/** Date grégorienne complète et localisée : « Vendredi 12 juillet 2026 ». */
internal fun formatGregorian(date: LocalDate, lang: String): String {
    val locale = formatLocale(lang)
    val raw = date.format(DateTimeFormatter.ofPattern("EEEE d MMMM yyyy", locale))
    return raw.replaceFirstChar { if (it.isLowerCase()) it.titlecase(locale) else it.toString() }
}

/** Date grégorienne courte : « 12 juil. 2026 ». */
internal fun formatGregorianShort(date: LocalDate, lang: String): String =
    date.format(DateTimeFormatter.ofPattern("d MMM yyyy", formatLocale(lang)))

/** Heure tabulaire d'un zman (HH:mm) dans le fuseau de la ville, ou « — » si null (latitude extrême). */
internal fun formatClock(time: ZonedDateTime?): String =
    time?.format(DateTimeFormatter.ofPattern("HH:mm")) ?: "—"

/**
 * Couleur de puce/badge selon la catégorie (mapping figé CONTRACTS §4.1 / DESIGN §4) :
 * fête=gold · jeûne=rose · Roch Hodech=twilight · Omer=sage · autre=inkMute.
 */
internal fun categoryColor(category: DayCategory, colors: MoedColors): Color = when (category) {
    DayCategory.HOLIDAY -> colors.gold
    DayCategory.FAST -> colors.rose
    DayCategory.ROSH_CHODESH -> colors.twilight
    DayCategory.OMER -> colors.sage
    DayCategory.CHOL_HAMOED, DayCategory.NONE -> colors.inkMute
}

/** Distance approximative (km) entre deux villes — tri des « villes proches ». */
internal fun haversineKm(a: City, b: City): Double {
    val r = 6371.0
    val dLat = Math.toRadians(b.lat - a.lat)
    val dLng = Math.toRadians(b.lng - a.lng)
    val lat1 = Math.toRadians(a.lat)
    val lat2 = Math.toRadians(b.lat)
    val h = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
        Math.cos(lat1) * Math.cos(lat2) * Math.sin(dLng / 2) * Math.sin(dLng / 2)
    return 2 * r * Math.asin(Math.min(1.0, Math.sqrt(h)))
}

/** Vrai si l'utilisateur a coupé les animations système (reduce-motion). */
internal fun reduceMotionEnabled(context: Context): Boolean = try {
    Settings.Global.getFloat(
        context.contentResolver,
        Settings.Global.ANIMATOR_DURATION_SCALE,
        1f,
    ) == 0f
} catch (_: Exception) {
    false
}

/**
 * Révélation « caractère qui tombe et cale » (DESIGN §5) : fade + translationY(24→0), overshoot spring.
 * Respecte reduce-motion (apparition instantanée, valeurs inchangées).
 */
@Composable
internal fun RevealOnAppear(
    index: Int = 0,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    val context = LocalContext.current
    val reduce = remember { reduceMotionEnabled(context) }
    var shown by remember { mutableStateOf(reduce) }
    val progress by animateFloatAsState(
        targetValue = if (shown) 1f else 0f,
        animationSpec = spring(dampingRatio = 0.75f, stiffness = Spring.StiffnessMedium),
        label = "reveal",
    )
    LaunchedEffect(Unit) {
        if (!reduce) {
            delay(index * 55L)
            shown = true
        }
    }
    Box(
        modifier.graphicsLayer {
            alpha = progress
            translationY = (1f - progress) * 24f
        },
    ) { content() }
}
