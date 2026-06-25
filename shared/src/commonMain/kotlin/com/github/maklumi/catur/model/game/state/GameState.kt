package com.github.maklumi.catur.model.game.state

import com.github.maklumi.catur.model.board.Board
import com.github.maklumi.catur.model.board.Position
import com.github.maklumi.catur.model.move.BoardMove
import com.github.maklumi.catur.model.piece.PieceColor

enum class PlayerType {
    HUMAN, ENGINE
}

data class PlayerConfig(
    val type: PlayerType,
    val name: String,
    val timeMillis: Long = 600_000L,
)

data class EngineSettings(
    val model: String = "maia3-5m",
    val isThinking: Boolean = false,
)

data class UiState(
    val isBoardFlipped: Boolean = false,
    val longPressedPosition: Position? = null,
    val moveEvaluations: Map<String, Int> = emptyMap(),
    val bestMoveArrow: Pair<Position, Position>? = null,
)

data class GameState(
    val snapshots: List<GameSnapshotState> = listOf(GameSnapshotState(context = ChessContext(board = Board.initial))),
    val currentIndex: Int = 0,
    val whitePlayer: PlayerConfig = PlayerConfig(PlayerType.HUMAN, "Human"),
    val blackPlayer: PlayerConfig = PlayerConfig(PlayerType.ENGINE, "Maia"),
    val engine: EngineSettings = EngineSettings(),
    val ui: UiState = UiState(),
) {
    // Helpers
    val whiteName get() = whitePlayer.name
    val blackName get() = blackPlayer.name
    val whiteTimeMillis get() = whitePlayer.timeMillis
    val blackTimeMillis get() = blackPlayer.timeMillis
    val engineModel get() = engine.model
    val isEngineThinking get() = engine.isThinking
    val isBoardFlipped get() = ui.isBoardFlipped
    val longPressedPosition get() = ui.longPressedPosition
    val moveEvaluations get() = ui.moveEvaluations
    val bestMoveArrow get() = ui.bestMoveArrow

    val currentSnapshot: GameSnapshotState get() = snapshots[currentIndex]

    val isViewingHistory: Boolean get() = currentIndex < snapshots.size - 1

    fun canGoBack(): Boolean = currentIndex > 0
    fun canGoForward(): Boolean = currentIndex < snapshots.size - 1

    val isEngineTurn: Boolean get() = !isViewingHistory && 
        currentSnapshot.status == GameStatus.ONGOING &&
        ((currentSnapshot.activeColor == PieceColor.WHITE && whitePlayer.type == PlayerType.ENGINE) ||
         (currentSnapshot.activeColor == PieceColor.BLACK && blackPlayer.type == PlayerType.ENGINE))
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
}
