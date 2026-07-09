package com.github.maklumi.catur.state.controller

import com.github.maklumi.catur.Platform
import com.github.maklumi.catur.domain.audio.SoundType
import com.github.maklumi.catur.domain.chess.board.isInCheck
import com.github.maklumi.catur.domain.chess.move.BoardMove
import com.github.maklumi.catur.state.model.GameStatus
import com.github.maklumi.catur.state.model.GameState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class SoundHandler(private val platform: Platform) {
    fun attach(scope: CoroutineScope, state: StateFlow<GameState>) {
        scope.launch {
            state
                .map { it.snapshots.size }
                .distinctUntilChanged()
                .drop(1) // Skip initial state
                .collect {
                    val currentState = state.value
                    if (currentState.snapshots.size < 2) return@collect
                    val lastSnapshot = currentState.snapshots.last()
                    val prevSnapshot = currentState.snapshots[currentState.snapshots.size - 2]

                    val boardMove = lastSnapshot.lastMove
                    if (boardMove != null && currentState.uiVisual.isSoundEnabled) {
                        when {
                            lastSnapshot.status == GameStatus.CHECKMATE ||
                                    lastSnapshot.status == GameStatus.STALEMATE -> {
                                platform.playSound(SoundType.GAME_END)
                            }

                            lastSnapshot.board.isInCheck(lastSnapshot.activeColor) -> {
                                platform.playSound(SoundType.CHECK)
                            }

                            prevSnapshot.board[boardMove.to] != null || boardMove is BoardMove.EnPassant -> {
                                platform.playSound(SoundType.CAPTURE)
                            }

                            else -> {
                                platform.playSound(SoundType.MOVE)
                            }
                        }
                    }
                }
        }
    }
}
