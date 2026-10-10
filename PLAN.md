# PLAN D'IMPLÉMENTATION — MOED ANDROID (natif)

> Cible : **Jetpack Compose + Material 3 (Expressive)**, `minSdk 26`, `targetSdk 34`, `compileSdk 34`, Kotlin 2.0.21, AGP 8.7.2, JDK 17.
> Peau : DA **« Le Louah Imprimé »** (`DESIGN.md`) — opaque, imprimé, chiffres-plomb, shape-morphing M3 Expressive.
> Moteur : **KosherJava `com.kosherjava:zmanim:2.5.0`**, 100 % on-device, offline, déterministe (`NATIVE_SPEC.md §3`).
> Modèle de portage : `/root/apps/mvp-moed/android/app/src/main/java/com/moed/app/` (impl. Kotlin existante, à reprendre puis re-skinner/étendre).
> Règles d'or non violables : zéro réseau au runtime pour le calcul · parité de calcul validée · light mode par défaut · RTL première classe · gratuit/sans compte/sans collecte.

---

## 0. Décisions structurantes (avant tout code)

| Sujet | Décision | Raison |
|---|---|---|
| Package / applicationId | `com.moed.app` (Play) | déjà signé dans `/root/.appstore/android-secrets.env`, cohérent avec le workflow CI |
| Langue de build min | `minSdk 26` (Android 8.0) | exigence ; couvre Glance 1.1, notifications channels, adaptive icons, themed icons (26+) |
| Architecture UI | **MVVM** un seul `MoedViewModel` (AndroidViewModel) + `StateFlow` | l'app est stateless côté serveur ; un VM suffit, pas de DI lourde |
| Persistance | **DataStore Preferences** (prefs + carnet sérialisé JSON) | déjà en place, offline, simple |
| Navigation | **Navigation Compose** — 5 destinations top-level + 3 sous-écrans | parité web/iOS, deep-links widgets |
| Réseau | **Optionnel, opt-in uniquement** (push serveur). Jamais pour le calcul. | produit offline-first ; voir §4 |
| Thème | `MoedTheme` custom (ColorScheme M3 + `MoedColors` via CompositionLocal) | tokens hors-ColorScheme (`gold`, `inkMute`, …) cf. DESIGN §1.3/§8 |
| DA | reprendre l'impl existante puis **remplacer entièrement le thème** (Color/Type/Shapes) par « Le Louah Imprimé » | l'existant est en DA « aube douce » web ; Android doit diverger |

---

## 1. Arborescence de fichiers proposée

