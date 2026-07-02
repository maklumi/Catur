package com.github.maklumi.catur.domain.chess.piece

enum class PieceColor {
    WHITE, BLACK;

    fun opposite() =
        when (this) {
            WHITE -> BLACK
            BLACK -> WHITE
        }
}
