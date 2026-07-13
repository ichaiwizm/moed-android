package com.moed.app.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.moed.app.data.CandleMode
import com.moed.app.data.Localization
import com.moed.app.data.NotifPrefs
import com.moed.app.data.Region
import com.moed.app.data.TzeitMethod
import com.moed.app.ui.MoedViewModel
import com.moed.app.ui.components.CitySearch
import com.moed.app.ui.components.PressCard
import com.moed.app.ui.theme.LocalMoedColors
import com.moed.app.ui.theme.rememberBodyFamily
import com.moed.app.ui.theme.rememberDisplayFamily

@Composable
fun SettingsScreen(vm: MoedViewModel) {
    val settings by vm.settings.collectAsState()
    val lang by vm.lang.collectAsState()
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current

    // Toggle notif en attente d'octroi de permission (API 33+).
    var pendingNotif by remember { mutableStateOf<((NotifPrefs) -> NotifPrefs)?>(null) }
    val permLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { granted ->
        val transform = pendingNotif
        pendingNotif = null
        if (granted && transform != null) {
            vm.updateSettings { it.copy(notif = transform(it.notif)) }
        }
    }

    fun toggleNotif(current: Boolean, apply: (NotifPrefs) -> NotifPrefs) {
        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        val needsPermission = !current &&
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) !=
            PackageManager.PERMISSION_GRANTED
        if (needsPermission) {
            pendingNotif = apply
            permLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        } else {
            vm.updateSettings { it.copy(notif = apply(it.notif)) }
        }
    }

    val scroll = rememberScrollState()
    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(scroll)
            .padding(horizontal = 20.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        Text(
            text = Localization.string("settings.title", lang),
            style = MaterialTheme.typography.headlineSmall,
            fontFamily = rememberDisplayFamily(lang),
            fontWeight = FontWeight.Black,
            color = MaterialTheme.colorScheme.onBackground,
        )

        // --- Langue ---
        SettingsSection(Localization.string("settings.language", lang), lang) {
            SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth()) {
                val langs = listOf("he" to "עברית", "fr" to "Français", "en" to "English")
                langs.forEachIndexed { i, (code, label) ->
                    SegmentedButton(
                        selected = settings.language == code,
                        onClick = { vm.setLanguage(code) },
                        shape = SegmentedButtonDefaults.itemShape(i, langs.size),
                    ) { Text(label) }
                }
            }
        }

        // --- Localisation ---
        SettingsSection(Localization.string("settings.location", lang), lang) {
            CitySearch(
                cities = vm.cities,
                lang = lang,
                onPick = { vm.setCity(it.slug) },
                modifier = Modifier.fillMaxWidth(),
            )
        }

        // --- Minhag & shita ---
        SettingsSection(Localization.string("settings.minhag", lang), lang) {
            LabelRow(Localization.string("settings.candleMinutes", lang), lang)
            SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth()) {
                val modes = listOf(
                    CandleMode.AUTO to Localization.string("settings.candleAuto", lang),
                    CandleMode.M18 to "18",
                    CandleMode.M20 to "20",
                    CandleMode.M30 to "30",
                    CandleMode.M40 to "40",
                )
                modes.forEachIndexed { i, (mode, label) ->
                    SegmentedButton(
                        selected = settings.candleMode == mode,
                        onClick = { vm.updateSettings { it.copy(candleMode = mode) } },
                        shape = SegmentedButtonDefaults.itemShape(i, modes.size),
                    ) { Text(label) }
                }
            }

            Spacer(Modifier.height(12.dp))
            LabelRow(Localization.string("settings.tzeitShita", lang), lang)
            SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth()) {
                val methods = listOf(
                    TzeitMethod.DEGREES to Localization.string("settings.tzeitDegrees", lang),
                    TzeitMethod.MINUTES to Localization.string("settings.tzeitMinutes", lang),
                )
                methods.forEachIndexed { i, (method, label) ->
                    SegmentedButton(
                        selected = settings.tzeitMethod == method,
                        onClick = { vm.updateSettings { it.copy(tzeitMethod = method) } },
                        shape = SegmentedButtonDefaults.itemShape(i, methods.size),
                    ) { Text(label) }
                }
            }

            Spacer(Modifier.height(12.dp))
            LabelRow(Localization.string("settings.israelDiaspora", lang), lang)
            SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth()) {
                val regions = listOf(
                    Region.AUTO to Localization.string("settings.auto", lang),
                    Region.IL to Localization.string("common.israel", lang),
                    Region.DIASPORA to Localization.string("common.diaspora", lang),
                )
                regions.forEachIndexed { i, (region, label) ->
                    SegmentedButton(
                        selected = settings.region == region,
                        onClick = { vm.updateSettings { it.copy(region = region) } },
                        shape = SegmentedButtonDefaults.itemShape(i, regions.size),
                    ) { Text(label) }
                }
            }
        }

        // --- Notifications ---
        SettingsSection(Localization.string("settings.notifications", lang), lang) {
            Text(
                text = Localization.string("settings.notifDesc", lang),
                style = MaterialTheme.typography.bodyMedium,
                fontFamily = rememberBodyFamily(lang),
                color = LocalMoedColors.current.inkMute,
            )
            Spacer(Modifier.height(8.dp))
            NotifToggle(Localization.string("settings.notifShabbat", lang), settings.notif.shabbat, lang) { on ->
                toggleNotif(on) { it.copy(shabbat = !on) }
            }
            NotifToggle(Localization.string("settings.notifOmer", lang), settings.notif.omer, lang) { on ->
                toggleNotif(on) { it.copy(omer = !on) }
            }
            NotifToggle(Localization.string("settings.notifHilula", lang), settings.notif.hilula, lang) { on ->
                toggleNotif(on) { it.copy(hilula = !on) }
            }
            NotifToggle(Localization.string("settings.notifYahrzeit", lang), settings.notif.yahrzeit, lang) { on ->
                toggleNotif(on) { it.copy(yahrzeit = !on) }
            }
        }
        Spacer(Modifier.height(24.dp))
    }
}

@Composable
private fun SettingsSection(title: String, lang: String, content: @Composable () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontFamily = rememberDisplayFamily(lang),
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onBackground,
        )
        PressCard { content() }
    }
}

@Composable
private fun LabelRow(text: String, lang: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelLarge,
        fontFamily = rememberBodyFamily(lang),
        color = LocalMoedColors.current.inkSmoke,
    )
    Spacer(Modifier.height(6.dp))
}

/** @param onToggle reçoit l'état COURANT (avant bascule). */
@Composable
private fun NotifToggle(label: String, checked: Boolean, lang: String, onToggle: (current: Boolean) -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            fontFamily = rememberBodyFamily(lang),
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f),
        )
        Switch(checked = checked, onCheckedChange = { onToggle(checked) })
    }
}
