package com.mygdx.game.model

import com.badlogic.gdx.graphics.g2d.Sprite
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.mygdx.game.SQUARE_SIZE

abstract class BoardSquare(x: Float, y: Float, val sprite: Sprite) {

    init {
        sprite.setSize(SQUARE_SIZE, SQUARE_SIZE)
        sprite.setPosition(x, y)
    }

    var x = x
        set(value) {
            field = value;
            sprite.setPosition(value, y)
        }

    var y = y
        set(value) {
            field = value
            sprite.setPosition(x, value)
        }

    fun draw(batch: SpriteBatch) = sprite.draw(batch)
}