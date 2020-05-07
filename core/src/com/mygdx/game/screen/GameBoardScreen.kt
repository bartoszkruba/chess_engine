package com.mygdx.game.screen

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.g2d.Sprite
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.math.Vector3
import com.badlogic.gdx.utils.Array
import com.github.bhlangonijr.chesslib.Board
import com.github.bhlangonijr.chesslib.Piece
import com.github.bhlangonijr.chesslib.Square
import com.github.bhlangonijr.chesslib.move.Move
import com.github.bhlangonijr.chesslib.move.MoveGenerator
import com.mygdx.game.Game
import com.mygdx.game.SQUARE_SIZE
import com.mygdx.game.assets.Textures
import com.mygdx.game.model.*
import com.mygdx.game.moveGenerator.blackPawnPromotionSquares
import com.mygdx.game.moveGenerator.moveIsPromotion
import com.mygdx.game.moveGenerator.whitePawnPromotionSquares
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Dispatchers.Default
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import ktx.app.KtxScreen
import ktx.collections.iterate
import ktx.graphics.use
import org.apache.commons.lang3.time.StopWatch
import kotlin.coroutines.CoroutineContext

class GameBoardScreen(val game: Game, private val chosenWhite: Boolean) : KtxScreen {

    private val textures = Textures(game.assets)

    private val camera = OrthographicCamera().apply {
        setToOrtho(false, 12 * SQUARE_SIZE, 9 * SQUARE_SIZE)
        position.x -= SQUARE_SIZE / 2f
        position.y -= SQUARE_SIZE / 2f
        update()
    }

    private val shapeRenderer = ShapeRenderer()

    private var wasLeftMousePressed: Boolean = false
    private val mousePosition = Vector2()
    private val mouseInitialPosition = Vector2()
    private var selectedPiece: BoardPiece? = null
    private val selectedPieceInitialPosition = Vector2()

    private val validationBoard = Board()
    private val board = createBoard()
    private val pieces = initializePieces()

    private val takenWhitePieces = Array<BoardPiece>()
    private val takenBlackPieces = Array<BoardPiece>()
    private val flipBoardButton = Sprite(textures.darkSquareTexture).apply {
        x = 9.25f * SQUARE_SIZE
        y = 5.75f * SQUARE_SIZE
        setSize(1.5f * SQUARE_SIZE, 0.75f * SQUARE_SIZE)
    }

    private var waitingForAIMove = false
    private val lastMove = Array<PossibleMove>()
    private val possibleMoves = Array<PossibleMove>()
    private var turn = 1
        set(value) {
            field = value
            whiteTurn = value % 2 != 0
        }
    private var whiteTurn = true
    private val stopwatch = StopWatch.createStarted()
    private var gameStatus = GameStatus.NONE
    private var gameBoardFlipped = false
    private var pieceSelectionOn = false

    private val numberToLetter = hashMapOf(1 to "A", 2 to "B", 3 to "C", 4 to "D", 5 to "E", 6 to "F",
            7 to "G", 8 to "H")
    private val letterToNumber = hashMapOf("A" to 1, "B" to 2, "C" to 3, "D" to 4, "E" to 5, "F" to 6,
            "G" to 7, "H" to 8)

    private val background = Sprite(textures.lightSquareTexture).apply {
        x = -SQUARE_SIZE
        y = -SQUARE_SIZE
        setSize(13 * SQUARE_SIZE, 10 * SQUARE_SIZE)
    }

    private val darkenGameBoard = Sprite(textures.darkSquareTexture).apply {
        x = -0.5f * SQUARE_SIZE
        y = -0.5f * SQUARE_SIZE
        setSize(9 * SQUARE_SIZE, 9 * SQUARE_SIZE)
        color = Color(0f, 0f, 0f, 0.4f)
    }

    private val selectQueen = Sprite(if (chosenWhite) textures.whiteQueen else textures.blackQueen).apply {
        x = SQUARE_SIZE
        y = 3.3f * SQUARE_SIZE
        setSize(1.3f * SQUARE_SIZE, 1.3f * SQUARE_SIZE)
    }

