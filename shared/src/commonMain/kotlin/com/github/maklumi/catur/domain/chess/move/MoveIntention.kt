package com.github.maklumi.catur.domain.chess.move

import com.github.maklumi.catur.domain.chess.board.Position

data class MoveIntention(
    val from: Position,
    val to: Position
)
