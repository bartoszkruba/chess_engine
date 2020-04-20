package com.mygdx.game.assets

import com.badlogic.gdx.assets.AssetManager

class Textures(assets: AssetManager) {
    val lightSquareTexture = assets[TextureAssets.LightBrownSquare]
    val darkSquareTexture = assets[TextureAssets.DarkBrownSquare]

    val whitePawn = assets[TextureAssets.WhitePawn]
    val whiteKnight = assets[TextureAssets.WhiteKnight]
    val whiteBishop = assets[TextureAssets.WhiteBishop]
    val whiteRook = assets[TextureAssets.WhiteRook]
    val whiteQueen = assets[TextureAssets.WhiteQueen]
    val whiteKing = assets[TextureAssets.WhiteKing]

    val blackPawn = assets[TextureAssets.BlackPawn]
    val blackKnight = assets[TextureAssets.BlackKnight]
    val blackBishop = assets[TextureAssets.BlackBishop]
    val blackRook = assets[TextureAssets.BlackRook]
    val blackQueen = assets[TextureAssets.BlackQueen]
    val blackKing = assets[TextureAssets.BlackKing]
}