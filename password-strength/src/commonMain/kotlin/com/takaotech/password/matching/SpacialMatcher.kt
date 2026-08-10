/*
 * Ported from nbvcxz (me.gosimple.nbvcxz.matching.SpacialMatcher).
 * Copyright (c) 2017, Adam Brusselback. MIT licence — see LICENSE-nbvcxz.txt.
 */
package com.takaotech.password.matching

import com.takaotech.password.matching.match.Match
import com.takaotech.password.matching.match.SpacialMatch
import com.takaotech.password.resources.AdjacencyGraph
import com.takaotech.password.resources.AdjacencyGraphUtil
import com.takaotech.password.resources.Configuration

/**
 * Finds walks across the keyboard: `qwerty`, `1qaz2wsx`.
 *
 * A run keeps growing while each character is a neighbour of the next on the graph being tried, and
 * only runs of more than two characters are reported.
 */
internal class SpacialMatcher : PasswordMatcher {

    override fun match(configuration: Configuration, password: String): List<Match> {
        val matches = mutableListOf<Match>()

        configuration.adjacencyGraphs.forEach { graph ->
            val neighbours = password.map { AdjacencyGraphUtil.neighbors(graph, it) }
            matches += matchGraph(graph, password, neighbours)
        }

        return matches
    }

    private fun matchGraph(graph: AdjacencyGraph, password: String, neighbours: List<Set<Char>>): List<Match> {
        val matches = mutableListOf<Match>()
        val run = StringBuilder()

        password.forEachIndexed { index, character ->
            val next = index + 1
            // While the next character can be reached from this one, the run continues.
            if (next < neighbours.size && character in neighbours[next]) {
                run.append(character)
                return@forEachIndexed
            }

            run.append(character)
            if (run.length > MIN_RUN_LENGTH) {
                val token = run.toString()
                matches += SpacialMatch(
                    match = token,
                    startIndex = index - token.length + 1,
                    endIndex = index,
                    adjacencyGraph = graph,
                    turns = AdjacencyGraphUtil.turns(graph, token),
                    shiftedCount = AdjacencyGraphUtil.shifts(graph, token),
                )
            }
            run.setLength(0)
        }

        return matches
    }

    private companion object {
        const val MIN_RUN_LENGTH = 2
    }
}