    private val selectRook = Sprite(if (chosenWhite) textures.whiteRook else textures.blackRook).apply {
        x = 2.3f * SQUARE_SIZE + 0.25f * SQUARE_SIZE
        y = 3.3f * SQUARE_SIZE
        setSize(1.3f * SQUARE_SIZE, 1.3f * SQUARE_SIZE)
    }

    private val selectBishop = Sprite(if (chosenWhite) textures.whiteBishop else textures.blackBishop).apply {
        x = 3.6f * SQUARE_SIZE + 0.5f * SQUARE_SIZE
        y = 3.3f * SQUARE_SIZE
        setSize(1.3f * SQUARE_SIZE, 1.3f * SQUARE_SIZE)
    }

    private val selectKnight = Sprite(if (chosenWhite) textures.whiteKnight else textures.blackKnight).apply {
        x = 4.9f * SQUARE_SIZE + 0.75f * SQUARE_SIZE
        y = 3.3f * SQUARE_SIZE
        setSize(1.3f * SQUARE_SIZE, 1.3f * SQUARE_SIZE)
    }

    override fun show() {
        super.show()
        if (!chosenWhite) {
            flipGameBoard()
        }
    }

    override fun render(delta: Float) {
        getMousePosInGameWorld()

        if (!checkBoardFlipControls() && (gameStatus == GameStatus.NONE || gameStatus == GameStatus.CHECK)) {
            if ((whiteTurn && chosenWhite) || (!whiteTurn && !chosenWhite)) {
                if (pieceSelectionOn) checkForPieceSelection()
                else processControls()
            } else if (!waitingForAIMove) {
                performAIMove()
            }
        }

        game.batch.projectionMatrix = camera.combined

        game.batch.use {
            try {
                background.draw(it)
                renderBoard(it)
                renderLastMove(it)
                renderPossibleMoves(it)
                renderBoardEnumeration(it)
                renderTakenPieces(it)
                renderTurnCounter(it)
                renderTime(it)
                renderStatus(it)
                flipBoardButton.draw(it)
                game.font.draw(it, "Flip Board", 9.5f * SQUARE_SIZE, 6.25f * SQUARE_SIZE)
            } catch (ex: Exception) {
                ex.printStackTrace()
            }
        }
        renderBoardBoundary(game.batch)
        renderTurnColor(game.batch)
        game.batch.use {
            try {
                renderPieces(it)
                if (pieceSelectionOn) renderPieceSelection(it)
            } catch (ex: Exception) {
                ex.printStackTrace()
            }
        }
    }

    private fun performAIMove() {
        waitingForAIMove = true
        CoroutineScope(IO).launch {
            val move = game.aiMoveGenerator.generateMove(validationBoard)
            CoroutineScope(Default).launch {
                selectedPiece = findPiece(squareToPosition(move.from))
                val newPosition = squareToPosition(move.to)
                selectedPiece?.x = newPosition.x
                selectedPiece?.y = newPosition.y

                checkForEnPassant(move, selectedPiece!!)
                checkForCastle(move)

                if (selectedPiece is Pawn && ((selectedPiece!!.dark && blackPawnPromotionSquares.contains(move.to))) ||
                        (!selectedPiece!!.dark && whitePawnPromotionSquares.contains(move.to))) {
                    validationBoard.doMove(move)
                    pieces.add(when (move.promotion) {
                        Piece.WHITE_QUEEN -> Queen(newPosition.x, newPosition.y, textures, false)
                        Piece.BLACK_QUEEN -> Queen(newPosition.x, newPosition.y, textures, true)
                        Piece.WHITE_ROOK -> Rook(newPosition.x, newPosition.y, textures, false)
                        Piece.BLACK_ROOK -> Rook(newPosition.x, newPosition.y, textures, true)
                        Piece.WHITE_BISHOP -> Bishop(newPosition.x, newPosition.y, textures, false)
                        Piece.BLACK_BISHOP -> Bishop(newPosition.x, newPosition.y, textures, true)
                        Piece.WHITE_KNIGHT -> Knight(newPosition.x, newPosition.y, textures, false)
                        Piece.BLACK_KNIGHT -> Knight(newPosition.x, newPosition.y, textures, true)
                        else -> throw RuntimeException("Invalid piece promotion")
                    })
                    pieces.removeIndex(pieces.indexOf(selectedPiece))
                } else {
                    validationBoard.doMove(move)
                }
                findPiece(selectedPiece!!)?.let {
                    addToTakenPieces(it)
                    pieces.removeIndex(pieces.indexOf(it))
                }

                turn++
                setLastMove(move)
                setGameStatus()
                waitingForAIMove = false
            }
        }
    }

