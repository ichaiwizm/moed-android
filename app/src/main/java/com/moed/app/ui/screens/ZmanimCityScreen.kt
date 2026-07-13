package com.moed.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.moed.app.data.AppSettings
import com.moed.app.data.Localization
import com.moed.app.data.TzeitMethod
import com.moed.app.engine.ZmanimEngine
import com.moed.app.ui.MoedViewModel
import com.moed.app.ui.components.Disclaimer
import com.moed.app.ui.components.InkRule
import com.moed.app.ui.components.ShabbatCard
import com.moed.app.ui.components.ZmanRegister
import com.moed.app.ui.theme.LocalMoedColors
import com.moed.app.ui.theme.rememberBodyFamily
import com.moed.app.ui.theme.rememberDisplayFamily
import java.time.LocalDate
import java.time.ZoneId

@Composable
fun ZmanimCityScreen(vm: MoedViewModel, citySlug: String) {
    val settings by vm.settings.collectAsState()
    val activeCity by vm.activeCity.collectAsState()
    val lang by vm.lang.collectAsState()
    val now by vm.now.collectAsState()

    val city = remember(citySlug, vm.cities) { vm.cities.firstOrNull { it.slug == citySlug } ?: activeCity }
    val engine = remember { ZmanimEngine() }
    val today = LocalDate.now(ZoneId.of(city.tz))

    val zmanim = remember(city, today, settings) { engine.zmanim(city, today, settings) }
    val shabbat = remember(city, today, settings) { engine.shabbat(city, today, settings) }

    val nearby = remember(city, vm.cities) {
        vm.cities.asSequence()
            .filter { it.slug != city.slug }
            .sortedBy { haversineKm(city, it) }
            .take(6)
            .toList()
    }

    val colors = LocalMoedColors.current
    val display = rememberDisplayFamily(lang)
    val body = rememberBodyFamily(lang)
    val scroll = rememberScrollState()

    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(scroll)
            .padding(horizontal = 20.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(
            text = Localization.string("zmanim.pageTitle", lang, city.name(lang)),
            style = MaterialTheme.typography.headlineSmall,
            fontFamily = display,
            fontWeight = FontWeight.Black,
            color = MaterialTheme.colorScheme.onBackground,
        )
        Text(
            text = city.countryNames[lang] ?: city.countryNames["en"] ?: city.country,
            style = MaterialTheme.typography.bodyMedium,
            fontFamily = body,
            color = colors.inkMute,
        )

        ShabbatCard(info = shabbat, now = now, lang = lang, modifier = Modifier.fillMaxWidth())

        ZmanRegister(zmanim = zmanim, lang = lang, modifier = Modifier.fillMaxWidth())

        // Disclaimer halakhique obligatoire + shita affichée.
        Disclaimer(shita = shitaLabel(settings, lang), lang = lang)

        if (nearby.isNotEmpty()) {
            InkRule()
            Text(
                text = Localization.string("zmanim.nearby", lang),
                style = MaterialTheme.typography.labelMedium,
                fontFamily = body,
                color = colors.inkMute,
            )
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                nearby.forEach { c ->
                    SuggestionChip(
                        onClick = { vm.setCity(c.slug) },
                        label = { Text(c.name(lang)) },
                    )
                }
            }
        }
        Spacer(Modifier.height(24.dp))
    }
}

/** Libellé de la shita active (tzeit degrés vs minutes) pour le disclaimer. */
internal fun shitaLabel(settings: AppSettings, lang: String): String = when (settings.tzeitMethod) {
    TzeitMethod.DEGREES -> Localization.string("settings.tzeitDegrees", lang)
    TzeitMethod.MINUTES -> Localization.string("settings.tzeitMinutes", lang)
}
