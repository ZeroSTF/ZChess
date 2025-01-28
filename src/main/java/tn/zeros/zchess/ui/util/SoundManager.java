package tn.zeros.zchess.ui.util;

import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;

import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;

import static tn.zeros.zchess.ui.util.UIConstants.*;

public class SoundManager {
    private static final Map<SoundType, Media> SOUND_CACHE = new EnumMap<>(SoundType.class);

    static {
        SOUND_CACHE.put(SoundType.MOVE, loadSound(MOVE_SOUND));
        SOUND_CACHE.put(SoundType.MOVE_CHECK, loadSound(MOVE_CHECK_SOUND));
        SOUND_CACHE.put(SoundType.MOVE_OPPONENT, loadSound(MOVE_OPPONENT_SOUND));
        SOUND_CACHE.put(SoundType.CAPTURE, loadSound(CAPTURE_SOUND));
        SOUND_CACHE.put(SoundType.CASTLE, loadSound(CASTLE_SOUND));
        SOUND_CACHE.put(SoundType.PROMOTION, loadSound(PROMOTION_SOUND));
        SOUND_CACHE.put(SoundType.PREMOVE, loadSound(PREMOVE_SOUND));
    }

    public static void initialize() {
    }

    private static Media loadSound(String path) {
        try {
            String url = Objects.requireNonNull(SoundManager.class.getResource(path)).toString();
            return new Media(url);
        } catch (Exception e) {
            System.err.println("Error loading sound: " + path);
            return null;
        }
    }

    public static void playSound(SoundType type) {
        Media sound = SOUND_CACHE.get(type);
        if (sound != null) {
            MediaPlayer player = new MediaPlayer(sound);
            player.play();
        }
    }

    public static void playMove() {
        playSound(SoundType.MOVE);
    }

    public static void playMoveCheck() {
        playSound(SoundType.MOVE_CHECK);
    }

    public static void playMoveOpponent() {
        playSound(SoundType.MOVE_OPPONENT);
    }

    public static void playCapture() {
        playSound(SoundType.CAPTURE);
    }

    public static void playCastle() {
        playSound(SoundType.CASTLE);
    }

    public static void playPromotion() {
        playSound(SoundType.PROMOTION);
    }

    public static void playPremove() {
        playSound(SoundType.PREMOVE);
    }

    public enum SoundType {
        MOVE, MOVE_CHECK, MOVE_OPPONENT,
        CAPTURE, CASTLE, PROMOTION, PREMOVE
    }
}