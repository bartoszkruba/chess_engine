package com.mygdx.game.desktop

import com.badlogic.gdx.backends.lwjgl.LwjglApplication
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration
import com.mygdx.game.Game
import com.mygdx.game.SQUARE_SIZE

fun main() {
    val config = LwjglApplicationConfiguration()
    config.width = (8 * SQUARE_SIZE).toInt()
    config.height = (8 * SQUARE_SIZE).toInt()
    LwjglApplication(Game(), config)
}