package com.mygdx.game.moveGenerator

import com.github.bhlangonijr.chesslib.Board
import com.github.bhlangonijr.chesslib.Piece
import com.github.bhlangonijr.chesslib.Side
import com.github.bhlangonijr.chesslib.Square
import com.github.bhlangonijr.chesslib.move.Move
import com.github.bhlangonijr.chesslib.move.MoveGenerator
import org.deeplearning4j.nn.graph.ComputationGraph
import org.nd4j.linalg.api.ndarray.INDArray
import org.nd4j.linalg.factory.Nd4j
import java.io.File

class ResnetVersion1MoveGenerator(modelPath: String) : AIMoveGenerator {

    class MoveProbability(val move: Move, val probability: Double)

    val model = ComputationGraph.load(File(modelPath), true)
    val moves = arrayOfNulls<String>(64 * 63).apply {
        var i = 0
        for (s1 in Square.values()) for (s2 in Square.values()) {
            if (s1.toString() != s2.toString() && s1 != Square.NONE && s2 != Square.NONE) {
                this[i] = s1.toString() + s2.toString()
                i++
            }
        }
    }

    private val letterToNumber = hashMapOf("A" to 0, "B" to 1, "C" to 2, "D" to 3, "E" to 4, "F" to 5, "G" to 6, "H" to 7)
    private val figureTypes = setOf(
            Piece.WHITE_PAWN,
            Piece.WHITE_KNIGHT,
            Piece.WHITE_BISHOP,
            Piece.WHITE_ROOK,
            Piece.WHITE_QUEEN,
            Piece.WHITE_KING,
            Piece.BLACK_PAWN,
            Piece.BLACK_KNIGHT,
            Piece.BLACK_BISHOP,
            Piece.BLACK_ROOK,
            Piece.BLACK_QUEEN,
            Piece.BLACK_KING
    )

    override fun generateMove(board: Board): Move {
        val output = model.output(boardToRepresentation(board))
        val legalMoves = MoveGenerator.generateLegalMoves(board)

        val probabilities = ArrayList<MoveProbability>()
        legalMoves.forEach {
            val legalMove = (it.from.toString() + it.to.toString()).toUpperCase()
            val probability = output[0].getDouble(0, moves.indexOf(legalMove))
            probabilities.add(MoveProbability(it, probability * 100))
        }
        probabilities.sortWith(compareByDescending { it.probability })

        val move = probabilities.first().move
        if (moveIsPromotion(board, move))
            return Move(move.from, move.to, if (board.sideToMove == Side.WHITE) Piece.WHITE_QUEEN else Piece.BLACK_QUEEN)
        return move
    }

    private fun boardToRepresentation(board: Board): INDArray {
        val representation = Nd4j.zeros(1, 13, 8, 8)
        for ((counter, figure) in figureTypes.withIndex()) {
            board.getPieceLocation(figure).forEach {
                val letter = it.toString()[0].toString()
                val number = it.toString()[1].toString().toInt()

                // It does not matter for neural network
                // but the input are formatted so that the board is printed out in the right way
                representation.putScalar(intArrayOf(0, counter, 8 - number, letterToNumber[letter]!!), 1)
            }
        }
        return representation
    }
}