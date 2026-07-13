package com.moed.app.data

import java.time.LocalDate
import java.util.Locale

/**
 * In-app internationalization for Moed — Hebrew (RTL, primary), French, English,
 * at parity. Everything is a hand-maintained table: the calendar libraries
 * (hebcal packages, KosherJava) only ship `en`/`he`, so French — and every UI string —
 * is served from here, never from the engine.
 *
 * Nothing here touches the network; all data is compiled into the app.
 *
 *  - [monthName]    Hebrew month name, leap-aware (Adar I/II)
 *  - [weekday]      weekday name for a Gregorian date
 *  - [zmanLabel]    label for each of the 16 zmanim keys
 *  - [holidayName]  localize a canonical (English) holiday name
 *  - [string]       UI string by `"section.key"`, with `{placeholder}` args
 *  - [localeTag]    BCP-47 tag (he-IL / fr-FR / en-US)
 *  - [isRtl]        true for Hebrew
 *  - [resolve]      map a language setting (`"system"` → he/fr/en, fallback en)
 */
object Localization {

    /** A trilingual string triple. */
    internal class T(val he: String, val fr: String, val en: String)

    private fun pick(t: T, lang: String): String = when (lang) {
        "he" -> t.he
        "fr" -> t.fr
        else -> t.en
    }

    // ---- Hebrew months (aligned with engine/hebrewDate.ts MONTH_NAMES) ----
    // Keyed by canonical hebcal/KosherJava month-name key.
    private val MONTH_NAMES: Map<String, T> = mapOf(
        "Nisan" to T("נִיסָן", "Nissan", "Nisan"),
        "Iyyar" to T("אִיָּיר", "Iyar", "Iyar"),
        "Sivan" to T("סִיוָן", "Sivan", "Sivan"),
        "Tamuz" to T("תַּמּוּז", "Tamouz", "Tammuz"),
        "Av" to T("אָב", "Av", "Av"),
        "Elul" to T("אֱלוּל", "Eloul", "Elul"),
        "Tishrei" to T("תִּשְׁרֵי", "Tichri", "Tishrei"),
        "Cheshvan" to T("חֶשְׁוָן", "Hechvan", "Cheshvan"),
        "Kislev" to T("כִּסְלֵו", "Kislev", "Kislev"),
        "Tevet" to T("טֵבֵת", "Tevet", "Tevet"),
        "Sh'vat" to T("שְׁבָט", "Chevat", "Sh'vat"),
        "Adar" to T("אֲדָר", "Adar", "Adar"),
        "Adar I" to T("אֲדָר א׳", "Adar I", "Adar I"),
        "Adar II" to T("אֲדָר ב׳", "Adar II", "Adar II"),
    )

    // ---- Weekdays, index 0 = Sunday .. 6 = Saturday ----
    private val WEEKDAY_NAMES: List<T> = listOf(
        T("רִאשׁוֹן", "Dimanche", "Sunday"),
        T("שֵׁנִי", "Lundi", "Monday"),
        T("שְׁלִישִׁי", "Mardi", "Tuesday"),
        T("רְבִיעִי", "Mercredi", "Wednesday"),
        T("חֲמִישִׁי", "Jeudi", "Thursday"),
        T("שִׁשִּׁי", "Vendredi", "Friday"),
        T("שַׁבָּת", "Chabbat", "Saturday"),
    )

