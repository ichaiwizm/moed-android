package com.moed.app.engine

import com.kosherjava.zmanim.hebrewcalendar.JewishCalendar

/**
 * Static haftara table (parasha → weekly haftara), Ashkenazi + Sephardic minhagim.
 *
 * KosherJava resolves the weekly *parsha* but ships no haftara data, so this table
 * supplies the standard readings for all 54 parshiot, the seven common combined
 * readings, and the special-Shabbat overrides (Shekalim, Zachor, Parah, HaChodesh,
 * HaGadol, Shuva). Book names are localized (he / fr / en); the chapter:verse spans
 * are language-neutral and rendered LTR after the book name.
 *
 * References follow the widely accepted lists (Ashkenazi default; Sephardic given
 * where it differs). Sources: standard printed chumashim / Sefaria. This is data,
 * not runtime computation — pure and offline.
 */
internal object HaftarahTable {

    /** Books of Neviim used by the haftara cycle, with localized names. */
    private enum class Book(val he: String, val fr: String, val en: String) {
        JOSHUA("יְהוֹשֻׁעַ", "Josué", "Joshua"),
        JUDGES("שׁוֹפְטִים", "Juges", "Judges"),
        SAMUEL1("שְׁמוּאֵל א׳", "Samuel I", "I Samuel"),
        SAMUEL2("שְׁמוּאֵל ב׳", "Samuel II", "II Samuel"),
        KINGS1("מְלָכִים א׳", "Rois I", "I Kings"),
        KINGS2("מְלָכִים ב׳", "Rois II", "II Kings"),
        ISAIAH("יְשַׁעְיָהוּ", "Isaïe", "Isaiah"),
        JEREMIAH("יִרְמְיָהוּ", "Jérémie", "Jeremiah"),
        EZEKIEL("יְחֶזְקֵאל", "Ézéchiel", "Ezekiel"),
        HOSEA("הוֹשֵׁעַ", "Osée", "Hosea"),
        JOEL("יוֹאֵל", "Joël", "Joel"),
        AMOS("עָמוֹס", "Amos", "Amos"),
        OBADIAH("עוֹבַדְיָה", "Abdias", "Obadiah"),
        MICAH("מִיכָה", "Michée", "Micah"),
        ZEPHANIAH("צְפַנְיָה", "Sophonie", "Zephaniah"),
        ZECHARIAH("זְכַרְיָה", "Zacharie", "Zechariah"),
        MALACHI("מַלְאָכִי", "Malachie", "Malachi");

        fun name(lang: String): String = when (lang) {
            "he" -> he
            "fr" -> fr
            else -> en
        }
    }

    /**
     * A haftara reading. [range] is the Ashkenazi span; [sephRange] (and optional
     * [sephBook]) override it for the Sephardic minhag when they differ.
     */
    private data class Ref(
        val book: Book,
        val range: String,
        val sephRange: String? = null,
        val sephBook: Book? = null,
        /** Optional second segment (e.g. Shabbat Shuva: Hosea + Joel/Micah), localized. */
        val extra: Ref? = null,
    ) {
        fun render(lang: String, sefarade: Boolean): String {
            val b = if (sefarade && sephBook != null) sephBook else book
            val r = if (sefarade && sephRange != null) sephRange else range
            val head = "${b.name(lang)} $r"
            return if (extra != null) "$head; ${extra.render(lang, sefarade)}" else head
        }
    }

