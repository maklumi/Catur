package com.github.maklumi.catur.domain.chess.board

import kotlin.jvm.JvmInline

@JvmInline
value class Position(val index: Int) {
    val file: Int get() = (index / 8) + 1
    val rank: Int get() = (index % 8) + 1

    val ordinal: Int get() = index

    val isLightSquare: Boolean get() = (ordinal + file % 2) % 2 == 0

    override fun toString(): String {
        val f = 'a' + (file - 1)
        return "$f$rank"
    }

    companion object {
        val a1 = Position(0); val a2 = Position(1); val a3 = Position(2); val a4 = Position(3)
        val a5 = Position(4); val a6 = Position(5); val a7 = Position(6); val a8 = Position(7)
        val b1 = Position(8); val b2 = Position(9); val b3 = Position(10); val b4 = Position(11)
        val b5 = Position(12); val b6 = Position(13); val b7 = Position(14); val b8 = Position(15)
        val c1 = Position(16); val c2 = Position(17); val c3 = Position(18); val c4 = Position(19)
        val c5 = Position(20); val c6 = Position(21); val c7 = Position(22); val c8 = Position(23)
        val d1 = Position(24); val d2 = Position(25); val d3 = Position(26); val d4 = Position(27)
        val d5 = Position(28); val d6 = Position(29); val d7 = Position(30); val d8 = Position(31)
        val e1 = Position(32); val e2 = Position(33); val e3 = Position(34); val e4 = Position(35)
        val e5 = Position(36); val e6 = Position(37); val e7 = Position(38); val e8 = Position(39)
        val f1 = Position(40); val f2 = Position(41); val f3 = Position(42); val f4 = Position(43)
        val f5 = Position(44); val f6 = Position(45); val f7 = Position(46); val f8 = Position(47)
        val g1 = Position(48); val g2 = Position(49); val g3 = Position(50); val g4 = Position(51)
        val g5 = Position(52); val g6 = Position(53); val g7 = Position(54); val g8 = Position(55)
        val h1 = Position(56); val h2 = Position(57); val h3 = Position(58); val h4 = Position(59)
        val h5 = Position(60); val h6 = Position(61); val h7 = Position(62); val h8 = Position(63)

        val entries = listOf(
            a1, a2, a3, a4, a5, a6, a7, a8,
            b1, b2, b3, b4, b5, b6, b7, b8,
            c1, c2, c3, c4, c5, c6, c7, c8,
            d1, d2, d3, d4, d5, d6, d7, d8,
            e1, e2, e3, e4, e5, e6, e7, e8,
            f1, f2, f3, f4, f5, f6, f7, f8,
            g1, g2, g3, g4, g5, g6, g7, g8,
            h1, h2, h3, h4, h5, h6, h7, h8
        )

        private fun validate(file: Int, rank: Int) {
            require(file in 1..8)
            require(rank in 1..8)
        }

        fun from(file: Int, rank: Int): Position {
            validate(file, rank)
            return entries[(file - 1) * 8 + (rank - 1)]
        }

        fun from(file: Char, rank: Int): Position =
            from(file - 'a' + 1, rank)

        fun valueOf(name: String): Position {
            val file = name[0] - 'a' + 1
            val rank = name[1].digitToInt()
            return from(file, rank)
        }
    }
}
