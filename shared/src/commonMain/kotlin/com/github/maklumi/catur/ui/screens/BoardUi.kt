package com.github.maklumi.catur.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.github.maklumi.catur.domain.chess.board.Position
import com.github.maklumi.catur.domain.chess.piece.*
import com.github.maklumi.catur.getPlatform
import com.github.maklumi.catur.state.controller.GameController
import com.github.maklumi.catur.state.model.*
import com.github.maklumi.catur.ui.components.*
import com.github.maklumi.catur.ui.screens.mobile.*

@Composable
fun ChessBoard(
    controller: GameController,
) {
    val boardState by controller.boardState.collectAsState(BoardState())
    val matchState by controller.matchState.collectAsState(MatchState())
    val clockState by controller.clockState.collectAsState(ClockState())
    val engineState by controller.engineState.collectAsState(EngineState())
    val puzzleState by controller.puzzleState.collectAsState(PuzzleState())
    val uiVisualState by controller.uiVisualState.collectAsState(UiVisualState())

    val snapshot = boardState.currentSnapshot

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val isMobile = getPlatform().isMobile

        if (isMobile) {
            when {
                puzzleState.currentPuzzleIndex != null -> {
                    MobilePuzzleScreen(
                        controller = controller,
                        boardState = boardState,
                        puzzleState = puzzleState,
                        uiVisualState = uiVisualState,
                    )
                }
                uiVisualState.currentScreen == Screen.ANALYSIS -> {
                    MobileAnalysisScreen(
                        controller = controller,
                        boardState = boardState,
                        matchState = matchState,
                        uiVisualState = uiVisualState,
                    )
                }
                else -> {
                    MobileGameScreen(
                        controller = controller,
                        boardState = boardState,
                        matchState = matchState,
                        clockState = clockState,
                        uiVisualState = uiVisualState,
                    )
                }
            }
        } else {
            DesktopLayout(
                controller = controller,
                boardState = boardState,
                matchState = matchState,
                clockState = clockState,
                engineState = engineState,
                puzzleState = puzzleState,
                uiVisualState = uiVisualState,
            )
        }

        snapshot.pendingPromotion?.let { moves ->
            PromotionDialog(
                moves = moves,
                onChoice = { controller.dispatch(GameAction.Move.PromotionChoice(it)) },
                onCancel = { controller.dispatch(GameAction.Move.CancelPromotion) },
            )
        }

        if (uiVisualState.isPgnImportDialogOpen) {
            PgnImportDialog(
                onImport = { controller.dispatch(GameAction.History.LoadGame(it)) },
                onCancel = { controller.dispatch(GameAction.Ui.SetPgnImportDialogOpen(open = false)) },
            )
        }
    }
}

