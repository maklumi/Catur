package com.github.maklumi.catur.domain.chess.board


fun idx(file: Int, rank: Int): Int =
    (file - 1) * 8 + (rank - 1)

fun validate(file: Int, rank: Int) {
    require(file >= 1)
    require(file <= 8)
    require(rank >= 1)
    require(rank <= 8)
}

fun Position.isLightSquare(): Boolean =
    (ordinal + file % 2) % 2 == 0

fun Position.isDarkSquare(): Boolean =
    (ordinal + file % 2) % 2 == 1
