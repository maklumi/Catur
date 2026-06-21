package com.github.maklumi.catur.model.game.state

import com.github.maklumi.catur.model.board.Board
import com.github.maklumi.catur.model.board.Position
import com.github.maklumi.catur.model.move.BoardMove
import com.github.maklumi.catur.model.piece.PieceColor

enum class PlayerType {
    HUMAN, ENGINE
}

data class GameState(
    val snapshots: List<GameSnapshotState> = listOf(GameSnapshotState(board = Board.initial)),
    val currentIndex: Int = 0,
    val whitePlayer: PlayerType = PlayerType.HUMAN,
    val blackPlayer: PlayerType = PlayerType.ENGINE,
    val whiteName: String = "Human",
    val blackName: String = "Maia",
    val whiteTimeMillis: Long = 600_000L,
    val blackTimeMillis: Long = 600_000L,
    val engineModel: String = "maia3-5m",
    val isBoardFlipped: Boolean = false,
    val isEngineThinking: Boolean = false,
) {
    val currentSnapshot: GameSnapshotState get() = snapshots[currentIndex]

    val isViewingHistory: Boolean get() = currentIndex < snapshots.size - 1

    fun canGoBack(): Boolean = currentIndex > 0
    fun canGoForward(): Boolean = currentIndex < snapshots.size - 1

    val isEngineTurn: Boolean get() = !isViewingHistory && 
        currentSnapshot.status == GameStatus.ONGOING &&
        ((currentSnapshot.activeColor == PieceColor.WHITE && whitePlayer == PlayerType.ENGINE) ||
         (currentSnapshot.activeColor == PieceColor.BLACK && blackPlayer == PlayerType.ENGINE))
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
}