    // ---- Labels for the 16 zmanim keys (engine ZmanimCatalog order) ----
    private val ZMAN_LABELS: Map<String, T> = mapOf(
        "alotHashachar" to T("עלות השחר (16.1°)", "Aube (alot hachahar, 16,1°)", "Dawn (alot hashachar, 16.1°)"),
        "alotHashachar72" to T("עלות השחר (72 דק׳)", "Aube (alot, 72 min)", "Dawn (alot, 72 min)"),
        "misheyakir" to T("משיכיר (טלית ותפילין)", "Misheyakir (talit & téfilin)", "Misheyakir (tallit & tefillin)"),
        "misheyakirMachmir" to T("משיכיר מחמיר (10.2°)", "Misheyakir mahmir (10,2°)", "Misheyakir machmir (10.2°)"),
        "hanetzHachama" to T("הנץ החמה", "Lever du soleil (netz)", "Sunrise (netz)"),
        "sofZmanShmaMGA" to T("סוף זמן שמע (מג״א)", "Fin Chéma (Magen Avraham)", "Latest Shema (Magen Avraham)"),
        "sofZmanShmaGRA" to T("סוף זמן שמע (גר״א)", "Fin Chéma (Gaon de Vilna)", "Latest Shema (Vilna Gaon)"),
        "sofZmanTfilaMGA" to T("סוף זמן תפילה (מג״א)", "Fin téfila (Magen Avraham)", "Latest tefillah (Magen Avraham)"),
        "sofZmanTfilaGRA" to T("סוף זמן תפילה (גר״א)", "Fin téfila (Gaon de Vilna)", "Latest tefillah (Vilna Gaon)"),
        "chatzot" to T("חצות היום", "Milieu du jour (hatzot)", "Midday (chatzot)"),
        "minchaGedola" to T("מנחה גדולה", "Minha guedola", "Mincha gedola"),
        "minchaKetana" to T("מנחה קטנה", "Minha ketana", "Mincha ketana"),
        "plagHamincha" to T("פלג המנחה", "Plag hamin'ha", "Plag hamincha"),
        "shkiaHachama" to T("שקיעה", "Coucher du soleil (chkia)", "Sunset (shkiah)"),
        "tzeitHakochavim" to T("צאת הכוכבים (8.5°)", "Tombée de la nuit (tzeit, 8,5°)", "Nightfall (tzeit, 8.5°)"),
        "tzeit72" to T("צאת (רבנו תם, 72 דק׳)", "Nuit (Rabbeinou Tam, 72 min)", "Night (Rabbeinu Tam, 72 min)"),
        // Convenience key used by the Shabbat card / widgets.
        "candleLighting" to T("הדלקת נרות", "Allumage des bougies", "Candle lighting"),
        "havdala" to T("הבדלה", "Havdala", "Havdalah"),
    )

    // ---- Holiday names: canonical hebcal English → trilingual base name ----
    // Matched by exact key then longest prefix; any English suffix (e.g. " I",
    // ": 2 Candles") is preserved untranslated. Longer keys take precedence.
    private val HOLIDAY_NAMES: List<Pair<String, T>> = listOf(
        "Rosh Hashana" to T("ראש השנה", "Roch Hachana", "Rosh Hashana"),
        "Yom Kippur" to T("יום כיפור", "Yom Kippour", "Yom Kippur"),
        "Sukkot" to T("סוכות", "Souccot", "Sukkot"),
        "Shmini Atzeret" to T("שמיני עצרת", "Chemini Atséret", "Shmini Atzeret"),
        "Simchat Torah" to T("שמחת תורה", "Sim'hat Torah", "Simchat Torah"),
        "Chanukah" to T("חנוכה", "Hanoucca", "Chanukah"),
        "Chag HaBanot" to T("חג הבנות", "Hag HaBanot", "Chag HaBanot"),
        "Tu BiShvat" to T("ט״ו בשבט", "Tou Bichvat", "Tu BiShvat"),
        "Purim Katan" to T("פורים קטן", "Pourim Katan", "Purim Katan"),
        "Shushan Purim" to T("שושן פורים", "Chouchan Pourim", "Shushan Purim"),
        "Purim" to T("פורים", "Pourim", "Purim"),
        "Ta'anit Esther" to T("תענית אסתר", "Jeûne d'Esther", "Fast of Esther"),
        "Pesach Sheni" to T("פסח שני", "Péssah Chéni", "Pesach Sheni"),
        "Pesach" to T("פסח", "Péssah", "Pesach"),
        "Lag BaOmer" to T("ל״ג בעומר", "Lag BaOmer", "Lag BaOmer"),
        "Shavuot" to T("שבועות", "Chavouot", "Shavuot"),
        "Rosh Chodesh" to T("ראש חודש", "Roch Hodech", "Rosh Chodesh"),
        "Tzom Gedaliah" to T("צום גדליה", "Jeûne de Guedalia", "Fast of Gedaliah"),
        "Asara B'Tevet" to T("עשרה בטבת", "10 Tévet", "Asarah B'Tevet"),
        "Ta'anit Bechorot" to T("תענית בכורות", "Jeûne des premiers-nés", "Fast of the Firstborn"),
        "Tzom Tammuz" to T("צום י״ז בתמוז", "17 Tamouz", "17th of Tammuz"),
        "Tish'a B'Av" to T("תשעה באב", "Ticha BeAv", "Tisha B'Av"),
        "Tu B'Av" to T("ט״ו באב", "Tou BeAv", "Tu B'Av"),
        "Shabbat" to T("שבת", "Chabbat", "Shabbat"),
        "Yom HaShoah" to T("יום השואה", "Yom HaShoah", "Yom HaShoah"),
        "Yom HaZikaron" to T("יום הזיכרון", "Yom HaZikaron", "Yom HaZikaron"),
        "Yom HaAtzma'ut" to T("יום העצמאות", "Yom HaAtsmaout", "Yom HaAtzma'ut"),
        "Yom Yerushalayim" to T("יום ירושלים", "Yom Yérouchalayim", "Yom Yerushalayim"),
        "Sigd" to T("סיגד", "Sigd", "Sigd"),
    ).sortedByDescending { it.first.length }

