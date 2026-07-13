# DIRECTION ARTISTIQUE FINALE — MOED ANDROID
## « LE LOUAH IMPRIMÉ » — almanach juif en presse typographique, réinterprété par Material 3 Expressive

> מועד — « le temps fixé ». Cette DA régit **exclusivement l'app native Android** (Jetpack Compose + Material 3 Expressive). Elle est **volontairement distincte** de la DA web/iOS « aube douce sur parchemin » (`/root/apps/mvp-moed/design/DA.md`) : Android reçoit sa propre identité **matérielle, opaque, imprimée**, à l'opposé exact du verre translucide d'Apple. La marque, les fonctions et le calcul restent identiques ; seule la peau change.

**Direction retenue : Proposition 2.**
Justification (3 lignes) :
1. **Fit Compose / M3 Expressive maximal** — le shape-morphing à formes variables est la signature phare de Material 3 Expressive ; en faire le cœur de la DA donne une identité 100 % Android native, impossible à confondre avec le verre iOS, tout en restant du Material standard (faisabilité élevée, pas de hacks).
2. **Cohérence de marque sans copier** — conserver **Fraunces + Frank Ruhl Libre** en display (déjà les polices titres du produit web/iOS) relie Android à la marque, alors que #1 (grotesques Archivo/Space) rompait le lien et ses coins vifs 4–6 dp combattaient la rondeur native de Compose.
3. **Usage quotidien de calendrier** — coins généreux, chiffres-plomb XXL et registres tabulaires restent lisibles et chaleureux au coup d'œil matinal ; #3 (« page de Guemara », trame riso, coins asymétriques) était trop dense/décoratif pour un louah qu'on consulte en 2 secondes.

---

## 0. Concept & principes

**Concept (1 phrase)** : un almanach juif de poche traité comme une **presse typographique vivante** — encre riche sur papier chaud, chiffres monumentaux estampés, mis en mouvement par la matière expressive de Material 3 — tout est **opaque, tactile, imprimé, sûr de lui**.

**Trois mots** : *Imprimé · Exact · Chaleureux.*

**Cinq lois non négociables** :
1. **Zéro translucidité, zéro flou, zéro dégradé vitré.** Que des aplats pleins, des filets d'encre, des ombres douces teintées encre. (L'anti-Apple.)
2. **La typographie est la matière.** Les chiffres de zmanim et de compte à rebours sont le héros de chaque écran, traités comme des blocs de plomb.
3. **La couleur ponctue.** ≈ 90 % papier + encre ; le vermillon (« ner », la flamme) n'apparaît que sur **UN** élément fort par vue.
4. **Light mode par défaut, toujours.** Dark mode « négatif d'imprimerie » fourni mais activé uniquement si l'utilisateur/le système le demande.
5. **RTL première classe, hébreu langue primaire.** Toute forme, tout filet, toute icône directionnelle se miroite ; les chiffres d'horaires restent LTR tabulaires.

---

## 1. Palette — encre & papier (aucune transparence)

### 1.1 Light (défaut)

