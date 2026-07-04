package com.github.maklumi.catur.domain.chess.logic

data class Opening(val name: String, val code: String)

object OpeningBook {
    private val openings = mapOf(
        "e2e4" to Opening("King's Pawn Opening", "B00"),
        "e2e4 e7e5" to Opening("Open Game", "C20"),
        "e2e4 e7e5 g1f3" to Opening("King's Knight Opening", "C40"),
        "e2e4 e7e5 g1f3 b8c6" to Opening("King's Knight Opening: Normal Variation", "C44"),
        "e2e4 e7e5 g1f3 b8c6 f1b5" to Opening("Ruy Lopez", "C60"),
        "e2e4 e7e5 g1f3 b8c6 f1c4" to Opening("Italian Game", "C50"),
        "e2e4 e7e5 g1f3 b8c6 d2d4" to Opening("Scotch Game", "C44"),
        "e2e4 e7e5 f2f4" to Opening("King's Gambit", "C30"),
        "e2e4 c7c5" to Opening("Sicilian Defense", "B20"),
        "e2e4 c7c5 g1f3 d7d6 d2d4 c5d4 f3d4 g8f6 b1c3 a7a6" to Opening("Sicilian Defense: Najdorf Variation", "B90"),
        "e2e4 c7c5 g1f3 e7e6 d2d4 c5d4 f3d4" to Opening("Sicilian Defense: Kan Variation", "B41"),
        "e2e4 e7e5 g1f3 b8c6 f1b5 a7a6 b5a4 g8f6 O-O f8e7" to Opening("Ruy Lopez: Closed Defense", "C84"),
        "e2e4 e7e5 g1f3 b8c6 f1b5 a7a6 b5a4 g8f6 O-O b7b5 a4b3 f8e7" to Opening("Ruy Lopez: Marshall Attack", "C89"),
        "d2d4 d7d5 c2c4 e7e6 b1c3 g8f6 c1g5 f8e7 e2e3 O-O g1f3" to Opening("Queen's Gambit Declined: Orthodox Defense", "D60"),
        "d2d4 d7d5 c2c4 c7c6 g1f3 g8f6 b1c3 e7e6" to Opening("Semi-Slav Defense", "D45"),
        "d2d4 g8f6 c2c4 g7g6 b1c3 f8g7 e2e4 d7d6 g1f3 O-O" to Opening("King's Indian Defense: Classical Variation", "E91"),
        "d2d4 g8f6 c2c4 e7e6 b1c3 f8b4" to Opening("Nimzo-Indian Defense", "E20"),
        "e2e4 e7e6" to Opening("French Defense", "C00"),
        "e2e4 c7c6" to Opening("Caro-Kann Defense", "B10"),
        "e2e4 g7g6" to Opening("Modern Defense", "B06"),
        "d2d4" to Opening("Queen's Pawn Opening", "D00"),
        "d2d4 d7d5" to Opening("Closed Game", "D00"),
        "d2d4 d7d5 c2c4" to Opening("Queen's Gambit", "D06"),
        "d2d4 g8f6" to Opening("Indian Defense", "A45"),
        "d2d4 g8f6 c2c4 e7e6 g1f3" to Opening("Bogo-Indian Defense", "E11"),
        "d2d4 g8f6 c2c4 g7g6" to Opening("King's Indian Defense", "E60"),
        "c2c4" to Opening("English Opening", "A10"),
        "g1f3" to Opening("Réti Opening", "A04"),
        "f2f4" to Opening("Bird's Opening", "A02"),
        "b2b3" to Opening("Larsen's Opening", "A01"),
    )

    fun getOpening(uciMoves: List<String>): Opening? {
        val fullLine = uciMoves.joinToString(" ")
        var currentMatch: Opening? = null
        var maxMoves = -1

        openings.forEach { (sequence, opening) ->
            if (fullLine.startsWith(sequence)) {
                val moveCount = sequence.split(" ").size
                if (moveCount > maxMoves) {
                    maxMoves = moveCount
                    currentMatch = opening
                }
            }
        }
        return currentMatch
    }

    fun getOpeningName(uciMoves: List<String>): String? = getOpening(uciMoves)?.name
}
