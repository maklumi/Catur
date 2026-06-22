package com.github.maklumi.catur.model.game.engine

interface ChessEngine {
    suspend fun getBestMove(moves: List<String>, model: String): String?
    suspend fun evaluate(moves: List<String>): Int // returns score in centipawns
    fun stop()
}
