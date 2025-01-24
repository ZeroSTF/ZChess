package tn.zeros.zchess.ui.util;


import javafx.scene.image.Image;
import tn.zeros.zchess.core.model.Piece;

import java.util.Objects;

public class AssetLoader {
    private static final String PIECE_ASSET_PATH = "/pieces/%s.png";

    public static Image getPieceImage(Piece piece) {
        try {
            String path = String.format(PIECE_ASSET_PATH, piece.name());
            return new Image(Objects.requireNonNull(
                    AssetLoader.class.getResourceAsStream(path)
            ));
        } catch (Exception e) {
            return null;
        }
    }
}