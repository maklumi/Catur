package com.github.maklumi.catur.state.controller

import com.github.maklumi.catur.Platform
import com.github.maklumi.catur.domain.chess.GameRecord
import com.github.maklumi.catur.domain.chess.notation.OpeningBook
import com.github.maklumi.catur.domain.chess.notation.PgnUtils
import com.github.maklumi.catur.domain.puzzle.PuzzleLoader
import com.github.maklumi.catur.state.model.GameAction
import com.github.maklumi.catur.state.model.GameStatus
import com.github.maklumi.catur.state.model.GameState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class PersistenceHandler(
    private val platform: Platform,
    private val dispatch: (GameAction) -> Unit,
) {
    fun attach(scope: CoroutineScope, state: StateFlow<GameState>) {
        // Load initial data
        scope.launch {
            val completed = platform.persistenceManager.loadCompletedPuzzles()
            val puzzles = PuzzleLoader.loadPuzzles(completed)
            if (puzzles.isNotEmpty()) {
                dispatch(GameAction.Puzzles.SetPuzzles(puzzles, completed))
            }
            
            val games = platform.persistenceManager.loadGames()
            dispatch(GameAction.History.SetPastGames(games))

            platform.persistenceManager.loadSettings()?.let { (theme, sound, engine) ->
                dispatch(GameAction.Ui.ApplySettings(theme, sound, engine))
            }
        }

        // Save settings when they change
        scope.launch {
            state.map { 
                Triple(it.uiVisual.boardTheme.name, it.uiVisual.isSoundEnabled, it.engine.model)
            }.distinctUntilChanged().drop(1).collect { settings ->
                val (theme, sound, engine) = settings
                platform.persistenceManager.saveSettings(theme, sound, engine)
            }
        }

        // Save completed puzzles
        scope.launch {
            state
                .map { it.puzzle.completedPuzzleIndices }
                .distinctUntilChanged()
                .collect { indices ->
                    if (indices.isNotEmpty()) {
                        platform.persistenceManager.saveCompletedPuzzles(indices)
                    }
                }
        }

        // Save History on Game End
        scope.launch {
            state
                .map { it.currentSnapshot.status }
                .distinctUntilChanged()
                .collect { status ->
                    if (status != GameStatus.ONGOING) {
                        val currentState = state.value
                        if ((currentState.snapshots.size > 1) && (currentState.match.id != null)) {
                            val pgn = PgnUtils.generatePgn(currentState)
                            val moves = currentState.snapshots
                                .asSequence()
                                .drop(1)
                                .mapNotNull { it.lastMoveUci }
                                .toList()
                            val opening = OpeningBook.getOpening(moves)
                            val recordId = "${currentState.match.id}-${opening?.code ?: "UNK"}"

                            val record = GameRecord(
                                id = recordId,
                                date = platform.getCurrentDate(),
                                white = currentState.match.whiteName,
                                black = currentState.match.blackName,
                                result = status.name,
                                opening = opening?.name,
                                pgn = pgn
                            )
                            platform.persistenceManager.saveGame(record)
                            
                            val updatedGames = platform.persistenceManager.loadGames()
                            dispatch(GameAction.History.SetPastGames(updatedGames))
                        }
                    }
                }
        }
    }
}
