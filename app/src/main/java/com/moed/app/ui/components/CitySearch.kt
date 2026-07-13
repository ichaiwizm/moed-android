package com.moed.app.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.moed.app.data.City
import com.moed.app.ui.theme.LocalMoedColors
import com.moed.app.ui.theme.MoedShapes
import com.moed.app.ui.theme.rememberBodyFamily
import java.util.Locale

/**
 * Recherche de ville (§4.6, DESIGN §4 « Réglages ») sur le dataset embarqué. Champ filtrant + registre
 * de résultats (cartes `medium`), filet d'encre entre chaque. La correspondance porte sur tous les noms
 * localisés de la ville (accent/casse ignorés au mieux) et sur le nom de pays de la langue active.
 *
 * @param onPick appelé avec la ville choisie (définit `AppSettings.citySlug` côté écran).
 */
@Composable
fun CitySearch(
    cities: List<City>,
    lang: String,
    onPick: (City) -> Unit,
    modifier: Modifier = Modifier,
) {
    val body = rememberBodyFamily(lang)
    val moed = LocalMoedColors.current
    var query by remember { mutableStateOf("") }

    val results = remember(query, cities, lang) {
        val q = query.trim().lowercase(Locale.ROOT)
        val base = if (q.isEmpty()) cities else cities.filter { city ->
            city.names.values.any { it.lowercase(Locale.ROOT).contains(q) } ||
                (city.countryNames[lang] ?: city.country).lowercase(Locale.ROOT).contains(q)
        }
        base.sortedBy { it.name(lang).lowercase(Locale.ROOT) }.take(60)
    }

    Column(modifier = modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = query,
            onValueChange = { query = it },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            textStyle = MaterialTheme.typography.bodyLarge.copy(fontFamily = body),
            leadingIcon = {
                Icon(Icons.Outlined.Search, contentDescription = null, tint = moed.inkMute)
            },
            placeholder = {
                Text(
                    text = when (lang) { "he" -> "חיפוש עיר"; "fr" -> "Rechercher une ville"; else -> "Search a city" },
                    style = MaterialTheme.typography.bodyLarge.copy(fontFamily = body),
                    color = moed.inkMute,
                )
            },
            shape = MoedShapes.medium,
            keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(imeAction = ImeAction.Search),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = moed.paperRaised,
                unfocusedContainerColor = moed.paperRaised,
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline,
            ),
        )

        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 360.dp)
                .padding(top = 12.dp),
        ) {
            items(results, key = { it.slug }) { city ->
                PressCard(
                    onClick = { onPick(city) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                ) {
                    androidx.compose.foundation.layout.Row(
                        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
                        horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(10.dp),
                    ) {
                        Icon(
                            Icons.Outlined.LocationOn,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                        )
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = city.name(lang),
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurface,
                            )
                            Text(
                                text = city.countryNames[lang] ?: city.country,
                                style = MaterialTheme.typography.bodySmall.copy(fontFamily = body),
                                color = moed.inkMute,
                            )
                        }
                    }
                }
            }
        }
    }
}
