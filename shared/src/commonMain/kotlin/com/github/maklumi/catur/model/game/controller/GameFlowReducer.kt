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
                snapshots = snapshots.toMutableList().apply { set(currentIndex, nextSnapshot) }
            )
        }
        is GameAction.OfferDraw -> {
            if (isViewingHistory || currentSnapshot.status != GameStatus.ONGOING) return this
            val snapshot = currentSnapshot
            if (snapshot.drawOfferedBy != null) return this
            val nextSnapshot = snapshot.copy(drawOfferedBy = snapshot.activeColor)
            copy(
                snapshots = snapshots.toMutableList().apply { set(currentIndex, nextSnapshot) }
            )
        }
        is GameAction.AcceptDraw -> {
            if (isViewingHistory || currentSnapshot.status != GameStatus.ONGOING) return this
            val snapshot = currentSnapshot
            if (snapshot.drawOfferedBy == null || snapshot.drawOfferedBy == snapshot.activeColor) return this
            val nextSnapshot = snapshot.copy(forcedStatus = GameStatus.DRAW)
            copy(
                snapshots = snapshots.toMutableList().apply { set(currentIndex, nextSnapshot) }
            )
        }
        is GameAction.DeclineDraw -> {
            if (isViewingHistory || currentSnapshot.status != GameStatus.ONGOING) return this
            val snapshot = currentSnapshot
            if (snapshot.drawOfferedBy == null || snapshot.drawOfferedBy == snapshot.activeColor) return this
            val nextSnapshot = snapshot.copy(drawOfferedBy = null)
            copy(
                snapshots = snapshots.toMutableList().apply { set(currentIndex, nextSnapshot) }
            )
        }
        is GameAction.Tick -> {
            if (isViewingHistory || currentSnapshot.status != GameStatus.ONGOING) return this
            
            val activeColor = currentSnapshot.activeColor
            val newState = if (activeColor == PieceColor.WHITE) {
                val newTime = (whiteTimeMillis - action.millis).coerceAtLeast(0L)
                copy(whitePlayer = whitePlayer.copy(timeMillis = newTime))
            } else {
                val newTime = (blackTimeMillis - action.millis).coerceAtLeast(0L)
                copy(blackPlayer = blackPlayer.copy(timeMillis = newTime))
            }
            
            if ((activeColor == PieceColor.WHITE && newState.whiteTimeMillis == 0L) ||
                (activeColor == PieceColor.BLACK && newState.blackTimeMillis == 0L)) {
                val forcedStatus = if (activeColor == PieceColor.WHITE) GameStatus.WHITE_OUT_OF_TIME else GameStatus.BLACK_OUT_OF_TIME
                val nextSnapshot = newState.currentSnapshot.copy(forcedStatus = forcedStatus)
                newState.copy(
                    snapshots = newState.snapshots.toMutableList().apply { set(newState.currentIndex, nextSnapshot) }
                )
            } else {
                newState
            }
        }
        else -> this
    }
}
