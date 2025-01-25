package tn.zeros.zchess.ui.util;

import javafx.scene.paint.Color;
import javafx.scene.text.Font;

public class UIConstants {
    // Sizes
    public static final int SQUARE_SIZE = 95;

    // Colors
    public static final Color LIGHT_SQUARE_COLOR = Color.valueOf("#F0D9B5");
    public static final Color DARK_SQUARE_COLOR = Color.valueOf("#B58863");
    public static final Color HIGHLIGHT_COLOR = Color.YELLOW;

    // Fonts
    public static final Font PIECE_FONT = Font.font("Arial", 24);

    // Rank and file labels
    public static final String[] FILES = {"a", "b", "c", "d", "e", "f", "g", "h"};
    public static final String[] RANKS = {"8", "7", "6", "5", "4", "3", "2", "1"};

    // Sounds
    public static final String MOVE_SOUND = "/sounds/move.mp3";
    public static final String MOVE_CHECK_SOUND = "/sounds/move-check.mp3";
    public static final String MOVE_OPPONENT_SOUND = "/sounds/move-opponent.mp3";
    public static final String CAPTURE_SOUND = "/sounds/capture.mp3";
    public static final String CASTLE_SOUND = "/sounds/castle.mp3";
    public static final String PROMOTION_SOUND = "/sounds/promotion.mp3";
    public static final String PREMOVE_SOUND = "/sounds/premove.mp3";

}