| Rôle sémantique | Token | Hex | Usage |
|---|---|---|---|
| Papier (fond app) | `paper` | `#F4ECDD` | Fond global, **jamais** blanc pur |
| Papier surélevé (carte) | `paperRaised` | `#FFFDF7` | Cartes, feuilles, dialogs |
| Papier creux (container bas) | `paperSunken` | `#E6D9BE` | Champs, pistes, zones enfoncées |
| Surface container M3 | `surfaceContainer` | `#EDE2C8` | Conteneurs neutres, bottom nav |
| Surface container high | `surfaceContainerHigh`| `#E3D6BB` | Éléments sélectionnés au repos |
| Filet / outline | `outline` | `#C9B78E` | Filets d'imprimeur, bordures cartes |
| Filet fort | `outlineStrong` | `#B29A66` | Séparateurs de registre, cadres |
| Encre (texte principal) | `ink` | `#171512` | Titres, chiffres, corps — noir **chaud** |
| Encre fumée | `inkSmoke` | `#3E3A32` | Texte secondaire |
| Encre estompée | `inkMute` | `#6E675B` | Labels, méta, hints |
| **Primaire — Ner (flamme)** | `ner` | `#B4471F` | Vermillon d'encre : l'accent unique/vue |
| Ner fort (pressed) | `nerStrong` | `#8F3616` | États pressés, texte sur wash |
| Ner wash (container) | `nerWash` | `#F3E2D0` | Fond de la puce/indicateur actif |
| Sur-ner (texte sur ner) | `onNer` | `#FFF6EC` | Texte/icône sur aplat vermillon |
| **Secondaire — Twilight** | `twilight` | `#1F3A5F` | Bleu-nuit d'imprimerie : Roch Hodech, molad |
| Twilight wash | `twilightWash` | `#E4E9F1` | Badges twilight |
| **Tertiaire — Omer (olive)** | `sage` | `#5A6B3A` | Vert olive : Omer, positif |
| Sage wash | `sageWash` | `#E9EBDC` | Badges Omer |
| **Estampe — Or mat** | `gold` | `#B0851F` | Fêtes / hiloula : accent estampé, **jamais brillant** |
| Gold wash | `goldWash` | `#F2E8CC` | Badges fête |
| **Jeûne / alerte douce** | `rose` | `#A6472E` | Jeûne, alerte, suppression |
| Rose wash | `roseWash` | `#F3E1D8` | Badges jeûne |
| Focus (a11y) | `focus` | `#2C6FB3` | Anneau de focus clavier/switch |

**Ombres** : douces, teintées encre (jamais noir pur). `e1` repos `0 1 2 rgba(23,21,18,.10)`, `e2` popover `0 4 12 rgba(23,21,18,.14)`, `e3` modale `0 12 32 rgba(23,21,18,.18)`. Un **halo ner** discret (`0 0 0 3 nerWash` + `0 2 8 rgba(180,71,31,.22)`) réservé au bloc « zman-clé / allumage ».

### 1.2 Dark — « négatif d'imprimerie » (opt-in)

Le dark n'est pas un gris Material générique : c'est une **presse en négatif**, encre profonde chaude + texte couleur parchemin. On garde la chaleur, on ne bascule jamais en bleu-froid.

| Rôle | Token | Hex |
|---|---|---|
| Fond (encre profonde) | `paper` | `#191510` |
| Surface | `paperRaised` | `#221D15` |
| Surface container | `surfaceContainer` | `#282218` |
| Surface container high | `surfaceContainerHigh` | `#332B1F` |
| Papier creux | `paperSunken` | `#14110C` |
| Outline | `outline` | `#4A4130` |
| Outline fort | `outlineStrong` | `#6B5D42` |
| Texte principal (parchemin) | `ink` | `#F0E6D2` |
| Texte fumée | `inkSmoke` | `#CBBE9F` |
| Texte estompé | `inkMute` | `#9A8C6F` |
| Ner (plus lumineux) | `ner` | `#E06A3C` |
| Ner fort | `nerStrong` | `#F0895C` |
| Ner wash | `nerWash` | `#3A2416` |
| Sur-ner | `onNer` | `#1B0F08` |
| Twilight | `twilight` | `#8AA6D0` |
| Twilight wash | `twilightWash` | `#20293A` |
| Sage | `sage` | `#9FB073` |
| Sage wash | `sageWash` | `#242A18` |
| Gold | `gold` | `#D9B45A` |
| Gold wash | `goldWash` | `#332912` |
| Rose | `rose` | `#D98060` |
| Rose wash | `roseWash` | `#331B14` |
| Focus | `focus` | `#6FA8DE` |

> Contraste AA garanti dans les deux thèmes : `ink`/`paper` ≈ 14:1 (light), `ink`/`paper` ≈ 13:1 (dark). Le `ner` sur `paper` sert au texte gras ≥ titre ; pour le corps sur wash, utiliser `nerStrong`.

### 1.3 Mapping vers `ColorScheme` Material 3 (Compose)

