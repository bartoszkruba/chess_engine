package com.mygdx.game.model

import com.badlogic.gdx.graphics.g2d.Sprite
import com.mygdx.game.assets.Textures

class Pawn(x: Float, y: Float, textures: Textures, dark: Boolean) : BoardSquare(
        x, y, Sprite(if (dark) textures.blackPawn else textures.whitePawn)
)

class Knight(x: Float, y: Float, textures: Textures, dark: Boolean) : BoardSquare(
        x, y, Sprite(if (dark) textures.blackKnight else textures.whiteKnight)
)

class Bishop(x: Float, y: Float, textures: Textures, dark: Boolean) : BoardSquare(
        x, y, Sprite(if (dark) textures.blackBishop else textures.whiteBishop)
)

class Rook(x: Float, y: Float, textures: Textures, dark: Boolean) : BoardSquare(
        x, y, Sprite(if (dark) textures.blackRook else textures.whiteRook)
)

class King(x: Float, y: Float, textures: Textures, dark: Boolean) : BoardSquare(
        x, y, Sprite(if (dark) textures.blackKing else textures.whiteKing)
)

class Queen(x: Float, y: Float, textures: Textures, dark: Boolean) : BoardSquare(
        x, y, Sprite(if (dark) textures.blackQueen else textures.whiteQueen)
)

