package com.github.maklumi.catur.model.board

enum class File {
    a, b, c, d, e, f, g, h
}

operator fun File.get(rank: Int): Position =
    Position.entries[this.ordinal * 8 + (rank - 1)]