    // en-dash for verse spans
    private val WEEKLY: Map<JewishCalendar.Parsha, Ref> = mapOf(
        JewishCalendar.Parsha.BERESHIS to Ref(Book.ISAIAH, "42:5–43:10", sephRange = "42:5–21"),
        JewishCalendar.Parsha.NOACH to Ref(Book.ISAIAH, "54:1–55:5", sephRange = "54:1–10"),
        JewishCalendar.Parsha.LECH_LECHA to Ref(Book.ISAIAH, "40:27–41:16"),
        JewishCalendar.Parsha.VAYERA to Ref(Book.KINGS2, "4:1–37", sephRange = "4:1–23"),
        JewishCalendar.Parsha.CHAYEI_SARA to Ref(Book.KINGS1, "1:1–31"),
        JewishCalendar.Parsha.TOLDOS to Ref(Book.MALACHI, "1:1–2:7"),
        JewishCalendar.Parsha.VAYETZEI to Ref(Book.HOSEA, "12:13–14:10", sephRange = "11:7–12:12"),
        JewishCalendar.Parsha.VAYISHLACH to Ref(Book.OBADIAH, "1:1–21"),
        JewishCalendar.Parsha.VAYESHEV to Ref(Book.AMOS, "2:6–3:8"),
        JewishCalendar.Parsha.MIKETZ to Ref(Book.KINGS1, "3:15–4:1"),
        JewishCalendar.Parsha.VAYIGASH to Ref(Book.EZEKIEL, "37:15–28"),
        JewishCalendar.Parsha.VAYECHI to Ref(Book.KINGS1, "2:1–12"),
        JewishCalendar.Parsha.SHEMOS to Ref(Book.ISAIAH, "27:6–28:13, 29:22–23", sephRange = "1:1–2:3", sephBook = Book.JEREMIAH),
        JewishCalendar.Parsha.VAERA to Ref(Book.EZEKIEL, "28:25–29:21"),
        JewishCalendar.Parsha.BO to Ref(Book.JEREMIAH, "46:13–28"),
        JewishCalendar.Parsha.BESHALACH to Ref(Book.JUDGES, "4:4–5:31", sephRange = "5:1–31"),
        JewishCalendar.Parsha.YISRO to Ref(Book.ISAIAH, "6:1–7:6, 9:5–6", sephRange = "6:1–13"),
        JewishCalendar.Parsha.MISHPATIM to Ref(Book.JEREMIAH, "34:8–22, 33:25–26"),
        JewishCalendar.Parsha.TERUMAH to Ref(Book.KINGS1, "5:26–6:13"),
        JewishCalendar.Parsha.TETZAVEH to Ref(Book.EZEKIEL, "43:10–27"),
        JewishCalendar.Parsha.KI_SISA to Ref(Book.KINGS1, "18:1–39", sephRange = "18:20–39"),
        JewishCalendar.Parsha.VAYAKHEL to Ref(Book.KINGS1, "7:40–50", sephRange = "7:13–26"),
        JewishCalendar.Parsha.PEKUDEI to Ref(Book.KINGS1, "7:51–8:21", sephRange = "7:40–50"),
        JewishCalendar.Parsha.VAYIKRA to Ref(Book.ISAIAH, "43:21–44:23"),
        JewishCalendar.Parsha.TZAV to Ref(Book.JEREMIAH, "7:21–8:3, 9:22–23"),
        JewishCalendar.Parsha.SHMINI to Ref(Book.SAMUEL2, "6:1–7:17", sephRange = "6:1–19"),
        JewishCalendar.Parsha.TAZRIA to Ref(Book.KINGS2, "4:42–5:19"),
        JewishCalendar.Parsha.METZORA to Ref(Book.KINGS2, "7:3–20"),
        JewishCalendar.Parsha.ACHREI_MOS to Ref(Book.AMOS, "9:7–15", sephRange = "22:1–16", sephBook = Book.EZEKIEL),
        JewishCalendar.Parsha.KEDOSHIM to Ref(Book.EZEKIEL, "22:1–19", sephRange = "20:2–20"),
        JewishCalendar.Parsha.EMOR to Ref(Book.EZEKIEL, "44:15–31"),
        JewishCalendar.Parsha.BEHAR to Ref(Book.JEREMIAH, "32:6–27"),
        JewishCalendar.Parsha.BECHUKOSAI to Ref(Book.JEREMIAH, "16:19–17:14"),
        JewishCalendar.Parsha.BAMIDBAR to Ref(Book.HOSEA, "2:1–22"),
        JewishCalendar.Parsha.NASSO to Ref(Book.JUDGES, "13:2–25"),
        JewishCalendar.Parsha.BEHAALOSCHA to Ref(Book.ZECHARIAH, "2:14–4:7"),
        JewishCalendar.Parsha.SHLACH to Ref(Book.JOSHUA, "2:1–24"),
        JewishCalendar.Parsha.KORACH to Ref(Book.SAMUEL1, "11:14–12:22"),
        JewishCalendar.Parsha.CHUKAS to Ref(Book.JUDGES, "11:1–33"),
        JewishCalendar.Parsha.BALAK to Ref(Book.MICAH, "5:6–6:8"),
        JewishCalendar.Parsha.PINCHAS to Ref(Book.KINGS1, "18:46–19:21"),
        JewishCalendar.Parsha.MATOS to Ref(Book.JEREMIAH, "1:1–2:3"),
        JewishCalendar.Parsha.MASEI to Ref(Book.JEREMIAH, "2:4–28, 3:4", sephRange = "2:4–28, 4:1–2"),
        JewishCalendar.Parsha.DEVARIM to Ref(Book.ISAIAH, "1:1–27"), // Shabbat Chazon
        JewishCalendar.Parsha.VAESCHANAN to Ref(Book.ISAIAH, "40:1–26"), // Shabbat Nachamu
        JewishCalendar.Parsha.EIKEV to Ref(Book.ISAIAH, "49:14–51:3"),
        JewishCalendar.Parsha.REEH to Ref(Book.ISAIAH, "54:11–55:5"),
        JewishCalendar.Parsha.SHOFTIM to Ref(Book.ISAIAH, "51:12–52:12"),
        JewishCalendar.Parsha.KI_SEITZEI to Ref(Book.ISAIAH, "54:1–10"),
        JewishCalendar.Parsha.KI_SAVO to Ref(Book.ISAIAH, "60:1–22"),
        JewishCalendar.Parsha.NITZAVIM to Ref(Book.ISAIAH, "61:10–63:9"),
        JewishCalendar.Parsha.VAYEILECH to Ref(Book.ISAIAH, "55:6–56:8"),
        JewishCalendar.Parsha.HAAZINU to Ref(Book.SAMUEL2, "22:1–51"),
        JewishCalendar.Parsha.VZOS_HABERACHA to Ref(Book.JOSHUA, "1:1–18", sephRange = "1:1–9"),
        // --- combined readings (haftara of the governing parsha) ---
        JewishCalendar.Parsha.VAYAKHEL_PEKUDEI to Ref(Book.KINGS1, "7:51–8:21", sephRange = "7:40–50"),
        JewishCalendar.Parsha.TAZRIA_METZORA to Ref(Book.KINGS2, "7:3–20"),
        JewishCalendar.Parsha.ACHREI_MOS_KEDOSHIM to Ref(Book.AMOS, "9:7–15", sephRange = "20:2–20", sephBook = Book.EZEKIEL),
        JewishCalendar.Parsha.BEHAR_BECHUKOSAI to Ref(Book.JEREMIAH, "16:19–17:14"),
        JewishCalendar.Parsha.CHUKAS_BALAK to Ref(Book.MICAH, "5:6–6:8"),
        JewishCalendar.Parsha.MATOS_MASEI to Ref(Book.JEREMIAH, "2:4–28, 3:4", sephRange = "2:4–28, 4:1–2"),
        JewishCalendar.Parsha.NITZAVIM_VAYEILECH to Ref(Book.ISAIAH, "61:10–63:9"),
    )

