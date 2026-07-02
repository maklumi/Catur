package com.github.maklumi.catur.domain.chess.logic

object OpeningBook {
    private val openings = mapOf(
        "e2e4" to "King's Pawn Opening",
        "e2e4 e7e5" to "Open Game",
        "e2e4 e7e5 g1f3" to "King's Knight Opening",
        "e2e4 e7e5 g1f3 b8c6" to "King's Knight Opening: Normal Variation",
        "e2e4 e7e5 g1f3 b8c6 f1b5" to "Ruy Lopez",
        "e2e4 e7e5 g1f3 b8c6 f1c4" to "Italian Game",
        "e2e4 e7e5 g1f3 b8c6 d2d4" to "Scotch Game",
        "e2e4 e7e5 f2f4" to "King's Gambit",
        "e2e4 c7c5 g1f3 d7d6 d2d4 c5d4 f3d4 g8f6 b1c3 a7a6" to "Sicilian Defense: Najdorf Variation",
        "e2e4 c7c5 g1f3 e7e6 d2d4 c5d4 f3d4" to "Sicilian Defense: Kan Variation",
        "e2e4 e7e5 g1f3 b8c6 f1b5 a7a6 b5a4 g8f6 O-O f8e7" to "Ruy Lopez: Closed Defense",
        "e2e4 e7e5 g1f3 b8c6 f1b5 a7a6 b5a4 g8f6 O-O b7b5 a4b3 f8e7" to "Ruy Lopez: Marshall Attack",
        "d2d4 d7d5 c2c4 e7e6 b1c3 g8f6 c1g5 f8e7 e2e3 O-O g1f3" to "Queen's Gambit Declined: Orthodox Defense",
        "d2d4 d7d5 c2c4 c7c6 g1f3 g8f6 b1c3 e7e6" to "Semi-Slav Defense",
        "d2d4 g8f6 c2c4 g7g6 b1c3 f8g7 e2e4 d7d6 g1f3 O-O" to "King's Indian Defense: Classical Variation",
        "d2d4 g8f6 c2c4 e7e6 b1c3 f8b4" to "Nimzo-Indian Defense",
        "e2e4 e7e6" to "French Defense",
        "e2e4 c7c6" to "Caro-Kann Defense",
        "e2e4 g7g6" to "Modern Defense",
        "d2d4" to "Queen's Pawn Opening",
        "d2d4 d7d5" to "Closed Game",
        "d2d4 d7d5 c2c4" to "Queen's Gambit",
        "d2d4 g8f6" to "Indian Defense",
        "d2d4 g8f6 c2c4 e7e6 g1f3" to "Bogo-Indian Defense",
        "d2d4 g8f6 c2c4 g7g6" to "King's Indian Defense",
        "c2c4" to "English Opening",
        "g1f3" to "Réti Opening",
        "f2f4" to "Bird's Opening",
        "b2b3" to "Larsen's Opening",
    )

    fun getOpeningName(uciMoves: List<String>): String? {
        val fullLine = uciMoves.joinToString(" ")
        // Find the longest match
        var currentMatch: String? = null
        var maxMoves = -1

        openings.forEach { (sequence, name) ->
            if (fullLine.startsWith(sequence)) {
                val moveCount = sequence.split(" ").size
                if (moveCount > maxMoves) {
                    maxMoves = moveCount
                    currentMatch = name
                }
            }
        }
        return currentMatch
    }
}
