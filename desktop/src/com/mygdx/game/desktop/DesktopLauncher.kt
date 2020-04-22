package com.mygdx.game.desktop

import com.badlogic.gdx.backends.lwjgl.LwjglApplication
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration
import com.mygdx.game.Game
import com.mygdx.game.SQUARE_SIZE

fun main() {
    val config = LwjglApplicationConfiguration()
    config.width = (12 * SQUARE_SIZE * 0.75).toInt()
    config.height = (9 * SQUARE_SIZE * 0.75).toInt()
    LwjglApplication(Game(), config)
}