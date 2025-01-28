package tn.zeros.zchess.ui.util;

import javafx.scene.image.Image;
import tn.zeros.zchess.core.model.Piece;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class AssetLoader {
    private static final String PIECE_ASSET_PATH = "/pieces/%s.png";
    private static final Map<Integer, Image> PIECE_CACHE = new HashMap<>();

    public static Image getPieceImage(int piece) {
        return PIECE_CACHE.computeIfAbsent(piece, p -> {
            String path = String.format(PIECE_ASSET_PATH, Piece.getName(p));
            try {
                return new Image(Objects.requireNonNull(AssetLoader.class.getResourceAsStream(path)),
                        UIConstants.SQUARE_SIZE - 10, UIConstants.SQUARE_SIZE - 10, true, true);
            } catch (Exception e) {
                return null;
            }
        });
    }
}