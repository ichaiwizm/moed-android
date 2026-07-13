package com.moed.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.moed.app.data.CalendarDay
import com.moed.app.data.DayCategory
import com.moed.app.data.Localization
import com.moed.app.engine.CalendarEngine
import com.moed.app.engine.JewishInfo
import com.moed.app.ui.MoedViewModel
import com.moed.app.ui.components.InkRule
import com.moed.app.ui.theme.LocalMoedColors
import com.moed.app.ui.theme.rememberBodyFamily
import com.moed.app.ui.theme.rememberDisplayFamily
import java.time.LocalDate
import java.time.format.TextStyle as JavaTextStyle

@Composable
fun CalendarScreen(vm: MoedViewModel) {
    val settings by vm.settings.collectAsState()
    val city by vm.activeCity.collectAsState()
    val lang by vm.lang.collectAsState()

    val engine = remember { CalendarEngine(JewishInfo()) }
    val il = regionIsrael(settings, city)

    var anchorEpoch by rememberSaveable { mutableStateOf(LocalDate.now().withDayOfMonth(1).toEpochDay()) }
    val anchor = LocalDate.ofEpochDay(anchorEpoch)

    val days = remember(anchorEpoch, il, lang) { engine.month(anchor, il, lang) }
    val today = LocalDate.now()

    val display = rememberDisplayFamily(lang)
    val locale = formatLocale(lang)
    val title = anchor.month.getDisplayName(JavaTextStyle.FULL, locale)
        .replaceFirstChar { if (it.isLowerCase()) it.titlecase(locale) else it.toString() } + " " + anchor.year

    Column(Modifier.fillMaxSize().padding(horizontal = 20.dp)) {
        Row(
            Modifier.fillMaxWidth().padding(vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = { anchorEpoch = anchor.minusMonths(1).toEpochDay() }) {
                Icon(
                    Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                    contentDescription = Localization.string("calendar.prevMonth", lang),
                )
            }
            Text(
                text = title,
                style = MaterialTheme.typography.headlineSmall,
                fontFamily = display,
                fontWeight = FontWeight.Black,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.weight(1f),
            )
            IconButton(onClick = { anchorEpoch = anchor.plusMonths(1).toEpochDay() }) {
                Icon(
                    Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = Localization.string("calendar.nextMonth", lang),
                )
            }
        }
        InkRule(strong = true)
        LazyColumn(Modifier.fillMaxSize()) {
            items(days, key = { it.gregorian.toEpochDay() }) { day ->
                CalendarDayRow(day = day, lang = lang, isToday = day.gregorian == today)
                InkRule()
            }
            item { Spacer(Modifier.height(24.dp)) }
        }
    }
}

/** Ligne de jour du registre calendrier : numéro grég + jour héb + puces d'événements colorées. */
@Composable
fun CalendarDayRow(
    day: CalendarDay,
    lang: String,
    isToday: Boolean = false,
    modifier: Modifier = Modifier,
) {
    val colors = LocalMoedColors.current
    val body = rememberBodyFamily(lang)
    val display = rememberDisplayFamily(lang)
    val locale = formatLocale(lang)
    val weekday = day.gregorian.dayOfWeek.getDisplayName(JavaTextStyle.SHORT, locale)

    Row(
        modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(if (isToday) colors.nerStrong else MoedTransparent),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = day.gregorian.dayOfMonth.toString(),
                style = MaterialTheme.typography.titleMedium,
                fontFamily = display,
                fontWeight = FontWeight.Bold,
                color = if (isToday) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onBackground,
            )
        }
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(
                text = weekday.replaceFirstChar { it.titlecase(locale) },
                style = MaterialTheme.typography.labelSmall,
                fontFamily = body,
                color = colors.inkMute,
            )
            Text(
                text = day.hebrewLabel,
                style = MaterialTheme.typography.bodyLarge,
                fontFamily = body,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onBackground,
            )
            if (day.events.isNotEmpty()) {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.padding(top = 4.dp),
                ) {
                    day.events.forEach { ev ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            EventDot(category = ev.category)
                            Spacer(Modifier.width(4.dp))
                            Text(
                                text = ev.label,
                                style = MaterialTheme.typography.labelMedium,
                                fontFamily = body,
                                color = colors.inkSmoke,
                            )
                        }
                    }
                }
            } else if (day.omerDay > 0) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 4.dp)) {
                    EventDot(category = DayCategory.OMER)
                    Spacer(Modifier.width(4.dp))
                    Text(
                        text = Localization.string("calendar.omerBadge", lang, day.omerDay),
                        style = MaterialTheme.typography.labelMedium,
                        fontFamily = body,
                        color = colors.inkSmoke,
                    )
                }
            }
        }
    }
}

/** Puce pleine colorée par catégorie (mapping figé). */
@Composable
fun EventDot(category: DayCategory, modifier: Modifier = Modifier) {
    val colors = LocalMoedColors.current
    Box(
        modifier
            .size(8.dp)
            .clip(CircleShape)
            .background(categoryColor(category, colors)),
    )
}

private val MoedTransparent = androidx.compose.ui.graphics.Color.Transparent