```
primary            = ner            onPrimary          = onNer
primaryContainer   = nerWash        onPrimaryContainer = nerStrong
secondary          = twilight       secondaryContainer = twilightWash
tertiary           = sage           tertiaryContainer  = sageWash
error              = rose           errorContainer     = roseWash
background         = paper          onBackground       = ink
surface            = paper          onSurface          = ink
surfaceVariant     = paperSunken    onSurfaceVariant   = inkSmoke
surfaceContainerLowest/Low/…/Highest = paperRaised…surfaceContainerHigh (rampe ci-dessus)
outline            = outline        outlineVariant     = paperSunken
```
`gold` est un **token custom hors ColorScheme** (extension `MoedColors`), car M3 n'a pas de 4e accent ; on l'expose via `CompositionLocal` (`LocalMoedColors`). Idem `inkMute`, `outlineStrong`, `nerGlow`.

---

## 2. Typographie — chiffres de plomb

| Rôle | Latin | Hébreu | Notes |
|---|---|---|---|
| **Display / chiffres héros** | **Fraunces** (opsz 144, poids Black, *italique* pour fêtes) | **Frank Ruhl Libre** (Black) | Zmanim XXL, compte à rebours, grand jour hébreu |
| **Headline / titres de section** | Fraunces (SemiBold, opsz 72) | Frank Ruhl Libre (Bold) | Titres d'écran, « Prochain Chabbat » |
| **Title / cartes** | Fraunces (Medium) | Frank Ruhl Libre (Medium) | Titres de carte, noms de tsadikim |
| **Body / UI** | **Instrument Sans** | **Assistant** | Corps, descriptions, réglages |
| **Label / méta / chips** | Instrument Sans (Medium, tracking +0.3) | Assistant (Medium) | Badges, onglets, boutons |
| **Mono / horaires tabulaires** | Fraunces `tnum` **ou** Instrument Sans `tnum` | — (chiffres restent latins LTR) | **Toujours `tabular figures`** en colonne |

**Règles** :
- **Self-host** les 4 familles (`res/font/`), zéro requête réseau. Fraunces et Frank Ruhl sont variables → embarquer les axes `opsz`/`wght` utilisés.
- **Tabular-nums obligatoire** partout où des heures s'alignent (registres zmanim, calendrier, compte à rebours) : `TextStyle(fontFeatureSettings = "tnum")`.
- **Hébreu +4 %** sur la taille pour équilibrer la couleur typographique (Frank Ruhl est plus léger que Fraunces à taille égale).
- Échelle modulaire ~1.25, base 16 sp. Exemple : displayLarge 57 / headlineMedium 28 / titleLarge 22 / bodyLarge 16 / labelMedium 12.
- Les **chiffres d'horaires ne se traduisent pas et restent LTR** même en hébreu (`unicodeBidi`/`TextDirection.Content`), bloc aligné à droite en RTL mais chiffres non miroités.

```kotlin
val Fraunces = FontFamily(Font(R.font.fraunces_variable, variationSettings = ...))
val FrankRuhl = FontFamily(Font(R.font.frank_ruhl_libre_variable, ...))
val Instrument = FontFamily(Font(R.font.instrument_sans_variable))
val Assistant  = FontFamily(Font(R.font.assistant_variable))
// choix latin/hébreu piloté par la locale active (he -> Frank/Assistant)
```

---

## 3. Formes — squircles francs & shape-morphing (le cœur M3 Expressive)

**C'est ici que Moed Android devient inimitable vs Apple.** On exploite `MaterialShapes` et le **shape-morphing** de Material 3 Expressive.

**Échelle de rayons (M3 shape scale)** :
| Token | dp | Usage |
|---|---|---|
| `extraSmall` | 8 | Chips « tampon », inputs, badges |
| `small` | 12 | Petits boutons, puces d'événement |
| `medium` | 16 | **Cartes standard** (registre zmanim, listes) |
| `large` | 24 | Cartes feature (Chabbat), sheets |
| `extraLarge` | 32 | Modales, grande carte d'allumage |
| `full` | 999 | Boutons pilule, FAB, indicateur nav |

**Cartouches asymétriques** (signature) : les **cartes-clés** (carte Chabbat, hiloula du jour) utilisent un `RoundedCornerShape` **asymétrique** discret — p.ex. `topStart 24, topEnd 8, bottomEnd 24, bottomStart 8` — évoquant un cartouche estampé / une « une » composée. Réservé aux 1–2 cartes héros par écran ; les cartes de liste restent `medium` symétrique pour la scannabilité. **En RTL, l'asymétrie se miroite** (start/end logiques).

