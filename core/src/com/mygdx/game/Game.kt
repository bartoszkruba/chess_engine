package com.mygdx.game

import com.badlogic.gdx.assets.AssetManager
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.mygdx.game.moveGenerator.AIMoveGenerator
import com.mygdx.game.moveGenerator.MinimaxMoveGenerator
import com.mygdx.game.moveGenerator.RandomMoveGenerator
import com.mygdx.game.moveGenerator.ResnetVersion1MoveGenerator
import ktx.app.KtxGame
import ktx.app.KtxScreen
import com.mygdx.game.screen.MenuScreen

private const val MODEL_1_PATH = "/home/bartoszkruba/Desktop/models/resnet_chess_1588732752320.zip"

class Game : KtxGame<KtxScreen>() {

    val batch by lazy { SpriteBatch() }
    val font by lazy { BitmapFont().apply { data.setScale(2f) } }
    val assets by lazy { AssetManager() }

    val aiMoveGenerator: AIMoveGenerator = ResnetVersion1MoveGenerator(MODEL_1_PATH)
//    val aiMoveGenerator: AIMoveGenerator = MinimaxMoveGenerator()

    override fun create() {
        addScreen(MenuScreen(this))
        setScreen<MenuScreen>()
        super.create()
    }

    override fun dispose() {
        batch.dispose()
        font.dispose()
        assets.dispose()
        super.dispose()
    }
}