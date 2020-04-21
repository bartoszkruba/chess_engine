package com.mygdx.game.model

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.Sprite
import com.mygdx.game.assets.Textures

class PossibleMove(x: Float, y: Float, textures: Textures) : BoardSquare(
        x, y, Sprite(textures.lightSquareTexture)
) {
    init {
        sprite.color = Color().apply { set(0f, 250f, 0f, 0.5f) }
    }
}