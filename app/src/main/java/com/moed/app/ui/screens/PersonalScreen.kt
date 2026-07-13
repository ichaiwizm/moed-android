package com.moed.app.ui.screens

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.LocalFireDepartment
import androidx.compose.material3.DatePicker
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.moed.app.data.Localization
import com.moed.app.data.PersonRecord
import com.moed.app.data.PersonType
import com.moed.app.engine.JewishInfo
import com.moed.app.ui.MoedViewModel
import com.moed.app.ui.components.InkRule
import com.moed.app.ui.components.PressCard
import com.moed.app.ui.theme.LocalMoedColors
import com.moed.app.ui.theme.TabularNums
import com.moed.app.ui.theme.rememberBodyFamily
import com.moed.app.ui.theme.rememberDisplayFamily
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import java.util.UUID

@Composable
fun PersonalScreen(vm: MoedViewModel) {
    val personal by vm.personal.collectAsState()
    val lang by vm.lang.collectAsState()

    val jewish = remember { JewishInfo() }
    val today = LocalDate.now()
    var showSheet by remember { mutableStateOf(false) }

    val sorted = remember(personal) {
        personal.sortedBy {
            jewish.nextOccurrences(it, today, 1).firstOrNull()?.gregorian ?: LocalDate.MAX
        }
    }

    Box(Modifier.fillMaxSize()) {
        if (personal.isEmpty()) {
            EmptyFamily(lang = lang, modifier = Modifier.fillMaxSize())
        } else {
            LazyColumn(
                Modifier.fillMaxSize().padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(vertical = 12.dp),
            ) {
                items(sorted, key = { it.id }) { person ->
                    val next = remember(person, lang) { jewish.nextOccurrences(person, today, 1).firstOrNull() }
                    PersonCard(
                        person = person,
                        nextHebrew = next?.hebrewLabel.orEmpty(),
                        nextGregorian = next?.gregorian?.let { formatGregorianShort(it, lang) }.orEmpty(),
                        lang = lang,
                        onRemove = { vm.removePerson(person.id) },
                    )
                }
                item { Spacer(Modifier.height(88.dp)) }
            }
        }

        MorphFab(
            onClick = { showSheet = true },
            label = Localization.string("common.add", lang),
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(24.dp),
        )
    }

    if (showSheet) {
        AddPersonSheet(
            lang = lang,
            onDismiss = { showSheet = false },
            onSave = { vm.addPerson(it); showSheet = false },
        )
    }
}

@Composable
private fun PersonCard(
    person: PersonRecord,
    nextHebrew: String,
    nextGregorian: String,
    lang: String,
    onRemove: () -> Unit,
) {
    val colors = LocalMoedColors.current
    val display = rememberDisplayFamily(lang)
    val body = rememberBodyFamily(lang)
    PressCard {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = person.name,
                        style = MaterialTheme.typography.titleLarge,
                        fontFamily = display,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Spacer(Modifier.width(8.dp))
                    TypeChip(type = person.type, lang = lang)
                }
                Spacer(Modifier.height(6.dp))
                Text(
                    text = Localization.string("personal.nextOccurrence", lang),
                    style = MaterialTheme.typography.labelSmall,
                    fontFamily = body,
                    color = colors.inkMute,
                )
                Text(
                    text = listOf(nextHebrew, nextGregorian).filter { it.isNotBlank() }.joinToString(" · "),
                    style = MaterialTheme.typography.titleMedium.merge(TabularNums),
                    fontFamily = body,
                    fontWeight = FontWeight.SemiBold,
                    color = colors.nerStrong,
                )
            }
            IconButton(onClick = onRemove) {
                Icon(
                    Icons.Outlined.Delete,
                    contentDescription = Localization.string("common.delete", lang),
                    tint = colors.inkMute,
                )
            }
        }
    }
}

@Composable
private fun TypeChip(type: PersonType, lang: String) {
    val colors = LocalMoedColors.current
    val body = rememberBodyFamily(lang)
    // yahrzeit = twilight, anniversaire = gold (DESIGN §4).
    val (fg, bg, key) = if (type == PersonType.YAHRZEIT) {
        Triple(colors.twilight, colors.twilightWash, "personal.addYahrzeit")
    } else {
        Triple(colors.gold, colors.goldWash, "personal.addBirthday")
    }
    Box(
        Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(bg)
            .padding(horizontal = 8.dp, vertical = 3.dp),
    ) {
        Text(
            text = Localization.string(key, lang),
            style = MaterialTheme.typography.labelSmall,
            fontFamily = body,
            fontWeight = FontWeight.SemiBold,
            color = fg,
        )
    }
}