    // ---- UI strings, "section.key" → trilingual (from src/messages/{he,fr,en}.json) ----
    private val STRINGS: Map<String, T> = mapOf(
        "meta.title" to T("מועד — הלוח היהודי, ברור ומדויק", "Moed — Le calendrier juif, clair et exact", "Moed — The Jewish calendar, clear and exact"),
        "meta.description" to T("זמנים, זמני הדלקת נרות, תאריכים עבריים, יארצייט והילולות — לעיר שלך, בשלווה. חינם, גם ללא חיבור, ללא פרסומות.", "Zmanim, horaires d'allumage des bougies, dates hébraïques, yahrzeit et hiloulot — pour votre ville, dans le calme. Gratuit, hors-ligne, sans publicité.", "Zmanim, candle-lighting times, Hebrew dates, yahrzeit and hilulot — for your city, calmly. Free, offline, ad-free."),
        "brand.name" to T("מועד", "Moed", "Moed"),
        "brand.tagline" to T("הזמן הקבוע", "Le temps fixé", "The appointed time"),
        "nav.today" to T("היום", "Aujourd'hui", "Today"),
        "nav.calendar" to T("לוח שנה", "Calendrier", "Calendar"),
        "nav.personal" to T("אישי", "Personnel", "Personal"),
        "nav.tsadikim" to T("צדיקים", "Tsadikim", "Tzadikim"),
        "nav.settings" to T("הגדרות", "Réglages", "Settings"),
        "common.city" to T("עיר", "Ville", "City"),
        "common.changeCity" to T("החלפת עיר", "Changer de ville", "Change city"),
        "common.search" to T("חיפוש", "Rechercher", "Search"),
        "common.searchCity" to T("חיפוש עיר…", "Rechercher une ville…", "Search a city…"),
        "common.aroundMe" to T("סביבי", "Autour de moi", "Around me"),
        "common.save" to T("שמירה", "Enregistrer", "Save"),
        "common.cancel" to T("ביטול", "Annuler", "Cancel"),
        "common.add" to T("הוספה", "Ajouter", "Add"),
        "common.delete" to T("מחיקה", "Supprimer", "Delete"),
        "common.close" to T("סגירה", "Fermer", "Close"),
        "common.loading" to T("טוען…", "Chargement…", "Loading…"),
        "common.source" to T("מקורות ושיטה", "Sources & méthode", "Sources & method"),
        "common.israel" to T("ארץ ישראל", "Israël", "Israel"),
        "common.diaspora" to T("חו״ל", "Diaspora", "Diaspora"),
        "common.today" to T("היום", "aujourd'hui", "today"),
        "common.learnMore" to T("מידע נוסף", "En savoir plus", "Learn more"),
        "common.empty" to T("אין עדיין", "Rien pour le moment", "Nothing yet"),
        "common.seeAll" to T("הצג הכל", "Tout voir", "See all"),
        "common.near" to T("בקרבת מקום", "À proximité", "Nearby"),
        "common.backHome" to T("חזרה לבית", "Retour à l'accueil", "Back home"),
        "home.gregorian" to T("תאריך לועזי", "Date grégorienne", "Gregorian date"),
        "home.hebrew" to T("תאריך עברי", "Date hébraïque", "Hebrew date"),
        "home.candleTitle" to T("כניסת השבת", "Entrée du Chabbat", "Shabbat begins"),
        "home.candleIn" to T("הדלקת נרות בעוד {time}", "Allumage dans {time}", "Candle lighting in {time}"),
        "home.candleAt" to T("הדלקת נרות ב{time}", "Allumage à {time}", "Candle lighting at {time}"),
        "home.havdala" to T("צאת השבת (הבדלה)", "Sortie (havdala)", "Ends (havdalah)"),
        "home.nextShabbat" to T("השבת הקרובה", "Prochain Chabbat", "Next Shabbat"),
        "home.zmanimTitle" to T("זמני היום", "Zmanim du jour", "Today's zmanim"),
        "home.allZmanim" to T("כל הזמנים", "Tous les zmanim", "All zmanim"),
        "home.parasha" to T("פרשת השבוע", "Parasha de la semaine", "This week's parashah"),
        "home.omerDay" to T("העומר — יום {n}", "Omer — {n}ᵉ jour", "Omer — day {n}"),
        "home.dafYomi" to T("דף היומי", "Daf Yomi", "Daf Yomi"),
        "home.now" to T("כעת", "en ce moment", "now"),
        "zmanim.alot" to T("עלות השחר", "Aube (alot hachahar)", "Dawn (alot hashachar)"),
        "zmanim.misheyakir" to T("משיכיר (טלית ותפילין)", "Misheyakir (talit & téfilin)", "Misheyakir (tallit & tefillin)"),
        "zmanim.sunrise" to T("הנץ החמה", "Lever du soleil (netz)", "Sunrise (netz)"),
        "zmanim.sofShmaMGA" to T("סוף זמן שמע (מג״א)", "Fin Chéma (Magen Avraham)", "Latest Shema (Magen Avraham)"),
        "zmanim.sofShmaGRA" to T("סוף זמן שמע (גר״א)", "Fin Chéma (Gaon de Vilna)", "Latest Shema (Vilna Gaon)"),
        "zmanim.sofTefila" to T("סוף זמן תפילה", "Fin téfila", "Latest tefillah"),
        "zmanim.chatzot" to T("חצות היום", "Milieu du jour (hatzot)", "Midday (chatzot)"),
        "zmanim.minchaGedola" to T("מנחה גדולה", "Minha guedola", "Mincha gedola"),
        "zmanim.minchaKetana" to T("מנחה קטנה", "Minha ketana", "Mincha ketana"),
        "zmanim.plag" to T("פלג המנחה", "Plag hamin'ha", "Plag hamincha"),
        "zmanim.sunset" to T("שקיעה", "Coucher du soleil (chkia)", "Sunset (shkiah)"),
        "zmanim.tzeit" to T("צאת הכוכבים", "Tombée de la nuit (tzeit)", "Nightfall (tzeit)"),
        "zmanim.tzeitRT" to T("צאת (רבנו תם)", "Nuit (Rabbeinou Tam)", "Night (Rabbeinu Tam)"),
        "zmanim.candles" to T("הדלקת נרות", "Allumage des bougies", "Candle lighting"),
        "zmanim.disclaimer" to T("הזמנים מחושבים (שיטה: {shita}, הדלקה {min} דקות לפני השקיעה). יש לאמת תמיד מול הקהילה שלך.", "Horaires calculés (shita : {shita}, allumage {min} min avant la chkia). Vérifiez toujours auprès de votre communauté.", "Times computed (shita: {shita}, lighting {min} min before sunset). Always verify with your community."),
        "zmanim.pageTitle" to T("זמנים ב{city}", "Zmanim à {city}", "Zmanim in {city}"),
        "zmanim.nearby" to T("ערים סמוכות", "Villes voisines", "Nearby cities"),
        "zmanim.thisShabbat" to T("השבת הקרובה", "Ce Chabbat", "This Shabbat"),
        "zmanim.metaTitle" to T("זמני הדלקת נרות וזמנים ב{city}", "Horaires d'allumage des bougies et zmanim à {city}", "Candle lighting times & zmanim in {city}"),
        "zmanim.metaDesc" to T("זמני הדלקת נרות, הבדלה וכל הזמנים ל{city}, {country}. חישוב מדויק, גם ללא חיבור.", "Horaires d'allumage des bougies, havdala et tous les zmanim à {city}, {country}. Calcul précis et hors-ligne.", "Candle lighting, havdalah and all zmanim for {city}, {country}. Accurate, offline calculation."),
        "converter.title" to T("ממיר תאריך עברי", "Convertisseur de date hébraïque", "Hebrew date converter"),
        "converter.subtitle" to T("המרת תאריך לועזי לעברי ולהפך.", "Convertissez une date grégorienne en date hébraïque et inversement.", "Convert a Gregorian date to Hebrew and back."),
        "converter.gregToHeb" to T("לועזי → עברי", "Grégorien → Hébreu", "Gregorian → Hebrew"),
        "converter.hebToGreg" to T("עברי → לועזי", "Hébreu → Grégorien", "Hebrew → Gregorian"),
        "converter.result" to T("תוצאה", "Résultat", "Result"),
        "converter.gregorianDate" to T("תאריך לועזי", "Date grégorienne", "Gregorian date"),
        "converter.day" to T("יום", "Jour", "Day"),
        "converter.month" to T("חודש", "Mois", "Month"),
        "converter.year" to T("שנה", "Année", "Year"),
        "converter.invalid" to T("תאריך לא תקין", "Date invalide", "Invalid date"),
        "personal.title" to T("פנקס המשפחה", "Carnet familial", "Family book"),
        "personal.subtitle" to T("ימי הולדת עבריים וימי יארצייט של יקיריכם, עם תזכורות שנתיות.", "Anniversaires hébraïques et yahrzeits de vos proches, avec rappels annuels.", "Hebrew birthdays and yahrzeits of your loved ones, with yearly reminders."),
        "personal.addBirthday" to T("יום הולדת עברי", "Anniversaire hébraïque", "Hebrew birthday"),
        "personal.addYahrzeit" to T("יארצייט", "Yahrzeit", "Yahrzeit"),
        "personal.name" to T("שם", "Nom", "Name"),
        "personal.deathDate" to T("תאריך הפטירה", "Date de décès", "Date of passing"),
        "personal.birthDate" to T("תאריך הלידה", "Date de naissance", "Date of birth"),
        "personal.afterSunset" to T("אחרי השקיעה", "Après le coucher du soleil", "After sunset"),
        "personal.nextOccurrence" to T("המועד הבא", "Prochaine occurrence", "Next occurrence"),
        "personal.emptyTitle" to T("הפנקס ריק", "Votre carnet est vide", "Your book is empty"),
        "personal.emptyBody" to T("הוסיפו יקיר כדי לקבל תזכורת מדי שנה.", "Ajoutez un proche pour recevoir les rappels chaque année.", "Add a loved one to receive reminders each year."),
        "personal.kaddish" to T("תזכורת לקדיש", "Rappel du kaddish", "Kaddish reminder"),
        "personal.addTitle" to T("הוספת יקיר", "Ajouter un proche", "Add a loved one"),
        "personal.type" to T("סוג", "Type", "Type"),
        "personal.enableReminders" to T("הפעלת תזכורות", "Activer les rappels", "Enable reminders"),
        "personal.born" to T("נולד/ה", "Né(e) le", "Born"),
        "personal.passed" to T("פטירה", "Décès", "Passing"),
        "tsadikim.title" to T("הילולות", "Hiloulot", "Hilulot"),
        "tsadikim.todayTitle" to T("הילולת היום", "Hiloula du jour", "Today's hilula"),
        "tsadikim.subtitle" to T("הצדיקים שהילולתם מצוינת, עם ביוגרפיה ממקורות.", "Les tsadikim dont on commémore la hiloula, avec une biographie sourcée.", "The tzadikim whose hilula is commemorated, with a sourced biography."),
        "tsadikim.hilulaDate" to T("תאריך ההילולה", "Date de hiloula", "Hilula date"),
        "tsadikim.kever" to T("מקום הקבר", "Lieu du kever", "Resting place"),
        "tsadikim.works" to T("חיבורים", "Œuvres", "Works"),
        "tsadikim.noneToday" to T("אין הילולה מרכזית היום.", "Aucune hiloula majeure aujourd'hui.", "No major hilula today."),
        "tsadikim.visualNote" to T("מתוך כבוד, אין דיוקן מומצא: התמונות סמליות.", "Par respect, aucun portrait n'est inventé : les visuels sont symboliques.", "Out of respect, no portrait is invented: visuals are symbolic."),
        "tsadikim.allTitle" to T("כל הצדיקים", "Tous les tsadikim", "All tzadikim"),
        "tsadikim.sources" to T("מקורות", "Sources", "Sources"),
        "tsadikim.nextHilula" to T("ההילולה הבאה", "Prochaine hiloula", "Next hilula"),
        "tsadikim.confidence" to T("מהימנות התאריך", "Fiabilité de la date", "Date reliability"),
        "tsadikim.confHigh" to T("מתועדת", "documentée", "documented"),
        "tsadikim.confMedium" to T("מבוססת", "bien attestée", "well-attested"),
        "tsadikim.confTraditional" to T("מסורתית", "traditionnelle", "traditional"),
        "tsadikim.metaTitle" to T("{name} — הילולה, ביוגרפיה ומקום הקבר", "{name} — hiloula, biographie et kever", "{name} — hilula, biography and resting place"),
        "tsadikim.metaDesc" to T("ביוגרפיה, תאריך ההילולה ומקום הקבר של {name}.", "Biographie, date de hiloula et lieu du kever de {name}.", "Biography, hilula date and resting place of {name}."),
        "calendar.title" to T("לוח שנה", "Calendrier", "Calendar"),
        "calendar.today" to T("היום", "Aujourd'hui", "Today"),
        "calendar.noEvents" to T("אין אירועים", "Aucun événement", "No events"),
        "calendar.prevMonth" to T("חודש קודם", "Mois précédent", "Previous month"),
        "calendar.nextMonth" to T("חודש הבא", "Mois suivant", "Next month"),
        "calendar.omerBadge" to T("עומר {n}", "Omer {n}", "Omer {n}"),
        "settings.title" to T("הגדרות", "Réglages", "Settings"),
        "settings.language" to T("שפה", "Langue", "Language"),
        "settings.location" to T("מיקום", "Lieu", "Location"),
        "settings.minhag" to T("מנהג ושיטה", "Minhag & shita", "Minhag & shita"),
        "settings.candleMinutes" to T("הדלקת נרות (דקות לפני השקיעה)", "Allumage (minutes avant la chkia)", "Candle lighting (minutes before sunset)"),
        "settings.candleAuto" to T("אוטומטי (לפי העיר)", "Auto (selon la ville)", "Auto (by city)"),
        "settings.tzeitShita" to T("צאת הכוכבים", "Tombée de la nuit", "Nightfall"),
        "settings.tzeitDegrees" to T("מעלות (8.5°)", "Degrés (8,5°)", "Degrees (8.5°)"),
        "settings.tzeitMinutes" to T("דקות (72 דק׳)", "Minutes (72 min)", "Minutes (72 min)"),
        "settings.israelDiaspora" to T("ארץ ישראל / חו״ל", "Israël / Diaspora", "Israel / Diaspora"),
        "settings.auto" to T("אוטומטי לפי העיר", "Automatique selon la ville", "Automatic by city"),
        "settings.notifications" to T("התראות", "Notifications", "Notifications"),
        "settings.notifDesc" to T("קבלו תזכורות שקטות, המחושבות במכשיר שלכם.", "Recevez des rappels discrets, calculés sur votre appareil.", "Receive quiet reminders, computed on your device."),
        "settings.notifShabbat" to T("תזכורת יום שישי (נרות)", "Rappel du vendredi (allumage)", "Friday reminder (candles)"),
        "settings.notifOmer" to T("ספירת העומר", "Compte du Omer", "Omer count"),
        "settings.notifYahrzeit" to T("תזכורות יארצייט", "Rappels de yahrzeit", "Yahrzeit reminders"),
        "settings.notifHilula" to T("הילולת היום", "Hiloula du jour", "Hilula of the day"),
        "settings.enableNotif" to T("הפעלת התראות", "Activer les notifications", "Enable notifications"),
        "settings.install" to T("התקנת האפליקציה", "Installer l'application", "Install the app"),
        "install.title" to T("התקינו את מועד", "Installez Moed", "Install Moed"),
        "install.body" to T("הוסיפו את מועד למסך הבית: גישה מיידית, גם ללא חיבור, ותזכורות.", "Ajoutez Moed à votre écran d'accueil : accès instantané, hors-ligne, et rappels.", "Add Moed to your home screen: instant access, offline, and reminders."),
        "install.iosHint" to T("הקישו שיתוף ואז ״הוסף למסך הבית״.", "Appuyez sur Partager puis « Sur l'écran d'accueil ».", "Tap Share, then \"Add to Home Screen\"."),
        "offline.title" to T("אתה במצב לא מקוון", "Vous êtes hors-ligne", "You are offline"),
        "offline.body" to T("מועד ממשיך לחשב את הזמנים והתאריכים גם ללא חיבור. התחבר מחדש לשאר.", "Moed continue de calculer vos zmanim et dates hors-ligne. Reconnectez-vous pour le reste.", "Moed keeps computing your zmanim and dates offline. Reconnect for the rest."),
    )