    private fun checkForMouseOverlap(sprite: Sprite): Boolean {
        return (mousePosition.x >= sprite.x && mousePosition.x <= sprite.x + sprite.width &&
                mousePosition.y >= sprite.y && mousePosition.y <= sprite.y + sprite.height)
    }

    private fun checkForPieceSelection() = selectedPiece?.let { selectedPiece ->
        if (Gdx.input.isButtonJustPressed(Input.Buttons.LEFT)) {
            val from = positionToSquare(selectedPieceInitialPosition)
            val to = positionToSquare(Vector2(selectedPiece.x, selectedPiece.y))

            when {
                checkForMouseOverlap(selectQueen) -> if (whiteTurn) Piece.WHITE_QUEEN else Piece.BLACK_QUEEN
                checkForMouseOverlap(selectRook) -> if (whiteTurn) Piece.WHITE_ROOK else Piece.BLACK_ROOK
                checkForMouseOverlap(selectBishop) -> if (whiteTurn) Piece.WHITE_BISHOP else Piece.BLACK_BISHOP
                checkForMouseOverlap(selectKnight) -> if (whiteTurn) Piece.WHITE_KNIGHT else Piece.BLACK_KNIGHT
                else -> null
            }?.let { performPromotionMove(Move(from, to, it), selectedPiece) }
        }
    }

    private fun performPromotionMove(move: Move, selectedPiece: BoardPiece) {
        validationBoard.doMove(move)
        turn++
        findPiece(selectedPiece)?.let {
            val index = pieces.indexOf(it)
            addToTakenPieces(pieces[index])
            pieces.removeIndex(index)
        }
        val newPiece = when (move.promotion) {
            Piece.WHITE_QUEEN -> Queen(selectedPiece.x, selectedPiece.y, textures, false)
            Piece.BLACK_QUEEN -> Queen(selectedPiece.x, selectedPiece.y, textures, true)
            Piece.WHITE_ROOK -> Rook(selectedPiece.x, selectedPiece.y, textures, false)
            Piece.BLACK_ROOK -> Rook(selectedPiece.x, selectedPiece.y, textures, true)
            Piece.WHITE_BISHOP -> Bishop(selectedPiece.x, selectedPiece.y, textures, false)
            Piece.BLACK_BISHOP -> Bishop(selectedPiece.x, selectedPiece.y, textures, true)
            Piece.WHITE_KNIGHT -> Knight(selectedPiece.x, selectedPiece.y, textures, false)
            else -> Knight(selectedPiece.x, selectedPiece.y, textures, true)
        }
        pieces.removeIndex(pieces.indexOf(selectedPiece))
        pieces.add(newPiece)
        this.selectedPiece = null
        setGameStatus()
        pieceSelectionOn = false
    }

    private fun flipGameBoard() {
        gameBoardFlipped = !gameBoardFlipped

        pieces.iterate { piece, _ ->
            val square = positionToSquare(Vector2(piece.x, piece.y), !gameBoardFlipped)
            val newPosition = squareToPosition(square)
            piece.x = newPosition.x
            piece.y = newPosition.y
        }

        lastMove.iterate { lastMove, _ ->
            val square = positionToSquare(Vector2(lastMove.x, lastMove.y), !gameBoardFlipped)
            val newPosition = squareToPosition(square)
            lastMove.x = newPosition.x
            lastMove.y = newPosition.y
        }
    }