```
/root/apps/moed/android/
├── NATIVE_SPEC.md                 (existe)
├── DESIGN.md                      (existe)
├── PLAN.md                        (ce fichier)
├── settings.gradle.kts
├── build.gradle.kts               (racine, versions plugins)
├── gradle.properties              (AndroidX, JVM args, non-transitive R)
├── gradle/
│   ├── libs.versions.toml         (version catalog — centralise deps)
│   └── wrapper/gradle-wrapper.properties  (Gradle 8.9+)
├── gradlew / gradlew.bat
├── keystore.properties            (gitignore ; injecté par la CI)
├── .gitignore                     (.android-sdk/, build/, local.properties, keystore.properties, *.jks)
├── .github/workflows/
│   └── android-release.yml        (copié depuis /root/.appstore/android-template, PACKAGE_NAME=com.moed.app)
└── app/
    ├── build.gradle.kts
    ├── proguard-rules.pro
    └── src/main/
        ├── AndroidManifest.xml
        ├── assets/
        │   ├── cities.json         (~189 villes — exporté depuis src/data/cities.ts)
        │   └── tzadikim.json       (~32 fiches — exporté depuis src/data/tsadikim.ts)
        ├── res/
        │   ├── font/               (fraunces, frank_ruhl_libre, instrument_sans, assistant — variables)
        │   ├── drawable/           (illustrations estampe/duotone + ic_* vecteurs)
        │   ├── mipmap-anydpi-v26/  (adaptive + themed icon : croissant→flamme)
        │   ├── values/             (strings.xml [en défaut], colors.xml, themes.xml)
        │   ├── values-fr/strings.xml
        │   ├── values-he/strings.xml
        │   └── xml/                (widget infos ×3, data_extraction_rules, backup_rules, locales_config)
        └── java/com/moed/app/
            ├── MoedApp.kt                    (Application : crée channels notif au boot)
            ├── MainActivity.kt               (ComponentActivity + setContent { MoedTheme { MoedRoot() } })
            ├── engine/
            │   ├── JewishInfo.kt             (dates héb, fêtes, parasha, omer, daf, next-occurrence)
            │   ├── ZmanimEngine.kt           (zmanim multi-shitot, allumage, havdala)
            │   ├── CalendarEngine.kt          (getCalendarRange : mois d'événements)   [NOUVEAU]
            │   ├── HebrewConverter.kt         (grég⇄héb pour le convertisseur)          [NOUVEAU]
            │   ├── Gematria.kt                (mispar hechrachi + décompo)              [NOUVEAU, v2 UI]
            │   └── ZmanimCatalog.kt           (les 16 clés/shitot du tableau NATIVE §3.3)[NOUVEAU]
            ├── data/
            │   ├── Models.kt                  (City, Tzadik, PersonRecord, AppSettings, CalendarDay…)
            │   ├── AssetData.kt               (charge cities.json/tzadikim.json, cache mémoire)
            │   ├── SettingsStore.kt           (DataStore prefs)
            │   ├── PersonalStore.kt           (DataStore carnet, JSON)
            │   └── Localization.kt            (tables he/fr/en : mois, jours, zmanim, fêtes)
            ├── net/                            [NOUVEAU — optionnel]
            │   ├── PushApi.kt                 (Retrofit/Ktor client pocketbase-moed, opt-in)
            │   └── AuthTokenStore.kt          (token PB en DataStore chiffré)
            ├── ui/
            │   ├── MoedViewModel.kt
            │   ├── MoedRoot.kt                (Scaffold + NavHost + bottom nav morphante)
            │   ├── theme/
            │   │   ├── Color.kt               (MoedLight/DarkColorScheme + MoedLight/DarkExtra)
            │   │   ├── Type.kt                (MoedTypography, sélection latin/hébreu par locale)
            │   │   ├── Shapes.kt              (MoedShapes + cartouche asymétrique)         [NOUVEAU]
            │   │   ├── MoedColors.kt          (data class + LocalMoedColors)               [NOUVEAU]
            │   │   └── Theme.kt               (MoedTheme, MotionScheme.expressive())
            │   ├── components/
            │   │   ├── Common.kt              (StampBadge, InkRule, PressCard…)
            │   │   ├── ZmanRegister.kt        (registre à filets 2dp, heures tabulaires)  [NOUVEAU]
            │   │   ├── ShabbatCard.kt         (cartouche asymétrique + countdown live)     [NOUVEAU]
            │   │   ├── MorphNavBar.kt         (indicateur pilule vermillon morphant)       [NOUVEAU]
            │   │   └── CitySearch.kt          (recherche ville)                            [NOUVEAU]
            │   └── screens/
            │       ├── TodayScreen.kt
            │       ├── CalendarScreen.kt
            │       ├── PersonalScreen.kt
            │       ├── TzadikimScreen.kt
            │       ├── TzadikDetailScreen.kt   [NOUVEAU]
            │       ├── ZmanimCityScreen.kt     [NOUVEAU — sous-vue "tous les zmanim"]
            │       ├── ConverterScreen.kt      [NOUVEAU]
            │       └── SettingsScreen.kt
            ├── notif/
            │   ├── NotificationScheduler.kt   (4 canaux, planif AlarmManager/WorkManager)
            │   ├── NotificationReceiver.kt
            │   └── BootReceiver.kt            (replanifie au reboot)
            └── widget/
                ├── Widgets.kt                 (3 GlanceAppWidget : candle/zmanim/omer)
                ├── WidgetData.kt              (recalcul offline, lit DataStore)
                └── WidgetUpdater.kt           (WorkManager refresh)
```

---

## 2. Modèles de données (data classes)

Le `Models.kt` de référence est **volontairement minimaliste** ; on l'étend pour couvrir les types trilingues de `NATIVE_SPEC §4`.