@Composable
private fun DesktopLayout(
    controller: GameController,
    boardState: BoardState,
    matchState: MatchState,
    clockState: ClockState,
    engineState: EngineState,
    puzzleState: PuzzleState,
    uiVisualState: UiVisualState
) {
    val snapshot = boardState.currentSnapshot
    val colorScheme = MaterialTheme.colorScheme
    val lastMoveToRank = snapshot.lastMove?.to?.rank
    val ranks = if (boardState.isBoardFlipped) 1..8 else 8 downTo 1
    val files = if (boardState.isBoardFlipped) 8 downTo 1 else 1..8

    Row(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Column(modifier = Modifier.weight(1f).fillMaxHeight()) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(onClick = { controller.dispatch(GameAction.Nav.NavigateTo(Screen.MENU)) }) {
                    Text("Menu")
                }
                
                if (uiVisualState.currentScreen == Screen.ANALYSIS) {
                    FilterChip(
                        selected = boardState.isEditMode,
                        onClick = { controller.dispatch(GameAction.Ui.SetEditMode(!boardState.isEditMode)) },
                        label = { Text(if (boardState.isEditMode) "Editing..." else "Edit Board") }
                    )
                    if (boardState.isEditMode) {
                        Button(
                            onClick = { controller.dispatch(GameAction.Ui.SetEditMode(false)) },
                            colors = ButtonDefaults.buttonColors(containerColor = colorScheme.primary, contentColor = colorScheme.onPrimary)
                        ) { Text("Done") }
                        Button(
                            onClick = { controller.dispatch(GameAction.Ui.ResetBoard) },
                            colors = ButtonDefaults.buttonColors(containerColor = colorScheme.secondaryContainer, contentColor = colorScheme.onSecondaryContainer)
                        ) { Text("Reset") }
                        Button(
                            onClick = { controller.dispatch(GameAction.Ui.ClearBoard) },
                            colors = ButtonDefaults.buttonColors(containerColor = colorScheme.errorContainer, contentColor = colorScheme.onErrorContainer)
                        ) { Text("Clear") }
                    }
                }

                if (uiVisualState.currentScreen == Screen.ANALYSIS) {
                    Button(onClick = { controller.dispatch(GameAction.Ui.SetPgnImportDialogOpen(true)) }) {
                        Text("Import PGN")
                    }
                }

                val activeName = if (snapshot.activeColor == PieceColor.WHITE) matchState.whiteName else matchState.blackName
                val prefix = if (uiVisualState.currentScreen == Screen.ANALYSIS) "To move: " else "Turn: "
                Column(
                    modifier = Modifier.padding(8.dp).height(56.dp),
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "$prefix$activeName",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = colorScheme.onBackground,
                        maxLines = 1
                    )
                    Text(
                        text = boardState.openingName ?: "",
                        fontSize = 14.sp,
                        color = colorScheme.secondary,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1
                    )
                }
                
                if (engineState.isThinking) {
                    Text(
                        text = "(Thinking...)",
                        color = colorScheme.outline,
                        fontSize = 16.sp,
                        modifier = Modifier.padding(8.dp)
                    )
                }

                Spacer(modifier = Modifier.weight(1f))

                if (puzzleState.currentPuzzleIndex != null) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        Text("Auto-forward", fontSize = 12.sp, color = colorScheme.outline)
                        Switch(
                            checked = puzzleState.isAutoForward,
                            onCheckedChange = { controller.dispatch(GameAction.Puzzles.SetAutoForward(it)) }
                        )
                        Button(
                            onClick = { controller.dispatch(GameAction.Puzzles.NextPuzzle) }
                        ) { Text("Next Puzzle") }
                    }
                } else if (uiVisualState.currentScreen == Screen.GAME && snapshot.status == GameStatus.ONGOING) {
                    Button(
                        onClick = { controller.dispatch(GameAction.Flow.Resign) },
                        colors = ButtonDefaults.buttonColors(containerColor = colorScheme.errorContainer, contentColor = colorScheme.onErrorContainer)
                    ) { Text("Resign") }
                    Button(
                        onClick = { controller.dispatch(GameAction.Flow.OfferDraw) }
                    ) { Text("Draw") }
                }
            }

            if (snapshot.status == GameStatus.ONGOING && snapshot.drawOfferedBy != null && snapshot.drawOfferedBy != snapshot.activeColor) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(8.dp)
                ) {
                    Text("Draw offered by ${snapshot.drawOfferedBy}", color = colorScheme.onBackground)
                    Button(onClick = { controller.dispatch(GameAction.Flow.AcceptDraw) }) { Text("Accept") }
                    Button(onClick = { controller.dispatch(GameAction.Flow.DeclineDraw) }) { Text("Decline") }
                }
            }

            Box(
                modifier = Modifier.weight(1f).fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    if (boardState.isEditMode) {
                        PiecePalette(
                            selectedPiece = uiVisualState.selectedPalettePiece,
                            onPieceSelect = { piece ->
                                if (piece != null) {
                                    controller.dispatch(GameAction.Ui.SelectPalettePiece(piece))
                                } else {
                                    controller.dispatch(GameAction.Ui.SelectEraser)
                                }
                            }
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    BoxWithConstraints(
                        modifier = Modifier.fillMaxHeight().padding(bottom = 16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        val boardSize = minOf(maxHeight, maxWidth - 48.dp) // 48dp for bar + spacing

                        Row(
                            modifier = Modifier.height(boardSize).width(boardSize + 48.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            EvaluationBar(
                                evaluation = uiVisualState.currentEvaluation,
                                isBoardFlipped = boardState.isBoardFlipped,
                                modifier = Modifier.width(32.dp).fillMaxHeight().padding(end = 16.dp)
                            )

                            Box(modifier = Modifier.size(boardSize)) {
                                Column(modifier = Modifier.fillMaxSize()) {
                                    for (rank in ranks) {
                                        val isDestinationRank = rank == lastMoveToRank
                                        Row(
                                            modifier = Modifier.weight(1f)
                                                .zIndex(if (isDestinationRank) 5f else 0f)
                                        ) {
                                            for (file in files) {
                                                val position = Position.from(file, rank)
                                                val isLeftmost = file == (if (boardState.isBoardFlipped) 8 else 1)
                                                val isBottom = rank == (if (boardState.isBoardFlipped) 8 else 1)

                                                SquareView(
                                                    position = position,
                                                    boardState = boardState,
                                                    uiVisualState = uiVisualState,
                                                    modifier = Modifier.weight(1f).fillMaxHeight(),
                                                    showRank = isLeftmost,
                                                    showFile = isBottom,
                                                    onAction = { controller.dispatch(it) }
                                                )
                                            }
                                        }
                                    }
                                }

                                if (uiVisualState.currentScreen == Screen.ANALYSIS && uiVisualState.topAnalysisMoves.isNotEmpty()) {
                                    ChessBoardArrows(
                                        moves = uiVisualState.topAnalysisMoves,
                                        isBoardFlipped = boardState.isBoardFlipped
                                    )
                                } else {
                                    uiVisualState.bestMoveArrow?.let { arrow ->
                                        ChessBoardOverlay(
                                            from = arrow.first,
                                            to = arrow.second,
                                            isBoardFlipped = boardState.isBoardFlipped
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Row(
                modifier = Modifier.padding(8.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Button(
                    onClick = { controller.dispatch(GameAction.Nav.StepBack) },
                    enabled = boardState.canGoBack()
                ) { Text("Back") }
                Text(
                    text = "${boardState.currentIndex} / ${boardState.snapshots.size - 1}",
                    modifier = Modifier.align(Alignment.CenterVertically),
                    color = colorScheme.onBackground
                )
                Button(
                    onClick = { controller.dispatch(GameAction.Nav.StepForward) },
                    enabled = boardState.canGoForward()
                ) { Text("Forward") }
            }
        }

        Spacer(modifier = Modifier.width(32.dp))

        Column(modifier = Modifier.weight(0.3f).fillMaxHeight()) {
            val topName = if (boardState.isBoardFlipped) matchState.whiteName else matchState.blackName
            val topCaptured = if (boardState.isBoardFlipped) snapshot.capturedBlack else snapshot.capturedWhite
            val topTime = if (boardState.isBoardFlipped) clockState.whiteTimeMillis else clockState.blackTimeMillis

            val bottomName = if (boardState.isBoardFlipped) matchState.blackName else matchState.whiteName
            val bottomCaptured = if (boardState.isBoardFlipped) snapshot.capturedWhite else snapshot.capturedBlack
            val bottomTime = if (boardState.isBoardFlipped) clockState.blackTimeMillis else clockState.whiteTimeMillis

            val topImbalance = if (boardState.isBoardFlipped) {
                if (snapshot.materialImbalance > 0) snapshot.materialImbalance else 0
            } else {
                if (snapshot.materialImbalance < 0) -snapshot.materialImbalance else 0
            }

            val bottomImbalance = if (boardState.isBoardFlipped) {
                if (snapshot.materialImbalance < 0) -snapshot.materialImbalance else 0
            } else {
                if (snapshot.materialImbalance > 0) snapshot.materialImbalance else 0
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = topName, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = colorScheme.onBackground)
                if (uiVisualState.currentScreen != Screen.PUZZLES && puzzleState.currentPuzzleIndex == null) {
                    Text(
                        text = formatTime(topTime),
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = if (topTime < 30000) colorScheme.error else colorScheme.onBackground
                    )
                }
            }
            CapturedPiecesView(pieces = topCaptured, imbalance = topImbalance)

            if (uiVisualState.currentScreen == Screen.ANALYSIS) {
                Spacer(modifier = Modifier.height(8.dp))
                TopMovesView(topMoves = uiVisualState.topAnalysisMoves)
            }

            Spacer(modifier = Modifier.height(8.dp))
            MoveHistoryList(controller = controller, modifier = Modifier.weight(1f))
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = bottomName, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = colorScheme.onBackground)
                if (uiVisualState.currentScreen != Screen.PUZZLES && puzzleState.currentPuzzleIndex == null) {
                    Text(
                        text = formatTime(bottomTime),
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = if (bottomTime < 30000) colorScheme.error else colorScheme.onBackground
                    )
                }
            }
            CapturedPiecesView(pieces = bottomCaptured, imbalance = bottomImbalance)

            Spacer(modifier = Modifier.height(16.dp))
            EngineLevelSelector(
                currentModel = engineState.model,
                onModelChange = { controller.dispatch(GameAction.Ui.ChangeEngineLevel(it)) })
        }

        if (puzzleState.currentPuzzleIndex != null) {
            Spacer(modifier = Modifier.width(32.dp))

            Column(modifier = Modifier.weight(0.3f).fillMaxHeight()) {
                PuzzleList(
                    controller = controller,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
fun PiecePalette(
    selectedPiece: Piece?,
    onPieceSelect: (Piece?) -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme
    val whitePieces = listOf(
        Pawn(PieceColor.WHITE), Rook(PieceColor.WHITE), Knight(PieceColor.WHITE),
        Bishop(PieceColor.WHITE), Queen(PieceColor.WHITE), King(PieceColor.WHITE)
    )
    val blackPieces = listOf(
        Pawn(PieceColor.BLACK), Rook(PieceColor.BLACK), Knight(PieceColor.BLACK),
        Bishop(PieceColor.BLACK), Queen(PieceColor.BLACK), King(PieceColor.BLACK)
    )

    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(colorScheme.surfaceVariant.copy(alpha = 0.8f))
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            whitePieces.forEach { piece ->
                PaletteItem(piece, selectedPiece == piece) { onPieceSelect(piece) }
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            blackPieces.forEach { piece ->
                PaletteItem(piece, selectedPiece == piece) { onPieceSelect(piece) }
            }
            // Eraser
            PaletteItem(null, selectedPiece == null) { onPieceSelect(null) }
        }
    }
}

@Composable
private fun PaletteItem(
    piece: Piece?,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme
    Box(
        modifier = Modifier
            .size(40.dp)
            .clip(RoundedCornerShape(4.dp))
            .background(if (isSelected) colorScheme.primary.copy(alpha = 0.3f) else Color.Transparent)
            .border(
                width = if (isSelected) 2.dp else 1.dp,
                color = if (isSelected) colorScheme.primary else colorScheme.outline.copy(alpha = 0.3f),
                shape = RoundedCornerShape(4.dp)
            )
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        if (piece != null) {
            PieceImage(piece = piece, modifier = Modifier.size(32.dp))
        } else {
            Text("X", color = colorScheme.error, fontWeight = FontWeight.Bold)
        }
    }
}
