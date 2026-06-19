package com.github.maklumi.catur.model.game.engine

interface ChessEngine {
    suspend fun getBestMove(moves: List<String>): String?
}