    /** Special-Shabbat haftara overrides (take precedence over the weekly reading). */
    private val SPECIAL: Map<JewishCalendar.Parsha, Ref> = mapOf(
        JewishCalendar.Parsha.SHKALIM to Ref(Book.KINGS2, "12:1–17", sephRange = "11:17–12:17"),
        JewishCalendar.Parsha.ZACHOR to Ref(Book.SAMUEL1, "15:2–34", sephRange = "15:1–34"),
        JewishCalendar.Parsha.PARA to Ref(Book.EZEKIEL, "36:16–38", sephRange = "36:16–36"),
        JewishCalendar.Parsha.HACHODESH to Ref(Book.EZEKIEL, "45:16–46:18", sephRange = "45:18–46:15"),
        JewishCalendar.Parsha.HAGADOL to Ref(Book.MALACHI, "3:4–24"),
        JewishCalendar.Parsha.SHUVA to Ref(Book.HOSEA, "14:2–10", extra = Ref(Book.JOEL, "2:15–27", sephRange = "7:18–20", sephBook = Book.MICAH)),
    )

    // Calendar-position overrides not exposed by KosherJava's getSpecialShabbos().
    private val SHABBAT_ROSH_CHODESH = Ref(Book.ISAIAH, "66:1–24")
    private val MACHAR_CHODESH = Ref(Book.SAMUEL1, "20:18–42")
    private val CHANUKAH = Ref(Book.ZECHARIAH, "2:14–4:7")
    private val CHANUKAH_DAY8 = Ref(Book.KINGS1, "7:40–50")
    /** Pinchas read during the Three Weeks takes the first haftara of affliction. */
    private val PINCHAS_THREE_WEEKS = Ref(Book.JEREMIAH, "1:1–2:3")

