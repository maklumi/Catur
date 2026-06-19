package com.github.maklumi.catur.model.game.engine

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.util.Scanner

class MaiaChessEngine : ChessEngine {
    override suspend fun getBestMove(moves: List<String>): String? = withContext(Dispatchers.IO) {
        try {
            val process = ProcessBuilder("python", "-m", "maia3.uci", "--model", "maia3-5m")
                .directory(File("E:\\maia3"))
                .redirectErrorStream(true)
                .start()

            val output = process.outputStream.bufferedWriter()
            val input = Scanner(process.inputStream)

            output.write("uci\n")
            output.flush()
            while (input.hasNextLine()) {
                val line = input.nextLine()
                if (line.contains("uciok")) break
            }

            output.write("isready\n")
            output.flush()
            while (input.hasNextLine()) {
                val line = input.nextLine()
                if (line.contains("readyok")) break
            }

            val positionCmd = if (moves.isEmpty()) "position startpos\n" else "position startpos moves ${moves.joinToString(" ")}\n"
            output.write(positionCmd)
            output.write("go movetime 1000\n")
            output.flush()

            var bestMove: String? = null
            while (input.hasNextLine()) {
                val line = input.nextLine()
                if (line.startsWith("bestmove")) {
                    bestMove = line.split(" ")[1]
                    break
                }
            }

            output.write("quit\n")
            output.flush()
            process.destroy()
            return@withContext bestMove
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
