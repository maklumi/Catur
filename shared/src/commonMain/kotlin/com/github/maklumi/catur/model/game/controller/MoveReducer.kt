package com.github.maklumi.catur.model.game.controller

import com.github.maklumi.catur.model.game.state.*
import com.github.maklumi.catur.model.piece.PieceColor
import kotlin.random.Random

internal fun GameState.reduceMove(action: GameAction): GameState {
    return when (action) {
        is GameAction.SquareClick -> {
            if (isViewingHistory) return this
            val currentSnapshot = currentSnapshot
            val nextSnapshotBeforeNotation = currentSnapshot.move(action.position)
            
            if (nextSnapshotBeforeNotation == currentSnapshot) return this

            if (nextSnapshotBeforeNotation.board != currentSnapshot.board) {
                val boardMove = nextSnapshotBeforeNotation.lastMove!!
                val isCheck = nextSnapshotBeforeNotation.board.isInCheck(nextSnapshotBeforeNotation.activeColor)
                val isMate = nextSnapshotBeforeNotation.status == GameStatus.CHECKMATE
                
                val notation = currentSnapshot.board.getNotation(boardMove, isCheck, isMate)
                val nextSnapshot = nextSnapshotBeforeNotation.copy(
                    history = nextSnapshotBeforeNotation.history.copy(notation = notation)
                )

                val puzzleRef = puzzle.puzzles.getOrNull(puzzle.currentPuzzleIndex ?: -1)
                if (puzzleRef != null) {
                    val expectedMove = puzzleRef.solutionMoves.getOrNull(puzzle.currentPuzzleStep)
                    if (nextSnapshot.notation != expectedMove) {
                        return this
                    }
                    val isPuzzleFinished = puzzle.currentPuzzleStep + 1 >= puzzleRef.solutionMoves.size
                    
                    val intermediateState = applyIncrement()
                        .copy(
                            board = board.copy(
                                snapshots = snapshots + nextSnapshot,
                                currentIndex = currentIndex + 1,
                                lastMoveId = Random.nextLong()
                            ),
                            uiVisual = uiVisual.copy(
                                longPressedPosition = null,
                                moveEvaluations = emptyMap(),
                                bestMoveArrow = null,
                                threats = emptyList()
                            ),
                            puzzle = puzzle.copy(currentPuzzleStep = puzzle.currentPuzzleStep + 1)
                        )
                    
                    return if (isPuzzleFinished && puzzle.currentPuzzleIndex != null) {
                        intermediateState.reduceUi(GameAction.PuzzleCompleted(puzzle.currentPuzzleIndex))
                    } else {
                        intermediateState
                    }
                }

                applyIncrement()
                    .copy(
                        board = board.copy(
                            snapshots = snapshots + nextSnapshot,
                            currentIndex = currentIndex + 1,
                            lastMoveId = Random.nextLong()
                        ),
                        uiVisual = uiVisual.copy(
                            longPressedPosition = null,
                            moveEvaluations = emptyMap(),
                            bestMoveArrow = null,
                            threats = emptyList()
                        )
                    )
            } else {
                copy(
                    board = board.copy(
                        snapshots = snapshots.toMutableList().apply { 
                            set(currentIndex, nextSnapshotBeforeNotation) 
                        }
                    ),
                    uiVisual = uiVisual.copy(longPressedPosition = null, moveEvaluations = emptyMap(), bestMoveArrow = null, threats = emptyList())
                )
            }
        }
        is GameAction.PromotionChoice -> {
            if (isViewingHistory) return this
            val currentSnapshot = currentSnapshot
            val nextSnapshotBeforeNotation = currentSnapshot.promote(action.move)
            
            val isCheck = nextSnapshotBeforeNotation.board.isInCheck(nextSnapshotBeforeNotation.activeColor)
            val isMate = nextSnapshotBeforeNotation.status == GameStatus.CHECKMATE
            val notation = currentSnapshot.board.getNotation(action.move, isCheck, isMate)
            val nextSnapshot = nextSnapshotBeforeNotation.copy(history = nextSnapshotBeforeNotation.history.copy(notation = notation))

            val puzzleRef = puzzle.puzzles.getOrNull(puzzle.currentPuzzleIndex ?: -1)
            if (puzzleRef != null) {
                val expectedMove = puzzleRef.solutionMoves.getOrNull(puzzle.currentPuzzleStep)
                if (nextSnapshot.notation != expectedMove) return this
                return applyIncrement()
                    .copy(
                        board = board.copy(
                            snapshots = snapshots + nextSnapshot,
                            currentIndex = currentIndex + 1,
                            lastMoveId = Random.nextLong()
                        ),
                        uiVisual = uiVisual.copy(bestMoveArrow = null, threats = emptyList()),
                        puzzle = puzzle.copy(currentPuzzleStep = puzzle.currentPuzzleStep + 1)
                    )
            }

            applyIncrement()
                .copy(
                    board = board.copy(
                        snapshots = snapshots + nextSnapshot,
                        currentIndex = currentIndex + 1,
                        lastMoveId = Random.nextLong()
                    ),
                    uiVisual = uiVisual.copy(bestMoveArrow = null, threats = emptyList())
                )
        }
        is GameAction.EngineMove -> {
            if (isViewingHistory) return this
            val currentSnapshot = currentSnapshot
            val boardMove = currentSnapshot.findMoveByUci(action.moveUci) ?: return this
            
            val nextSnapshotBeforeNotation = currentSnapshot.promote(boardMove)
            
            val isCheck = nextSnapshotBeforeNotation.board.isInCheck(nextSnapshotBeforeNotation.activeColor)
            val isMate = nextSnapshotBeforeNotation.status == GameStatus.CHECKMATE
            val notation = currentSnapshot.board.getNotation(boardMove, isCheck, isMate)
            val nextSnapshot = nextSnapshotBeforeNotation.copy(history = nextSnapshotBeforeNotation.history.copy(notation = notation))

            val puzzleRef = puzzle.puzzles.getOrNull(puzzle.currentPuzzleIndex ?: -1)
            if (puzzleRef != null) {
                return applyIncrement()
                    .copy(
                        board = board.copy(
                            snapshots = snapshots + nextSnapshot,
                            currentIndex = currentIndex + 1,
                            lastMoveId = Random.nextLong()
                        ),
                        uiVisual = uiVisual.copy(bestMoveArrow = null, threats = emptyList()),
                        puzzle = puzzle.copy(currentPuzzleStep = puzzle.currentPuzzleStep + 1)
                    )
            }

            applyIncrement()
                .copy(
                    board = board.copy(
                        snapshots = snapshots + nextSnapshot,
                        currentIndex = currentIndex + 1,
                        lastMoveId = Random.nextLong()
                    ),
                    uiVisual = uiVisual.copy(bestMoveArrow = null, threats = emptyList())
                )
        }
        else -> this
    }
}

private fun GameState.applyIncrement(): GameState {
    val justMovedColor = currentSnapshot.activeColor
    return if (justMovedColor == PieceColor.WHITE) {
        copy(clock = clock.copy(whiteTimeMillis = clock.whiteTimeMillis + 3000L))
    } else {
        copy(clock = clock.copy(blackTimeMillis = clock.blackTimeMillis + 3000L))
    }
}