    /** Parshiot whose fixed haftara stands over an actual Shabbat Rosh Chodesh. */
    private val KEEPS_WEEKLY_ON_ROSH_CHODESH: Set<JewishCalendar.Parsha> = setOf(
        JewishCalendar.Parsha.MASEI, JewishCalendar.Parsha.MATOS_MASEI,
    )

    /**
     * Parshiot whose fixed haftara stands over Machar Chodesh — the affliction /
     * consolation cycle keeps its reading on erev Rosh Chodesh (but Isaiah 66 still
     * wins on the Rosh Chodesh Shabbat itself, hence the smaller set above).
     */
    private val KEEPS_WEEKLY_ON_MACHAR_CHODESH: Set<JewishCalendar.Parsha> = setOf(
        JewishCalendar.Parsha.MASEI, JewishCalendar.Parsha.MATOS_MASEI,
        JewishCalendar.Parsha.VAESCHANAN, JewishCalendar.Parsha.EIKEV, JewishCalendar.Parsha.REEH,
        JewishCalendar.Parsha.SHOFTIM, JewishCalendar.Parsha.KI_SEITZEI, JewishCalendar.Parsha.KI_SAVO,
        JewishCalendar.Parsha.NITZAVIM,
    )

    /**
     * Localized haftara reference for a Shabbat, applying the halakhic precedence:
     * arba-parshiyot / HaGadol / Shuva (special) → Shabbat Chanukah → Shabbat Rosh
     * Chodesh (except Masei / Matot-Masei, which keep their weekly reading) → Machar
     * Chodesh → the weekly haftara.
     *
     * @param parsha the weekly parsha (may be a combined enum value).
     * @param special the special-Shabbat parsha from `getSpecialShabbos()`, or NONE.
     * @param sefarade true for the Sephardic minhag.
     * @param isChanukah true when the Shabbat falls during Chanukah.
     * @param chanukahDay day of Chanukah (1..8), or ≤ 0 when not Chanukah.
     * @param isRoshChodesh true when the Shabbat itself is Rosh Chodesh.
     * @param isMacharChodesh true when the following day is Rosh Chodesh (erev).
     * Returns an empty string when no haftara maps (e.g. a Yom-Tov week with no parsha).
     */
    fun haftara(
        parsha: JewishCalendar.Parsha,
        special: JewishCalendar.Parsha,
        sefarade: Boolean,
        lang: String,
        isChanukah: Boolean = false,
        chanukahDay: Int = 0,
        isRoshChodesh: Boolean = false,
        isMacharChodesh: Boolean = false,
        afterSeventeenTammuz: Boolean = false,
    ): String {
        if (special == JewishCalendar.Parsha.SHUVA) {
            // Shabbat Shuva's second segment: Joel with Ha'azinu, Micah with Vayeilech;
            // the Sephardic minhag always reads Micah (forced by rendering as Sephardic).
            val withVayeilech = parsha == JewishCalendar.Parsha.VAYEILECH ||
                parsha == JewishCalendar.Parsha.NITZAVIM_VAYEILECH
            return SPECIAL.getValue(JewishCalendar.Parsha.SHUVA).render(lang, sefarade || withVayeilech)
        }
        SPECIAL[special]?.let { return it.render(lang, sefarade) }
        if (isChanukah) {
            val ref = if (chanukahDay == 8) CHANUKAH_DAY8 else CHANUKAH
            return ref.render(lang, sefarade)
        }
        // Pinchas read after 17 Tammuz reads the first haftara of affliction.
        if (parsha == JewishCalendar.Parsha.PINCHAS && afterSeventeenTammuz) {
            return PINCHAS_THREE_WEEKS.render(lang, sefarade)
        }
        if (isRoshChodesh && parsha !in KEEPS_WEEKLY_ON_ROSH_CHODESH) {
            return SHABBAT_ROSH_CHODESH.render(lang, sefarade)
        }
        if (isMacharChodesh && parsha !in KEEPS_WEEKLY_ON_MACHAR_CHODESH) {
            return MACHAR_CHODESH.render(lang, sefarade)
        }
        val ref = WEEKLY[parsha] ?: return ""
        return ref.render(lang, sefarade)
    }
}
