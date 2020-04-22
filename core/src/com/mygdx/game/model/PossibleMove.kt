package com.mygdx.game.model

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.Sprite
import com.mygdx.game.assets.Textures

class PossibleMove(x: Float, y: Float, textures: Textures, red: Boolean) : BoardSquare(
        x, y, Sprite(textures.lightSquareTexture)
) {
    init {
        if (red) sprite.color = Color().apply { set(255f, 0f, 0f, 0.5f) }
        else sprite.color = Color().apply { set(0f, 255f, 0f, 0.5f) }
    }
}