package com.github.maklumi.catur.data.persistence

interface PersistenceManager {
    fun saveCompletedPuzzles(indices: Set<Int>)
    fun loadCompletedPuzzles(): Set<Int>
}
