# Moed Android — CONTRACTS (vocabulaire partagé figé)

**Rôle de ce document** : c'est le **contrat d'interface** que TOUS les modules générés du natif
Android **Moed** doivent respecter **à la lettre**. Noms de packages, de types, de champs, de tokens
de design, de signatures de fonctions : rien ne s'invente, rien ne se renomme. Si un module a besoin
d'un type/token/getter absent d'ici, on **complète d'abord ce fichier**, puis on code. En cas de
divergence entre un module et ce document, **ce document gagne**.

Cible : **Jetpack Compose · Material 3 Expressive (`androidx.compose.material3` ≥ 1.4) · Kotlin 2.0.21
· minSdk 26 · targetSdk/compileSdk 34 · AGP 8.7.2 · JDK 17 · module `app` unique**.
Moteur : **KosherJava `com.kosherjava:zmanim:2.5.0`**, 100 % on-device, offline, déterministe.
DA : **« Le Louah Imprimé »** (opaque, imprimé, chiffres-plomb, shape-morphing M3 Expressive).

**Package racine** : `com.moed.app` · **applicationId** = `com.moed.app`. Toute classe ci-dessous vit
sous ce package ; le sous-package est indiqué entre parenthèses.

**Lois non négociables** (rappel — s'appliquent à tous les modules) :
1. **Zéro réseau au runtime pour le calcul** (offline complet ; `net/` = push opt-in seulement).
2. **Parité de calcul** validée vs référence kosher-zmanim/Hebcal (0 tol. dates, ≤ 2 min zmanim).
3. **Light mode par défaut** ; **RTL première classe**, hébreu primaire ; 3 langues (he/fr/en) à parité.
4. Gratuit, sans compte, sans pub, **aucune donnée personnelle ne quitte l'appareil**.

**Conventions Kotlin** :
- Modèles = `data class` / `enum class` au niveau du sous-package (pas de préfixe). Dates =
  `java.time.LocalDate` / `java.time.ZonedDateTime` (desugaring `desugar_jdk_libs` activé).
- **Pas de Hilt/Koin, pas de Room.** Un seul `MoedViewModel : AndroidViewModel`, source de vérité UI.
  Persistance = **DataStore Preferences**. Assets JSON parsés par **kotlinx-serialization**.
- Tokens hors-ColorScheme exposés via `data class MoedColors` + `LocalMoedColors`.
- Zman **null** si le soleil n'atteint pas l'angle (jamais NaN) → UI affiche « — ».

---

## 1. MODÈLES (data classes) — noms et champs figés (`data/Models.kt`)

```kotlin
// ---- Ville (aligné src/data/cities.ts, asset cities.json) ----
data class City(
    val slug: String,                       // kebab-case unique
    val names: Map<String, String>,         // {"fr","en","he"}
    val country: String,                    // ISO2
    val countryNames: Map<String, String>,
    val lat: Double, val lng: Double,
    val tz: String,                         // IANA (pilote DST)
    val elevation: Double = 0.0,
    val israel: Boolean,
    val candleMinutes: Int,                 // 18/20/30/40
    val community: Int? = null,
) { fun name(lang: String): String = names[lang] ?: names["en"] ?: slug }

// ---- Tsadik (aligné src/data/tsadikim.ts, asset tzadikim.json) ----
enum class TzadikCategory { TANNA, RISHON, ACHARON, HASSID, HABAD, SEFARADE }
enum class Confidence { HIGH, MEDIUM, TRADITIONAL }
data class HilulaDate(val day: Int, val monthEn: String)     // day=18, monthEn="Iyar"
data class Kever(val place: String, val country: String)
data class Tzadik(
    val slug: String,
    val names: Map<String, String>,
    val epithet: Map<String, String>? = null,
    val category: TzadikCategory,
    val hilula: HilulaDate,
    val hilulaNote: Map<String, String>? = null,
    val yearGregorian: Int? = null,
    val kever: Kever? = null,
    val works: List<String> = emptyList(),
    val bio: Map<String, String>,
    val confidence: Confidence,
    val sources: List<String> = emptyList(),
    val motif: String,                      // "motif-tanna" | "kever" | "hilula-candle"…
)

// ---- Carnet familial (persisté) ----
enum class PersonType { YAHRZEIT, BIRTHDAY }
data class PersonRecord(
    val id: String,                         // UUID
    val type: PersonType,
    val name: String,
    val date: java.time.LocalDate,          // date grégorienne saisie
    val afterSunset: Boolean = false,       // ancre la date héb (+1 jour)
)

// ---- Réglages (persistés, défauts = NATIVE_SPEC §7.2) ----
enum class TzeitMethod { DEGREES, MINUTES }        // 8.5° vs offset fixe
enum class Region { AUTO, IL, DIASPORA }
enum class CandleMode { AUTO, M18, M20, M30, M40 } // AUTO = minhag de la ville
data class NotifPrefs(
    val shabbat: Boolean = false, val omer: Boolean = false,
    val hilula: Boolean = false, val yahrzeit: Boolean = false,
)
data class AppSettings(
    val citySlug: String = "paris",
    val candleMode: CandleMode = CandleMode.AUTO,
    val tzeitMethod: TzeitMethod = TzeitMethod.DEGREES,
    val region: Region = Region.AUTO,
    val language: String = "system",        // system→he/fr/en (fallback en)
    val notif: NotifPrefs = NotifPrefs(),
)

// ---- Sorties du moteur ----
data class Zman(val key: String, val shita: String, val time: java.time.ZonedDateTime?) // null = latitude extrême
data class ShabbatInfo(
    val candleLighting: java.time.ZonedDateTime,
    val havdala: java.time.ZonedDateTime,
    val parasha: String, val haftara: String,
)
enum class DayCategory { NONE, HOLIDAY, FAST, ROSH_CHODESH, OMER, CHOL_HAMOED }
data class CalendarEvent(val label: String, val category: DayCategory)
data class CalendarDay(
    val gregorian: java.time.LocalDate,
    val hebrewLabel: String,
    val omerDay: Int,                        // 0 si hors période
    val events: List<CalendarEvent>,
)
data class Occurrence(                        // yahrzeit/anniversaire
    val gregorian: java.time.LocalDate,
    val hebrewLabel: String,
)
```

**Notes de migration** : le `Models.kt` de référence (`mvp-moed/android`) utilise
`cityName`/`gYear,gMonth,gDay` → on **migre** vers `citySlug` + `LocalDate`. `SettingsStore`/
`PersonalStore` gèrent la migration des clés (ou reset propre, app non publiée).

---

## 2. MOTEUR (calcul offline, déterministe) — API figée (`engine/`)

Tout `object`/`class` du moteur est **pur** (aucun I/O, aucun réseau). Entrées = `City` +
`LocalDate`/heure + `AppSettings` ; sorties = types §1. TZ par IANA (jamais offset device).

```kotlin
// engine/ZmanimCatalog.kt — les 16 clés/shitot figées (NATIVE_SPEC §3.3)
object ZmanimCatalog {
    data class ZmanDef(val key: String, val shitaLabelKey: String)  // getter KosherJava associé en interne
    val home: List<String>          // sous-ensemble HOME_ZMANIM (écran Aujourd'hui)
    val all: List<ZmanDef>          // 16 zmanim, ordre spec
}

// engine/ZmanimEngine.kt — ComplexZmanimCalendar + GeoLocation
class ZmanimEngine {
    fun zmanim(city: City, date: LocalDate, settings: AppSettings): List<Zman>      // les 16 (time=null si extrême)
    fun homeZmanim(city: City, date: LocalDate, settings: AppSettings): List<Zman>
    fun shabbat(city: City, date: LocalDate, settings: AppSettings): ShabbatInfo    // allumage = sunset − candleMinutes ; havdala 8.5°/42/50/72
}

// engine/JewishInfo.kt — JewishCalendar, HebrewDateFormatter, YomiCalculator
class JewishInfo {
    fun hebrewDate(date: LocalDate, afterSunset: Boolean, lang: String): String
    fun dayEvents(date: LocalDate, il: Boolean, lang: String): List<CalendarEvent>  // fêtes/jeûnes/RH/chol hamoed
    fun omerDay(date: LocalDate): Int                                               // 0 hors période
    fun parasha(date: LocalDate, il: Boolean, sefarade: Boolean, lang: String): Pair<String, String> // (parasha, haftara)
    fun dafYomi(date: LocalDate, lang: String): String
    fun nextOccurrences(person: PersonRecord, from: LocalDate, count: Int): List<Occurrence> // yahrzeit/anniv (Cheshvan-30/Kislev-30/Adar)
    fun hebrewMonthDay(date: LocalDate): HilulaDate                                 // (day, monthEn) du jour hébreu → match hiloula (== Tzadik.hilula)
    fun nextHilula(hilula: HilulaDate, from: LocalDate): Occurrence?                // prochaine occurrence grégorienne d'une hiloula
}

// engine/CalendarEngine.kt — équivalent getCalendarRange(start,end,{il,geo,locale})
class CalendarEngine(jewishInfo: JewishInfo) {
    fun range(start: LocalDate, end: LocalDate, il: Boolean, lang: String): List<CalendarDay>
    fun month(anchor: LocalDate, il: Boolean, lang: String): List<CalendarDay>
}

// engine/HebrewConverter.kt — grég ⇄ héb bidirectionnel
class HebrewConverter {
    fun gregToHebrew(date: LocalDate, afterSunset: Boolean, lang: String): String
    fun hebrewToGreg(hebYear: Int, hebMonth: Int, hebDay: Int): LocalDate?          // null si invalide
    fun hebrewMonths(hebYear: Int, lang: String): List<String>                     // gère Adar I/II
}

// engine/Gematria.kt — mispar hechrachi (UI = v2)
object Gematria {
    data class GematriaResult(val total: Int, val letters: List<Pair<Char, Int>>)
    fun compute(word: String): GematriaResult
}
```

**Région** : `AppSettings.region` → booléen `il` (AUTO = `city.israel`). **Sépharade/Ashkénaze** pour
la haftara : paramètre `sefarade` (défaut Ashkénaze sauf réglage). **FR** : les libs ne connaissent
que en/he → tous les libellés FR viennent de `Localization` (§3.3), passé via `lang`.

**Validation obligatoire** (bloquant avant release) : matrice 10 villes × 2 ans × 4 types comparée à
kosher-zmanim/Hebcal ; livrer `validation-report.json`. Disclaimer + shita affichés sur ZmanimCity.

---

## 3. DONNÉES, PERSISTANCE, i18n — signatures figées (`data/`)

### 3.1 Assets (`data/AssetData.kt`)
```kotlin
object AssetData {
    fun cities(context: Context): List<City>       // charge assets/cities.json (cache mémoire)
    fun tzadikim(context: Context): List<Tzadik>   // charge assets/tzadikim.json (cache mémoire)
    fun city(slug: String): City?                  // getCity(slug)
}
```

### 3.2 Stores DataStore (`data/SettingsStore.kt`, `data/PersonalStore.kt`)
```kotlin
class SettingsStore(context: Context) {
    val flow: Flow<AppSettings>
    suspend fun update(transform: (AppSettings) -> AppSettings)
}
class PersonalStore(context: Context) {            // carnet sérialisé JSON (clé migrée de moed_family)
    val flow: Flow<List<PersonRecord>>
    suspend fun add(person: PersonRecord)
    suspend fun remove(id: String)
}
```

### 3.3 Localisation (`data/Localization.kt`)
```kotlin
object Localization {
    fun monthName(hebMonth: Int, hebYear: Int, lang: String): String
    fun weekday(date: LocalDate, lang: String): String
    fun zmanLabel(key: String, lang: String): String       // labels des 16 zmanim
    fun holidayName(canonicalEn: String, lang: String): String
    fun string(key: String, lang: String, vararg args: Any): String   // ex "home.omerDay {n}", "zmanim.pageTitle {city}"
    fun localeTag(lang: String): String                    // he-IL / fr-FR / en-US
    fun isRtl(lang: String): Boolean                        // he → true
    fun resolve(languageSetting: String): String           // "system" → he/fr/en (fallback en)
}
```

### 3.4 Réseau OPTIONNEL, opt-in (`net/`) — jamais pour le calcul
Feature-flaggé par un toggle « rappels serveur (bêta) » dans Réglages, **désactivé par défaut**.
Backend réutilisé : `pocketbase-moed` (`https://pb-moed.vpsdashboard.space`) + routes
`https://moed.vpsdashboard.space/api/push/{subscribe,unsubscribe}`, collection `push_subscriptions`.
```kotlin
class AuthTokenStore(context: Context) {           // token PB en DataStore
    fun read(): String?; fun save(token: String); fun clear()
}
interface PushApi {                                // Retrofit + kotlinx-serialization ; auth header token
    // auth-with-password → { token, record } ; upsert/delete push_subscriptions ; subscribe/unsubscribe
}
```
Auth par **token** PocketBase (`{ token, record }` sur `auth-with-password`), injecté en header
`Authorization`, refresh via `auth-refresh`. Tout appel **best-effort** (échec silencieux ; les
rappels **locaux** restent la source de vérité). Aucune donnée du carnet ne transite.

---

## 4. DESIGN SYSTEM (Compose) — API figée (`ui/theme/`)

DA « Le Louah Imprimé » : **opaque, zéro flou/translucidité/dégradé vitré**. Aplats pleins, filets
d'encre 2 dp, ombres douces teintées encre, chiffres tabulaires héros, shape-morphing M3 Expressive.

### 4.1 ColorScheme + tokens hors-scheme (`ui/theme/Color.kt`, `ui/theme/MoedColors.kt`)
```kotlin
val MoedLightColorScheme: ColorScheme   // mapping DESIGN §1.3 (primary=ner #B4471F, background=paper #F4ECDD…)
val MoedDarkColorScheme: ColorScheme    // « négatif d'imprimerie » (DESIGN §1.2), chaud, jamais bleu-froid

data class MoedColors(                   // tokens custom hors ColorScheme (DESIGN §8)
    val gold: Color, val goldWash: Color,
    val inkMute: Color, val inkSmoke: Color,
    val outlineStrong: Color, val nerStrong: Color, val nerGlow: Color,
    val twilight: Color, val twilightWash: Color,
    val sage: Color, val sageWash: Color,
    val rose: Color, val roseWash: Color,
    val paperRaised: Color, val paperSunken: Color,
)
val MoedLightExtra: MoedColors; val MoedDarkExtra: MoedColors
val LocalMoedColors: ProvidableCompositionLocal<MoedColors>   // staticCompositionLocalOf { light defaults }
```
Correspondance catégorie → token (badges/puces, figée) : fête=`gold`, jeûne=`rose`,
Roch Hodech=`twilight`, Omer=`sage`, autre=`inkMute`.

### 4.2 Typographie (`ui/theme/Type.kt`)
```kotlin
val Fraunces: FontFamily; val FrankRuhl: FontFamily      // display/headline/title — latin / hébreu
val Instrument: FontFamily; val Assistant: FontFamily    // body/label — latin / hébreu
val MoedTypography: Typography                            // échelle ~1.25, base 16 (DESIGN §2)
@Composable fun rememberDisplayFamily(lang: String): FontFamily  // he → FrankRuhl, sinon Fraunces
@Composable fun rememberBodyFamily(lang: String): FontFamily     // he → Assistant, sinon Instrument
val TabularNums: TextStyle                                // fontFeatureSettings = "tnum" (heures, jamais miroité)
```

### 4.3 Formes (`ui/theme/Shapes.kt`)
```kotlin
val MoedShapes: Shapes                   // xs 8 / s 12 / m 16 / l 24 / xl 32 dp (full = 999 via CircleShape)
fun cartoucheShape(rtl: Boolean): Shape  // asymétrique topStart 24/topEnd 8/bottomEnd 24/bottomStart 8 (miroir si rtl)
```

### 4.4 Thème racine (`ui/theme/Theme.kt`)
```kotlin
@Composable
fun MoedTheme(
    dark: Boolean = isSystemInDarkTheme(),   // défaut app v1 = false sauf préférence explicite
    content: @Composable () -> Unit,
) // MaterialTheme(colorScheme, MoedTypography, MoedShapes, motionScheme = MotionScheme.expressive())
  // + CompositionLocalProvider(LocalMoedColors provides extra)
```

### 4.5 Motion (`ui/theme/Theme.kt` / helpers)
- Base = `MotionScheme.expressive()`. Un seul « moment » orchestré par vue (cascade Aujourd'hui :
  `fade + translationY(8→0)`, overshoot spring `dampingRatio ≈ 0.75`, `stiffness = medium`).
- Apparition cartes = `fade + scale(0.98→1)` ≤ 220 ms. Compte à rebours = MAJ seconde **sans anim**.
- `reduce-motion` respecté (`ANIMATOR_DURATION_SCALE == 0`) → apparition instantanée, valeurs inchangées.

### 4.6 Composants signature réutilisables (`ui/components/`) — signatures figées
```kotlin
@Composable fun StampBadge(label: String, category: DayCategory, modifier: Modifier = Modifier)  // tampon extraSmall
@Composable fun InkRule(strong: Boolean = false, modifier: Modifier = Modifier)                  // filet 2 dp plein
@Composable fun PressCard(onClick: (() -> Unit)? = null, shape: Shape = MoedShapes.medium,
                          modifier: Modifier = Modifier, content: @Composable ColumnScope.() -> Unit)
@Composable fun ZmanRegister(zmanim: List<Zman>, lang: String, modifier: Modifier = Modifier)    // registre filets 2 dp, heures tabulaires colonne, "—" si null
@Composable fun ShabbatCard(info: ShabbatInfo, now: java.time.Instant, lang: String, modifier: Modifier = Modifier) // cartouche asymétrique + countdown live + halo ner
@Composable fun MorphNavBar(current: String, onSelect: (String) -> Unit)                         // surfaceContainer plein, 5 onglets, indicateur pilule vermillon morphant
@Composable fun CitySearch(cities: List<City>, lang: String, onPick: (City) -> Unit, modifier: Modifier = Modifier)
@Composable fun ConfidenceChip(confidence: Confidence)                                            // gold/twilight/inkMute
@Composable fun CountdownText(target: java.time.ZonedDateTime, now: java.time.Instant, lang: String)
@Composable fun Disclaimer(shita: String, lang: String)                                           // obligatoire ZmanimCity
```

---

## 5. STRUCTURE (Application, navigation, état) — figée

### 5.1 Application & Activity (racine)
```kotlin
class MoedApp : Application               // crée les 4 canaux notif au boot
class MainActivity : ComponentActivity    // setContent { MoedTheme(dark = …) { MoedRoot() } }, enableEdgeToEdge
```

### 5.2 État — ViewModel unique (`ui/MoedViewModel.kt`)
```kotlin
class MoedViewModel(app: Application) : AndroidViewModel(app) {
    val settings: StateFlow<AppSettings>           // ← SettingsStore.flow (stateIn Eagerly)
    val personal: StateFlow<List<PersonRecord>>    // ← PersonalStore.flow
    val cities: List<City>                         // AssetData.cities (une fois)
    val tzadikim: List<Tzadik>                     // AssetData.tzadikim (une fois)
    val activeCity: StateFlow<City>                // dérivé de settings.citySlug
    val lang: StateFlow<String>                    // Localization.resolve(settings.language)
    val now: StateFlow<java.time.Instant>          // horloge live 1 s (countdown)

    fun updateSettings(transform: (AppSettings) -> AppSettings)   // persist → rescheduleAll + widget update
    fun addPerson(person: PersonRecord)
    fun removePerson(id: String)
    fun setLanguage(lang: String); fun setCity(slug: String)
}
```
Dérivés (dates/zmanim/countdown) recalculés dans les écrans via `remember(settings, now)` /
`derivedStateOf` (calcul synchrone KosherJava, pas de coroutine). Toute mutation → DataStore **puis**
`NotificationScheduler.rescheduleAll()` + `WidgetUpdater.requestUpdate()`.

### 5.3 Navigation Compose (`ui/MoedRoot.kt`)
```kotlin
object Routes {
    const val TODAY = "today"; const val CALENDAR = "calendar"; const val PERSONAL = "personal"
    const val TZADIKIM = "tsadikim"; const val SETTINGS = "settings"
    const val TZADIK_DETAIL = "tsadikim/{slug}"; const val ZMANIM_CITY = "zmanim/{citySlug}"
    const val CONVERTER = "converter"
    fun tzadik(slug: String) = "tsadikim/$slug"; fun zmanimCity(slug: String) = "zmanim/$slug"
}
@Composable fun MoedRoot()   // Scaffold(bottomBar = MorphNavBar) + NavHost (5 top-level + 3 sous-écrans)
                             // + CompositionLocalProvider(LocalLayoutDirection = if(he) Rtl else Ltr)
```
Chrome commun : `MorphNavBar` (§4.6), 5 onglets `today/calendar/personal/tsadikim/settings`, labels
toujours visibles, indicateur pilule vermillon morphant, haptique `select` au changement.

### 5.4 Écrans (`ui/screens/`) — Composables signature
```kotlin
@Composable fun TodayScreen(vm: MoedViewModel, onAllZmanim: (String) -> Unit)
@Composable fun CalendarScreen(vm: MoedViewModel)
@Composable fun PersonalScreen(vm: MoedViewModel)
@Composable fun TzadikimScreen(vm: MoedViewModel, onOpenTzadik: (String) -> Unit)
@Composable fun TzadikDetailScreen(vm: MoedViewModel, slug: String)
@Composable fun ZmanimCityScreen(vm: MoedViewModel, citySlug: String)
@Composable fun ConverterScreen(vm: MoedViewModel)
@Composable fun SettingsScreen(vm: MoedViewModel)
```
Sous-composants figés : `DateHeader`, `ParashaDafCard`, `CalendarDayRow`, `EventDot`, `MorphFab`,
`AddPersonSheet`, `EmptyFamily` (voir DESIGN §4).

### 5.5 Notifications (`notif/`) — locales, planifiées, opt-in
```kotlin
class NotificationScheduler(context: Context) {   // 4 canaux : moed_shabbat / moed_yahrzeit / moed_omer / moed_holiday
    fun rescheduleAll(settings: AppSettings, personal: List<PersonRecord>, city: City)  // fenêtre glissante, AlarmManager/WorkManager
}
class NotificationReceiver : BroadcastReceiver
class BootReceiver : BroadcastReceiver             // replanifie au reboot (RECEIVE_BOOT_COMPLETED)
```
Permission `POST_NOTIFICATIONS` demandée **à l'opt-in** (API 33+), pas au lancement. Toutes les
échéances déterministes → aucune dépendance push serveur.

### 5.6 Widgets Glance (`widget/`) — ×3, offline
```kotlin
class CandleWidget : GlanceAppWidget        // allumage : heure + countdown Chabbat
class ZmanimWidget : GlanceAppWidget        // zmanim du jour
class OmerWidget : GlanceAppWidget          // Omer du jour
object WidgetData { /* recalcul offline, lit DataStore */ }
class WidgetUpdater { fun requestUpdate() } // WorkManager
```
Glance ne supporte pas les polices custom → **fallback système** en gras + tabular ; garder l'esprit
« bloc de plomb ». Fond `paperRaised` opaque, accent `ner` allumage / `sage` Omer / `twilight` RH.

---

## 6. DÉCOUPAGE EN MODULES (sous-packages de `com.moed.app`)

| Module | Sous-package | Contenu |
|---|---|---|
| `app-shell` | (racine) | `MoedApp`, `MainActivity` |
| `designsystem` | `ui/theme` + `ui/components` | Color/MoedColors, Type, Shapes, Theme, motion, composants signature (§4) |
| `data/model` | `data` (`Models.kt`) | tous les modèles domaine (§1) |
| `data/local` | `data` (`AssetData`,`SettingsStore`,`PersonalStore`,`Localization`) | assets JSON, DataStore, i18n (§3.1-3.3) |
| `engine` | `engine` | JewishInfo, ZmanimEngine, CalendarEngine, HebrewConverter, Gematria, ZmanimCatalog (§2) |
| `net` | `net` | PushApi, AuthTokenStore — **opt-in, jamais pour le calcul** (§3.4) |
| `viewmodel` | `ui` (`MoedViewModel.kt`) | ViewModel unique + horloge live (§5.2) |
| `ui/nav` | `ui` (`MoedRoot.kt`) | `MoedRoot`, `Routes`, MorphNavBar, RTL/locale (§5.3) |
| `ui/screens` | `ui/screens` | les 8 écrans + sous-composants (§5.4) |
| `notif` | `notif` | NotificationScheduler, receivers, BootReceiver (§5.5) |
| `widget` | `widget` | 3 GlanceAppWidget + WidgetData + WidgetUpdater (§5.6) |

Dépendances autorisées (sens unique, aucun cycle) : `ui/screens` → `viewmodel`+`designsystem`+
`engine`+`data/*` ; `viewmodel` → `data/*`+`engine`+`notif`+`widget` ; `engine` → `data/model` ;
`notif`/`widget` → `engine`+`data/*` ; `net` → `data/model` (isolé, opt-in) ; `designsystem` ne
dépend d'aucune couche data.
