package com.mygdx.game

import com.github.bhlangonijr.chesslib.Board
import com.github.bhlangonijr.chesslib.move.Move
import com.github.bhlangonijr.chesslib.move.MoveGenerator

class AIMoveGenerator {
    public fun generateMove(board: Board): Move {
        return MoveGenerator.generateLegalMoves(board).toList().random()
    }
}