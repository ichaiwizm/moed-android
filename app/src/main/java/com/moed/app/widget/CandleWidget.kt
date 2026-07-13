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
import androidx.glance.layout.Alignment
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.width
import androidx.glance.text.Text
import com.moed.app.data.Localization
import com.moed.app.data.SettingsStore
import kotlinx.coroutines.flow.first

/**
 * Widget **Allumage** (CONTRACTS §5.6 : « allumage : heure + countdown Chabbat »).
 *
 * DA « Le Louah Imprimé » : fond `paperRaised` opaque, **un seul** accent `ner` (la flamme) porté par
 * l'heure d'allumage en chiffre-plomb héros ; date hébraïque + ville en encre, havdala et compte à
 * rebours en registre dessous. Zéro réseau ([WidgetData.load]).
 */
class CandleWidget : GlanceAppWidget() {
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val data = WidgetData.load(context)
        val lang = Localization.resolve(SettingsStore(context).flow.first().language)
        provideContent {
            GlanceTheme { CandleContent(data, lang) }
        }
    }
}

@Composable
private fun CandleContent(data: WidgetSnapshot, lang: String) {
    Column(
        modifier = paperBlock(),
        verticalAlignment = Alignment.Vertical.CenterVertically,
    ) {
        // Sur-titre estampé : « Prochain Chabbat ».
        Text(text = Localization.string("widget.candle.title", lang), style = WidgetType.title)
        Text(text = data.parasha, style = WidgetType.emphasis)
        Spacer(GlanceModifier.height(6.dp))

        // Chiffre-plomb héros : l'heure d'allumage, accent ner.
        Text(text = data.candleTime, style = WidgetType.hero(WidgetColors.ner, size = 38))
        Text(text = data.candleCountdown, style = WidgetType.sub)

        Spacer(GlanceModifier.height(10.dp))
        InkRule()
        Spacer(GlanceModifier.height(8.dp))

        // Registre bas : havdala + ville / date hébraïque.
        Row(modifier = GlanceModifier.fillMaxWidth()) {
            Text(
                text = Localization.string("widget.candle.havdala", lang),
                style = WidgetType.title,
                modifier = GlanceModifier.defaultWeight(),
            )
            Text(text = data.havdalaTime, style = WidgetType.time(WidgetColors.twilight))
        }
        Spacer(GlanceModifier.height(4.dp))
        Row(modifier = GlanceModifier.fillMaxWidth()) {
            Text(text = data.cityName, style = WidgetType.label, modifier = GlanceModifier.defaultWeight())
            Spacer(GlanceModifier.width(8.dp))
            Text(text = data.hebrewDate, style = WidgetType.sub)
        }
    }
}

/** Receiver déclaré au manifest pour le widget Allumage. */
class CandleWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = CandleWidget()
}
