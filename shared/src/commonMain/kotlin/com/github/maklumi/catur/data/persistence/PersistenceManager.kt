package com.github.maklumi.catur.data.persistence

import com.github.maklumi.catur.domain.chess.GameRecord

interface PersistenceManager {
    fun saveCompletedPuzzles(indices: Set<Int>)
    fun loadCompletedPuzzles(): Set<Int>
    
    fun saveGame(record: GameRecord)
    fun loadGames(): List<GameRecord>
}