/** FAB « carnet » vermillon morphant : rond → pilule étirée au press (DESIGN §3). */
@Composable
fun MorphFab(
    onClick: () -> Unit,
    label: String,
    modifier: Modifier = Modifier,
) {
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val corner by animateDpAsState(if (pressed) 16.dp else 28.dp, label = "fabCorner")
    val haptic = LocalHapticFeedback.current
    Surface(
        onClick = {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            onClick()
        },
        shape = RoundedCornerShape(corner),
        color = MaterialTheme.colorScheme.primary,
        contentColor = MaterialTheme.colorScheme.onPrimary,
        interactionSource = interaction,
        modifier = modifier.heightIn(min = 56.dp),
    ) {
        Row(
            Modifier.padding(horizontal = 20.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(Icons.Filled.Add, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text(label, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
        }
    }
}

/** État vide illustré (aucun proche enregistré). */
@Composable
fun EmptyFamily(lang: String, modifier: Modifier = Modifier) {
    val colors = LocalMoedColors.current
    val display = rememberDisplayFamily(lang)
    val body = rememberBodyFamily(lang)
    Column(
        modifier.padding(40.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Box(
            Modifier
                .size(96.dp)
                .clip(androidx.compose.foundation.shape.CircleShape)
                .background(colors.nerGlow.copy(alpha = 0.18f)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                Icons.Outlined.LocalFireDepartment,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(48.dp),
            )
        }
        Spacer(Modifier.height(20.dp))
        Text(
            text = Localization.string("personal.emptyTitle", lang),
            style = MaterialTheme.typography.headlineSmall,
            fontFamily = display,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = Localization.string("personal.emptyBody", lang),
            style = MaterialTheme.typography.bodyMedium,
            fontFamily = body,
            color = colors.inkSmoke,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
        )
    }
}

/** Bottom sheet opaque d'ajout : segmented type · nom · date picker M3 · toggle « après le coucher ». */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddPersonSheet(
    lang: String,
    onDismiss: () -> Unit,
    onSave: (PersonRecord) -> Unit,
) {
    val colors = LocalMoedColors.current
    val body = rememberBodyFamily(lang)
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val haptic = LocalHapticFeedback.current

    var type by remember { mutableStateOf(PersonType.YAHRZEIT) }
    var name by remember { mutableStateOf("") }
    var afterSunset by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = Instant.now().toEpochMilli(),
    )

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = colors.paperRaised,
    ) {
        Column(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                text = Localization.string("personal.addTitle", lang),
                style = MaterialTheme.typography.titleLarge,
                fontFamily = rememberDisplayFamily(lang),
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface,
            )
            SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth()) {
                SegmentedButton(
                    selected = type == PersonType.YAHRZEIT,
                    onClick = { type = PersonType.YAHRZEIT },
                    shape = SegmentedButtonDefaults.itemShape(0, 2),
                ) { Text(Localization.string("personal.addYahrzeit", lang)) }
                SegmentedButton(
                    selected = type == PersonType.BIRTHDAY,
                    onClick = { type = PersonType.BIRTHDAY },
                    shape = SegmentedButtonDefaults.itemShape(1, 2),
                ) { Text(Localization.string("personal.addBirthday", lang)) }
            }
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text(Localization.string("personal.name", lang)) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
            DatePicker(
                state = datePickerState,
                title = null,
                headline = null,
                showModeToggle = true,
                modifier = Modifier.heightIn(max = 420.dp),
            )
            Row(
                Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = Localization.string("personal.afterSunset", lang),
                    style = MaterialTheme.typography.bodyLarge,
                    fontFamily = body,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f),
                )
                Switch(checked = afterSunset, onCheckedChange = { afterSunset = it })
            }
            InkRule()
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
            ) {
                TextButton(onClick = onDismiss) { Text(Localization.string("common.cancel", lang)) }
                Spacer(Modifier.width(8.dp))
                TextButton(
                    enabled = name.isNotBlank() && datePickerState.selectedDateMillis != null,
                    onClick = {
                        val millis = datePickerState.selectedDateMillis ?: return@TextButton
                        val date = Instant.ofEpochMilli(millis).atZone(ZoneOffset.UTC).toLocalDate()
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onSave(
                            PersonRecord(
                                id = UUID.randomUUID().toString(),
                                type = type,
                                name = name.trim(),
                                date = date,
                                afterSunset = afterSunset,
                            ),
                        )
                    },
                ) {
                    Text(
                        Localization.string("common.save", lang),
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
            }
        }
    }
}
