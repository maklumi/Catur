package com.github.maklumi.catur.state.controller

import com.github.maklumi.catur.Platform
import com.github.maklumi.catur.getPlatform
import com.github.maklumi.catur.domain.chess.board.*
import com.github.maklumi.catur.domain.chess.notation.findMoveByNotation
import com.github.maklumi.catur.domain.chess.notation.PgnUtils
import com.github.maklumi.catur.domain.chess.notation.OpeningBook
import com.github.maklumi.catur.domain.audio.SoundType
import com.github.maklumi.catur.domain.engine.ChessEngine
import com.github.maklumi.catur.domain.puzzle.PuzzleLoader
import com.github.maklumi.catur.domain.chess.GameRecord
import com.github.maklumi.catur.domain.chess.move.BoardMove
import com.github.maklumi.catur.domain.chess.piece.*
import com.github.maklumi.catur.state.model.*
import com.github.maklumi.catur.state.reducer.gameReducer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.milliseconds

class GameController(
    private val engine: ChessEngine? = null,
    scope: CoroutineScope = CoroutineScope(Dispatchers.Main),
    private val platform: Platform = getPlatform()
) {
    private val _state = MutableStateFlow(GameState())
    val state: StateFlow<GameState> = _state.asStateFlow()

    val boardState = _state.map { it.board }.distinctUntilChanged()
    val matchState = _state.map { it.match }.distinctUntilChanged()
    val clockState = _state.map { it.clock }.distinctUntilChanged()
    val engineState = _state.map { it.engine }.distinctUntilChanged()
    val puzzleState = _state.map { it.puzzle }.distinctUntilChanged()
    val uiVisualState = _state.map { it.uiVisual }.distinctUntilChanged()
    val historyState = _state.map { it.history }.distinctUntilChanged()

    init {
        // Load puzzles
        scope.launch {
            val completed = platform.persistenceManager.loadCompletedPuzzles()
            val puzzles = PuzzleLoader.loadPuzzles(completed)
            if (puzzles.isNotEmpty()) {
                dispatch(GameAction.Puzzles.SetPuzzles(puzzles, completed))
            }
        }

        // Load History
        scope.launch {
            val games = platform.persistenceManager.loadGames()
            dispatch(GameAction.History.SetPastGames(games))
        }

        // Save completed puzzles when they change
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
                        // Only save if it was a real game (has moves and matches a start ID)
                        if (currentState.snapshots.size > 1 && currentState.match.id != null) {
                            val pgn = PgnUtils.generatePgn(currentState)
                            val moves = currentState.snapshots.drop(1).mapNotNull { it.lastMoveUci }
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
                            
                            // Refresh history list
                            val updatedGames = platform.persistenceManager.loadGames()
                            dispatch(GameAction.History.SetPastGames(updatedGames))
                        }
                    }
                }
        }

        // Clock timer logic
        scope.launch {
            while (true) {
                delay(100.milliseconds)
                val currentState = state.value
                if (!currentState.board.isViewingHistory && 
                    currentState.currentSnapshot.status == GameStatus.ONGOING &&
                    currentState.puzzle.currentPuzzleIndex == null
                ) {
                    dispatch(GameAction.Flow.Tick(100))
                }
            }
        }

        // Sound effects logic
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
                        val platform = getPlatform()
                        when {
                            lastSnapshot.status == GameStatus.CHECKMATE ||
                                    lastSnapshot.status == GameStatus.STALEMATE -> {
                                platform.playSound(SoundType.GAME_END)
                            }

                            lastSnapshot.board.isInCheck(lastSnapshot.activeColor) -> {
                                platform.playSound(SoundType.CHECK)
                            }

                            prevSnapshot.board[boardMove.to].isNotEmpty || boardMove is BoardMove.EnPassant -> {
                                platform.playSound(SoundType.CAPTURE)
                            }

                            else -> {
                                platform.playSound(SoundType.MOVE)
                            }
                        }
                    }
                }
        }

        scope.launch {
            state
                .map { Triple(it.snapshots.size, it.board.currentIndex, it.isEngineTurn) }
                .distinctUntilChanged()
                .collectLatest { (snapshotSize, currentIndex, isEngineTurn) ->
                    val currentState = state.value
                    if (isEngineTurn && engine != null && !currentState.engine.isThinking) {
                        dispatch(GameAction.Ui.SetEngineThinking(true))

                        val moves = currentState.snapshots
                            .drop(1)
                            .mapNotNull { it.lastMoveUci }

                        val bestMove = engine.getBestMove(moves, currentState.engine.model)

                        dispatch(GameAction.Ui.SetEngineThinking(false))

                        if (bestMove != null) {
                            delay(1000.milliseconds)
                            dispatch(GameAction.Move.EngineMove(bestMove))
                        }
                    } else if (!isEngineTurn && engine != null && !currentState.engine.isThinking) {
                        // Background analysis for best move arrow and threats (Disabled during puzzles and board setup)
                        if (currentState.puzzle.currentPuzzleIndex == null && !currentState.board.isEditMode) {
                            // Calculate Moves up to current index
                            val moves = currentState.snapshots
                                .take(currentState.board.currentIndex + 1)
                                .drop(1)
                                .mapNotNull { it.lastMoveUci }

                            // Smarter Position Logic
                            val isStandardStart = currentState.isFromInitialPosition()
                            val engineFen = if (isStandardStart) null else currentState.currentSnapshot.generateFen()
                            val engineMoves = if (isStandardStart) moves else emptyList()

                            // Calculate Threats
                            val snapshot = currentState.currentSnapshot
                            val activeColor = snapshot.activeColor
                            val opponentColor = activeColor.opposite()
                            val threats = snapshot.board.piecesMap.keys.filter { pos ->
                                val piece = snapshot.board[pos]
                                piece?.pieceColor == activeColor && snapshot.board.isAttacked(
                                    pos,
                                    opponentColor
                                )
                            }
                            dispatch(GameAction.Ui.SetThreats(threats))

                            // Best move arrow
                            val bestMove = engine.getBestMove(engineMoves, currentState.engine.model, engineFen)
                            if (bestMove != null && bestMove.length >= 4) {
                                try {
                                    val from = Position.valueOf(bestMove.substring(0, 2))
                                    val to = Position.valueOf(bestMove.substring(2, 4))
                                    dispatch(GameAction.Ui.SetBestMoveArrow(from, to))
                                } catch (_: Exception) {
                                }
                            }

                            // Current Position Evaluation
                            val eval = engine.evaluate(engineMoves, engineFen)
                            dispatch(GameAction.Ui.SetCurrentEvaluation(eval))
                        }
                    }
                }
        }

        scope.launch {
            state.map { it.longPressedPosition }.distinctUntilChanged().collectLatest { pos ->
                dispatch(GameAction.Ui.SetMoveEvaluations(emptyMap()))
                if (pos != null) {
                    val currentMoves = state.value.snapshots.take(state.value.board.currentIndex + 1).mapNotNull { it.lastMoveUci }
                    val legalMoves = state.value.currentSnapshot.getLegalMovesForPosition(pos)
                    val evals = mutableMapOf<String, Int>()

                    for (boardMove in legalMoves) {
                        val uci = boardMove.toUciString()
                        val score = engine?.evaluate(currentMoves + uci)
                        evals[uci] = score ?: 0
                        dispatch(GameAction.Ui.SetMoveEvaluations(evals.toMap()))
                    }
                }
            }
        }

        scope.launch {
            state.map { it.puzzle.currentPuzzleStep }.distinctUntilChanged().collect { step ->
                val puzzle = state.value.puzzle.puzzles.getOrNull(state.value.puzzle.currentPuzzleIndex ?: -1)
                // If it's an odd step (0 = user, 1 = opponent, 2 = user...), play automatically
                if (puzzle != null && step % 2 != 0) {
                    val nextNotation = puzzle.solutionMoves.getOrNull(step)
                    if (nextNotation != null) {
                        delay(1000.milliseconds) // Wait for realism
                        val snapshot = state.value.currentSnapshot
                        val boardMove = snapshot.board.findMoveByNotation(
                            nextNotation,
                            snapshot.activeColor,
                            snapshot.lastMove,
                            snapshot.movedPositions
                        )
                        if (boardMove != null) {
                            dispatch(GameAction.Move.EngineMove(boardMove.toUciString()))
                        }
                    }
                }
            }
        }

        // Auto-forward puzzle logic
        scope.launch {
            state.map { it.puzzle.isPuzzleFinished }.distinctUntilChanged().collect { isFinished ->
                if (isFinished && state.value.puzzle.isAutoForward) {
                    delay(3000.milliseconds)
                    dispatch(GameAction.Puzzles.NextPuzzle)
                }
            }
        }
    }

    fun dispatch(action: GameAction) {
        _state.update { currentState ->
            gameReducer(currentState, action)
        }
    }

    fun dispose() {
        engine?.stop()
    }
}
