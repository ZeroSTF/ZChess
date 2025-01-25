package tn.zeros.zchess.ui.util;

import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;

import java.util.Objects;

import static tn.zeros.zchess.ui.util.UIConstants.*;

public class SoundManager {

    private static Media loadSound(String path) {
        try {
            String url = Objects.requireNonNull(SoundManager.class.getResource(path)).toString();
            return new Media(url);
        } catch (Exception e) {
            System.err.println("Error loading sound: " + path);
            return null;
        }
    }

    public static void playMove() {
        playSound(loadSound(MOVE_SOUND));
    }

    public static void playMoveCheck() {
        playSound(loadSound(MOVE_CHECK_SOUND));
    }

    public static void playMoveOpponent() {
        playSound(loadSound(MOVE_OPPONENT_SOUND));
    }

    public static void playCapture() {
        playSound(loadSound(CAPTURE_SOUND));
    }

    public static void playCastle() {
        playSound(loadSound(CASTLE_SOUND));
    }

    public static void playPromotion() {
        playSound(loadSound(PROMOTION_SOUND));
    }

    public static void playPremove() {
        playSound(loadSound(PREMOVE_SOUND));
    }

    private static void playSound(Media sound) {
        if (sound != null) {
            MediaPlayer player = new MediaPlayer(sound);
            player.play();
        }
    }
}