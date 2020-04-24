package com.mygdx.game

import com.badlogic.gdx.assets.AssetManager
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import ktx.app.KtxGame
import ktx.app.KtxScreen
import com.mygdx.game.screen.MenuScreen

class Game : KtxGame<KtxScreen>() {

    val batch by lazy { SpriteBatch() }
    val font by lazy { BitmapFont().apply { data.setScale(2f) } }
    val assets by lazy { AssetManager() }
    val aiMoveGenerator = AIMoveGenerator()

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