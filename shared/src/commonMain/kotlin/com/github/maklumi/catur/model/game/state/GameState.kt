package com.github.maklumi.catur.model.game.state

import com.github.maklumi.catur.model.board.Board
import com.github.maklumi.catur.model.board.Position
import com.github.maklumi.catur.model.game.puzzle.Puzzle
import com.github.maklumi.catur.model.move.BoardMove
import com.github.maklumi.catur.model.piece.PieceColor

enum class PlayerType {
    HUMAN, ENGINE
}

enum class Screen {
    MENU, PLAY_SELECTION, GAME, PUZZLES, ANALYSIS
}

data class BoardState(
    val snapshots: List<GameSnapshotState> = listOf(GameSnapshotState(context = ChessContext(board = Board.initial))),
    val currentIndex: Int = 0,
    val isBoardFlipped: Boolean = false,
    val lastMoveId: Long? = null
) {
    val currentSnapshot: GameSnapshotState get() = snapshots[currentIndex]
    val isViewingHistory: Boolean get() = currentIndex < snapshots.size - 1
    fun canGoBack(): Boolean = currentIndex > 0
    fun canGoForward(): Boolean = currentIndex < snapshots.size - 1
}

data class MatchState(
    val whiteName: String = "Human",
    val whiteType: PlayerType = PlayerType.HUMAN,
    val blackName: String = "Maia",
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
    val currentScreen: Screen = Screen.MENU
)

data class GameState(
    val board: BoardState = BoardState(),
    val match: MatchState = MatchState(),
    val clock: ClockState = ClockState(),
    val engine: EngineState = EngineState(),
    val puzzle: PuzzleState = PuzzleState(),
    val uiVisual: UiVisualState = UiVisualState()
) {
    // Legacy helper accessors to avoid breaking everything at once
    val snapshots get() = board.snapshots
    val currentIndex get() = board.currentIndex
    val currentSnapshot get() = board.currentSnapshot
    val isViewingHistory get() = board.isViewingHistory
    val isBoardFlipped get() = board.isBoardFlipped

    val whiteName get() = match.whiteName
    val blackName get() = match.blackName

    val engineModel get() = engine.model
    val isEngineThinking get() = engine.isThinking
    
    val puzzles get() = puzzle.puzzles
    val currentPuzzleIndex get() = puzzle.currentPuzzleIndex
    val completedPuzzleIndices get() = puzzle.completedPuzzleIndices
    
    val longPressedPosition get() = uiVisual.longPressedPosition

    fun canGoBack() = board.canGoBack()
    fun canGoForward() = board.canGoForward()

    val isEngineTurn: Boolean get() = !isViewingHistory && 
        currentSnapshot.status == GameStatus.ONGOING &&
        ((currentSnapshot.activeColor == PieceColor.WHITE && match.whiteType == PlayerType.ENGINE) ||
         (currentSnapshot.activeColor == PieceColor.BLACK && match.blackType == PlayerType.ENGINE))
}

sealed class GameAction {
    data class SquareClick(val position: Position) : GameAction()
    data class PromotionChoice(val move: BoardMove) : GameAction()
    object StepBack : GameAction()
    object StepForward : GameAction()
    data class JumpToHistory(val index: Int) : GameAction()
    data class EngineMove(val moveUci: String) : GameAction()
    object ReverseSides : GameAction()
    object Resign : GameAction()
    object OfferDraw : GameAction()
    object AcceptDraw : GameAction()
    object DeclineDraw : GameAction()
    data class SetEngineThinking(val isThinking: Boolean) : GameAction()
    data class Tick(val millis: Long) : GameAction()
    data class ChangeEngineLevel(val model: String) : GameAction()
    data class SquareLongPress(val position: Position) : GameAction()
    object ClearLongPress : GameAction()
    data class SetMoveEvaluations(val evaluations: Map<String, Int>) : GameAction()
    data class SetBestMoveArrow(val from: Position?, val to: Position?) : GameAction()
    data class SetThreats(val threats: List<Position>) : GameAction()
    data class SetPuzzles(val puzzles: List<Puzzle>) : GameAction()
    data class SelectPuzzle(val index: Int) : GameAction()
    data class PuzzleCompleted(val index: Int) : GameAction()
    data class SetCurrentEvaluation(val evaluation: Int?) : GameAction()
    data class NavigateTo(val screen: Screen) : GameAction()
    object NewGame : GameAction()
    object StartLocalGame : GameAction()
    object StartComputerGame : GameAction()
}
