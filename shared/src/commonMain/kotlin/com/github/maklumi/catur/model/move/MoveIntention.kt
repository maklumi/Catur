package com.github.maklumi.catur.model.move

import com.github.maklumi.catur.model.board.Position

data class MoveIntention(
    val from: Position,
    val to: Position
)