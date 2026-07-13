package com.moed.app.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.dp
import com.moed.app.ui.theme.LocalMoedColors
import com.moed.app.ui.theme.MoedShapes

/**
 * Carte imprimée réutilisable (§4.6) — la brique de mise en page de la DA.
 *
 * Opaque `paperRaised`, **filet d'encre 2 dp** (jamais de bordure fine grise), ombre douce **teintée
 * encre** (jamais noir pur, jamais floue « verre »). Si [onClick] est fourni, la carte s'« estampe » au
 * press (léger `scale 0.98`) avec retour haptique sec ; l'animation est neutralisée sous reduce-motion.
 *
 * @param content contenu, en `ColumnScope` (empilement vertical, marge interne 16 dp).
 */
@Composable
fun PressCard(
    onClick: (() -> Unit)? = null,
    shape: Shape = MoedShapes.medium,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    val moed = LocalMoedColors.current
    val inkTint = MaterialTheme.colorScheme.scrim
    val reduceMotion = rememberReduceMotion()
    val haptics = LocalHapticFeedback.current

    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed && !reduceMotion) 0.98f else 1f,
        label = "pressCardScale",
    )

    val base = modifier
        .let { if (onClick != null && !reduceMotion) it.scale(scale) else it }
        .shadow(
            elevation = 2.dp,
            shape = shape,
            ambientColor = inkTint,
            spotColor = inkTint,
        )
        .clip(shape)
        .background(moed.paperRaised)
        .border(2.dp, MaterialTheme.colorScheme.outline, shape)

    val clickable = if (onClick != null) {
        base.clickable(interactionSource = interaction, indication = null) {
            haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
            onClick()
        }
    } else base

    Column(
        modifier = clickable.padding(16.dp),
        content = content,
    )
}
