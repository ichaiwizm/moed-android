package com.moed.app.ui.theme

import androidx.compose.foundation.shape.AbsoluteRoundedCornerShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp

/*
 * Formes — squircles francs & cartouches estampés (DESIGN §3).
 *
 * Échelle de rayons M3 (« shape scale ») :
 *   extraSmall 8 · small 12 · medium 16 · large 24 · extraLarge 32 · full 999 (pilule/CircleShape).
 */

/** Rayon « pilule » (boutons pilule, FAB, indicateur de nav). `full = 999` de la DA. */
val MoedFullShape: Shape = RoundedCornerShape(999.dp)

val MoedShapes: Shapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),   // chips « tampon », inputs, badges
    small = RoundedCornerShape(12.dp),       // petits boutons, puces d'événement
    medium = RoundedCornerShape(16.dp),      // cartes standard (registre zmanim, listes)
    large = RoundedCornerShape(24.dp),       // cartes feature (Chabbat), sheets
    extraLarge = RoundedCornerShape(32.dp),  // modales, grande carte d'allumage
)

/**
 * Cartouche asymétrique signature (DESIGN §3) réservé aux 1–2 cartes héros par écran
 * (carte Chabbat, hiloula du jour) : `topStart 24 / topEnd 8 / bottomEnd 24 / bottomStart 8`,
 * évoquant une « une » composée.
 *
 * On utilise un [AbsoluteRoundedCornerShape] (coins gauche/droite, non résolus par la direction) et on
 * miroite **explicitement** selon [rtl] afin de garder un contrôle total (start/end logiques) :
 * en RTL, `start` passe à droite → les rayons s'inversent.
 */
fun cartoucheShape(rtl: Boolean): Shape {
    val big = 24.dp
    val small = 8.dp
    return if (!rtl) {
        // LTR : start = gauche, end = droite
        AbsoluteRoundedCornerShape(
            topLeft = big,      // topStart
            topRight = small,   // topEnd
            bottomRight = big,  // bottomEnd
            bottomLeft = small, // bottomStart
        )
    } else {
        // RTL : start = droite, end = gauche (miroir)
        AbsoluteRoundedCornerShape(
            topLeft = small,    // topEnd
            topRight = big,     // topStart
            bottomRight = small,// bottomStart
            bottomLeft = big,   // bottomEnd
        )
    }
}