```kotlin
// ---- Ville (aligné src/data/cities.ts) ----
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
) { fun name(lang: String) = names[lang] ?: names["en"] ?: slug }

// ---- Tsadik (aligné src/data/tsadikim.ts) ----
enum class TzadikCategory { TANNA, RISHON, ACHARON, HASSID, HABAD, SEFARADE }
enum class Confidence { HIGH, MEDIUM, TRADITIONAL }
data class HilulaDate(val day: Int, val monthEn: String)   // ex. day=18, monthEn="Iyar"
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
    val afterSunset: Boolean = false,       // ancre correctement la date héb
)

// ---- Réglages (persistés, défauts = NATIVE_SPEC §7.2) ----
enum class TzeitMethod { DEGREES, MINUTES }       // 8.5° vs offset fixe
enum class Region { AUTO, IL, DIASPORA }
enum class CandleMode { AUTO, M18, M20, M30, M40 } // AUTO = minhag de la ville
data class NotifPrefs(
    val shabbat: Boolean = false, val omer: Boolean = false,
    val hilula: Boolean = false, val yahrzeit: Boolean = false,
)
data class AppSettings(
    val citySlug: String = "paris",         // défaut spec = paris
    val candleMode: CandleMode = CandleMode.AUTO,
    val tzeitMethod: TzeitMethod = TzeitMethod.DEGREES,
    val region: Region = Region.AUTO,
    val language: String = "system",        // system→he/fr/en (fallback en)
    val notif: NotifPrefs = NotifPrefs(),
)

// ---- Sorties du moteur ----
data class Zman(val key: String, val shita: String, val time: java.time.ZonedDateTime?) // null si latitude extrême
data class ShabbatInfo(
    val candleLighting: java.time.ZonedDateTime,
    val havdala: java.time.ZonedDateTime,
    val parasha: String, val haftara: String,
)
enum class DayCategory { NONE, HOLIDAY, FAST, ROSH_CHODESH, OMER, CHOL_HAMOED }
data class CalendarDay(
    val gregorian: java.time.LocalDate,
    val hebrewLabel: String,
    val omerDay: Int,                        // 0 si hors période
    val events: List<CalendarEvent>,
)
data class CalendarEvent(val label: String, val category: DayCategory)
data class Occurrence(                       // yahrzeit/anniversaire
    val gregorian: java.time.LocalDate,
    val hebrewLabel: String,
)
```

> Note : le `Models.kt` de référence utilise `cityName`/`gYear,gMonth,gDay`. On migre vers `citySlug` + `LocalDate` (plus robuste, aligné web). `SettingsStore`/`PersonalStore` doivent gérer une **migration** des clés existantes si un build antérieur est installé (sinon reset propre, app non publiée → OK).

---

## 3. Couche moteur (calcul, offline)

| Module | Source KosherJava | Rôle |
|---|---|---|
| `JewishInfo` | `JewishCalendar`, `HebrewDateFormatter`, `YomiCalculator` | date héb, fêtes/jeûnes/RH, parasha, Omer, Daf, next-occurrence yahrzeit/anniv |
| `ZmanimEngine` | `ComplexZmanimCalendar`, `GeoLocation` | 16 zmanim (§3.3), allumage `sunset−candleMinutes`, havdala 8.5°/42/50/72 min |
| `CalendarEngine` | boucle sur `JewishCalendar` jour par jour | équivalent `getCalendarRange(start,end,{il,geo,locale})` |
| `HebrewConverter` | `JewishCalendar(y,m,d)` ↔ `gregorianCalendar` | convertisseur bidirectionnel + validation d'entrée |
| `Gematria` | table lettres → valeurs | mispar hechrachi (UI = v2) |

Règles à respecter (NATIVE_SPEC §3.4) : TZ par **IANA** (jamais offset device) ; zman `null` si le soleil n'atteint pas l'angle (jamais NaN → UI affiche « — ») ; flag `afterSunset` avance la date héb d'un jour ; élévation seulement si `>0` ; `il` bascule fêtes/parasha/haftara. **FR fourni par `Localization.kt`** (hebcal/KosherJava ne connaissent que en/he).

**Validation obligatoire** (§3.6, barre halakhique) : script `tools/validate.py` (ou test instrumenté) rejouant la matrice **10 villes × 2 ans × 4 types** et comparant aux sorties de `kosher-zmanim` (référence interne, déjà PASS) : dates exactes (0 tol.), zmanim ≤1 min idéal / échec >2 min. Livrer `validation-report.json` avant toute publication. Disclaimer halakhique + shita affichés sur `ZmanimCityScreen`.

