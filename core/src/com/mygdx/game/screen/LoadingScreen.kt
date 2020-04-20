package com.mygdx.game.screen

import com.mygdx.game.Game
import com.mygdx.game.assets.TextureAssets
import com.mygdx.game.assets.load
import ktx.app.KtxScreen

class LoadingScreen(val game: Game) : KtxScreen {

    override fun show() {
        TextureAssets.values().forEach { game.assets.load(it) }
    }

    override fun render(delta: Float) {
        game.assets.update()

        if (game.assets.isFinished) {
            game.addScreen(GameBoardScreen(game))
            game.setScreen<GameBoardScreen>()
            dispose()
        }
    }
}