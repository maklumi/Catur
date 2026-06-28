package com.github.maklumi.catur.model

interface PersistenceManager {
    fun saveCompletedPuzzles(indices: Set<Int>)
    fun loadCompletedPuzzles(): Set<Int>
}
