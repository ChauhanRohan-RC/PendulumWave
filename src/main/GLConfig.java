package main;

import org.jetbrains.annotations.NotNull;
import pendulum.PendulumDrawStyle;
import sound.MidiNotePlayer;
import util.U;

import java.awt.*;

public class GLConfig {

    public static final float FRAME_RATE = 120;
    public static final boolean DEFAULT_WINDOW_IN_SCREEN_CENTER = true;

    public static final boolean DEFAULT_SOUND_ENABLED = true;
    public static final boolean DEFAULT_POLY_RHYTHM_ENABLED = MidiNotePlayer.DEFAULT_POLY_RHYTHM_ENABLED;

    public static final float DEFAULT_HUE_START = 0.8f;
    public static final int MIN_PENDULUMS_FOR_FULL_HUE_CYCLE = 70;     // full hue cycle is of 360 degrees

    public static final boolean DEFAULT_HUD_ENABLED = true;
    public static final boolean DEFAULT_SHOW_KEY_BINDINGS = true;
    public static final boolean DEFAULT_DRAW_ONLY_BOB = false;

    public static final long CAMERA_EXPLICIT_RESET_ANIMATION_MILLS = 300;
    public static final long CAMERA_ROTATIONS_ANIMATION_MILLS = 250;        // set to 0 for no animation

    // Depends on the number of pendulums and MIN_PENDULUMS_FOR_FULL_HUE_CYCLE
    public static float getDefaultHueRange(int numPendulums) {
        return (float) Math.min(numPendulums, GLConfig.MIN_PENDULUMS_FOR_FULL_HUE_CYCLE) / GLConfig.MIN_PENDULUMS_FOR_FULL_HUE_CYCLE; // in range of [0, 1]
    }

    public static float getDefaultHueEnd(int numPendulums) {
        return DEFAULT_HUE_START + getDefaultHueRange(numPendulums);
    }

    /* Colors and Fonts ................................. */
    public static final Color BG = new Color(0, 0, 0, 255);
    public static final Color FG_DARK = new Color(255, 255, 255, 255);
    public static final Color FG_MEDIUM = new Color(230, 230, 230, 255);
    public static final Color FG_LIGHT = new Color(200, 200, 200, 255);
    public static final Color ACCENT = new Color(107, 196, 255, 255);
    public static final Color ACCENT_HIGHLIGHT = new Color(255, 221, 83, 255);

    public static final Color PENDULUMS_SUPPORT_FILL = new Color(114, 66, 25);
    public static final Color PENDULUMS_SUPPORT_STROKE = new Color(84, 45, 0);

    public static final float TEXT_SIZE_HUGE = 0.034f;
    public static final float TEXT_SIZE_EXTRA_LARGE = 0.028f;
    public static final float TEXT_SIZE_LARGE = 0.026f;
    public static final float TEXT_SIZE_NORMAL = 0.023f;
    public static final float TEXT_SIZE_SMALL1 = 0.019f;
    public static final float TEXT_SIZE_SMALL2 = 0.0175f;
    public static final float TEXT_SIZE_SMALL3 = 0.016f;
    public static final float TEXT_SIZE_TINY = 0.014f;


    public static final float TEXT_SIZE_CONTROL_KEY_BINDING_LABEL = TEXT_SIZE_SMALL3;
    public static final Color FG_CONTROL_KEY_BINDING_LABEL = new Color(204, 178, 60, 255);

    public static final float TEXT_SIZE__STATUS_CONTROLS = TEXT_SIZE_SMALL2;
    public static final Color FG__STATUS_CONTROLS = FG_DARK;

    public static final float TEXT_SIZE_MAIN_CONTROLS_LABEL = TEXT_SIZE_SMALL3;
    public static final float TEXT_SIZE_MAIN_CONTROLS_KEY_LABEL = TEXT_SIZE_SMALL2;
    public static final float TEXT_SIZE_MAIN_CONTROLS_VALUE = TEXT_SIZE_SMALL1;
    public static final Color FG_MAIN_CONTROLS_LABEL = FG_MEDIUM;
    public static final Color FG_MAIN_CONTROLS_KEY_LABEL = FG_CONTROL_KEY_BINDING_LABEL;
    public static final Color FG_MAIN_CONTROLS_VALUE = ACCENT_HIGHLIGHT;


    public static final Color COLOR_X_AXIS = new Color(246, 92, 92, 255);
    public static final Color COLOR_Y_AXIS = new Color(88, 250, 88, 255);
    public static final Color COLOR_Z_AXIS = new Color(95, 95, 241, 255);


    public static float getTextSize(float width, float height, float size) {
        return Math.min(width, height) * size;
    }


    @NotNull
    public static PendulumDrawStyle createHueCycleDrawStyle2D(int numPendulums, int index, boolean highlight, float hueStart, float hueEnd) {
        final Color c = Color.getHSBColor(numPendulums > 1 ? U.map(index, 0, numPendulums - 1, hueStart, hueEnd) : hueStart, 1, 1);

        return new PendulumDrawStyle(
                2,
                c,
                highlight ? 4 : 0,
                highlight ? Color.WHITE : null,
                true,
                c,
                0
        );
    }


    @NotNull
    public static PendulumDrawStyle createHueCycleDrawStyle2D(int numPendulums, int index, boolean highlight, float hueStart) {
        return createHueCycleDrawStyle2D(numPendulums, index, highlight, hueStart, hueStart + getDefaultHueRange(numPendulums));
    }

    @NotNull
    public static PendulumDrawStyle createHueCycleDrawStyle2D(int numPendulums, int index, boolean highlight) {
        return createHueCycleDrawStyle2D(numPendulums, index, highlight, DEFAULT_HUE_START);
    }


    @NotNull
    public static PendulumDrawStyle createHueCycleDrawStyle3D(int numPendulums, int index, boolean highlight, float hueStart, float hueEnd) {
        final Color c = Color.getHSBColor(numPendulums > 1 ? U.map(index, 0, numPendulums - 1, hueStart, hueEnd) : hueStart, 1, 1);

        return new PendulumDrawStyle(
                2,
                c,
                0,
                null,
                true,
                highlight ? Color.WHITE : c,
                highlight ? 4 : 0
        );
    }

    @NotNull
    public static PendulumDrawStyle createHueCycleDrawStyle3D(int numPendulums, int index, boolean highlight, float hueStart) {
        return createHueCycleDrawStyle3D(numPendulums, index, highlight, hueStart, hueStart + getDefaultHueRange(numPendulums));
    }

    @NotNull
    public static PendulumDrawStyle createHueCycleDrawStyle3D(int numPendulums, int index, boolean highlight) {
        return createHueCycleDrawStyle3D(numPendulums, index, highlight, DEFAULT_HUE_START);
    }


    /* ................................ Processing PApplet ................................... */

//    public static float getTextSize(@NotNull PApplet app, float size) {
//        return getTextSize(app.width, app.height, size);
//    }

//    public PShape createCylinder(float r, float h, )




}
