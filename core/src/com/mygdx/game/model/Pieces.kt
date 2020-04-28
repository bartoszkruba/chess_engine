package com.mygdx.game.model

import com.badlogic.gdx.graphics.g2d.Sprite
import com.mygdx.game.assets.Textures

abstract class BoardPiece(x: Float, y: Float, val dark: Boolean, sprite: Sprite) : BoardSquare(x, y, sprite)

class Pawn(x: Float, y: Float, textures: Textures, dark: Boolean) : BoardPiece(
        x, y, dark, Sprite(if (dark) textures.blackPawn else textures.whitePawn)
)

class Knight(x: Float, y: Float, textures: Textures, dark: Boolean) : BoardPiece(
        x, y, dark, Sprite(if (dark) textures.blackKnight else textures.whiteKnight)
)

class Bishop(x: Float, y: Float, textures: Textures, dark: Boolean) : BoardPiece(
        x, y, dark, Sprite(if (dark) textures.blackBishop else textures.whiteBishop)
)

class Rook(x: Float, y: Float, textures: Textures, dark: Boolean) : BoardPiece(
        x, y, dark, Sprite(if (dark) textures.blackRook else textures.whiteRook)
)

class King(x: Float, y: Float, textures: Textures, dark: Boolean) : BoardPiece(
        x, y, dark, Sprite(if (dark) textures.blackKing else textures.whiteKing)
)

class Queen(x: Float, y: Float, textures: Textures, dark: Boolean) : BoardPiece(
        x, y, dark, Sprite(if (dark) textures.blackQueen else textures.whiteQueen)
)

