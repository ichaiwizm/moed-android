package com.moed.app.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

/*
 * DA « Le Louah Imprimé » — encre & papier, aucun aplat translucide (DESIGN §1).
 * Tous les tokens bruts vivent ici (light = préfixe `L`, dark = préfixe `D`) et
 * sont réutilisés par `MoedLightColorScheme` / `MoedDarkColorScheme` (§1.3) et par
 * `MoedColors` (tokens hors-ColorScheme, cf. MoedColors.kt).
 */

// ---------------------------------------------------------------------------
// LIGHT — papier chaud (DESIGN §1.1)
// ---------------------------------------------------------------------------
val LPaper = Color(0xFFF4ECDD)            // fond app, jamais blanc pur
val LPaperRaised = Color(0xFFFFFDF7)      // cartes, feuilles, dialogs
val LPaperSunken = Color(0xFFE6D9BE)      // champs, pistes, zones enfoncées
val LSurfaceContainer = Color(0xFFEDE2C8)
val LSurfaceContainerLow = Color(0xFFF1E6D0)
val LSurfaceContainerHigh = Color(0xFFE3D6BB)
val LSurfaceContainerHighest = Color(0xFFDACBAC)
val LSurfaceDim = Color(0xFFE0D3B8)
val LOutline = Color(0xFFC9B78E)          // filet d'imprimeur
val LOutlineStrong = Color(0xFFB29A66)    // séparateur de registre, cadre
val LInk = Color(0xFF171512)              // noir chaud, texte principal
val LInkSmoke = Color(0xFF3E3A32)         // texte secondaire
val LInkMute = Color(0xFF6E675B)          // labels, méta, hints
val LNer = Color(0xFFB4471F)              // vermillon — accent unique/vue
val LNerStrong = Color(0xFF8F3616)        // pressed / texte sur wash
val LNerGlow = Color(0xFFE39A6A)          // halo « zman-clé / allumage »
val LNerWash = Color(0xFFF3E2D0)          // fond puce/indicateur actif
val LOnNer = Color(0xFFFFF6EC)            // texte/icône sur aplat vermillon
val LTwilight = Color(0xFF1F3A5F)         // bleu-nuit — Roch Hodech, molad
val LTwilightWash = Color(0xFFE4E9F1)
val LSage = Color(0xFF5A6B3A)             // olive — Omer, positif
val LSageWash = Color(0xFFE9EBDC)
val LGold = Color(0xFFB0851F)             // or mat — fêtes / hiloula
val LGoldWash = Color(0xFFF2E8CC)
val LRose = Color(0xFFA6472E)             // jeûne, alerte douce, suppression
val LRoseWash = Color(0xFFF3E1D8)
val LFocus = Color(0xFF2C6FB3)

// ---------------------------------------------------------------------------
// DARK — « négatif d'imprimerie », chaud, jamais bleu-froid (DESIGN §1.2)
// ---------------------------------------------------------------------------
val DPaper = Color(0xFF191510)
val DPaperRaised = Color(0xFF221D15)
val DPaperSunken = Color(0xFF14110C)
val DSurfaceContainer = Color(0xFF282218)
val DSurfaceContainerLow = Color(0xFF1E1912)
val DSurfaceContainerHigh = Color(0xFF332B1F)
val DSurfaceContainerHighest = Color(0xFF3A3122)
val DSurfaceDim = Color(0xFF191510)
val DOutline = Color(0xFF4A4130)
val DOutlineStrong = Color(0xFF6B5D42)
val DInk = Color(0xFFF0E6D2)              // texte couleur parchemin
val DInkSmoke = Color(0xFFCBBE9F)
val DInkMute = Color(0xFF9A8C6F)
val DNer = Color(0xFFE06A3C)              // plus lumineux sur fond sombre
val DNerStrong = Color(0xFFF0895C)
val DNerGlow = Color(0xFF7A3B1E)
val DNerWash = Color(0xFF3A2416)
val DOnNer = Color(0xFF1B0F08)
val DTwilight = Color(0xFF8AA6D0)
val DTwilightWash = Color(0xFF20293A)
val DSage = Color(0xFF9FB073)
val DSageWash = Color(0xFF242A18)
val DGold = Color(0xFFD9B45A)
val DGoldWash = Color(0xFF332912)
val DRose = Color(0xFFD98060)
val DRoseWash = Color(0xFF331B14)
val DFocus = Color(0xFF6FA8DE)

// ---------------------------------------------------------------------------
// ColorScheme Material 3 — mapping figé DESIGN §1.3
// ---------------------------------------------------------------------------
val MoedLightColorScheme: ColorScheme = lightColorScheme(
    primary = LNer,
    onPrimary = LOnNer,
    primaryContainer = LNerWash,
    onPrimaryContainer = LNerStrong,
    inversePrimary = DNer,
    secondary = LTwilight,
    onSecondary = LOnNer,
    secondaryContainer = LTwilightWash,
    onSecondaryContainer = LTwilight,
    tertiary = LSage,
    onTertiary = LOnNer,
    tertiaryContainer = LSageWash,
    onTertiaryContainer = LSage,
    error = LRose,
    onError = LOnNer,
    errorContainer = LRoseWash,
    onErrorContainer = LRose,
    background = LPaper,
    onBackground = LInk,
    surface = LPaper,
    onSurface = LInk,
    surfaceVariant = LPaperSunken,
    onSurfaceVariant = LInkSmoke,
    surfaceTint = LNer,
    inverseSurface = LInk,
    inverseOnSurface = LPaper,
    outline = LOutline,
    outlineVariant = LPaperSunken,
    scrim = LInk,
    surfaceBright = LPaperRaised,
    surfaceDim = LSurfaceDim,
    surfaceContainerLowest = LPaperRaised,
    surfaceContainerLow = LSurfaceContainerLow,
    surfaceContainer = LSurfaceContainer,
    surfaceContainerHigh = LSurfaceContainerHigh,
    surfaceContainerHighest = LSurfaceContainerHighest,
)

val MoedDarkColorScheme: ColorScheme = darkColorScheme(
    primary = DNer,
    onPrimary = DOnNer,
    primaryContainer = DNerWash,
    onPrimaryContainer = DNerStrong,
    inversePrimary = LNer,
    secondary = DTwilight,
    onSecondary = DOnNer,
    secondaryContainer = DTwilightWash,
    onSecondaryContainer = DTwilight,
    tertiary = DSage,
    onTertiary = DOnNer,
    tertiaryContainer = DSageWash,
    onTertiaryContainer = DSage,
    error = DRose,
    onError = DOnNer,
    errorContainer = DRoseWash,
    onErrorContainer = DRose,
    background = DPaper,
    onBackground = DInk,
    surface = DPaper,
    onSurface = DInk,
    surfaceVariant = DPaperSunken,
    onSurfaceVariant = DInkSmoke,
    surfaceTint = DNer,
    inverseSurface = DInk,
    inverseOnSurface = DPaper,
    outline = DOutline,
    outlineVariant = DPaperSunken,
    scrim = Color(0xFF000000),
    surfaceBright = DSurfaceContainerHigh,
    surfaceDim = DSurfaceDim,
    surfaceContainerLowest = DPaperSunken,
    surfaceContainerLow = DSurfaceContainerLow,
    surfaceContainer = DSurfaceContainer,
    surfaceContainerHigh = DSurfaceContainerHigh,
    surfaceContainerHighest = DSurfaceContainerHighest,
)
