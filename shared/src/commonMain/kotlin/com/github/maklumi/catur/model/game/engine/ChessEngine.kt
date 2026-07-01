package com.github.maklumi.catur.model.game.engine

interface ChessEngine {
    suspend fun getBestMove(moves: List<String>, model: String, fen: String? = null): String?
    suspend fun evaluate(moves: List<String>, fen: String? = null): Int // returns score in centipawns
    fun stop()
}
