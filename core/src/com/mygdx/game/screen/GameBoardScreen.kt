package com.mygdx.game.screen

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.math.Vector3
import com.badlogic.gdx.utils.Array
import com.mygdx.game.Game
import com.mygdx.game.SQUARE_SIZE
import com.mygdx.game.assets.Textures
import com.mygdx.game.model.*
import ktx.app.KtxScreen
import ktx.collections.iterate
import ktx.graphics.use

class GameBoardScreen(val game: Game) : KtxScreen {
    private val textures = Textures(game.assets)

    private val camera = OrthographicCamera().apply {
        setToOrtho(false, 960f, 960f)
    }

    private var wasLeftMousePressed: Boolean = false
    private val mousePosition = Vector2()
    private val mouseInitialPosition = Vector2()
    private var selectedPiece: BoardSquare? = null
    private val selectedPieceInitialPosition = Vector2()

    private val board = createBoard()
    private val pieces = initializePieces()

    override fun render(delta: Float) {
        super.render(delta)

        getMousePosInGameWorld()
        processControls()

        game.batch.projectionMatrix = camera.combined
        game.batch.use {
            renderBoard(it)
            renderPieces(it)
        }
    }

    private fun processControls() {

        if (Gdx.input.isButtonJustPressed(Input.Buttons.LEFT)) {
            wasLeftMousePressed = true
            val piece = findPiece(mousePosition)
            if (piece != null) {
                selectedPieceInitialPosition.y = piece.y
                selectedPieceInitialPosition.x = piece.x
                selectedPiece = piece

                mouseInitialPosition.y = mousePosition.y
                mouseInitialPosition.x = mousePosition.x
            }

        } else if (wasLeftMousePressed) {
            if (Gdx.input.isButtonPressed(Input.Keys.CONTROL_LEFT)) {
                val deltaX = mouseInitialPosition.x - mousePosition.x
                val deltaY = mouseInitialPosition.y - mousePosition.y
                selectedPiece?.y = selectedPieceInitialPosition.y - deltaY
                selectedPiece?.x = selectedPieceInitialPosition.x - deltaX
            } else {
                wasLeftMousePressed = false
                selectedPiece?.y = selectedPieceInitialPosition.y
                selectedPiece?.x = selectedPieceInitialPosition.x
                selectedPiece = null
            }
        }
    }

    private fun findPiece(position: Vector2): BoardSquare? = pieces.find { boardSquare ->
        (position.x > boardSquare.x && position.x < boardSquare.x + SQUARE_SIZE &&
                position.y > boardSquare.y && position.y < boardSquare.y + SQUARE_SIZE)
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
        val pieces = Array<BoardSquare>()

        for (i in 0 until 2) {
            for (j in 0 until 8) {
                val dark = i != 0
                pieces.add(Pawn(j * SQUARE_SIZE, i * 5 * SQUARE_SIZE + SQUARE_SIZE, textures, dark))
            }
        }

        for (i in 0 until 2) {
            val dark = i != 0
            pieces.add(Knight(SQUARE_SIZE, i * 7 * SQUARE_SIZE, textures, dark))
            pieces.add(Knight(6 * 120f, i * 7 * 120f, textures, dark))
        }

        for (i in 0 until 2) {
            val dark = i != 0
            pieces.add(Rook(0f, i * 7 * SQUARE_SIZE, textures, dark))
            pieces.add(Rook(7 * SQUARE_SIZE, i * 7 * SQUARE_SIZE, textures, dark))
        }

        for (i in 0 until 2) {
            val dark = i != 0
            pieces.add(Bishop(2 * SQUARE_SIZE, i * 7 * SQUARE_SIZE, textures, dark))
            pieces.add(Bishop(5 * SQUARE_SIZE, i * 7 * SQUARE_SIZE, textures, dark))
        }

        pieces.add(King(4 * SQUARE_SIZE, 0f, textures, false))
        pieces.add(King(4 * SQUARE_SIZE, 7 * SQUARE_SIZE, textures, true))

        pieces.add(Queen(3 * SQUARE_SIZE, 0f, textures, false))
        pieces.add(Queen(3 * SQUARE_SIZE, 7 * SQUARE_SIZE, textures, true))

        return pieces
    }

    private fun getMousePosInGameWorld() {
        val position = camera.unproject(Vector3(Gdx.input.x.toFloat(), Gdx.input.y.toFloat(), 0f))
        mousePosition.x = position.x
        mousePosition.y = position.y
    }
}