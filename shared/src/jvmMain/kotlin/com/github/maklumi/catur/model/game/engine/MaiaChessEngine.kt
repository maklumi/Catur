package com.github.maklumi.catur.model.game.engine

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedWriter
import java.io.File
import java.util.Scanner

class MaiaChessEngine : ChessEngine {
    private var process: Process? = null
    private var currentModel: String? = null
    private var output: BufferedWriter? = null
    private var input: Scanner? = null

    init {
        Runtime.getRuntime().addShutdownHook(Thread {
            stop()
        })
    }

    private fun startProcess(model: String) {
        stop()
        try {
            val p = ProcessBuilder("python", "-m", "maia3.uci", "--model", model)
                .directory(File("E:\\maia3"))
                .redirectErrorStream(true)
                .start()
            
            process = p
            currentModel = model
            output = p.outputStream.bufferedWriter()
            input = Scanner(p.inputStream)

            val out = output!!
            val inp = input!!

            out.write("uci\n")
            out.flush()
            while (inp.hasNextLine()) {
                if (inp.nextLine().contains("uciok")) break
            }

            out.write("isready\n")
            out.flush()
            while (inp.hasNextLine()) {
                if (inp.nextLine().contains("readyok")) break
            }
        } catch (e: Exception) {
            e.printStackTrace()
            stop()
        }
    }

    override suspend fun getBestMove(moves: List<String>, model: String): String? = withContext(Dispatchers.IO) {
        if (process == null || currentModel != model || !process!!.isAlive) {
            startProcess(model)
        }

        val out = output ?: return@withContext null
        val inp = input ?: return@withContext null

        try {
            val positionCmd = if (moves.isEmpty()) "position startpos\n" else "position startpos moves ${moves.joinToString(" ")}\n"
            out.write(positionCmd)
            out.write("go movetime 1000\n")
            out.flush()

            while (inp.hasNextLine()) {
                val line = inp.nextLine()
                if (line.startsWith("bestmove")) {
                    return@withContext line.split(" ")[1]
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            stop()
        }
        null
    }

    override suspend fun evaluate(moves: List<String>): Int = withContext(Dispatchers.IO) {
        if (process == null || !process!!.isAlive) {
            startProcess(currentModel ?: "maia3-5m")
        }

        val out = output ?: return@withContext 0
        val inp = input ?: return@withContext 0

        try {
            val positionCmd = if (moves.isEmpty()) "position startpos\n" else "position startpos moves ${moves.joinToString(" ")}\n"
            out.write(positionCmd)
            out.write("go movetime 100\n")
            out.flush()

            var score = 0
            while (inp.hasNextLine()) {
                val line = inp.nextLine()
                if (line.contains("score cp")) {
                    val parts = line.split("score cp ")
                    if (parts.size > 1) {
                        score = parts[1].split(" ")[0].toIntOrNull() ?: 0
                    }
                }
                if (line.startsWith("bestmove")) {
                    return@withContext score
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            stop()
        }
        0
    }

    override fun stop() {
        try {
            output?.write("quit\n")
            output?.flush()
        } catch (_: Exception) {}
        
        process?.destroy()
        if (process?.isAlive == true) {
            process?.destroyForcibly()
        }
        
        process = null
        output = null
        input = null
        currentModel = null
    }
}
