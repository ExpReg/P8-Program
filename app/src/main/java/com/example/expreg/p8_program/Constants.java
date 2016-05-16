package com.example.expreg.p8_program;

public abstract class Constants {
    // Thresholds for the different detection settings
    public static final float ACCELERATION_THRESHOLD_LENIENT = 3.5f;
    public static final float DECELERATION_THRESHOLD_LENIENT = 4.0f;
    public static final float ACCELERATION_THRESHOLD_STRICT = 1.5f;
    public static final float DECELERATION_THRESHOLD_STRICT = 2.0f;

    // How long the screen should stay red at detection. In ns.
    public static long TIME_AS_RED = 5000000000L;
}
