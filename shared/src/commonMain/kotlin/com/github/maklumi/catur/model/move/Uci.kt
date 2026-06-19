package com.github.maklumi.catur.model.move

fun PrimaryMove.toUciString(): String {
    val base = "${from}${to}"
    return if (this is PromotionMove) {
        base + promotedPiece.textSymbol.lowercase()
    } else {
        base
    }
}
