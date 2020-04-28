package com.mygdx.game.model

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.Sprite
import com.mygdx.game.assets.Textures

class PossibleMove(x: Float, y: Float, textures: Textures, color: Color) : BoardSquare(
        x, y, Sprite(textures.lightSquareTexture)
) {
    enum class Color {
        GREEN, RED, BLUE
    }

    init {
        sprite.color = when (color) {
            Color.GREEN -> Color().apply { set(0f, 255f, 0f, 0.5f) }
            Color.RED -> Color().apply { set(255f, 0f, 0f, 0.5f) }
            Color.BLUE -> Color().apply { set(0f, 0f, 255f, 0.5f) }
        }
    }
}