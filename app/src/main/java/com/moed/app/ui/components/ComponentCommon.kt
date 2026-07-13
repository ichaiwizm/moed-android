package com.moed.app.ui.components

import android.provider.Settings
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import com.moed.app.data.DayCategory
import com.moed.app.ui.theme.LocalMoedColors
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

/*
 * Helpers internes partagés par les composants signature (§4.6). Aucun accès aux stores/moteur :
 * seuls les modèles de `data/model` transitent (DayCategory, Zman, City, ShabbatInfo, Confidence).
 */

/** Paire de couleurs (premier plan / lavis) associée à une catégorie de jour — mapping figé §4.1. */
internal data class CategorySwatch(val fg: Color, val bg: Color)

/**
 * Correspondance catégorie → token (badges/puces) : fête = gold, jeûne = rose, Roch Hodech = twilight,
 * Omer = sage, autre = inkMute (CONTRACTS §4.1). Chol Hamoed suit la fête (or).
 */
@Composable
internal fun categorySwatch(category: DayCategory): CategorySwatch {
    val m = LocalMoedColors.current
    return when (category) {
        DayCategory.HOLIDAY, DayCategory.CHOL_HAMOED -> CategorySwatch(m.gold, m.goldWash)
        DayCategory.FAST -> CategorySwatch(m.rose, m.roseWash)
        DayCategory.ROSH_CHODESH -> CategorySwatch(m.twilight, m.twilightWash)
        DayCategory.OMER -> CategorySwatch(m.sage, m.sageWash)
        DayCategory.NONE -> CategorySwatch(m.inkMute, MaterialTheme.colorScheme.surfaceContainer)
    }
}

/** `Locale` de formatage pour une langue Moed (he/fr/en). */
internal fun moedLocale(lang: String): Locale = when (lang) {
    "he" -> Locale("he", "IL")
    "fr" -> Locale.FRENCH
    else -> Locale.ENGLISH
}

/** L'hébreu est RTL ; français et anglais LTR (DESIGN §6). */
internal fun isRtlLang(lang: String): Boolean = lang == "he"

private val HourFormatter: DateTimeFormatter =
    DateTimeFormatter.ofPattern("HH:mm", Locale.ROOT) // Locale.ROOT → chiffres latins, jamais miroités

/** Heure `HH:mm` en chiffres latins, ou « — » si l'instant est absent (latitude extrême, §Zman null). */
internal fun formatHour(time: ZonedDateTime?): String =
    time?.format(HourFormatter) ?: "—"

/**
 * Reste avant [target] rendu « 3 h 12 » / « 3h 12m » / « בעוד 3 ש 12 ד » selon la langue.
 * Renvoie une chaîne « à l'heure » si l'échéance est passée. Précision minute (le compte à rebours se
 * met à jour à la seconde côté appelant, sans animation — DESIGN §5).
 */
internal fun formatCountdown(remainingSeconds: Long, lang: String): String {
    if (remainingSeconds <= 0) return when (lang) {
        "he" -> "עכשיו"
        "fr" -> "maintenant"
        else -> "now"
    }
    val totalMinutes = remainingSeconds / 60
    val h = totalMinutes / 60
    val m = totalMinutes % 60
    return when (lang) {
        "he" -> if (h > 0) "בעוד $h ש׳ $m ד׳" else "בעוד $m ד׳"
        "fr" -> if (h > 0) "dans %d h %02d".format(h, m) else "dans %d min".format(m)
        else -> if (h > 0) "in %dh %02dm".format(h, m) else "in %d min".format(m)
    }
}

/**
 * Humanise une clé de zman (`sofZmanShmaGRA` → « Sof Zman Shma GRA ») pour un affichage neutre en
 * langue. Les libellés pleinement traduits proviennent de `Localization.zmanLabel` côté écran ; le
 * designsystem — qui ne dépend d'aucune couche data — offre ce repli lisible à partir du seul modèle.
 */
internal fun humanizeZmanKey(key: String): String {
    val acronyms = setOf("GRA", "MGA")
    val sb = StringBuilder()
    key.forEachIndexed { i, c ->
        if (i > 0 && c.isUpperCase() && !key[i - 1].isUpperCase()) sb.append(' ')
        if (i > 0 && c.isDigit() && !key[i - 1].isDigit()) sb.append(' ')
        sb.append(c)
    }
    return sb.toString()
        .split(' ')
        .filter { it.isNotBlank() }
        .joinToString(" ") { w ->
            val upper = w.uppercase(Locale.ROOT)
            if (upper in acronyms) upper else w.replaceFirstChar { it.titlecase(Locale.ROOT) }
        }
}

/**
 * Vrai si l'utilisateur a désactivé les animations système (`ANIMATOR_DURATION_SCALE == 0`).
 * Les composants doivent alors apparaître instantanément, valeurs inchangées (DESIGN §5, CONTRACTS §4.5).
 */
@Composable
internal fun rememberReduceMotion(): Boolean {
    val context = LocalContext.current
    return remember {
        Settings.Global.getFloat(
            context.contentResolver,
            Settings.Global.ANIMATOR_DURATION_SCALE,
            1f,
        ) == 0f
    }
}
