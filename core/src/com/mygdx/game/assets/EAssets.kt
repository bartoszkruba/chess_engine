package com.mygdx.game.assets

import com.badlogic.gdx.assets.AssetManager
import com.badlogic.gdx.graphics.Texture
import ktx.assets.Asset
import ktx.assets.getAsset
import ktx.assets.load

enum class TextureAssets(val path: String) {
    DarkBrownSquare("1x/square brown dark_1x.png"),
    LightBrownSquare("1x/square brown light_1x.png"),
    WhiteBishop("1x/w_bishop_1x.png"),
    WhiteKing("1x/w_king_1x.png"),
    WhiteKnight("1x/w_knight_1x.png"),
    WhitePawn("1x/w_pawn_1x.png"),
    WhiteQueen("1x/w_queen_1x.png"),
    WhiteRook("1x/w_rook_1x.png"),
    BlackBishop("1x/b_bishop_1x.png"),
    BlackKing("1x/b_king_1x.png"),
    BlackKnight("1x/b_knight_1x.png"),
    BlackPawn("1x/b_pawn_1x.png"),
    BlackQueen("1x/b_queen_1x.png"),
    BlackRook("1x/b_rook_1x.png")
}

inline fun AssetManager.load(asset: TextureAssets) = load<Texture>(asset.path)
inline operator fun AssetManager.get(asset: TextureAssets) = getAsset<Texture>(asset.path)