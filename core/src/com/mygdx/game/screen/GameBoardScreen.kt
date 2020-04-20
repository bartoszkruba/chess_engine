package com.mygdx.game.screen

import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.utils.Array
import com.mygdx.game.Game
import com.mygdx.game.assets.Textures
import com.mygdx.game.model.BoardSquare
import com.mygdx.game.model.DarkBoardSquare
import com.mygdx.game.model.LightBoardSquare
import ktx.app.KtxScreen
import ktx.collections.iterate
import ktx.graphics.use

class GameBoardScreen(val game: Game) : KtxScreen {
    private val textures = Textures(game.assets)

    private val camera = OrthographicCamera().apply {
        setToOrtho(false, 960f, 960f)
    }

    private val board = createBoard()
    private val pieces = initializePieces()

    override fun render(delta: Float) {
        super.render(delta)
        game.batch.projectionMatrix = camera.combined
        game.batch.use {
            renderBoard(it)
            renderPieces(it)
        }
    }

    private fun renderBoard(batch: SpriteBatch) = board.iterate { square, _ -> square.draw(batch) }

    private fun renderPieces(batch: SpriteBatch) = pieces.iterate { piece, _ -> piece.draw(batch) }

    private fun createBoard(): Array<BoardSquare> {
        val board = Array<BoardSquare>()
        var dark = true

        for (i in 0 until 8) {
            for (j in 0 until 8) {
                if (dark) board.add(DarkBoardSquare(i * 120f, j * 120f, textures))
                else board.add(LightBoardSquare(i * 120f, j * 120f, textures))
                dark = !dark
            }
            dark = !dark
        }
        return board
    }

    private fun initializePieces(): Array<BoardSquare> {
        return Array()
    }
}