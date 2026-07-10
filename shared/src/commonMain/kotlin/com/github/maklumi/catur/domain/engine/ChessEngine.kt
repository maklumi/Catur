package com.github.maklumi.catur.domain.engine

interface ChessEngine {
    suspend fun getBestMove(moves: List<String>, model: String, fen: String? = null): String?
    suspend fun getTopMoves(moves: List<String>, model: String, count: Int, fen: String? = null): List<Pair<String, Int>> // returns UCI to score
    suspend fun evaluate(moves: List<String>, model: String, fen: String? = null): Int // returns score in centipawns
    fun stop()
}
