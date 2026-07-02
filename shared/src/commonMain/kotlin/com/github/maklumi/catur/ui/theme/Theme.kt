package com.github.maklumi.catur.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import com.github.maklumi.catur.model.game.state.BoardTheme

@Immutable
data class BoardColors(
    val lightSquare: Color = BoardLight,
    val darkSquare: Color = BoardDark,
    val lastMove: Color = BoardLastMove,
    val selected: Color = BoardSelected,
    val threatenedLight: Color = BoardThreatenedLight,
    val threatenedDark: Color = BoardThreatenedDark
)

val LocalBoardColors = staticCompositionLocalOf { BoardColors() }

private val DarkColorScheme = darkColorScheme(
    primary = Purple80,
    secondary = PurpleGrey80,
    tertiary = Pink80,
    background = Color(0xFF1C1B1F),
    onBackground = Color(0xFFE6E1E5),
    surface = Color(0xFF1C1B1F),
    onSurface = Color(0xFFE6E1E5)
)

private val LightColorScheme = lightColorScheme(
    primary = Purple40,
    secondary = PurpleGrey40,
    tertiary = Pink40,
    background = Color(0xFFFFFBFE),
    onBackground = Color(0xFF1C1B1F),
    surface = Color(0xFFFFFBFE),
    onSurface = Color(0xFF1C1B1F)
)

@Composable
fun CaturTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    boardTheme: BoardTheme = BoardTheme.GREEN,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    val boardColors = when (boardTheme) {
        BoardTheme.GREEN -> BoardColors(BoardLight, BoardDark)
        BoardTheme.WOOD -> BoardColors(WoodLight, WoodDark)
        BoardTheme.BLUE -> BoardColors(BlueLight, BlueDark)
        BoardTheme.CLASSIC -> BoardColors(ClassicLight, ClassicDark)
    }

    CompositionLocalProvider(LocalBoardColors provides boardColors) {
        MaterialTheme(
            colorScheme = colorScheme,
            content = content
        )
    }
}

object CaturTheme {
    val board: BoardColors
        @Composable
        get() = LocalBoardColors.current
}
