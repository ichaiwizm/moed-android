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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.outlined.MenuBook
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.moed.app.data.CalendarEvent
import com.moed.app.data.City
import com.moed.app.data.DayCategory
import com.moed.app.data.Localization
import com.moed.app.engine.JewishInfo
import com.moed.app.engine.ZmanimEngine
import com.moed.app.ui.MoedViewModel
import com.moed.app.ui.components.InkRule
import com.moed.app.ui.components.PressCard
import com.moed.app.ui.components.ShabbatCard
import com.moed.app.ui.components.StampBadge
import com.moed.app.ui.components.ZmanRegister
import com.moed.app.ui.theme.LocalMoedColors
import com.moed.app.ui.theme.rememberBodyFamily
import com.moed.app.ui.theme.rememberDisplayFamily
import java.time.LocalDate
import java.time.ZoneId

@Composable
fun TodayScreen(vm: MoedViewModel, onAllZmanim: (String) -> Unit) {
    val settings by vm.settings.collectAsState()
    val city by vm.activeCity.collectAsState()
    val lang by vm.lang.collectAsState()
    val now by vm.now.collectAsState()

    val jewish = remember { JewishInfo() }
    val zmanimEngine = remember { ZmanimEngine() }

    val today = LocalDate.now(ZoneId.of(city.tz))
    val il = regionIsrael(settings, city)

    val hebrewDate = remember(today, il, lang) { jewish.hebrewDate(today, afterSunset = false, lang = lang) }
    val events = remember(today, il, lang) { jewish.dayEvents(today, il, lang) }
    val omerDay = remember(today) { jewish.omerDay(today) }
    val parasha = remember(today, il, lang) { jewish.parasha(today, il, sefarade = false, lang = lang) }
    val daf = remember(today, lang) { jewish.dafYomi(today, lang) }
    val homeZmanim = remember(city, today, settings) { zmanimEngine.homeZmanim(city, today, settings) }
    val shabbat = remember(city, today, settings) { zmanimEngine.shabbat(city, today, settings) }

    val badges = buildList {
        addAll(events)
        if (omerDay > 0) add(CalendarEvent(Localization.string("home.omerDay", lang, omerDay), DayCategory.OMER))
    }

    val scroll = rememberScrollState()
    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(scroll)
            .padding(horizontal = 20.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        RevealOnAppear(index = 0) {
            DateHeader(
                hebrewDate = hebrewDate,
                gregorian = formatGregorian(today, lang),
                city = city,
                badges = badges,
                lang = lang,
            )
        }
        RevealOnAppear(index = 1) {
            ShabbatCard(info = shabbat, now = now, lang = lang, modifier = Modifier.fillMaxWidth())
        }
        RevealOnAppear(index = 2) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                ZmanRegister(zmanim = homeZmanim, lang = lang, modifier = Modifier.fillMaxWidth())
                OutlinedButton(
                    onClick = { onAllZmanim(city.slug) },
                    shape = CircleShape,
                    modifier = Modifier.align(Alignment.End),
                ) {
                    Text(Localization.string("home.allZmanim", lang))
                }
            }
        }
        RevealOnAppear(index = 3) {
            ParashaDafCard(
                parasha = parasha.first,
                haftara = parasha.second,
                daf = daf,
                lang = lang,
                modifier = Modifier.fillMaxWidth(),
            )
        }
        Spacer(Modifier.height(24.dp))
    }
}

/** En-tête date : jour hébraïque héros (display), date grégorienne muette, ville, rangée de tampons. */
@Composable
fun DateHeader(
    hebrewDate: String,
    gregorian: String,
    city: City,
    badges: List<CalendarEvent>,
    lang: String,
    modifier: Modifier = Modifier,
) {
    val colors = LocalMoedColors.current
    val display = rememberDisplayFamily(lang)
    val body = rememberBodyFamily(lang)
    Column(modifier, verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            text = gregorian,
            style = MaterialTheme.typography.labelLarge,
            fontFamily = body,
            color = colors.inkMute,
        )
        Text(
            text = hebrewDate,
            style = MaterialTheme.typography.displaySmall,
            fontFamily = display,
            fontWeight = FontWeight.Black,
            color = MaterialTheme.colorScheme.onBackground,
        )
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                Icons.Filled.LocationOn,
                contentDescription = Localization.string("common.city", lang),
                tint = colors.twilight,
                modifier = Modifier.size(18.dp),
            )
            Spacer(Modifier.width(6.dp))
            Text(
                text = city.name(lang),
                style = MaterialTheme.typography.titleMedium,
                fontFamily = body,
                color = colors.twilight,
            )
        }
        if (badges.isNotEmpty()) {
            Spacer(Modifier.height(4.dp))
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                badges.forEach { StampBadge(label = it.label, category = it.category) }
            }
        }
    }
}

/** Carte parasha de la semaine + Daf Yomi (carte medium). */
@Composable
fun ParashaDafCard(
    parasha: String,
    haftara: String,
    daf: String,
    lang: String,
    modifier: Modifier = Modifier,
) {
    val colors = LocalMoedColors.current
    val display = rememberDisplayFamily(lang)
    val body = rememberBodyFamily(lang)
    PressCard(modifier = modifier) {
        Text(
            text = Localization.string("home.parasha", lang),
            style = MaterialTheme.typography.labelMedium,
            fontFamily = body,
            color = colors.inkMute,
        )
        Spacer(Modifier.height(2.dp))
        Text(
            text = parasha,
            style = MaterialTheme.typography.titleLarge,
            fontFamily = display,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface,
        )
        if (haftara.isNotBlank()) {
            Text(
                text = haftara,
                style = MaterialTheme.typography.bodyMedium,
                fontFamily = body,
                fontStyle = FontStyle.Italic,
                color = colors.inkSmoke,
            )
        }
        if (daf.isNotBlank()) {
            Spacer(Modifier.height(12.dp))
            InkRule()
            Spacer(Modifier.height(12.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Icon(
                    Icons.Outlined.MenuBook,
                    contentDescription = null,
                    tint = colors.inkMute,
                    modifier = Modifier.size(18.dp),
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = Localization.string("home.dafYomi", lang),
                    style = MaterialTheme.typography.labelMedium,
                    fontFamily = body,
                    color = colors.inkMute,
                    modifier = Modifier.weight(1f),
                )
                Text(
                    text = daf,
                    style = MaterialTheme.typography.titleMedium,
                    fontFamily = body,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.End,
                )
            }
        }
    }
}
