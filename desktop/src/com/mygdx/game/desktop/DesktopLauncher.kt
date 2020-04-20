package com.mygdx.game.desktop

import com.badlogic.gdx.backends.lwjgl.LwjglApplication
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration
import com.mygdx.game.Game

fun main() {
    val config = LwjglApplicationConfiguration()
    config.width = 960
    config.height = 960
    LwjglApplication(Game(), config)
}