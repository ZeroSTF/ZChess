package tn.zeros.zchess.ui.util;

import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.RadialGradient;
import javafx.scene.paint.Stop;

public class EffectUtils {
    // Create gradient for check effect
    public static Stop[] stops = new Stop[]{
            new Stop(0, Color.rgb(255, 0, 0, 1.0)),
            new Stop(0.25, Color.rgb(231, 0, 0, 1.0)),
            new Stop(0.89, Color.rgb(169, 0, 0, 0.0)),
            new Stop(1.0, Color.rgb(158, 0, 0, 0.0))
    };
    public static RadialGradient gradient = new RadialGradient(
            0, 0,  // focus angle, focus distance
            0.5, 0.5,  // center X, center Y
            0.5,  // radius
            true,  // proportional
            CycleMethod.NO_CYCLE,
            stops
    );
}
