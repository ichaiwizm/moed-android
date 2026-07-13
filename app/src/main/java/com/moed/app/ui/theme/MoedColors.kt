package com.moed.app.ui.theme

import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

/**
 * Tokens de couleur **hors `ColorScheme`** de la DA « Le Louah Imprimé » (DESIGN §8, CONTRACTS §4.1).
 *
 * Material 3 ne dispose que de trois accents ; Moed en a besoin de plus (or estampé, olive Omer,
 * bleu-nuit twilight) plus des nuances d'encre et de papier. Ces tokens sont fournis via
 * [LocalMoedColors] par `MoedTheme` et lus par les composants signature.
 *
 * Correspondance catégorie → token (badges/puces, figée) :
 *   fête = [gold] · jeûne = [rose] · Roch Hodech = [twilight] · Omer = [sage] · autre = [inkMute].
 */
data class MoedColors(
    val gold: Color,
    val goldWash: Color,
    val inkMute: Color,
    val inkSmoke: Color,
    val outlineStrong: Color,
    val nerStrong: Color,
    val nerGlow: Color,
    val twilight: Color,
    val twilightWash: Color,
    val sage: Color,
    val sageWash: Color,
    val rose: Color,
    val roseWash: Color,
    val paperRaised: Color,
    val paperSunken: Color,
)

/** Extra tokens du thème clair (DESIGN §1.1). */
val MoedLightExtra = MoedColors(
    gold = LGold,
    goldWash = LGoldWash,
    inkMute = LInkMute,
    inkSmoke = LInkSmoke,
    outlineStrong = LOutlineStrong,
    nerStrong = LNerStrong,
    nerGlow = LNerGlow,
    twilight = LTwilight,
    twilightWash = LTwilightWash,
    sage = LSage,
    sageWash = LSageWash,
    rose = LRose,
    roseWash = LRoseWash,
    paperRaised = LPaperRaised,
    paperSunken = LPaperSunken,
)

/** Extra tokens du thème sombre — « négatif d'imprimerie » (DESIGN §1.2). */
val MoedDarkExtra = MoedColors(
    gold = DGold,
    goldWash = DGoldWash,
    inkMute = DInkMute,
    inkSmoke = DInkSmoke,
    outlineStrong = DOutlineStrong,
    nerStrong = DNerStrong,
    nerGlow = DNerGlow,
    twilight = DTwilight,
    twilightWash = DTwilightWash,
    sage = DSage,
    sageWash = DSageWash,
    rose = DRose,
    roseWash = DRoseWash,
    paperRaised = DPaperRaised,
    paperSunken = DPaperSunken,
)

/**
 * `CompositionLocal` des tokens hors-scheme. Défaut = thème clair (light-first, DESIGN §8).
 * `staticCompositionLocalOf` : ces valeurs ne changent qu'au basculement de thème (rare), pas besoin
 * de suivi de lecture fin.
 */
val LocalMoedColors = staticCompositionLocalOf { MoedLightExtra }