**Formes M3 Expressive animées** :
- L'**indicateur de la bottom nav** est une **pilule vermillon** (`nerWash` + label/icône `nerStrong`) qui **morph** entre onglets via `MaterialShapes` (ex. `Pill ↔ Cookie`) — l'effet le plus caractéristique de M3 Expressive.
- Le **FAB « carnet »** (ajout d'un proche) morph au press (rond → pilule étirée).
- **Filets d'imprimeur** : séparateurs = `outline`/`outlineStrong` de **2 dp**, pleins, pleine largeur en registre (jamais un divider fin gris M3 par défaut). Aucun neumorphisme, aucune ombre portée « floue ».

**Motifs (parcimonie extrême)** : un **croissant du molad** en filet fin `twilight` peut estamper le coin d'un en-tête de mois (calendrier) ou la carte molad. Pas de trame riso en fond d'écran (réservée aux illustrations d'assets, cf. §7). Le fond reste un aplat papier plein.

---

## 4. Composants clés (par écran)

**Bottom navigation (M3 Expressive)** — conteneur `surfaceContainer` plein (pas de barre translucide), 5 onglets (`today / calendar / personal / tsadikim / settings`), indicateur **pilule vermillon morphant**, labels toujours visibles en Instrument/Assistant Medium. Icônes pleines (filled) quand actif, outline sinon.

**Écran « Aujourd'hui »** :
- **En-tête date** : grand **jour hébraïque** en Fraunces/Frank Ruhl Black (héros typographique), date grégorienne en `inkMute`, ville avec icône localisation, rangée de **badges tampons** carrés (`extraSmall`) : Omer=`sage`, fête=`gold`, Roch Hodech=`twilight`, jeûne=`rose`.
- **Carte Chabbat (feature)** = **cartouche estampé asymétrique** `extraLarge`, fond `paperRaised`, **halo ner**, titre « Prochain Chabbat » en Fraunces italique, **heure d'allumage en chiffres-plomb XXL** (displayLarge tabular), **compte à rebours live** (« allumage dans 3 h 12 »), havdala en registre dessous. Illustration bougies (asset) en filet, jamais photo. C'est **le seul** élément vermillon de la vue.
- **Registre zmanim clés** (HOME_ZMANIM) : liste-registre à **filets d'imprimeur 2 dp**, chaque ligne = icône + label (Instrument) + **heure tabulaire alignée en colonne** ; lien « tous les zmanim » en bouton pilule outline. Zman nul → « — » (jamais NaN).
- **Parasha + Daf Yomi** : carte `medium`, nom de parasha en Fraunces, haftara/daf en `inkSmoke`.

**Écran « Calendrier »** = **bloc d'imprimerie**. En-tête mois+année en Fraunces Black (croissant molad estampé en coin), chevrons mois préc./suiv. (**miroités en RTL**). Grille/registre des jours : chaque jour = date hébraïque + **puces pleines** colorées par catégorie (fête=gold, jeûne=rose, roch hodech=twilight, omer=sage, autre=inkMute). Filets fins `outline` entre les jours.

**Écran « Personnel »** (carnet) : liste de cartes `medium`, chaque proche = nom (Fraunces Medium), type (chip tampon yahrzeit=twilight / anniversaire=gold), **prochaine occurrence** en chiffres tabulaires (grég + hébreu). **FAB pilule vermillon** morphant « Ajouter » → **bottom sheet** (`extraLarge`, opaque) : segmented yahrzeit/anniversaire, nom, date picker M3, toggle « après le coucher ». État vide illustré (asset `empty-family`). Bloc opt-in notifications.

**Écran « Tsadikim »** : section « Hiloula du jour » = **cartouche estampé** (si personne : état illustré). Répertoire = grille de cartes `medium` à **visuel symbolique par courant** (`motif-tanna/rishon/…`) — **jamais de faux portrait**. Fiche détail : nom trilingue en display, épithète Fraunces italique, dates de hiloula, kever, œuvres, bio, **badge de confiance** (high/medium/traditional = chip gold/twilight/inkMute) + **sources** (liens).

**Écran « Réglages »** : sections en registres à filets. Langue he/fr/en (change locale + RTL), recherche de ville (CitySearch), Minhag (segmented allumage 18/20/30/40, shita tzeit degrees/minutes, région auto/il/diaspora), toggles notifications opt-in. Switches M3 dans la teinte `ner` active.

**Sous-vues** : Zmanim par ville (registre complet + **disclaimer halakhique** obligatoire + shita affichée + villes proches), Convertisseur de dates (segmented grég⇄hébreu).

**Widgets Glance (×3, conservés)** : allumage (heure + compte à rebours), zmanim du jour, Omer du jour. Même DA — fond `paperRaised` opaque, chiffres tabulaires, accent `ner` sur l'allumage, `sage` sur l'Omer, `twilight` sur roch hodech. **Glance ne supporte pas les polices custom → fallback système** en gras + tabular ; garder l'esprit « bloc de plomb » via tailles et aplats. Refresh WorkManager, **zéro réseau** (`WidgetData.kt`).

---

## 5. Motion — la matière expressive (springs, pas de fondus vitrés)

Utiliser **`MotionScheme.expressive()`** de Material 3 comme base (springs spatiaux/effectifs).

- **Un seul « moment » orchestré par vue** : à l'ouverture d'« Aujourd'hui », révélation **en cascade** des blocs (en-tête → carte Chabbat → registre zmanim → parasha), chaque bloc **« tombe et cale »** comme un caractère composé : `fade + translationY(8→0)` avec **overshoot spring** (`dampingRatio ≈ 0.75`, `stiffness = medium`). Pas de fondu long, pas de flou.
- **Shape-morph nav** : l'indicateur pilule se **déforme** entre onglets (M3 Expressive shape animation), pas un simple slide.
- **Encre qui imbibe** : apparition des cartes = `fade + scale(0.98→1)` court (≤ 220 ms), donnant l'impression que l'encre « prend » sur le papier.
- **Compte à rebours** : mise à jour à la seconde **sans animation** du chiffre (pas de flip vitré) — l'exactitude prime ; seul le dernier segment peut avoir un léger crossfade.
- **Haptique tactile** (`HapticFeedback`) sur chaque « estampe » : press du FAB, sélection d'onglet, ajout d'un proche, toggle de notification — un retour sec, court.
- **Transitions d'écran** : translation nette horizontale (respect start/end → **inversée en RTL**), jamais de push « verre » iOS.
- **`reduce-motion` respecté** : si l'utilisateur a désactivé les animations (`Settings.Global.ANIMATOR_DURATION_SCALE == 0` / accessibilité), désactiver cascade, morph et overshoot → simple apparition instantanée ; les valeurs restent identiques.

---

## 6. RTL & internationalisation (première classe)

- **3 langues à parité** : hébreu (`he`, **RTL, primaire**), français (`fr`, LTR), anglais (`en`, LTR). Défaut code = `fr` ; au 1er lancement, mapper la langue système → he/fr/en (sinon `en`). Changement **in-app** dans Réglages (pas seulement langue système).
- **Mise en page 100 % logique** : `start/end` partout, **jamais** `left/right`. `LocalLayoutDirection` piloté par la locale. `android:supportsRtl="true"`.
- **Miroir** : filets, cartouches asymétriques, transitions d'écran, **icônes directionnelles** (chevrons mois préc./suiv., flèches « suivant ») → miroitées en RTL (`autoMirrored`). **Icônes non directionnelles** (bougie, lune, croissant molad) → **jamais** miroitées.
- **Chiffres d'horaires restent LTR** et **tabulaires** même en hébreu (bloc aligné end, chiffres non inversés).
- **Frank Ruhl Libre / Assistant** obligatoires pour l'hébreu, +4 % de taille. Noms de mois hébreux / jours / labels de zmanim / titres de fêtes = **tables de traduction maison** he/fr/en (les libs `@hebcal/KosherJava` ne connaissent que en/he ; le FR vient de nos tables, cf. `Localization.kt`). Paramètres dans les chaînes (`home.omerDay {n}`, `zmanim.pageTitle {city}`).

---

## 7. Assets & iconographie

- **Illustrations** (bougies, zmanim, parasha, omer, molad, synagogue, birthday, yahrzeit, empty states) : style **estampe / duotone riso** (encre + un accent), aplats, grain d'impression discret — **jamais** de photo ni de rendu 3D vitré. Embarquer en assets multi-densité mdpi→xxxhdpi (ou vecteurs quand possible).
- **Tsadikim** : visuels **symboliques par courant** (`motif-tanna/rishon/acharon/hassid/habad/sefarade`, `kever`, `hilula-candle`) — **jamais de faux portrait photoréaliste** (exigence éditoriale dure).
- **App icon Moed** : concept **« croissant du molad → flamme »**, en aplat vermillon `ner` sur papier `paper`, style tampon d'imprimeur (adaptive icon Android : foreground croissant/flamme, background papier). Monochrome (Android 13+ themed icon) en `ink`.
- **Grain** : une **trame riso** très discrète est tolérée **dans les illustrations et l'app icon uniquement**, jamais en fond d'écran (les surfaces UI restent des aplats pleins pour la lisibilité et la performance).

---

## 8. Accessibilité & tokens Compose

- **Contraste AA** garanti (voir §1). Ne jamais mettre du corps `ner` sur `paper` en petit — utiliser `nerStrong` ou `ink`.
- **Cibles tactiles ≥ 48 dp**, focus visible (anneau `focus` 2 dp), `contentDescription` sur toutes les icônes/illustrations.
- **Dynamic type** : respecter `fontScale` système ; les chiffres-plomb XXL doivent rester dans leur bloc (autosize/`maxLines` gérés), le registre zmanim ne doit jamais tronquer une heure.
- **Espacement** : échelle 4 dp (4 8 12 16 20 24 32 40 56 72 96), marge de page mobile **20 dp**.
- Exposer les tokens hors-ColorScheme via un `data class MoedColors` + `CompositionLocal` :

```kotlin
data class MoedColors(
  val gold: Color, val goldWash: Color,
  val inkMute: Color, val inkSmoke: Color,
  val outlineStrong: Color, val nerStrong: Color, val nerGlow: Color,
  val twilight: Color, val twilightWash: Color,
  val sage: Color, val sageWash: Color,
  val rose: Color, val roseWash: Color,
  val paperRaised: Color, val paperSunken: Color,
)
val LocalMoedColors = staticCompositionLocalOf { /* light defaults */ }

@Composable
fun MoedTheme(dark: Boolean = isSystemInDarkTheme() /* défaut app = false */, content: @Composable () -> Unit) {
  val scheme = if (dark) MoedDarkColorScheme else MoedLightColorScheme
  val extra  = if (dark) MoedDarkExtra else MoedLightExtra
  CompositionLocalProvider(LocalMoedColors provides extra) {
    MaterialTheme(colorScheme = scheme, typography = MoedTypography, shapes = MoedShapes,
                  motionScheme = MotionScheme.expressive(), content = content)
  }
}
```
> **Light mode par défaut, toujours** : `MoedTheme` force `dark = false` en v1 sauf préférence explicite (réglage app ou opt-in). Ne jamais partir en dark par défaut.

---

## 9. Ce qu'on ne fait JAMAIS (garde-fous anti-Apple)

- Pas de `blur` / matériaux translucides / « frosted glass » / vibrancy.
- Pas de dégradés lisses, pas d'ombres floues neutres genre iOS card.
- Pas de coins uniformément arrondis « sans caractère » sur les cartes héros (elles ont leur cartouche asymétrique).
- Pas de sans-serif humaniste iOS pour les titres — les titres sont **Fraunces/Frank Ruhl**, servi.
- Pas d'animation de flip/parallax « verre » ; le motion est **matière & spring**.
- Pas de faux portraits de tsadikim, jamais.
- Pas d'appel réseau au runtime (offline complet, cf. NATIVE_SPEC §3, §7, §10).

---

## 10. Références

- Spec produit natif : `/root/apps/moed-android/NATIVE_SPEC.md`
- DA web/iOS (autre peau, à ne pas confondre) : `/root/apps/mvp-moed/design/DA.md`
- Modèle de portage Android existant : `/root/apps/mvp-moed/android/app/src/main/java/com/moed/app/` (`ui/`, `widget/`, `notif/`, `data/`, `engine/`)
- Material 3 Expressive : `MaterialShapes`, shape-morphing, `MotionScheme.expressive()` (androidx.compose.material3 ≥ 1.4).