    /** Hebrew month name, leap-aware. `hebMonth` is Nisan-based (1 = Nisan .. 13 = Adar II). */
    fun monthName(hebMonth: Int, hebYear: Int, lang: String): String {
        val key = when (hebMonth) {
            1 -> "Nisan"
            2 -> "Iyyar"
            3 -> "Sivan"
            4 -> "Tamuz"
            5 -> "Av"
            6 -> "Elul"
            7 -> "Tishrei"
            8 -> "Cheshvan"
            9 -> "Kislev"
            10 -> "Tevet"
            11 -> "Sh'vat"
            12 -> if (isHebrewLeapYear(hebYear)) "Adar I" else "Adar"
            13 -> "Adar II"
            else -> "Nisan"
        }
        return MONTH_NAMES[key]?.let { pick(it, lang) } ?: key
    }

    /** Localized weekday name for a Gregorian date. */
    fun weekday(date: LocalDate, lang: String): String {
        // LocalDate: MONDAY=1 .. SUNDAY=7 → table index where 0 = Sunday.
        val index = date.dayOfWeek.value % 7
        return pick(WEEKDAY_NAMES[index], lang)
    }

    /** Label for one of the 16 zmanim keys (falls back to the key itself). */
    fun zmanLabel(key: String, lang: String): String =
        ZMAN_LABELS[key]?.let { pick(it, lang) } ?: key

