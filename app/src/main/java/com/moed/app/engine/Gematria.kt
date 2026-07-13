package com.moed.app.engine

/**
 * Gematria — the standard numeric value (mispar hechrachi) of a Hebrew word.
 *
 * Uses the absolute values of the 22 letters, with final (sofit) letters counted
 * the same as their non-final form (standard mispar hechrachi). Nikud, spaces,
 * punctuation and non-Hebrew characters are ignored. Pure, deterministic, offline.
 *
 * UI is scheduled for v2 (CONTRACTS §2), but the computation lives here so the
 * converter/tools screens can consume it without a stub.
 */
object Gematria {

    /** Result of a gematria computation: the total plus the per-letter breakdown. */
    data class GematriaResult(
        val total: Int,
        /** Each counted Hebrew letter paired with its value, in reading order. */
        val letters: List<Pair<Char, Int>>,
    )

    /** Mispar hechrachi values for each Hebrew consonant (final forms folded in). */
    private val LETTER_VALUES: Map<Char, Int> = mapOf(
        'א' to 1, 'ב' to 2, 'ג' to 3, 'ד' to 4, 'ה' to 5, 'ו' to 6, 'ז' to 7,
        'ח' to 8, 'ט' to 9, 'י' to 10,
        'כ' to 20, 'ך' to 20, 'ל' to 30, 'מ' to 40, 'ם' to 40, 'נ' to 50, 'ן' to 50,
        'ס' to 60, 'ע' to 70, 'פ' to 80, 'ף' to 80, 'צ' to 90, 'ץ' to 90,
        'ק' to 100, 'ר' to 200, 'ש' to 300, 'ת' to 400,
    )

    /**
     * Compute the standard gematria (mispar hechrachi) of a Hebrew string.
     * Characters without a gematria value are skipped and do not appear in
     * [GematriaResult.letters].
     *
     * Examples: "שלום" → 376, "חי" → 18.
     */
    fun compute(word: String): GematriaResult {
        val letters = ArrayList<Pair<Char, Int>>(word.length)
        var total = 0
        for (ch in word) {
            val value = LETTER_VALUES[ch] ?: continue
            total += value
            letters.add(ch to value)
        }
        return GematriaResult(total, letters)
    }
}
