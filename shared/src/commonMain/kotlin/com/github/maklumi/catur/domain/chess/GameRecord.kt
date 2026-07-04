package com.github.maklumi.catur.domain.chess

data class GameRecord(
    val id: String,
    val date: String,
    val white: String,
    val black: String,
    val result: String,
    val opening: String?,
    val pgn: String
)
