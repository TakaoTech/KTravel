/*
 * Ported from nbvcxz (me.gosimple.nbvcxz.resources.Dictionary and DictionaryUtil).
 * Copyright (c) 2017, Adam Brusselback. MIT licence — see LICENSE-nbvcxz.txt.
 */
package com.takaotech.password.resources

/**
 * A word list with a rank per entry: the lower the rank, the more common the word and the cheaper
 * it is to guess. Keys must be lower case for matching to work.
 *
 * @property exclusion when true, anything matching this dictionary is worth zero entropy — used for
 *  words the caller wants outright banned rather than merely penalised.
 */
internal class Dictionary(val dictionaryName: String, val dictionary: Map<String, Int>, val exclusion: Boolean) {

    /**
     * Entries ordered by length, then alphabetically. The edit-distance search walks this instead of
     * the whole map so it can stop once the candidates get too long.
     */
    val sortedDictionary: List<String> = dictionary.keys.sortedWith(
        compareBy({ it.length }, { it }),
    )

    /** For a given length, the first index in [sortedDictionary] where entries of that length start. */
    val sortedDictionaryLengthLookup: Map<Int, Int>

    /** Longest entry worth checking against this dictionary. */
    val maxLength: Int

    init {
        val lookup = mutableMapOf<Int, Int>()
        sortedDictionary.forEachIndexed { index, entry ->
            // The list is sorted by length, so the first index seen for a length is where it starts.
            if (!lookup.containsKey(entry.length)) lookup[entry.length] = index
        }

        // Fill the gaps so a lookup for a length no entry has still lands on the first longer entry.
        // The bound is re-read every iteration because the body grows the map, which is also what
        // decides maxLength — kept as upstream, since both are load-bearing.
        var lastLength = 0
        var length = 0
        while (length < lookup.size) {
            if (!lookup.containsKey(length)) {
                var nextKey = length
                while (!lookup.containsKey(nextKey)) {
                    nextKey++
                }
                lookup[length] = lookup.getValue(nextKey)
            }
            lastLength = length
            length++
        }

        sortedDictionaryLengthLookup = lookup
        maxLength = lastLength
    }
}

/** Builds the [Dictionary] instances out of the generated word lists. */
internal object Dictionaries {

    /**
     * Ranked: position in the file is the rank, most common first.
     *
     * Upstream reads these from resources; here they come from constants generated at build time
     * by the `generateDictionarySources` task, which is what keeps the module free of any
     * platform-specific resource loading.
     */
    private fun ranked(name: String, lines: List<String>): Dictionary =
        Dictionary(name, lines.withIndex().associate { (index, word) -> word to index + 1 }, false)

    /** Unranked: every entry gets the same rank, half the list size, as upstream does. */
    private fun unranked(name: String, lines: List<String>): Dictionary {
        val distinct = lines.toSet()
        val rank = lines.size / 2
        return Dictionary(name, distinct.associateWith { rank }, false)
    }

    /**
     * Materialised on first use and kept for the lifetime of the process: parsing all six lists is
     * the expensive part of an estimation, and it does not depend on the password.
     */
    val defaults: List<Dictionary> by lazy(LazyThreadSafetyMode.SYNCHRONIZED) {
        listOf(
            ranked("passwords", PasswordsDictionary.lines()),
            ranked("male_names", MaleNamesDictionary.lines()),
            ranked("female_names", FemaleNamesDictionary.lines()),
            ranked("surnames", SurnamesDictionary.lines()),
            ranked("english", EnglishDictionary.lines()),
            unranked("eff_large", EffLargeDictionary.lines()),
        )
    }

    /** Values the attacker plausibly knows, ranked as if they were the most common words there are. */
    fun userInputs(inputs: List<String>): Dictionary =
        ranked("user_inputs", inputs.map { it.lowercase() }.filter(String::isNotEmpty).distinct())
}
