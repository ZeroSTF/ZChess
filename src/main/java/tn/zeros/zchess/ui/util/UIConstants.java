package tn.zeros.zchess.ui.util;

import javafx.scene.paint.*;
import javafx.scene.text.Font;

public class UIConstants {
    // Sizes
    public static final int SQUARE_SIZE = 95;

    // Colors
    public static final Color LIGHT_SQUARE_COLOR = Color.web("#F0D9B5");
    public static final Color DARK_SQUARE_COLOR = Color.web("#B58863");
    public static final Color LAST_MOVE_COLOR = Color.web("#9BC700", 0.41);
    public static final Color SELECTED_SQUARE_COLOR = Color.web("#14551E", 0.5);
    public static final Color LEGAL_MOVE_DOT_COLOR = Color.web("#14551E", 0.5);
    public static final double LEGAL_MOVE_DOT_RADIUS = 0.14;
    public static final Color LEGAL_MOVE_HOVER_COLOR = Color.web("#14551E", 0.3);
    public static final Paint CAPTURE_HIGHLIGHT = new RadialGradient(
            0, 0,
            0.5, 0.5,
            0.7,
            true,
            CycleMethod.NO_CYCLE,
            new Stop(0.81, Color.TRANSPARENT),
            new Stop(0.82, Color.rgb(20, 85, 0, 0.3))
    );
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

    // Engine
    public static final long DEFAULT_SEARCH_TIME_MS = 500;

}
