package com.github.maklumi.catur.model.piece

enum class PieceColor {
    WHITE, BLACK;

    fun opposite() =
        when (this) {
            WHITE -> BLACK
            BLACK -> WHITE
        }
}


