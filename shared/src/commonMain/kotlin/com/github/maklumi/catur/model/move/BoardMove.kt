package com.github.maklumi.catur.model.move

import kotlin.random.Random

data class BoardMove(
    val move: PrimaryMove,
    val id: Long = Random.nextLong()
)
