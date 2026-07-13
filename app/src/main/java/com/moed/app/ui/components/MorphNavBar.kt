package com.moed.app.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoStories
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Today
import androidx.compose.material.icons.outlined.AutoStories
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.People
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Today
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.moed.app.ui.theme.LocalMoedColors
import com.moed.app.ui.theme.MoedFullShape

private data class NavTab(
    val key: String,
    val label: String,
    val filled: ImageVector,
    val outlined: ImageVector,
)

/** Les 5 onglets top-level, ordre figé (CONTRACTS §5.3). Libellés français (langue par défaut du code). */
private val NAV_TABS = listOf(
    NavTab("today", "Aujourd'hui", Icons.Filled.Today, Icons.Outlined.Today),
    NavTab("calendar", "Calendrier", Icons.Filled.CalendarMonth, Icons.Outlined.CalendarMonth),
    NavTab("personal", "Carnet", Icons.Filled.People, Icons.Outlined.People),
    NavTab("tsadikim", "Tsadikim", Icons.Filled.AutoStories, Icons.Outlined.AutoStories),
    NavTab("settings", "Réglages", Icons.Filled.Settings, Icons.Outlined.Settings),
)

/**
 * Bottom navigation Material 3 Expressive de Moed (§4.6, DESIGN §4) — conteneur `surfaceContainer`
 * **plein** (jamais translucide), 5 onglets, **indicateur pilule vermillon morphant** (`nerWash` +
 * icône/label `nerStrong`), labels toujours visibles, icône pleine si actif / outline sinon. Haptique
 * `select` (retour sec) au changement. Morph et scale de la pilule neutralisés sous reduce-motion.
 *
 * @param current clé de l'onglet actif (`today/calendar/personal/tsadikim/settings`).
 * @param onSelect appelé avec la clé choisie.
 */
@Composable
fun MorphNavBar(
    current: String,
    onSelect: (String) -> Unit,
) {
    val moed = LocalMoedColors.current
    val haptics = LocalHapticFeedback.current
    val reduceMotion = rememberReduceMotion()

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceContainer)
            .windowInsetsPadding(WindowInsets.navigationBars)
            .padding(horizontal = 8.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        NAV_TABS.forEach { tab ->
            val selected = tab.key == current
            val interaction = remember { MutableInteractionSource() }

            val pillColor by animateColorAsState(
                targetValue = if (selected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent,
                label = "navPill",
            )
            val pillScale by animateFloatAsState(
                targetValue = if (selected && !reduceMotion) 1f else if (selected) 1f else 0.92f,
                label = "navPillScale",
            )
            val contentColor = if (selected) moed.nerStrong else moed.inkMute

            Column(
                modifier = Modifier
                    .weight(1f)
                    .clip(MoedFullShape)
                    .clickable(
                        interactionSource = interaction,
                        indication = null,
                    ) {
                        if (!selected) {
                            haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                            onSelect(tab.key)
                        }
                    }
                    .padding(vertical = 6.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                Icon(
                    imageVector = if (selected) tab.filled else tab.outlined,
                    contentDescription = tab.label,
                    tint = contentColor,
                    modifier = Modifier
                        .scale(pillScale)
                        .clip(MoedFullShape)
                        .background(pillColor)
                        .padding(horizontal = 18.dp, vertical = 4.dp)
                        .size(24.dp),
                )
                Text(
                    text = tab.label,
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
                    ),
                    color = contentColor,
                )
            }
        }
    }
}
