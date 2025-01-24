package tn.zeros.zchess.ui.util;


import javafx.scene.image.Image;
import tn.zeros.zchess.core.model.Piece;

import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;

public class AssetLoader {
    private static final String PIECE_ASSET_PATH = "/pieces/%s.png";
    private static final Map<Piece, Image> PIECE_CACHE = new EnumMap<>(Piece.class);

    public static Image getPieceImage(Piece piece) {
        return PIECE_CACHE.computeIfAbsent(piece, p -> {
            String path = String.format(PIECE_ASSET_PATH, p.name());
            try {
                return new Image(Objects.requireNonNull(
                        AssetLoader.class.getResourceAsStream(path)
                ));
            } catch (Exception e) {
                return null;
            }
        });
    }
}