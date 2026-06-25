package com.github.maklumi.catur.model.game.engine

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.BufferedWriter
import java.io.File
import java.io.InputStreamReader

class MaiaChessEngine : ChessEngine {
    private var process: Process? = null
    private var currentModel: String? = null
    private var output: BufferedWriter? = null
    private var input: BufferedReader? = null
    private val mutex = Mutex()

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
            input = BufferedReader(InputStreamReader(p.inputStream))

            val out = output!!
            val inp = input!!

            out.write("uci\n")
            out.flush()
            println("[Maia] Sent 'uci', waiting for 'uciok'...")
            while (true) {
                val line = inp.readLine() ?: break
                println("[Maia Raw] $line")
                if (line.contains("uciok")) break
            }

            out.write("isready\n")
            out.flush()
            println("[Maia] Sent 'isready', waiting for 'readyok'...")
            while (true) {
                val line = inp.readLine() ?: break
                println("[Maia Raw] $line")
                if (line.contains("readyok")) break
            }
            println("[Maia] Engine process started and ready.")
        } catch (e: Exception) {
            e.printStackTrace()
            stop()
        }
    }

    override suspend fun getBestMove(moves: List<String>, model: String): String? = withContext(Dispatchers.IO) {
        mutex.withLock {
            if (process == null || currentModel != model || !process!!.isAlive) {
                startProcess(model)
            }

            val out = output ?: return@withLock null
            val inp = input ?: return@withLock null

            try {
                val positionCmd = if (moves.isEmpty()) "position startpos\n" else "position startpos moves ${moves.joinToString(" ")}\n"
                println("[Maia] Sending: $positionCmd")
                out.write(positionCmd)
                out.write("go movetime 1000\n")
                out.flush()

                while (true) {
                    val line = inp.readLine() ?: break
                    println("[Maia Raw] $line")
                    if (line.startsWith("bestmove")) {
                        val parts = line.split(" ")
                        val move = if (parts.size > 1) parts[1] else null
                        return@withLock if (move == null || move == "(none)") null else move
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                stop()
            }
            null
        }
    }

    override suspend fun evaluate(moves: List<String>): Int = withContext(Dispatchers.IO) {
        mutex.withLock {
            if (process == null || !process!!.isAlive) {
                startProcess(currentModel ?: "maia3-5m")
            }

            val out = output ?: return@withLock 0
            val inp = input ?: return@withLock 0

            try {
                val positionCmd = if (moves.isEmpty()) "position startpos\n" else "position startpos moves ${moves.joinToString(" ")}\n"
                println("[Maia] Sending eval: $positionCmd")
                out.write(positionCmd)
                out.write("go movetime 100\n")
                out.flush()

                var score = 0
                while (true) {
                    val line = inp.readLine() ?: break
                    println("[Maia Raw] $line")
                    if (line.contains("score cp")) {
                        val parts = line.split("score cp ")
                        if (parts.size > 1) {
                            val scorePart = parts[1].split(" ").firstOrNull()
                            score = scorePart?.toIntOrNull() ?: score
                        }
                    }
                    if (line.startsWith("bestmove")) {
                        return@withLock score
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                stop()
            }
            0
        }
    }

    override fun stop() {
        // Run in a separate thread/scope to not block if called from main
        // But since it's a simple destroy, we'll just try-lock or similar if needed
        // For simplicity in KMP/JVM, we'll just use a non-blocking approach
        
        val p = process
        val out = output
        
        try {
            out?.write("quit\n")
            out?.flush()
        } catch (_: Exception) {}
        
        p?.destroy()
        if (p?.isAlive == true) {
            p?.destroyForcibly()
        }
        
        process = null
        output = null
        input = null
        currentModel = null
    }
}