---

## 4. Couche réseau (optionnelle, opt-in, jamais pour le calcul)

**Position produit** : le calcul et les datasets sont 100 % embarqués → **aucun réseau requis au runtime**. Une couche réseau minimale n'existe que pour **une** fonction facultative : le **push serveur** (belt-and-suspenders au-dessus des notifications locales), en réutilisant le backend web déjà déployé.

**Backend réutilisé** : `pocketbase-moed` → `https://pb-moed.vpsdashboard.space` (+ routes Next.js `https://moed.vpsdashboard.space/api/push/{subscribe,unsubscribe}`), collection `push_subscriptions` (`endpoint`, `keys`, `city`, `lang`, `prefs`).

| Endpoint | Méthode | Auth | Usage |
|---|---|---|---|
| `/api/collections/_superusers/auth-with-password` *(ou users)* | POST | identity/password → **token** | obtenir un token PocketBase |
| `/api/collections/push_subscriptions/records` | POST/PATCH/DELETE | `Authorization: <token>` | upsert/suppr. l'abonnement de l'appareil |
| `/api/push/subscribe` · `/unsubscribe` | POST | header token | enregistrer/retirer l'appareil côté web push |

**Auth par token** : PocketBase renvoie un JWT dans `{ token, record }` sur `auth-with-password`. Le client (`PushApi.kt`) l'attache en header `Authorization`. Le token est stocké dans `AuthTokenStore.kt` (DataStore) et rafraîchi via `/api/collections/*/auth-refresh` si expiré. **Aucune donnée personnelle du carnet ne transite** — seuls endpoint push + prefs de rappel.

**Implémentation** : `Retrofit + OkHttp + kotlinx-serialization` (ou Ktor client). Intercepteur qui injecte le token, retry unique sur 401 après refresh. Tout appel est **best-effort** : échec réseau = silencieux, les rappels locaux restent la source de vérité. Feature-flaggé par un toggle « rappels serveur (bêta) » dans Réglages, **désactivé par défaut**.

> Par défaut, `net/` n'est jamais sollicité : Moed fonctionne intégralement hors-ligne. On le documente ici parce que c'est le seul point de contact avec le backend existant et pour respecter la demande « endpoints réutilisés + auth par token ».

---

## 5. Gestion d'état

- **`MoedViewModel : AndroidViewModel`** (déjà en place, à étendre) : source unique de vérité UI.
  - `settings: StateFlow<AppSettings>` ← `SettingsStore.flow` (`stateIn`, `SharingStarted.Eagerly`).
  - `personal: StateFlow<List<PersonRecord>>` ← `PersonalStore.flow`.
  - `cities: List<City>` / `tzadikim: List<Tzadik>` chargés une fois depuis les assets (cache mémoire `AssetData`).
  - Dérivés recalculés par `derivedStateOf`/`remember(settings, now)` dans les écrans (dates, zmanim, countdown) — calcul synchrone instantané (KosherJava), pas de coroutine nécessaire.
- **Horloge live** : `TodayScreen` collecte un `flow { while(true){ emit(Instant.now()); delay(1000) } }` pour le compte à rebours d'allumage (mise à jour seconde, chiffre sans animation cf. DESIGN §5).
- **Effets de bord** : toute mutation (`updateSettings`, `addPerson`, `removePerson`) → persistance DataStore **puis** `NotificationScheduler.rescheduleAll()` + `WidgetUpdater.requestUpdate()` (pattern déjà présent).
- **Locale/RTL** : la langue effective (system→he/fr/en) est dérivée de `settings.language` ; `MoedRoot` enveloppe le `NavHost` dans `CompositionLocalProvider(LocalLayoutDirection provides if(he) Rtl else Ltr)` et fixe la locale de formatage (`localeTag` BCP-47 : he-IL/fr-FR/en-US).
- **Navigation state** : `rememberNavController()` ; back-stack standard ; les 3 sous-écrans reçoivent des args (`tsadikSlug`, `citySlug`).
- Pas de Room, pas de Hilt/Koin : périmètre trop petit, l'injection manuelle via `viewModel()` suffit.

---

## 6. Écrans & composables

