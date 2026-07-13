package com.moed.app.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextMotion
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.moed.app.R

/*
 * Typographie « chiffres de plomb » (DESIGN §2).
 *
 * Quatre familles variables self-hostées dans `res/font/` (zéro requête réseau) :
 *   - Fraunces    (latin, display/headline/title, serif à fort contraste)
 *   - Frank Ruhl  (hébreu, display/headline/title)
 *   - Instrument  (latin, body/label)
 *   - Assistant   (hébreu, body/label)
 *
 * Le choix latin/hébreu est piloté par la locale active via [rememberDisplayFamily] /
 * [rememberBodyFamily]. Les fichiers de police attendus (portés depuis mvp-moed/android) :
 *   fraunces_var.ttf · frank_ruhl_libre_var.ttf · instrument_sans_var.ttf · assistant_var.ttf
 */

@OptIn(ExperimentalTextApi::class)
private fun variable(resId: Int, weight: FontWeight) = Font(
    resId = resId,
    weight = weight,
    variationSettings = FontVariation.Settings(FontVariation.weight(weight.weight)),
)

// ---- Familles latines --------------------------------------------------------
val Fraunces = FontFamily(
    variable(R.font.fraunces_var, FontWeight.Normal),
    variable(R.font.fraunces_var, FontWeight.Medium),
    variable(R.font.fraunces_var, FontWeight.SemiBold),
    variable(R.font.fraunces_var, FontWeight.Bold),
    variable(R.font.fraunces_var, FontWeight.Black),
)

val Instrument = FontFamily(
    variable(R.font.instrument_sans_var, FontWeight.Normal),
    variable(R.font.instrument_sans_var, FontWeight.Medium),
    variable(R.font.instrument_sans_var, FontWeight.SemiBold),
    variable(R.font.instrument_sans_var, FontWeight.Bold),
)

// ---- Familles hébraïques -----------------------------------------------------
val FrankRuhl = FontFamily(
    variable(R.font.frank_ruhl_libre_var, FontWeight.Normal),
    variable(R.font.frank_ruhl_libre_var, FontWeight.Medium),
    variable(R.font.frank_ruhl_libre_var, FontWeight.Bold),
    variable(R.font.frank_ruhl_libre_var, FontWeight.Black),
)

val Assistant = FontFamily(
    variable(R.font.assistant_var, FontWeight.Normal),
    variable(R.font.assistant_var, FontWeight.Medium),
    variable(R.font.assistant_var, FontWeight.SemiBold),
    variable(R.font.assistant_var, FontWeight.Bold),
)

/**
 * Style « chiffres tabulaires » — obligatoire partout où des heures s'alignent en colonne
 * (registres zmanim, calendrier, compte à rebours). Les chiffres restent latins LTR, jamais miroités
 * (DESIGN §2, §6). À composer par-dessus n'importe quel style via [TextStyle.merge].
 */
val TabularNums: TextStyle = TextStyle(
    fontFeatureSettings = "tnum",
    textMotion = TextMotion.Static,
)

/**
 * Échelle Material 3 dérivée de l'échelle modulaire ~1.25 (base 16 sp) de la DA (DESIGN §2).
 * Display / headline / title servent la **Fraunces** (serif, poids lourds — « caractères de plomb »),
 * body / label servent **Instrument** (grotesque humaniste discret). L'hébreu est substitué au moment
 * du rendu via [rememberDisplayFamily] / [rememberBodyFamily].
 */
val MoedTypography = Typography(
    displayLarge = TextStyle(
        fontFamily = Fraunces, fontWeight = FontWeight.Black,
        fontSize = 57.sp, lineHeight = 62.sp, letterSpacing = (-0.02).em,
        textMotion = TextMotion.Animated,
    ),
    displayMedium = TextStyle(
        fontFamily = Fraunces, fontWeight = FontWeight.Black,
        fontSize = 45.sp, lineHeight = 50.sp, letterSpacing = (-0.015).em,
    ),
    displaySmall = TextStyle(
        fontFamily = Fraunces, fontWeight = FontWeight.Bold,
        fontSize = 36.sp, lineHeight = 42.sp,
    ),
    headlineLarge = TextStyle(
        fontFamily = Fraunces, fontWeight = FontWeight.SemiBold,
        fontSize = 32.sp, lineHeight = 38.sp,
    ),
    headlineMedium = TextStyle(
        fontFamily = Fraunces, fontWeight = FontWeight.SemiBold,
        fontSize = 28.sp, lineHeight = 34.sp,
    ),
    headlineSmall = TextStyle(
        fontFamily = Fraunces, fontWeight = FontWeight.SemiBold,
        fontSize = 24.sp, lineHeight = 30.sp,
    ),
    titleLarge = TextStyle(
        fontFamily = Fraunces, fontWeight = FontWeight.Medium,
        fontSize = 22.sp, lineHeight = 28.sp,
    ),
    titleMedium = TextStyle(
        fontFamily = Fraunces, fontWeight = FontWeight.Medium,
        fontSize = 18.sp, lineHeight = 24.sp,
    ),
    titleSmall = TextStyle(
        fontFamily = Instrument, fontWeight = FontWeight.SemiBold,
        fontSize = 14.sp, lineHeight = 20.sp, letterSpacing = 0.1.sp,
    ),
    bodyLarge = TextStyle(
        fontFamily = Instrument, fontWeight = FontWeight.Normal,
        fontSize = 16.sp, lineHeight = 24.sp,
    ),
    bodyMedium = TextStyle(
        fontFamily = Instrument, fontWeight = FontWeight.Normal,
        fontSize = 14.sp, lineHeight = 20.sp,
    ),
    bodySmall = TextStyle(
        fontFamily = Instrument, fontWeight = FontWeight.Normal,
        fontSize = 12.sp, lineHeight = 16.sp,
    ),
    labelLarge = TextStyle(
        fontFamily = Instrument, fontWeight = FontWeight.Medium,
        fontSize = 14.sp, lineHeight = 20.sp, letterSpacing = 0.3.sp,
    ),
    labelMedium = TextStyle(
        fontFamily = Instrument, fontWeight = FontWeight.Medium,
        fontSize = 12.sp, lineHeight = 16.sp, letterSpacing = 0.3.sp,
    ),
    labelSmall = TextStyle(
        fontFamily = Instrument, fontWeight = FontWeight.Medium,
        fontSize = 11.sp, lineHeight = 16.sp, letterSpacing = 0.4.sp,
    ),
)

/**
 * Famille display/headline/title selon la langue : hébreu → **Frank Ruhl Libre**, sinon **Fraunces**
 * (DESIGN §2, §6). À appliquer sur les styles `display`, `headline` et `title` (Fraunces/Medium+).
 */
@Composable
fun rememberDisplayFamily(lang: String): FontFamily =
    remember(lang) { if (lang == "he") FrankRuhl else Fraunces }

/**
 * Famille body/label selon la langue : hébreu → **Assistant**, sinon **Instrument** (DESIGN §2, §6).
 */
@Composable
fun rememberBodyFamily(lang: String): FontFamily =
    remember(lang) { if (lang == "he") Assistant else Instrument }
