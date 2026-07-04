package com.github.maklumi.catur.state.reducer

import com.github.maklumi.catur.state.model.*
import com.github.maklumi.catur.domain.chess.piece.PieceColor
import com.github.maklumi.catur.domain.chess.board.isInCheck
import com.github.maklumi.catur.domain.chess.logic.getNotation
import kotlin.random.Random

internal fun GameState.reduceMove(action: GameAction.Move): GameState {
    return when (action) {
        is GameAction.Move.PlacePiece -> {
            val snapshot = currentSnapshot
            val updatedPieces = snapshot.board.piecesMap.toMutableMap()
            updatedPieces[action.position] = action.piece
            
            val newSnapshot = snapshot.copy(
                context = snapshot.context.copy(board = snapshot.board.copy(piecesMap = updatedPieces)),
                history = ChessHistory()
            )
            
            updateBoard {
                copy(
                    snapshots = listOf(newSnapshot),
                    currentIndex = 0,
                    lastMoveId = Random.nextLong()
                )
            }
        }
        is GameAction.Move.RemovePiece -> {
            val snapshot = currentSnapshot
            val updatedPieces = snapshot.board.piecesMap.toMutableMap()
            updatedPieces.remove(action.position)
            
            val newSnapshot = snapshot.copy(
                context = snapshot.context.copy(board = snapshot.board.copy(piecesMap = updatedPieces)),
                history = ChessHistory()
            )
            
            updateBoard {
                copy(
                    snapshots = listOf(newSnapshot),
                    currentIndex = 0,
                    lastMoveId = Random.nextLong()
                )
            }
        }
        is GameAction.Move.SquareClick -> {
            if (board.isEditMode) {
                val piece = uiVisual.selectedPalettePiece
                return if (piece != null) {
                    reduceMove(GameAction.Move.PlacePiece(action.position, piece))
                } else {
                    reduceMove(GameAction.Move.RemovePiece(action.position))
                }
            }

            if (board.isViewingHistory) return this

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
                    
                    // Robust comparison: ignore symbols like +, #, =, and case
                    fun normalize(s: String?) = s?.replace("+", "")?.replace("#", "")?.replace("=", "")?.lowercase() ?: ""
                    
                    if (normalize(nextSnapshot.notation) != normalize(expectedMove)) {
                        return this
                    }
                    val isPuzzleFinished = puzzle.currentPuzzleStep + 1 >= puzzleRef.solutionMoves.size
                    
                    val intermediateState = applyIncrement()
                        .updateBoard {
                            copy(
                                snapshots = snapshots + nextSnapshot,
                                currentIndex = currentIndex + 1,
                                lastMoveId = Random.nextLong()
                            )
                        }
                        .updateVisual {
                            copy(
                                longPressedPosition = null,
                                moveEvaluations = emptyMap(),
                                bestMoveArrow = null,
                                threats = emptyList()
                            )
                        }
                        .updatePuzzle {
                            copy(currentPuzzleStep = currentPuzzleStep + 1)
                        }
                    
                    return if (isPuzzleFinished && puzzle.currentPuzzleIndex != null) {
                        intermediateState.reducePuzzles(GameAction.Puzzles.PuzzleCompleted(puzzle.currentPuzzleIndex))
                    } else {
                        intermediateState
                    }
                }

                applyIncrement()
                    .updateBoard {
                        copy(
                            snapshots = snapshots + nextSnapshot,
                            currentIndex = currentIndex + 1,
                            lastMoveId = Random.nextLong()
                        )
                    }
                    .updateVisual {
                        copy(
                            longPressedPosition = null,
                            moveEvaluations = emptyMap(),
                            bestMoveArrow = null,
                            threats = emptyList()
                        )
                    }
            } else {
                updateBoard {
                    copy(
                        snapshots = snapshots.toMutableList().apply { 
                            set(currentIndex, nextSnapshotBeforeNotation) 
                        }
                    )
                }.updateVisual {
                    copy(longPressedPosition = null, moveEvaluations = emptyMap(), bestMoveArrow = null, threats = emptyList())
                }
            }
        }
        is GameAction.Move.PromotionChoice -> {
            if (board.isViewingHistory) return this
            val currentSnapshot = currentSnapshot
            val nextSnapshotBeforeNotation = currentSnapshot.promote(action.move)
            
            val isCheck = nextSnapshotBeforeNotation.board.isInCheck(nextSnapshotBeforeNotation.activeColor)
            val isMate = nextSnapshotBeforeNotation.status == GameStatus.CHECKMATE
            val notation = currentSnapshot.board.getNotation(action.move, isCheck, isMate)
            val nextSnapshot = nextSnapshotBeforeNotation.copy(history = nextSnapshotBeforeNotation.history.copy(notation = notation))

            val puzzleRef = puzzle.puzzles.getOrNull(puzzle.currentPuzzleIndex ?: -1)
            if (puzzleRef != null) {
                val expectedMove = puzzleRef.solutionMoves.getOrNull(puzzle.currentPuzzleStep)
                
                // Robust comparison: ignore symbols like +, #, =, and case
                fun normalize(s: String?) = s?.replace("+", "")?.replace("#", "")?.replace("=", "")?.lowercase() ?: ""

                if (normalize(nextSnapshot.notation) != normalize(expectedMove)) {
                    // Reset pending promotion so the dialog disappears on wrong move
                    return updateBoard {
                        copy(
                            snapshots = snapshots.toMutableList().apply {
                                set(currentIndex, currentSnapshot.copy(pendingPromotion = null, context = currentSnapshot.context.copy(selectedPosition = null)))
                            }
                        )
                    }
                }
                return applyIncrement()
                    .updateBoard {
                        copy(
                            snapshots = snapshots + nextSnapshot,
                            currentIndex = currentIndex + 1,
                            lastMoveId = Random.nextLong()
                        )
                    }
                    .updateVisual { copy(bestMoveArrow = null, threats = emptyList()) }
                    .updatePuzzle { copy(currentPuzzleStep = currentPuzzleStep + 1) }
            }

            applyIncrement()
                .updateBoard {
                    copy(
                        snapshots = snapshots + nextSnapshot,
                        currentIndex = currentIndex + 1,
                        lastMoveId = Random.nextLong()
                    )
                }
                .updateVisual { copy(bestMoveArrow = null, threats = emptyList()) }
        }
        is GameAction.Move.CancelPromotion -> {
            val snapshot = currentSnapshot
            updateBoard {
                copy(
                    snapshots = snapshots.toMutableList().apply {
                        set(currentIndex, snapshot.copy(pendingPromotion = null, context = snapshot.context.copy(selectedPosition = null)))
                    }
                )
            }
        }
        is GameAction.Move.EngineMove -> {
            if (board.isViewingHistory) return this
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
                    .updateBoard {
                        copy(
                            snapshots = snapshots + nextSnapshot,
                            currentIndex = currentIndex + 1,
                            lastMoveId = Random.nextLong()
                        )
                    }
                    .updateVisual { copy(bestMoveArrow = null, threats = emptyList()) }
                    .updatePuzzle { copy(currentPuzzleStep = currentPuzzleStep + 1) }
            }

            applyIncrement()
                .updateBoard {
                    copy(
                        snapshots = snapshots + nextSnapshot,
                        currentIndex = currentIndex + 1,
                        lastMoveId = Random.nextLong()
                    )
                }
                .updateVisual { copy(bestMoveArrow = null, threats = emptyList()) }
        }
    }
}

private fun GameState.applyIncrement(): GameState {
    val justMovedColor = currentSnapshot.activeColor
    return if (justMovedColor == PieceColor.WHITE) {
        updateClock { copy(whiteTimeMillis = whiteTimeMillis + 3000L) }
    } else {
        updateClock { copy(blackTimeMillis = blackTimeMillis + 3000L) }
    }
}
