package com.github.maklumi.catur.model.game.puzzle

import catur.shared.generated.resources.Res
import org.jetbrains.compose.resources.ExperimentalResourceApi

data class Puzzle(
    val title: String,
    val initialFen: String,
    val solution: String,
    val solutionMoves: List<String> = emptyList(),
    val isCompleted: Boolean = false
)

object PuzzleLoader {
    @OptIn(ExperimentalResourceApi::class)
    suspend fun loadPuzzles(completedIndices: Set<Int> = emptySet()): List<Puzzle> {
        return try {
            val bytes = Res.readBytes("files/m8n4.txt")
            val content = bytes.decodeToString()
            parse(content, completedIndices)
        } catch (_: Exception) {
            emptyList()
        }
    }

    private fun parse(content: String, completedIndices: Set<Int>): List<Puzzle> {
        val lines = content.lines().filter { it.isNotBlank() }
        val puzzles = mutableListOf<Puzzle>()
        
        // The file has a 3-line block per puzzle (Metadata, FEN, Solution)
        // separated by whitespace which we filtered out.
        var puzzleIndex = 0
        for (i in lines.indices step 3) {
            if (i + 2 < lines.size) {
                val solution = lines[i+2].trim()
                puzzles.add(Puzzle(
                    title = lines[i].trim(),
                    initialFen = lines[i+1].trim(),
                    solution = solution,
                    solutionMoves = parseSolution(solution),
                    isCompleted = completedIndices.contains(puzzleIndex)
                ))
                puzzleIndex++
            }
        }
        return puzzles
    }

    private fun parseSolution(solution: String): List<String> {
        // Regex removes "1. ", "1... ", "2. ", etc.
        return solution.replace(Regex("\\d+\\.+\\s*"), " ")
            .split(" ")
            .map { it.trim() }
            .filter { it.isNotBlank() }
    }
}
