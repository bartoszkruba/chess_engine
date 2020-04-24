package com.mygdx.game.screen

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.g2d.Sprite
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.math.Vector3
import com.mygdx.game.Game
import com.mygdx.game.SQUARE_SIZE
import com.mygdx.game.assets.TextureAssets
import com.mygdx.game.assets.Textures
import com.mygdx.game.assets.load
import ktx.app.KtxScreen
import ktx.graphics.use

class MenuScreen(val game: Game) : KtxScreen {

    private val camera = OrthographicCamera().apply {
        setToOrtho(false, 12 * SQUARE_SIZE, 9 * SQUARE_SIZE)
        position.x -= SQUARE_SIZE / 2f
        position.y -= SQUARE_SIZE / 2f
        update()
    }

    private val mousePosition = Vector2()

    private val textures by lazy { Textures(game.assets) }
    private val hal9000 by lazy {
        Sprite(textures.hal9000).apply {
            setSize(12 * SQUARE_SIZE, 9 * SQUARE_SIZE)
            setPosition(-SQUARE_SIZE / 2f, -SQUARE_SIZE / 2f)
        }
    }
    private val whitePawn by lazy {
        Sprite(textures.whitePawn).apply {
            setSize(SQUARE_SIZE, SQUARE_SIZE)
            setPosition(SQUARE_SIZE, 2 * SQUARE_SIZE)
        }
    }
    private val blackPawn by lazy {
        Sprite(textures.blackPawn).apply {
            setSize(SQUARE_SIZE, SQUARE_SIZE)
            setPosition(9 * SQUARE_SIZE, 2 * SQUARE_SIZE)
        }
    }

    override fun show() {
        TextureAssets.values().forEach { game.assets.load(it) }
    }

    override fun render(delta: Float) {
        game.assets.update()
        if (game.assets.isFinished) {
            game.batch.projectionMatrix = camera.combined
            game.batch.use {
                getMousePosInGameWorld()
                hal9000.draw(it)
                whitePawn.draw(it)
                blackPawn.draw(it)
                checkControls()
            }
        }
    }

    private fun checkControls() {
        if (Gdx.input.isButtonJustPressed(Input.Buttons.LEFT)) {
            if (mousePosition.x >= whitePawn.x && mousePosition.x <= whitePawn.x + whitePawn.width &&
                    mousePosition.y >= whitePawn.y && mousePosition.y <= whitePawn.y + whitePawn.height) {
                switchToGameBoard(true)
            } else if (mousePosition.x >= blackPawn.x && mousePosition.x <= blackPawn.x + blackPawn.width &&
                    mousePosition.y >= blackPawn.y && mousePosition.y <= blackPawn.y + whitePawn.height) {
                switchToGameBoard(false)
            }
        }
    }

    private fun switchToGameBoard(white: Boolean) {
        game.addScreen(GameBoardScreen(game, white))
        game.setScreen<GameBoardScreen>()
        dispose()
    }

    private fun getMousePosInGameWorld() {
        val position = camera.unproject(Vector3(Gdx.input.x.toFloat(), Gdx.input.y.toFloat(), 0f))
        mousePosition.x = position.x
        mousePosition.y = position.y
    }
}