| Route | Écran | Vues / composables clés |
|---|---|---|
| `today` | **TodayScreen** | `DateHeader` (jour héb Fraunces/FrankRuhl Black + grég `inkMute` + ville + rangée `StampBadge` Omer/fête/RH/jeûne) · **`ShabbatCard`** (cartouche asymétrique `extraLarge`, halo ner, allumage XXL tabulaire, `CountdownText` live, havdala) · **`ZmanRegister`** (HOME_ZMANIM, filets 2dp, heures colonne, « — » si null) · `ParashaDafCard` (`medium`) · états fête/jeûne |
| `calendar` | **CalendarScreen** | En-tête mois+année Fraunces Black (croissant molad estampé), chevrons ◀▶ **miroités RTL** · liste `CalendarDayRow` : date héb + `EventDot` pleins colorés (fête=gold, jeûne=rose, RH=twilight, omer=sage, autre=inkMute) · source `CalendarEngine.range(...)` |
| `personal` | **PersonalScreen** | Cartes `medium` par proche (nom + chip type yahrzeit=twilight/anniv=gold + **prochaine occurrence** grég+héb tabulaire) · **`MorphFab`** vermillon « Ajouter » → **`AddPersonSheet`** (bottom sheet `extraLarge` opaque : segmented type, nom, `DatePicker` M3, toggle « après le coucher ») · swipe-to-delete · `EmptyFamily` illustré · bloc opt-in notif |
| `tsadikim` | **TzadikimScreen** | Section **Hiloula du jour** (cartouche estampé si match sinon état illustré) · Répertoire = grille `medium` à motif par courant (**jamais de portrait**) · nav → détail |
| `tsadikim/{slug}` | **TzadikDetailScreen** | Nom trilingue display · épithète Fraunces italique · catégorie · hiloula (+ note) · prochaine hiloula · kever · œuvres · bio · `ConfidenceChip` (gold/twilight/inkMute) · liens `sources` · note visuel symbolique |
| `zmanim/{citySlug}` | **ZmanimCityScreen** | Allumage Chabbat + **registre complet des 16 zmanim** (label + shita affichée + heure tabulaire) · **`Disclaimer` halakhique obligatoire** · villes proches |
| `converter` | **ConverterScreen** | Segmented grég→héb / héb→grég · grég : `DatePicker` → date héb formatée · héb : sélecteurs jour(1–30)/mois héb/année → date grég (+ gestion invalide) |
| `settings` | **SettingsScreen** | Langue he/fr/en (change locale+RTL) · **`CitySearch`** sur 189 villes → ville active · Minhag (segmented allumage 18/20/30/40, shita degrees/minutes, région auto/il/diaspora) · toggles notif opt-in (demande permission `POST_NOTIFICATIONS` au moment de l'opt-in, pas au lancement) · (option bêta push serveur) |

**Chrome commun** : `MoedRoot` = `Scaffold(bottomBar = MorphNavBar(...))` + `NavHost`. `MorphNavBar` = conteneur `surfaceContainer` plein (jamais translucide), 5 onglets, **indicateur pilule vermillon morphant** (`MaterialShapes`, ex. Pill↔Cookie), labels toujours visibles, icônes filled/outline. Motion `MotionScheme.expressive()` : cascade d'entrée « Aujourd'hui », shape-morph nav, haptique sur estampes, `reduce-motion` respecté.

---

## 7. Dépendances (app/build.gradle.kts + libs.versions.toml)

Reprendre la base existante et **monter Material 3 en ≥ 1.4** pour les API Expressive (`MaterialShapes`, `MotionScheme.expressive()`).

```kotlin
dependencies {
    // Compose BOM (viser 2025.xx qui embarque material3 1.4.x Expressive)
    implementation(platform("androidx.compose:compose-bom:2025.05.01"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")           // ≥1.4 (Expressive)
    implementation("androidx.compose.material:material-icons-extended")
    implementation("androidx.compose.animation:animation-graphics")  // shape-morph / AVD
    debugImplementation("androidx.compose.ui:ui-tooling")

    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.core:core-splashscreen:1.0.1")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.6")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.6")
    implementation("androidx.activity:activity-compose:1.9.3")
    implementation("androidx.navigation:navigation-compose:2.8.3")

    // Persistance locale (offline)
    implementation("androidx.datastore:datastore-preferences:1.1.1")

    // Widgets Glance
    implementation("androidx.glance:glance-appwidget:1.1.1")
    implementation("androidx.glance:glance-material3:1.1.1")

    // Notifications planifiées + refresh widgets
    implementation("androidx.work:work-runtime-ktx:2.9.1")

    // Moteur zmanim / calendrier hébraïque (déterministe, offline)
    implementation("com.kosherjava:zmanim:2.5.0")

    // JSON assets
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.7.3")

    // --- Réseau OPTIONNEL (push serveur opt-in seulement) ---
    implementation("com.squareup.retrofit2:retrofit:2.11.0")
    implementation("com.jakewharton.retrofit:retrofit2-kotlinx-serialization-converter:1.0.0")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")

    // Tests
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
}
```
Plugins racine : `com.android.application 8.7.2`, `kotlin.android 2.0.21`, `kotlin.plugin.compose 2.0.21`, `kotlin.plugin.serialization 2.0.21`.
`core-library-desugaring` (`com.android.tools:desugar_jdk_libs:2.1.x`) **requis** si on utilise `java.time.*` sur `minSdk 26` avec toutes les API (26 couvre l'essentiel de java.time, mais activer le desugaring pour `LocalDate`/`ZonedDateTime` complets en sécurité).

---

## 8. Build, signature, CI/CD

**Le pipeline existe déjà** dans `/root/.appstore/` — le réutiliser tel quel :

- **Signature** : keystore + secrets déjà générés dans `/root/.appstore/android-secrets.env`
  (`ANDROID_KEYSTORE_BASE64`, `ANDROID_KEYSTORE_PASSWORD`, `ANDROID_KEY_ALIAS`, `ANDROID_KEY_PASSWORD`) et `upload-keystore.jks`.
  `app/build.gradle.kts` doit lire un `keystore.properties` (storeFile/storePassword/keyAlias/keyPassword) pour le `signingConfigs { release }` et l'appliquer au `buildTypes.release`.
- **Injection des secrets** : les clés vivent dans Ariane (annexe `stores`) depuis le 22/09/2026 — `signer moed` les ressort le temps d'un build, `ariane stores.upload` dépose le binaire.
  → pousse les 4 secrets de signature + le service account Play sur le repo GitHub.
- **Workflow** : copier `/root/.appstore/android-template/.github/workflows/android-release.yml` → `.github/workflows/`, en remplaçant `__PACKAGE_NAME__` par `com.moed.app`.
  ⚠️ Le template est écrit pour **Capacitor** (`npx cap sync android`, `ANDROID_DIR: android`). Pour un projet **natif pur**, adapter :
  - supprimer `setup-node` + `npm ci` + `npx cap sync android` ;
  - régler `ANDROID_DIR: .` (le projet Gradle est à la racine) ;
  - conserver decode-keystore → `keystore.properties`, `./gradlew bundleRelease --no-daemon`, upload via `r0adkll/upload-google-play@v1` (chemin `app/build/outputs/bundle/release/app-release.aab`, `track` = input).
- **Play Console** : le compte de service vit dans les secrets d'Ariane ; le dépôt se fait par `ariane stores.upload`. Track par défaut `internal`.
- **SDK local** (builds hors-CI) : `bash /root/.appstore/install-android-sdk.sh` (installe cmdline-tools + `platforms;android-34` + `build-tools;34.0.0` sous `/root/Android/sdk`) ou réutiliser `.android-sdk/` existant du projet de référence. Déclarer `sdk.dir` dans `local.properties` (gitignoré).
- **Build local** : `./gradlew assembleDebug` (APK debug ~19 Mo attendu, cf. référence) ; `./gradlew bundleRelease` (AAB signé).
- **Versionnage** : `versionCode`/`versionName` dans `defaultConfig` ; tag `v*` déclenche aussi le workflow (`on: push: tags: v*`).
- **`.gitignore`** : `build/`, `.gradle/`, `.android-sdk/`, `local.properties`, `keystore.properties`, `*.jks`, `*.p8`.

**AndroidManifest** : `android:supportsRtl="true"`, `POST_NOTIFICATIONS` (33+), `RECEIVE_BOOT_COMPLETED` (replanif), `SCHEDULE_EXACT_ALARM`/`USE_EXACT_ALARM` (allumage à l'heure), `<receiver>` boot + notif, `<receiver>` Glance ×3 (+ `res/xml/*_widget_info.xml`), themed icon (26+), `android:localeConfig` (res/xml/locales_config.xml : he/fr/en).

---

## 9. Ordre de construction recommandé (jalons)

**J0 — Squelette buildable.** settings/build.gradle, version catalog, wrapper Gradle 8.9, `MainActivity` vide + `MoedTheme` stub, manifest, icône adaptive. Cible : `./gradlew assembleDebug` vert.

**J1 — Thème « Le Louah Imprimé ».** `Color.kt` (ColorScheme light+dark), `MoedColors.kt`+`LocalMoedColors`, `Type.kt` (4 polices self-hosted, latin/hébreu par locale, tabular-nums), `Shapes.kt` (échelle M3 + cartouche asymétrique), `Theme.kt` (`MotionScheme.expressive()`, dark=false par défaut). Previews de validation visuelle.

**J2 — Données + moteur + validation.** Exporter `cities.json`/`tzadikim.json` depuis `src/data/*.ts` → `assets/`. `Models.kt`, `AssetData.kt`. Porter `JewishInfo`/`ZmanimEngine`, écrire `CalendarEngine`/`HebrewConverter`/`ZmanimCatalog`, `Localization.kt` (tables he/fr/en). **Écrire et faire PASSER la matrice de validation** (§3.6) — bloquant.

**J3 — Persistance + état.** `SettingsStore`, `PersonalStore`, `MoedViewModel` (StateFlow), horloge live. Tests unitaires occurrences yahrzeit/anniv (Cheshvan-30/Kislev-30/Adar).

**J4 — Navigation + chrome.** `MoedRoot`, `MorphNavBar` (indicateur morphant), NavHost 5+3 routes, RTL/locale câblés, splash.

**J5 — Écran Aujourd'hui (vertical slice de référence).** `DateHeader`, `ShabbatCard` (countdown), `ZmanRegister`, `ParashaDafCard`, cascade d'entrée + haptique. Sert de gabarit DA pour le reste.

**J6 — Écrans restants.** Calendar, Personal (+AddPersonSheet+FAB morph), Tzadikim + détail, ZmanimCity (+disclaimer), Converter, Settings (langue/ville/minhag/notif).

**J7 — Notifications.** `NotificationScheduler` (4 canaux `moed_shabbat/yahrzeit/omer/holiday`), `NotificationReceiver`, `BootReceiver`, permission `POST_NOTIFICATIONS` à l'opt-in, replanif fenêtre glissante.

**J8 — Widgets Glance ×3.** candle / zmanim / omer, `WidgetData` (recalcul offline), `WidgetUpdater` (WorkManager), fallback police système (Glance) en gras+tabular.

**J9 — i18n/RTL/a11y finitions.** he/fr/en à parité, miroir icônes directionnelles, contrastes AA, cibles ≥48dp, contentDescription, Dynamic Type sans troncature d'heures, `reduce-motion`.

**J10 — Réseau opt-in (facultatif) + CI + release.** `net/PushApi`+`AuthTokenStore` derrière le toggle bêta. Copier/adapter le workflow, `setup-android-secrets.sh <repo>`, `keystore.properties`, `bundleRelease` signé, upload track `internal`, tag `v1.0.0`.

**J11 — Durcissement.** ProGuard/R8 (garder KosherJava), tests device réel, screenshots, rapport de validation joint, revue DA vs `DESIGN.md`.

---

### Rappels garde-fous (à vérifier à chaque jalon)
1. Aucun appel réseau pour le calcul (offline complet) — `net/` uniquement opt-in push.
2. Parité de calcul validée vs référence kosher-zmanim/Hebcal (0 tol. dates, ≤2 min zmanim).
3. Light mode par défaut ; RTL première classe ; hébreu primaire ; 3 langues à parité.
4. Aucune donnée personnelle ne quitte l'appareil ; pas de compte, pas de pub, pas de collecte.
5. DA « Le Louah Imprimé » : opaque, zéro flou/translucidité/dégradé vitré ; jamais de faux portrait de tsadik.
</content>
</invoke>
