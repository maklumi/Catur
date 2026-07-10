package com.github.maklumi.catur.domain.engine

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
data class MoveResponse(val move: String?)

@Serializable
data class EvalResponse(val score: Int)

@Serializable
data class TopMove(val move: String, val score: Int)

@Serializable
data class TopMovesResponse(val moves: List<TopMove>)

class RemoteChessEngine(
    private val baseUrl: String
) : ChessEngine {
    
    private val client = HttpClient {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
            })
        }
    }
    
    override suspend fun getBestMove(
        moves: List<String>,
        model: String,
        fen: String?
    ): String? {
        return try {
            val response = client.get("$baseUrl/best-move") {
                parameter("moves", moves.joinToString(" "))
                parameter("model", model)
                fen?.let { parameter("fen", it) }
            }
            response.body<MoveResponse>().move
        } catch (t: Throwable) {
            println("Engine Error (getBestMove): ${t.message}")
            null
        }
    }

    override suspend fun getTopMoves(
        moves: List<String>,
        model: String,
        count: Int,
        fen: String?
    ): List<Pair<String, Int>> {
        return try {
            val response = client.get("$baseUrl/top-moves") {
                parameter("moves", moves.joinToString(" "))
                parameter("model", model)
                parameter("count", count)
                fen?.let { parameter("fen", it) }
            }
            response.body<TopMovesResponse>().moves.map { it.move to it.score }
        } catch (t: Throwable) {
            println("Engine Error (getTopMoves): ${t.message}")
            emptyList()
        }
    }

    override suspend fun evaluate(moves: List<String>, model: String, fen: String?): Int {
        return try {
            val response = client.get("$baseUrl/evaluate") {
                parameter("moves", moves.joinToString(" "))
                parameter("model", model)
                fen?.let { parameter("fen", it) }
            }
            response.body<EvalResponse>().score
        } catch (t: Throwable) {
            println("Engine Error (evaluate): ${t.message}")
            0
        }
    }

    override fun stop() {
        client.close()
    }
}