    /**
     * Localize a canonical (English) holiday name coming from the engine.
     * Unknown names are returned unchanged; any English suffix on a known base
     * (e.g. "Sukkot I", "Chanukah: 2 Candles") is preserved.
     */
    fun holidayName(canonicalEn: String, lang: String): String {
        if (lang == "en") return canonicalEn
        val match = HOLIDAY_NAMES.firstOrNull {
            canonicalEn == it.first || canonicalEn.startsWith(it.first)
        } ?: return canonicalEn
        val base = pick(match.second, lang)
        val remainder = canonicalEn.removePrefix(match.first)
        return (base + remainder).trim()
    }

    /**
     * UI string by `"section.key"`. Occurrences of `{placeholder}` are replaced,
     * in textual order, by [args] (`string("home.omerDay", lang, 33)`,
     * `string("zmanim.pageTitle", lang, "Paris")`). Unknown keys return the key.
     */
    fun string(key: String, lang: String, vararg args: Any): String {
        val t = STRINGS[key] ?: return key
        var out = pick(t, lang)
        if (args.isNotEmpty()) {
            var i = 0
            out = PLACEHOLDER.replace(out) { m ->
                if (i < args.size) args[i++].toString() else m.value
            }
        }
        return out
    }

    /** BCP-47 tag for date/number formatting. */
    fun localeTag(lang: String): String = when (lang) {
        "he" -> "he-IL"
        "fr" -> "fr-FR"
        else -> "en-US"
    }

    /** True when the language is written right-to-left (Hebrew). */
    fun isRtl(lang: String): Boolean = lang == "he"

    /**
     * Resolve a stored language setting to a concrete language.
     * `"system"` maps the device locale → he/fr/en, falling back to `en`.
     */
    fun resolve(languageSetting: String): String {
        val l = languageSetting.lowercase()
        if (l == "he" || l == "fr" || l == "en") return l
        // "system" (or anything unexpected) → device language.
        return when (Locale.getDefault().language) {
            "he", "iw" -> "he"   // "iw" = legacy Hebrew code
            "fr" -> "fr"
            "en" -> "en"
            else -> "en"
        }
    }

    /** Hebrew leap year: 7 of every 19 years (Adar I/II). */
    private fun isHebrewLeapYear(year: Int): Boolean = ((7 * year + 1) % 19) < 7

    private val PLACEHOLDER = Regex("\\{[^}]*\\}")
}
