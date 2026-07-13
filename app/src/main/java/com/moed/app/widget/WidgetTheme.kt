package com.moed.app.widget

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceModifier
import androidx.glance.appwidget.cornerRadius
import androidx.glance.background
import androidx.glance.color.ColorProvider
import androidx.glance.unit.ColorProvider
import androidx.glance.layout.Box
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.text.FontFamily
import androidx.glance.text.FontStyle
import androidx.glance.text.FontWeight
import androidx.glance.text.TextAlign
import androidx.glance.text.TextStyle
import androidx.compose.runtime.Composable

/**
 * Jeton de la DA « Le Louah Imprimé » transposés à Glance (widgets home-screen).
 *
 * Glance ne sait PAS charger les polices custom (Fraunces / Frank Ruhl / Instrument / Assistant) ni
 * activer les features OpenType (`tnum`) — cf. CONTRACTS §5.6 & DESIGN §4. On garde donc l'esprit
 * « bloc de plomb » avec les seuls leviers disponibles :
 *  - fond `paperRaised` **opaque** (jamais de flou / translucidité — garde-fou anti-Apple, DESIGN §9),
 *  - chiffres héros en **gras + [FontFamily.Monospace]** (fallback système « tabular » : chasse fixe →
 *    les heures s'alignent en colonne comme des caractères de plomb),
 *  - filets d'imprimeur pleins de 2 dp (`outlineStrong`),
 *  - un **seul** accent couleur par widget : `ner` (allumage), `sage` (Omer), `twilight` (Roch Hodech).
 *
 * Chaque couleur est un [ColorProvider] jour/nuit : le dark « négatif d'imprimerie » (DESIGN §1.2) est
 * fourni pour respecter le thème système, mais l'app reste light par défaut (DESIGN §8).
 */
internal object WidgetColors {
    // Papier & encre (DESIGN §1.1 light / §1.2 dark) --------------------------------------------
    val paperRaised = ColorProvider(day = Color(0xFFFFFDF7), night = Color(0xFF221D15))
    val paperSunken = ColorProvider(day = Color(0xFFE6D9BE), night = Color(0xFF14110C))
    val ink = ColorProvider(day = Color(0xFF171512), night = Color(0xFFF0E6D2))
    val inkSmoke = ColorProvider(day = Color(0xFF3E3A32), night = Color(0xFFCBBE9F))
    val inkMute = ColorProvider(day = Color(0xFF6E675B), night = Color(0xFF9A8C6F))
    val outlineStrong = ColorProvider(day = Color(0xFFB29A66), night = Color(0xFF6B5D42))

    // Accents (un seul par vue) ------------------------------------------------------------------
    /** Ner (flamme) — accent allumage Chabbat / zmanim héros. */
    val ner = ColorProvider(day = Color(0xFFB4471F), night = Color(0xFFE06A3C))
    val nerWash = ColorProvider(day = Color(0xFFF3E2D0), night = Color(0xFF3A2416))

    /** Sage (olive) — accent Omer. */
    val sage = ColorProvider(day = Color(0xFF5A6B3A), night = Color(0xFF9FB073))
    val sageWash = ColorProvider(day = Color(0xFFE9EBDC), night = Color(0xFF242A18))

    /** Twilight (bleu-nuit d'imprimerie) — accent Roch Hodech / molad. */
    val twilight = ColorProvider(day = Color(0xFF1F3A5F), night = Color(0xFF8AA6D0))
    val twilightWash = ColorProvider(day = Color(0xFFE4E9F1), night = Color(0xFF20293A))
}

/** Styles de texte partagés — l'échelle typographique « presse » du louah, en fallback système. */
internal object WidgetType {
    /** Chiffre-plomb héros (heure d'allumage, jour du Omer) : gras + chasse fixe, aligné colonne. */
    fun hero(accent: ColorProvider, size: Int = 34) = TextStyle(
        color = accent,
        fontSize = size.sp,
        fontWeight = FontWeight.Bold,
        fontFamily = FontFamily.Monospace,
    )

    /** Heure tabulaire de registre (zmanim) : monospace pour l'alignement en colonne. */
    fun time(accent: ColorProvider = WidgetColors.ner) = TextStyle(
        color = accent,
        fontSize = 16.sp,
        fontWeight = FontWeight.Bold,
        fontFamily = FontFamily.Monospace,
    )

    /** Titre / sur-titre de la vignette (méta estampée). */
    val title = TextStyle(
        color = WidgetColors.inkMute,
        fontSize = 12.sp,
        fontWeight = FontWeight.Medium,
    )

    /** Nom de ville / date hébraïque : encre pleine, serif-esque via poids fort. */
    val label = TextStyle(
        color = WidgetColors.ink,
        fontSize = 14.sp,
        fontWeight = FontWeight.Medium,
    )

    /** Ligne secondaire (havdala, compte à rebours, note). */
    val sub = TextStyle(
        color = WidgetColors.inkSmoke,
        fontSize = 12.sp,
        fontWeight = FontWeight.Normal,
    )

    /** Ligne secondaire centrée (libellé Omer sous le chiffre héros). */
    val subCentered = TextStyle(
        color = WidgetColors.inkSmoke,
        fontSize = 12.sp,
        fontWeight = FontWeight.Normal,
        textAlign = TextAlign.Center,
    )

    /** Titre de fête / accent estampé en italique (esprit Fraunces italique). */
    val emphasis = TextStyle(
        color = WidgetColors.ink,
        fontSize = 13.sp,
        fontWeight = FontWeight.Medium,
        fontStyle = FontStyle.Italic,
    )
}

/**
 * Bloc papier racine d'un widget : aplat `paperRaised` **opaque**, coins généreux (24 dp, échelle
 * `large` M3), marge de page 16 dp. Aucune ombre/flou (impossible & indésirable en Glance).
 */
internal fun paperBlock(): GlanceModifier =
    GlanceModifier
        .fillMaxSize()
        .background(WidgetColors.paperRaised)
        .cornerRadius(24.dp)
        .padding(16.dp)

/** Filet d'imprimeur plein de 2 dp, pleine largeur (séparateur de registre — DESIGN §3). */
@Composable
internal fun InkRule(color: ColorProvider = WidgetColors.outlineStrong) {
    Box(
        modifier = GlanceModifier
            .fillMaxWidth()
            .height(2.dp)
            .background(color)
    ) {}
}
