package com.mygdx.game.moveGenerator

import com.github.bhlangonijr.chesslib.Board
import com.github.bhlangonijr.chesslib.PieceType
import com.github.bhlangonijr.chesslib.Side
import com.github.bhlangonijr.chesslib.move.Move
import com.github.bhlangonijr.chesslib.move.MoveGenerator

private data class MoveValue(val move: Move, val value: Int)

var total = 0

class MinimaxMoveGenerator(val depth: Int = 5) : AIMoveGenerator {
    companion object {
        private val board = Board()
    }

    override fun generateMove(board: Board) = Node(board.fen, this.depth, board.sideToMove == Side.WHITE)
            .children.map {
                MoveValue(it.move!!, it.getValue(board.sideToMove == Side.WHITE))
            }.shuffled().sortedWith(Comparator { p0, p1 ->
                if (board.sideToMove != Side.WHITE) p0!!.value - p1!!.value
                else p1!!.value - p0!!.value
            }).first().move

    private class Node(val state: String, depth: Int, whiteTurn: Boolean, val move: Move? = null,
                       val parent: Node? = null) {
        var children: Array<Node> = emptyArray()
        private val boardValue = getValue(whiteTurn)

        init {
            total++
            if (total % 10 == 0) println(total)
            if (depth > 0 && shouldGetChildren(whiteTurn)) children = getChildren(depth, whiteTurn)
        }

        private fun shouldGetChildren(whiteTurn: Boolean): Boolean {
            if (parent == null) return true
            return (whiteTurn && parent.boardValue <= boardValue) ||
                    (!whiteTurn && parent.boardValue >= boardValue)
        }

        private fun getChildren(depth: Int, whiteTurn: Boolean): Array<Node> {
            board.loadFromFen(state)
            return MoveGenerator.generateLegalMoves(board).map {
                board.doMove(it)
                val node = Node(board.fen, depth - 1, whiteTurn, it, this)
                board.loadFromFen(state)
                node
            }.toTypedArray()
        }

        fun getValue(whiteTurn: Boolean): Int {
            return if (children.isEmpty()) boardValue
            else children.map { it.getValue(whiteTurn) }.reduce { v1, v2 ->
                if (whiteTurn) {
                    if (v1 > v2) return v1 else v2
                } else {
                    if (v1 < v2) return v1 else v2
                }
            }
        }

        private fun getBoardValue(board: Board) = board.boardToArray().map {
            val value = when (it.pieceType) {
                PieceType.PAWN -> 10
                PieceType.KNIGHT, PieceType.BISHOP -> 30
                PieceType.ROOK -> 50
                PieceType.QUEEN -> 90
                else -> 900
            } * if (it.pieceSide == Side.WHITE) 1 else if (it.pieceType == null) 0 else -1
            value
        }.reduce { v1, v2 -> v1 + v2 }

        private fun getBoardValue(fen: String) = getBoardValue(Board().apply { loadFromFen(fen) })
    }
}