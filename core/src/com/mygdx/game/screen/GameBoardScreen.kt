package com.mygdx.game.screen

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.math.Vector3
import com.badlogic.gdx.utils.Array
import com.github.bhlangonijr.chesslib.Board
import com.github.bhlangonijr.chesslib.Square
import com.github.bhlangonijr.chesslib.move.Move
import com.github.bhlangonijr.chesslib.move.MoveGenerator
import com.mygdx.game.Game
import com.mygdx.game.SQUARE_SIZE
import com.mygdx.game.assets.Textures
import com.mygdx.game.model.*
import ktx.app.KtxScreen
import ktx.collections.iterate
import ktx.graphics.use
import java.lang.Exception

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

    private val validationBoard = Board()
    private val board = createBoard()
    private val pieces = initializePieces()
    private val possibleMoves = Array<PossibleMove>()

    private val numberToLetter = hashMapOf(1 to "A", 2 to "B", 3 to "C", 4 to "D", 5 to "E", 6 to "F",
            7 to "G", 8 to "H")
    private val letterToNumber = hashMapOf("A" to 1, "B" to 2, "C" to 3, "D" to 4, "E" to 5, "F" to 6,
            "G" to 7, "H" to 8)

    override fun render(delta: Float) {
        super.render(delta)

        getMousePosInGameWorld()
        processControls()

        game.batch.projectionMatrix = camera.combined
        game.batch.use {
            renderBoard(it)
            renderPossibleMoves(it)
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

                val square = positionToSquare(Vector2(selectedPiece!!.x, selectedPiece!!.y))
                MoveGenerator.generateLegalMoves(validationBoard)
                        .filter { it.from == square }
                        .map { squareToPosition(it.to) }
                        .map { PossibleMove(it.x, it.y, textures) }
                        .forEach { possibleMoves.add(it) }

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
                possibleMoves.clear()
                wasLeftMousePressed = false

                val position = mousePosition

                if (moveCanBePerformed(selectedPieceInitialPosition, position)) {
                    normalizePosition(position)
                    validationBoard.doMove(Move(positionToSquare(selectedPieceInitialPosition), positionToSquare(position)))
                    selectedPiece?.x = position.x
                    selectedPiece?.y = position.y
                } else {
                    selectedPiece?.y = selectedPieceInitialPosition.y
                    selectedPiece?.x = selectedPieceInitialPosition.x
                }
                selectedPiece = null
            }
        }
    }

    private fun normalizePosition(position: Vector2) {
        position.x = (position.x / SQUARE_SIZE).toInt() * SQUARE_SIZE
        position.y = (position.y / SQUARE_SIZE).toInt() * SQUARE_SIZE
    }

    private fun moveCanBePerformed(from: Vector2, to: Vector2): Boolean {
        return try {
            val initial = positionToSquare(from)
            val new = positionToSquare(to)
            validationBoard.isMoveLegal(Move(initial, new), true)
        } catch (ex: Exception) {
            false
        }
    }

    private fun findPiece(position: Vector2): BoardSquare? = pieces.find { boardSquare ->
        (position.x > boardSquare.x && position.x < boardSquare.x + SQUARE_SIZE &&
                position.y > boardSquare.y && position.y < boardSquare.y + SQUARE_SIZE)
    }

    private fun positionToSquare(position: Vector2) = Square.fromValue(
            (numberToLetter[((position.x / SQUARE_SIZE).toInt() + 1)] +
                    ((position.y / SQUARE_SIZE).toInt() + 1)))

    private fun squareToPosition(square: Square): Vector2 {
        println(square.toString())
        val letter = square.toString()[0].toString()
        val number = square.toString()[1].toString().toInt()
        val x = ((letterToNumber[letter]!! - 1) * SQUARE_SIZE)
        val y = (number - 1) * SQUARE_SIZE
        return Vector2(x, y)
    }

    private fun renderBoard(batch: SpriteBatch) = board.iterate { square, _ -> square.draw(batch) }

    private fun renderPieces(batch: SpriteBatch) = pieces.iterate { piece, _ -> piece.draw(batch) }

    private fun renderPossibleMoves(batch: SpriteBatch) = possibleMoves.iterate { move, _ -> move.draw(batch) }

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