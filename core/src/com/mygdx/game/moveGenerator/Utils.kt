package com.mygdx.game.moveGenerator

import com.github.bhlangonijr.chesslib.Board
import com.github.bhlangonijr.chesslib.Piece
import com.github.bhlangonijr.chesslib.Side
import com.github.bhlangonijr.chesslib.Square
import com.github.bhlangonijr.chesslib.move.Move

val whitePawnPromotionSquares = arrayListOf(Square.A8, Square.B8, Square.C8, Square.D8, Square.E8,
        Square.F8, Square.G8, Square.H8)

val blackPawnPromotionSquares = arrayListOf(Square.A1, Square.B1, Square.C1, Square.D1, Square.E1,
        Square.F1, Square.G1, Square.H1)

fun moveIsPromotion(board: Board, move: Move): Boolean {
    val whiteTurn = board.sideToMove == Side.WHITE
    val piece = board.getPiece(move.from)
    return (whiteTurn && piece == Piece.WHITE_PAWN && whitePawnPromotionSquares.contains(move.to) ||
            (!whiteTurn && piece == Piece.BLACK_PAWN && blackPawnPromotionSquares.contains(move.to)))
}