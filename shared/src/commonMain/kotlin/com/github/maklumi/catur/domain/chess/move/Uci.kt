package com.github.maklumi.catur.domain.chess.move

fun PrimaryMove.toUciString(): String {
    val base = "${from}${to}"
    return if (this is PromotionMove) {
        base + promotedPiece.textSymbol.lowercase()
    } else {
        base
    }
}
