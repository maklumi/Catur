package com.github.maklumi.catur.state.model

import com.github.maklumi.catur.domain.chess.board.Board
import com.github.maklumi.catur.domain.chess.board.Position
import com.github.maklumi.catur.domain.puzzle.Puzzle
import com.github.maklumi.catur.domain.chess.move.BoardMove
import com.github.maklumi.catur.domain.chess.piece.Piece
import com.github.maklumi.catur.domain.chess.piece.PieceColor
import com.github.maklumi.catur.domain.chess.logic.OpeningBook
import com.github.maklumi.catur.domain.chess.GameRecord

enum class PlayerType { HUMAN, ENGINE }

enum class Screen { MENU, PLAY_SELECTION, GAME, PUZZLES, ANALYSIS, SETTINGS, HISTORY }

enum class BoardTheme { GREEN, WOOD, BLUE, CLASSIC }

data class BoardState(
    val snapshots: List<GameSnapshotState> = listOf(GameSnapshotState(context = ChessContext(board = Board.initial))),
    val currentIndex: Int = 0,
    val isBoardFlipped: Boolean = false,
    val lastMoveId: Long? = null,
    val isEditMode: Boolean = false,
    val openingName: String? = null
) {
    val currentSnapshot: GameSnapshotState get() = snapshots[currentIndex]
    val isViewingHistory: Boolean get() = currentIndex < snapshots.size - 1
    fun canGoBack(): Boolean = currentIndex > 0
    fun canGoForward(): Boolean = currentIndex < snapshots.size - 1
}

data class MatchState(
    val id: String? = null,
    val whiteName: String = "White",
    val whiteType: PlayerType = PlayerType.HUMAN,
    val blackName: String = "Black",
    val blackType: PlayerType = PlayerType.ENGINE
)

data class ClockState(
    val whiteTimeMillis: Long = 600_000L,
    val blackTimeMillis: Long = 600_000L
)

data class EngineState(
    val model: String = "maia3-5m",
    val isThinking: Boolean = false
)

data class PuzzleState(
    val puzzles: List<Puzzle> = emptyList(),
    val currentPuzzleIndex: Int? = null,
    val currentPuzzleStep: Int = 0,
    val completedPuzzleIndices: Set<Int> = emptySet()
)

data class UiVisualState(
    val longPressedPosition: Position? = null,
    val moveEvaluations: Map<String, Int> = emptyMap(),
    val bestMoveArrow: Pair<Position, Position>? = null,
    val threats: List<Position> = emptyList(),
    val currentEvaluation: Int? = null,
    val currentScreen: Screen = Screen.MENU,
    val selectedPalettePiece: Piece? = null,
    val boardTheme: BoardTheme = BoardTheme.GREEN,
    val isSoundEnabled: Boolean = true
)

data class HistoryState(
    val pastGames: List<GameRecord> = emptyList()
)

data class GameState(
    val board: BoardState = BoardState(),
    val match: MatchState = MatchState(),
    val clock: ClockState = ClockState(),
    val engine: EngineState = EngineState(),
    val puzzle: PuzzleState = PuzzleState(),
    val uiVisual: UiVisualState = UiVisualState(),
    val history: HistoryState = HistoryState()
) {
    // Shared Derived State
    val snapshots get() = board.snapshots
    val currentSnapshot get() = board.currentSnapshot
    val isViewingHistory get() = board.isViewingHistory
    val longPressedPosition get() = uiVisual.longPressedPosition
    val puzzles get() = puzzle.puzzles

    fun canGoBack(): Boolean = board.canGoBack()
    fun canGoForward(): Boolean = board.canGoForward()

    fun isFromInitialPosition(): Boolean =
        snapshots.isNotEmpty() && snapshots[0].board.piecesMap == Board.initialPieces

    val isEngineTurn: Boolean get() = !isViewingHistory &&
        currentSnapshot.status == GameStatus.ONGOING &&
        ((currentSnapshot.activeColor == PieceColor.WHITE && match.whiteType == PlayerType.ENGINE) ||
         (currentSnapshot.activeColor == PieceColor.BLACK && match.blackType == PlayerType.ENGINE))

    fun identifyOpening(): String? {
        val moves = board.snapshots.take(board.currentIndex + 1).mapNotNull { it.lastMoveUci }
        return OpeningBook.getOpeningName(moves)
    }
}

