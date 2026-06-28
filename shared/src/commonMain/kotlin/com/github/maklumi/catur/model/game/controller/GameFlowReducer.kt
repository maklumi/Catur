package com.github.maklumi.catur.model.game.controller

import com.github.maklumi.catur.model.game.state.*
import com.github.maklumi.catur.model.piece.PieceColor

internal fun GameState.reduceGameFlow(action: GameAction): GameState {
    return when (action) {
        is GameAction.Resign -> {
            if (isViewingHistory || currentSnapshot.status != GameStatus.ONGOING) return this
            val snapshot = currentSnapshot
            val forcedStatus = if (snapshot.activeColor == PieceColor.WHITE) GameStatus.WHITE_RESIGNED else GameStatus.BLACK_RESIGNED
            val nextSnapshot = snapshot.copy(forcedStatus = forcedStatus)
            copy(
                board = board.copy(
                    snapshots = snapshots.toMutableList().apply { set(currentIndex, nextSnapshot) }
                )
            )
        }
        is GameAction.OfferDraw -> {
            if (isViewingHistory || currentSnapshot.status != GameStatus.ONGOING) return this
            val snapshot = currentSnapshot
            if (snapshot.drawOfferedBy != null) return this
            val nextSnapshot = snapshot.copy(drawOfferedBy = snapshot.activeColor)
            copy(
                board = board.copy(
                    snapshots = snapshots.toMutableList().apply { set(currentIndex, nextSnapshot) }
                )
            )
        }
        is GameAction.AcceptDraw -> {
            if (isViewingHistory || currentSnapshot.status != GameStatus.ONGOING) return this
            val snapshot = currentSnapshot
            if (snapshot.drawOfferedBy == null || snapshot.drawOfferedBy == snapshot.activeColor) return this
            val nextSnapshot = snapshot.copy(forcedStatus = GameStatus.DRAW)
            copy(
                board = board.copy(
                    snapshots = snapshots.toMutableList().apply { set(currentIndex, nextSnapshot) }
                )
            )
        }
        is GameAction.DeclineDraw -> {
            if (isViewingHistory || currentSnapshot.status != GameStatus.ONGOING) return this
            val snapshot = currentSnapshot
            if (snapshot.drawOfferedBy == null || snapshot.drawOfferedBy == snapshot.activeColor) return this
            val nextSnapshot = snapshot.copy(drawOfferedBy = null)
            copy(
                board = board.copy(
                    snapshots = snapshots.toMutableList().apply { set(currentIndex, nextSnapshot) }
                )
            )
        }
        is GameAction.Tick -> {
            val justMovedColor = currentSnapshot.activeColor
            if (justMovedColor == PieceColor.WHITE) {
                copy(clock = clock.copy(whiteTimeMillis = (clock.whiteTimeMillis - action.millis).coerceAtLeast(0)))
            } else {
                copy(clock = clock.copy(blackTimeMillis = (clock.blackTimeMillis - action.millis).coerceAtLeast(0)))
            }
        }
        else -> this
    }
}
