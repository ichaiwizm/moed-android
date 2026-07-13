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
import androidx.glance.layout.Spacer
import androidx.glance.layout.height
import androidx.glance.text.Text
import com.moed.app.data.Localization
import com.moed.app.data.SettingsStore
import kotlinx.coroutines.flow.first

/**
 * Widget **Omer du jour** (CONTRACTS §5.6). Accent `sage` (olive) — le seul de la vue. Le numéro du
 * jour est le chiffre-plomb héros ; hors période (49 jours), affiche « — ». Zéro réseau
 * ([WidgetData.load]).
 */
class OmerWidget : GlanceAppWidget() {
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val data = WidgetData.load(context)
        val lang = Localization.resolve(SettingsStore(context).flow.first().language)
        provideContent {
            GlanceTheme { OmerContent(data, lang) }
        }
    }
}

@Composable
private fun OmerContent(data: WidgetSnapshot, lang: String) {
    Column(
        modifier = paperBlock(),
        verticalAlignment = Alignment.Vertical.CenterVertically,
        horizontalAlignment = Alignment.Horizontal.CenterHorizontally,
    ) {
        Text(text = Localization.string("widget.omer.title", lang), style = WidgetType.title)
        Spacer(GlanceModifier.height(6.dp))
        val heroText = if (data.omerDay > 0) data.omerDay.toString() else "—"
        Text(text = heroText, style = WidgetType.hero(WidgetColors.sage, size = 52))
        Text(text = data.omerLabel, style = WidgetType.subCentered)
    }
}

/** Receiver déclaré au manifest pour le widget Omer. */
class OmerWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = OmerWidget()
}
