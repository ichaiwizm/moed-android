package com.moed.app.widget

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.provideContent
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.text.Text
import com.moed.app.data.Localization
import com.moed.app.data.SettingsStore
import kotlinx.coroutines.flow.first

/**
 * Widget **Zmanim du jour** (CONTRACTS §5.6). Registre à filets d'imprimeur : chaque ligne = libellé
 * de zman en encre + **heure tabulaire alignée en colonne** (monospace, esprit « bloc de plomb »,
 * accent `ner`). Ville en tête. Zman null → « — ». Zéro réseau ([WidgetData.load]).
 */
class ZmanimWidget : GlanceAppWidget() {
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val data = WidgetData.load(context)
        val lang = Localization.resolve(SettingsStore(context).flow.first().language)
        provideContent {
            GlanceTheme { ZmanimContent(data, lang) }
        }
    }
}

@Composable
private fun ZmanimContent(data: WidgetSnapshot, lang: String) {
    Column(modifier = paperBlock()) {
        // Tête de registre : ville + date hébraïque.
        Row(modifier = GlanceModifier.fillMaxWidth()) {
            Text(text = data.cityName, style = WidgetType.label, modifier = GlanceModifier.defaultWeight())
            Text(text = data.hebrewDate, style = WidgetType.title)
        }
        Spacer(GlanceModifier.height(6.dp))
        InkRule()
        Spacer(GlanceModifier.height(6.dp))

        // Registre des zmanim clés du jour.
        data.zmanim.forEachIndexed { index, line ->
            if (index > 0) Spacer(GlanceModifier.height(6.dp))
            ZmanRow(line)
        }
    }
}

@Composable
private fun ZmanRow(line: WidgetSnapshot.ZmanLine) {
    Row(modifier = GlanceModifier.fillMaxWidth()) {
        Text(text = line.label, style = WidgetType.label, modifier = GlanceModifier.defaultWeight())
        Text(text = line.time, style = WidgetType.time(WidgetColors.ner))
    }
}

/** Receiver déclaré au manifest pour le widget Zmanim. */
class ZmanimWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = ZmanimWidget()
}
