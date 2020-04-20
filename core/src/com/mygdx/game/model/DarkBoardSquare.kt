package com.mygdx.game.model

import com.badlogic.gdx.assets.AssetManager
import com.badlogic.gdx.graphics.g2d.Sprite
import com.mygdx.game.assets.Textures

class DarkBoardSquare(x: Float, y: Float, textures: Textures) : BoardSquare(x, y, Sprite(textures.darkSquareTexture))