    private fun checkBoardFlipControls(): Boolean {
        return if (Gdx.input.isButtonJustPressed(Input.Buttons.LEFT)
                && mousePosition.x >= flipBoardButton.x &&
                mousePosition.x <= flipBoardButton.x + flipBoardButton.width &&
                mousePosition.y >= flipBoardButton.y && mousePosition.y <= flipBoardButton.y + flipBoardButton.height) {
            flipGameBoard()
            true
        } else false
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
                        .map {
                            if (findPiece(it) != null) PossibleMove(it.x, it.y, textures, PossibleMove.Color.RED)
                            else PossibleMove(it.x, it.y, textures, PossibleMove.Color.GREEN)
                        }.forEach { possibleMoves.add(it) }
                possibleMoves.add(PossibleMove(piece.x, piece.y, textures, PossibleMove.Color.BLUE))

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
                normalizePosition(position)
                try {
                    val move = Move(positionToSquare(selectedPieceInitialPosition), positionToSquare(position))
                    if (MoveGenerator.generateLegalMoves(validationBoard)
                                    .find { it.from == move.from && it.to == move.to } != null) {
                        selectedPiece?.x = position.x
                        selectedPiece?.y = position.y
                        checkForEnPassant(move, selectedPiece!!)
                        checkForCastle(move)
                        if (checkForPawnPromotion(move)) return
                        validationBoard.doMove(move)
                        turn++
                        findPiece(selectedPiece!!)?.let {
                            val index = pieces.indexOf(it)
                            addToTakenPieces(pieces[index])
                            pieces.removeIndex(index)
                        }
                        setGameStatus()
                        setLastMove(move)
                    } else throw RuntimeException()
                } catch (ex: Exception) {
                    selectedPiece?.y = selectedPieceInitialPosition.y
                    selectedPiece?.x = selectedPieceInitialPosition.x
                }
                selectedPiece = null
            }
        }
    }

    private fun setGameStatus() {
        gameStatus = when {
            validationBoard.isMated -> GameStatus.CHECK_MATE
            validationBoard.isDraw -> GameStatus.DRAW
            validationBoard.isStaleMate -> GameStatus.STALE_MATE
            validationBoard.isKingAttacked -> GameStatus.CHECK
            else -> GameStatus.NONE
        }
    }

    private fun checkForCastle(move: Move): Boolean {
        if (selectedPiece !is King) return true

        return if (whiteTurn && move.from == Square.E1 && move.to == Square.G1) {
            val moveRookTo = squareToPosition(Square.F1)
            findPiece(squareToPosition(Square.H1))?.apply { x = moveRookTo.x; y = moveRookTo.y; }
            false
        } else if (whiteTurn && move.from == Square.E1 && move.to == Square.C1) {
            val moveRookTo = squareToPosition(Square.D1)
            findPiece(squareToPosition(Square.A1))?.apply { x = moveRookTo.x; y = moveRookTo.y; }
            false
        } else if (!whiteTurn && move.from == Square.E8 && move.to == Square.G8) {
            val moveRookTo = squareToPosition(Square.F8)
            findPiece(squareToPosition(Square.H8))?.apply { x = moveRookTo.x; y = moveRookTo.y; }
            false
        } else if (!whiteTurn && move.from == Square.E8 && move.to == Square.C8) {
            val moveRookTo = squareToPosition(Square.D8)
            findPiece(squareToPosition(Square.A8))?.apply { x = moveRookTo.x; y = moveRookTo.y; }
            false
        } else false
    }

    private fun checkForEnPassant(move: Move, selectedPiece: BoardPiece): Boolean {
        if (selectedPiece !is Pawn) return false
        val from = move.from.name
        val to = move.to.name

        if (from[0] != to[0] && findPiece(selectedPiece) == null) {
            val removePieceFrom: Square
            removePieceFrom = if (whiteTurn) {
                Square.fromValue(to[0].toString() + (to[1].toString().toInt() - 1))
            } else {
                Square.fromValue(to[0].toString() + (to[1].toString().toInt() + 1))
            }
            val pieceToRemove = findPiece(squareToPosition(removePieceFrom))!!
            addToTakenPieces(pieceToRemove)
            pieces.removeIndex(pieces.indexOf(pieceToRemove))
        }

        return false
    }

    private fun checkForPawnPromotion(move: Move): Boolean {
        return if (moveIsPromotion(validationBoard, move)) {
            pieceSelectionOn = true
            true
        } else false
    }

    private fun setLastMove(move: Move) {
        val fromPosition = squareToPosition(move.from)
        val toPosition = squareToPosition(move.to)
        if (lastMove.size == 0) {
            lastMove.add(PossibleMove(fromPosition.x, fromPosition.y, textures, PossibleMove.Color.YELLOW));
            lastMove.add(PossibleMove(toPosition.x, toPosition.y, textures, PossibleMove.Color.YELLOW));
        } else {
            lastMove[0].x = fromPosition.x
            lastMove[0].y = fromPosition.y
            lastMove[1].x = toPosition.x
            lastMove[1].y = toPosition.y
        }
    }

    private fun addToTakenPieces(piece: BoardPiece) {
        var row = 0
        var column = 0
        piece.sprite.setSize(0.35f * SQUARE_SIZE, 0.35f * SQUARE_SIZE)
        if (piece.dark) {
            takenBlackPieces.add(piece)
            takenBlackPieces.sort { o1, o2 -> pieceToValue(o2) - pieceToValue(o1) }
        } else {
            takenWhitePieces.add(piece)
            takenWhitePieces.sort { o1, o2 -> pieceToValue(o2) - pieceToValue(o1) }
        }
        val calculatePositions = { boardSquare: BoardSquare ->
            if (row > 6) {
                row = 0
                column++
            }
            boardSquare.x = 8.6f * SQUARE_SIZE + row * 1.1f * boardSquare.sprite.width
            boardSquare.y = 5.25f * SQUARE_SIZE - column * 1.1f * boardSquare.sprite.height
            row++
        }
        takenWhitePieces.iterate { boardSquare, _ -> calculatePositions(boardSquare) }
        if (takenWhitePieces.size != 0) {
            row = 0
            column++
        }
        takenBlackPieces.iterate { boardSquare, _ -> calculatePositions(boardSquare) }
    }

    private fun pieceToValue(piece: BoardPiece) = when (piece) {
        is King -> 6
        is Queen -> 5
        is Rook -> 4
        is Bishop -> 3
        is Knight -> 2
        is Pawn -> 1
        else -> 0
    }


    private fun normalizePosition(position: Vector2) {
        position.x = (position.x / SQUARE_SIZE).toInt() * SQUARE_SIZE
        position.y = (position.y / SQUARE_SIZE).toInt() * SQUARE_SIZE
    }

    private fun findPiece(position: Vector2): BoardPiece? = pieces.find { boardSquare ->
        (position.x >= boardSquare.x && position.x < boardSquare.x + SQUARE_SIZE &&
                position.y >= boardSquare.y && position.y < boardSquare.y + SQUARE_SIZE)
    }

    private fun findPiece(position: BoardSquare): BoardPiece? = pieces.find { boardSquare ->
        (position.x >= boardSquare.x && position.x < boardSquare.x + SQUARE_SIZE &&
                position.y >= boardSquare.y && position.y < boardSquare.y + SQUARE_SIZE) && boardSquare != position
    }

    private fun positionToSquare(position: Vector2, gameBoardFlipped: Boolean = this.gameBoardFlipped): Square {
        return if (gameBoardFlipped) {
            Square.fromValue(numberToLetter[8 - (position.x / SQUARE_SIZE).toInt()] + (8 -
                    (position.y / SQUARE_SIZE).toInt()))
        } else {
            Square.fromValue((numberToLetter[((position.x / SQUARE_SIZE).toInt() + 1)] +
                    ((position.y / SQUARE_SIZE).toInt() + 1)))
        }
    }

    private fun squareToPosition(square: Square, gameBoardFlipped: Boolean = this.gameBoardFlipped): Vector2 {
        val letter = square.toString()[0].toString()
        val number = square.toString()[1].toString().toInt()

        return if (gameBoardFlipped) {
            val x = 7 * SQUARE_SIZE - ((letterToNumber[letter]!! - 1) * SQUARE_SIZE)
            val y = 7 * SQUARE_SIZE - ((number - 1) * SQUARE_SIZE)
            Vector2(x, y)
        } else {
            val x = ((letterToNumber[letter]!! - 1) * SQUARE_SIZE)
            val y = (number - 1) * SQUARE_SIZE
            Vector2(x, y)
        }
    }

    private fun renderBoardBoundary(batch: SpriteBatch) {
        shapeRenderer.projectionMatrix = batch.projectionMatrix
        shapeRenderer.setAutoShapeType(true)
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled)
        shapeRenderer.color = Color.BLACK
        shapeRenderer.rectLine(-2f, -2f, -2f, 8 * SQUARE_SIZE + 2f, 4f)
        shapeRenderer.rectLine(-2f, -2f, 8 * SQUARE_SIZE + 2f, -2f, 4f)
        shapeRenderer.rectLine(8 * SQUARE_SIZE + 2f, -2f, 8 * SQUARE_SIZE + 2f, 8 * SQUARE_SIZE + 2f, 4f)
        shapeRenderer.rectLine(-2f, 8 * SQUARE_SIZE + 2f, 8 * SQUARE_SIZE + 2f, 8 * SQUARE_SIZE + 2f, 4f)

        shapeRenderer.rectLine(-SQUARE_SIZE / 2f, -SQUARE_SIZE / 2f,
                -SQUARE_SIZE / 2f, 8 * SQUARE_SIZE + SQUARE_SIZE / 2F, 8f)
        shapeRenderer.rectLine(-SQUARE_SIZE / 2f, -SQUARE_SIZE / 2f,
                8 * SQUARE_SIZE + SQUARE_SIZE / 2f, -SQUARE_SIZE / 2f, 8f)
        shapeRenderer.rectLine(-SQUARE_SIZE / 2f, -SQUARE_SIZE / 2f,
                -SQUARE_SIZE / 2f, 8 * SQUARE_SIZE + SQUARE_SIZE / 2f, 8f)
        shapeRenderer.rectLine(8 * SQUARE_SIZE + SQUARE_SIZE / 2f, -SQUARE_SIZE / 2f,
                8 * SQUARE_SIZE + SQUARE_SIZE / 2f, 8 * SQUARE_SIZE + SQUARE_SIZE / 2f, 8f)

        shapeRenderer.end()
    }

    private fun renderTurnColor(batch: SpriteBatch) {
        shapeRenderer.projectionMatrix = batch.projectionMatrix

        shapeRenderer.color = if (whiteTurn) Color.WHITE else Color.BLACK
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled)
        shapeRenderer.rectLine(8.75f * SQUARE_SIZE, 7.9f * SQUARE_SIZE, 9f * SQUARE_SIZE,
                7.9f * SQUARE_SIZE, 30f)
        shapeRenderer.end()
    }

    private fun renderTurnCounter(batch: SpriteBatch) {
        game.font.draw(batch, "TURN : $turn", 9.15f * SQUARE_SIZE, 8 * SQUARE_SIZE)
    }

    private fun renderTime(batch: SpriteBatch) {
        game.font.draw(batch, "TIME : ${stopwatch.formatTime().dropLast(4)}", 9.15f * SQUARE_SIZE,
                7.5f * SQUARE_SIZE)
    }

    private fun renderStatus(batch: SpriteBatch) {
        if (gameStatus != GameStatus.NONE) {
            val status = gameStatus.name.replace('_', ' ')
            game.font.draw(batch, status, 9.15f * SQUARE_SIZE, 7f * SQUARE_SIZE)
        }
    }

    private fun renderBoardEnumeration(batch: SpriteBatch) {
        if (gameBoardFlipped) for (i in 0 until 8) {
            game.font.draw(batch, numberToLetter[8 - i], SQUARE_SIZE / 2f - 13f + i * SQUARE_SIZE, -15f)
            game.font.draw(batch, numberToLetter[8 + -i], SQUARE_SIZE / 2f - 13f + i * SQUARE_SIZE,
                    8 * SQUARE_SIZE + 40f)

            game.font.draw(batch, "" + (8 - i), -35f, SQUARE_SIZE / 2f + 14f + i * SQUARE_SIZE)
            game.font.draw(batch, "" + (8 - i), 8 * SQUARE_SIZE + 19f, SQUARE_SIZE / 2f + 14f + i * SQUARE_SIZE)
        } else for (i in 0 until 8) {
            game.font.draw(batch, numberToLetter[i + 1], SQUARE_SIZE / 2f - 13f + i * SQUARE_SIZE, -15f)
            game.font.draw(batch, numberToLetter[i + 1], SQUARE_SIZE / 2f - 13f + i * SQUARE_SIZE,
                    8 * SQUARE_SIZE + 40f)

            game.font.draw(batch, "" + (i + 1), -35f, SQUARE_SIZE / 2f + 14f + i * SQUARE_SIZE)
            game.font.draw(batch, "" + (i + 1), 8 * SQUARE_SIZE + 19f, SQUARE_SIZE / 2f + 14f + i * SQUARE_SIZE)
        }
    }

    private fun renderBoard(batch: SpriteBatch) = board.iterate { square, _ -> square.draw(batch) }

    private fun renderPieces(batch: SpriteBatch) {
        pieces.iterate { piece, _ -> if (piece != selectedPiece) piece.draw(batch) }
        selectedPiece?.draw(batch)
    }

    private fun renderPieceSelection(batch: SpriteBatch) {
        darkenGameBoard.draw(batch)
        selectQueen.draw(batch)
        selectRook.draw(batch)
        selectBishop.draw(batch)
        selectKnight.draw(batch)
    }

    private fun renderTakenPieces(batch: SpriteBatch) {
        takenWhitePieces.iterate { piece, _ -> piece.draw(batch) }
        takenBlackPieces.iterate { piece, _ -> piece.draw(batch) }
    }

    private fun renderPossibleMoves(batch: SpriteBatch) = possibleMoves.iterate { move, _ -> move.draw(batch) }
    private fun renderLastMove(batch: SpriteBatch) = lastMove.iterate { move, _ -> move.draw(batch) }

    private fun createBoard(): Array<BoardSquare> {
        val board = Array<BoardSquare>()
        var dark = true

        for (i in 0 until 8) {
            for (j in 0 until 8) {
                if (dark) board.add(DarkBoardSquare(i * SQUARE_SIZE, j * SQUARE_SIZE, textures))
                else board.add(LightBoardSquare(i * SQUARE_SIZE, j * SQUARE_SIZE, textures))
                dark = !dark
            }
            dark = !dark
        }
        return board
    }

    private fun initializePieces(): Array<BoardPiece> {
        val pieces = Array<BoardPiece>()

        for (i in 0 until 2) {
            for (j in 0 until 8) {
                val dark = i != 0
                pieces.add(Pawn(j * SQUARE_SIZE, i * 5 * SQUARE_SIZE + SQUARE_SIZE, textures, dark))
            }
        }

        for (i in 0 until 2) {
            val dark = i != 0
            pieces.add(Knight(SQUARE_SIZE, i * 7 * SQUARE_SIZE, textures, dark))
            pieces.add(Knight(6 * SQUARE_SIZE, i * 7 * SQUARE_SIZE, textures, dark))
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

    private enum class GameStatus {
        NONE, CHECK, CHECK_MATE, DRAW, STALE_MATE
    }
}