// Grouped Actions
sealed class GameAction {
    sealed class Move : GameAction() {
        data class SquareClick(val position: Position) : Move()
        data class PromotionChoice(val move: BoardMove) : Move()
        data class EngineMove(val moveUci: String) : Move()
        data class PlacePiece(val position: Position, val piece: Piece) : Move()
        data class RemovePiece(val position: Position) : Move()
    }

    sealed class Nav : GameAction() {
        object StepBack : Nav()
        object StepForward : Nav()
        data class JumpToHistory(val index: Int) : Nav()
        data class NavigateTo(val screen: Screen) : Nav()
    }

    sealed class Flow : GameAction() {
        object Resign : Flow()
        object OfferDraw : Flow()
        object AcceptDraw : Flow()
        object DeclineDraw : Flow()
        data class Tick(val millis: Long) : Flow()
        object ReverseSides : Flow()
        object NewGame : Flow()
        object StartLocalGame : Flow()
        data class StartComputerGame(
            val model: String = "maia3-5m",
            val playerColor: PieceColor = PieceColor.WHITE
        ) : Flow()
        object StartAnalysis : Flow()
    }

    sealed class Ui : GameAction() {
        data class SquareLongPress(val position: Position) : Ui()
        object ClearLongPress : Ui()
        data class SetMoveEvaluations(val evaluations: Map<String, Int>) : Ui()
        data class SetBestMoveArrow(val from: Position, val to: Position) : Ui()
        object ClearBestMoveArrow : Ui()
        data class SetThreats(val threats: List<Position>) : Ui()
        data class SetCurrentEvaluation(val evaluation: Int) : Ui()
        object ClearCurrentEvaluation : Ui()
        data class SelectPalettePiece(val piece: Piece) : Ui()
        object SelectEraser : Ui()
        object ClearBoard : Ui()
        object ResetBoard : Ui()
        data class SetEditMode(val enabled: Boolean) : Ui()
        data class SetEngineThinking(val isThinking: Boolean) : Ui()
        data class ChangeEngineLevel(val model: String) : Ui()
        data class SetBoardTheme(val theme: BoardTheme) : Ui()
        data class SetSoundEnabled(val enabled: Boolean) : Ui()
    }

    sealed class Puzzles : GameAction() {
        data class SetPuzzles(val puzzles: List<Puzzle>, val completedIndices: Set<Int>) : Puzzles()
        data class SelectPuzzle(val index: Int) : Puzzles()
        data class PuzzleCompleted(val index: Int) : Puzzles()
    }

    sealed class History : GameAction() {
        data class SetPastGames(val games: List<GameRecord>) : History()
        data class LoadGame(val pgn: String) : History()
    }
}

// DSL-like State Update Helpers
fun GameState.updateBoard(block: BoardState.() -> BoardState) = copy(board = board.block())
fun GameState.updateMatch(block: MatchState.() -> MatchState) = copy(match = match.block())
fun GameState.updateClock(block: ClockState.() -> ClockState) = copy(clock = clock.block())
fun GameState.updateEngine(block: EngineState.() -> EngineState) = copy(engine = engine.block())
fun GameState.updatePuzzle(block: PuzzleState.() -> PuzzleState) = copy(puzzle = puzzle.block())
fun GameState.updateVisual(block: UiVisualState.() -> UiVisualState) = copy(uiVisual = uiVisual.block())
fun GameState.updateHistory(block: HistoryState.() -> HistoryState) = copy(history = history.block())
