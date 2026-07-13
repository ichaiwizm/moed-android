package com.moed.app.engine

/**
 * Frozen catalogue of the 16 zmanim keys and their shitot (opinions).
 *
 * The order of [all] is the canonical display order defined in NATIVE_SPEC §3.3.
 * Each [ZmanDef] pairs a stable machine `key` with a language-neutral technical
 * `shitaLabelKey` (e.g. "16.1°", "GRA", "MGA 72 min"). The KosherJava getter that
 * actually computes each key is resolved internally by [ZmanimEngine]; this object
 * intentionally carries no I/O and no dependency on Android or KosherJava so it can
 * be referenced from any layer (UI, widgets, engine).
 *
 * [home] is the ordered subset shown on the "Today" screen (HOME_ZMANIM).
 *
 * These are the only zman keys the app knows about — nothing is invented at
 * runtime, everything is validated against the reference matrix (NATIVE_SPEC §3.6).
 */
object ZmanimCatalog {

    /** A single zman definition: its stable key + the shita label displayed to the user. */
    data class ZmanDef(val key: String, val shitaLabelKey: String)

    // --- Individual keys (single source of truth for string keys) -----------------

    const val ALOT_HASHACHAR = "alotHashachar"
    const val ALOT_HASHACHAR_72 = "alotHashachar72"
    const val MISHEYAKIR = "misheyakir"
    const val MISHEYAKIR_MACHMIR = "misheyakirMachmir"
    const val HANETZ_HACHAMA = "hanetzHachama"
    const val SOF_ZMAN_SHMA_MGA = "sofZmanShmaMGA"
    const val SOF_ZMAN_SHMA_GRA = "sofZmanShmaGRA"
    const val SOF_ZMAN_TFILA_MGA = "sofZmanTfilaMGA"
    const val SOF_ZMAN_TFILA_GRA = "sofZmanTfilaGRA"
    const val CHATZOT = "chatzot"
    const val MINCHA_GEDOLA = "minchaGedola"
    const val MINCHA_KETANA = "minchaKetana"
    const val PLAG_HAMINCHA = "plagHamincha"
    const val SHKIA_HACHAMA = "shkiaHachama"
    const val TZEIT_HAKOCHAVIM = "tzeitHakochavim"
    const val TZEIT_72 = "tzeit72"

    /** The 16 zmanim in canonical spec order (NATIVE_SPEC §3.3). */
    val all: List<ZmanDef> = listOf(
        // --- Dawn ---
        ZmanDef(ALOT_HASHACHAR, "16.1°"),
        ZmanDef(ALOT_HASHACHAR_72, "72 min"),
        ZmanDef(MISHEYAKIR, "11.5°"),
        ZmanDef(MISHEYAKIR_MACHMIR, "10.2°"),
        // --- Sunrise (netz / hanetz) ---
        ZmanDef(HANETZ_HACHAMA, "sunrise"),
        // --- Latest Shema ---
        ZmanDef(SOF_ZMAN_SHMA_MGA, "MGA 72 min"),
        ZmanDef(SOF_ZMAN_SHMA_GRA, "GRA"),
        // --- Latest Shacharit (tefila) ---
        ZmanDef(SOF_ZMAN_TFILA_MGA, "MGA 72 min"),
        ZmanDef(SOF_ZMAN_TFILA_GRA, "GRA"),
        // --- Midday ---
        ZmanDef(CHATZOT, "astronomical noon"),
        // --- Afternoon ---
        ZmanDef(MINCHA_GEDOLA, "GRA (0.5 shaʿah)"),
        ZmanDef(MINCHA_KETANA, "GRA (9.5 shaʿot)"),
        ZmanDef(PLAG_HAMINCHA, "GRA (10.75 shaʿot)"),
        // --- Sunset (shkia) ---
        ZmanDef(SHKIA_HACHAMA, "sunset"),
        // --- Nightfall (tzeit) ---
        ZmanDef(TZEIT_HAKOCHAVIM, "8.5° (3 stars)"),
        ZmanDef(TZEIT_72, "72 min (Rabbeinu Tam)"),
    )

    /** Keys of the zmanim shown on the "Today" screen, in display order. */
    val home: List<String> = listOf(
        ALOT_HASHACHAR,
        HANETZ_HACHAMA,
        SOF_ZMAN_SHMA_GRA,
        CHATZOT,
        MINCHA_GEDOLA,
        SHKIA_HACHAMA,
        TZEIT_HAKOCHAVIM,
    )

    /** [ZmanDef] for a given key, or null if the key is unknown. */
    fun def(key: String): ZmanDef? = all.firstOrNull { it.key == key }
}
