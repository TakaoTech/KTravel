/*
 * Ported from nbvcxz (me.gosimple.nbvcxz.resources.AdjacencyGraph and the logic of
 * me.gosimple.nbvcxz.resources.AdjacencyGraphUtil).
 * Copyright (c) 2017, Adam Brusselback. MIT licence — see LICENSE-nbvcxz.txt.
 */
package com.takaotech.password.resources

/**
 * One keyboard or keypad layout: for each key, the keys reachable in each direction.
 *
 * The tables themselves live in [KeyboardGraphs].
 */
internal class AdjacencyGraph(val name: String, val keyMap: Map<Char, Array<String?>>) {

    /**
     * Mean number of neighbours per key. On QWERTY `g` has six, being next to `ftyhbv`, while `\`
     * has one — the average is what prices a spatial pattern.
     */
    val averageDegree: Double by lazy {
        keyMap.values.sumOf { neighbours -> neighbours.count { it != null } }.toDouble() / keyMap.size
    }
}

internal object AdjacencyGraphUtil {

    /** Every character reachable from [key], in any direction, shifted or not. */
    fun neighbors(graph: AdjacencyGraph, key: Char): Set<Char> =
        graph.keyMap[key].orEmpty().filterNotNull().flatMapTo(mutableSetOf()) { it.asIterable() }

    /**
     * How many times the path changes direction. `zxcv` turns once, `zxcvfr` twice.
     *
     * Starts at one because the first straight run already counts as a direction.
     */
    fun turns(graph: AdjacencyGraph, part: String): Int {
        var direction = 0
        var turns = 1

        part.forEachIndexed { index, character ->
            if (index + 1 >= part.length) return@forEachIndexed
            val next = part[index + 1]
            val neighbours = graph.keyMap[character] ?: return@forEachIndexed

            neighbours.forEachIndexed { slot, neighbour ->
                if (neighbour == null) return@forEachIndexed
                neighbour.forEach { neighbourCharacter ->
                    if (next == neighbourCharacter) {
                        if (direction == 0) {
                            direction = slot
                        } else if (direction != slot) {
                            turns++
                            direction = slot
                        }
                    }
                }
            }
        }
        return turns
    }

    /**
     * How many times the path switches between the unshifted and the shifted character of a key —
     * `%` instead of `5`, `A` instead of `a`.
     *
     * The position within a neighbour slot is the shift level, so comparing positions across steps
     * is what detects the switch.
     */
    fun shifts(graph: AdjacencyGraph, part: String): Int {
        var currentShift = -1
        var shifts = 0

        part.forEachIndexed { index, character ->
            if (index + 1 >= part.length) return@forEachIndexed
            val next = part[index + 1]
            val neighbours = graph.keyMap[character] ?: return@forEachIndexed

            neighbours.forEach { neighbour ->
                if (neighbour == null) return@forEach
                neighbour.forEachIndexed { shiftLevel, neighbourCharacter ->
                    if (next == neighbourCharacter) {
                        if (currentShift == -1) {
                            currentShift = shiftLevel
                        } else if (currentShift != shiftLevel) {
                            shifts++
                            currentShift = shiftLevel
                        }
                    }
                }
            }
        }
        return shifts
    }
}
