package com.mygdx.game.moveGenerator

import com.github.bhlangonijr.chesslib.Board
import com.github.bhlangonijr.chesslib.move.Move
import com.github.bhlangonijr.chesslib.move.MoveGenerator

class RandomMoveGenerator : AIMoveGenerator {
    override fun generateMove(board: Board): Move {
        return MoveGenerator.generateLegalMoves(board).toList().random()
    }
}