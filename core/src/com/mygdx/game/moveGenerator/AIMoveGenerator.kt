package com.mygdx.game.moveGenerator

import com.github.bhlangonijr.chesslib.Board
import com.github.bhlangonijr.chesslib.move.Move

interface AIMoveGenerator {
    fun generateMove(board: Board): Move
}