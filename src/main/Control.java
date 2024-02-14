package main;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import peasy.PeasyCam;
import processing.core.PApplet;
import processing.event.Event;
import processing.event.KeyEvent;
import util.U;

import java.awt.*;
import java.util.StringJoiner;
import java.util.function.BiFunction;
import java.util.function.Function;

public enum Control {

    PENDULUM_COUNT("Pendulums",
            "Number of Pendulums in the Pendulum Wave",
            ui -> String.valueOf(ui.getPendulumWave().pendulumCount()),
            "[Ctr | Shf]-N",
            (ui, ev) -> {
                if (ev.getKeyCode() == java.awt.event.KeyEvent.VK_N) {
                    ui.getPendulumWave().stepPendulumCount(!ev.isShiftDown(), !ev.isControlDown());
                    return true;
                }

                return false;
            }, false),

    PLAY_PAUSE("Pause",
            "Play/Pause simulation",
            ui -> ui.getPendulumWave().isPaused() ? "Paused" : "Playing",
            "SPACE",
            (ui, ev) -> {
                final int mod = ev.getModifiers();
                if (ev.getKeyCode() == java.awt.event.KeyEvent.VK_SPACE && mod == 0) {
                    ui.getPendulumWave().togglePlayPause();
                    return true;
                }

                return false;
            }, false),

    RESET("Reset",
            "Resets the Simulation. R -> Reset Pendulums State  |  Ctrl-R -> Reset Pendulum Count  |  Shift-R -> Reset Simulation Environment",
            ui -> "",
            "[Ctr | Shf]-R",
            (ui, ev) -> {
                final int mod = ev.getModifiers();
                if (ev.getKeyCode() == java.awt.event.KeyEvent.VK_R) {
                    if (mod == 0) {
                        ui.getPendulumWave().resetPendulums();
                    } else if (ev.isShiftDown()) {
                        ui.getPendulumWave().resetSimulation(ev.isControlDown());
                    } else if (ev.isControlDown()) {
                        ui.getPendulumWave().resetPendulumCount(true);
                    }

                    return true;
                }

                return false;
            }, false),

    DRAW_ONLY_BOBS("Bobs Only",
            "Draw pendulum bobs only (do not draw pendulum chords and support)",
            ui -> ui.isDrawOnlyBobEnabled() ? "ON" : "OFF",
            "B",
            (ui, ev) -> {
                final int mod = ev.getModifiers();
                if (ev.getKeyCode() == java.awt.event.KeyEvent.VK_B && mod == 0) {
                    ui.toggleDrawOnlyBob();
                    return true;
                }

                return false;
            }, false),

    HUD_ENABLED("HUD",
            "Show/Hide HUD",
            ui -> ui.isHudEnabled() ? "ON" : "OFF",
            "H",
            (ui, ev) -> {
                final int mod = ev.getModifiers();
                if (ev.getKeyCode() == java.awt.event.KeyEvent.VK_H && mod == 0) {
                    ui.toggleHudEnabled();
                    return true;
                }

                return false;
            }, false, true, null),

    SHOW_KEY_BINDINGS("Controls",
            "Show/Hide Control Key Bindings",
            ui -> ui.areKeyBindingsShown() ? "ON" : "OFF",
            "C",
            (ui, ev) -> {
                final int mod = ev.getModifiers();
                if (ev.getKeyCode() == java.awt.event.KeyEvent.VK_C && mod == 0) {
                    ui.toggleShowKeyBindings();
                    return true;
                }

                return false;
            }, false, true, null),


    SOUND("Sound",
            "Toggle Sounds",
            ui -> ui.isSoundEnabled() ? "ON" : "OFF",
            "S",
            (ui, ev) -> {
                final int mod = ev.getModifiers();
                if (ev.getKeyCode() == java.awt.event.KeyEvent.VK_S && mod == 0) {
                    ui.toggleSoundEnabled();
                    return true;
                }

                return false;
            }, false),

    POLY_RHYTHM("Poly Rhythm",
            "Toggle Poly Rhythm (play multiple notes at once)",
            ui -> ui.isPolyRhythmEnabled() ? "ON" : "OFF",
            "Shf-S",
            (ui, ev) -> {
                final int mod = ev.getModifiers();
                if (ev.getKeyCode() == java.awt.event.KeyEvent.VK_S && mod == Event.SHIFT /* Shift only */) {
                    ui.togglePolyRhythmEnabled();
                    return true;
                }

                return false;
            }, false),

    SIMULATION_SPEED("Sim Speed",
            "Simulation Speed, in both multiples and percentage",
            ui -> String.format("%sx (%s%%)",
                    U.nf001(ui.getPendulumWave().getSpeed()),
                    U.nf001(ui.getPendulumWave().getSpeedPercent())),
            "[Shf]-/",
            (ui, ev) -> {
                final int mod = ev.getModifiers();
                if (ev.getKeyCode() == java.awt.event.KeyEvent.VK_SLASH && (mod == 0 || mod == Event.SHIFT) /* Only shift allowed */) {
                    ui.getPendulumWave().stepSpeed(mod == 0);
                    return true;
                }

                return false;
            }, true),

    GRAVITY("Gravity",
            "Acceleration due to Gravity (in ms-2)",
            ui -> U.nf002(ui.getPendulumWave().gravity()) + " ms-2",
            "[Shf]-G",
            (ui, ev) -> {
                final int mod = ev.getModifiers();
                if (ev.getKeyCode() == java.awt.event.KeyEvent.VK_G && (mod == 0 || mod == Event.SHIFT) /* Only shift allowed */) {
                    ui.getPendulumWave().stepGravity(mod == 0, false);
                    return true;
                }

                return false;
            }, true),

    DRAG("Drag",
            "Drag Coefficient (in g/s). Positive -> drag, Negative -> push",
            ui -> U.nf002(ui.getPendulumWave().drag() * 1000) + " g/s",
            "[Shf]-D",
            (ui, ev) -> {
                final int mod = ev.getModifiers();
                if (ev.getKeyCode() == java.awt.event.KeyEvent.VK_D && (mod == 0 || mod == Event.SHIFT) /* Only shift allowed */) {
                    ui.getPendulumWave().stepDrag(mod == 0, false);
                    return true;
                }

                return false;
            }, true),

    PENDULUM_MASS("Mass",
            "Mass of each pendulum Bob (in grams)",
            ui -> U.nf002(ui.getPendulumWave().getPendulumMass() * 1000) + " g",
            "[Shf]-M",
            (ui, ev) -> {
                final int mod = ev.getModifiers();
                if (ev.getKeyCode() == java.awt.event.KeyEvent.VK_M && (mod == 0 || mod == Event.SHIFT) /* Only shift allowed */) {
                    ui.getPendulumWave().stepPendulumMass(mod == 0, false);
                    return true;
                }

                return false;
            }, true),

    PENDULUM_START_ANGLE("Start Angle",
            "Start angle for each pendulum (in degrees)",
            ui -> U.nf001(PApplet.degrees(ui.getPendulumWave().getPendulumStartAngle())) + "°",
            "[Shf]-A",
            (ui, ev) -> {
                final int mod = ev.getModifiers();
                if (ev.getKeyCode() == java.awt.event.KeyEvent.VK_A && (mod == 0 || mod == Event.SHIFT) /* Only shift allowed */) {
                    ui.getPendulumWave().stepPendulumStartAngle(mod == 0, false);
                    return true;
                }

                return false;
            }, true),

    WAVE_PERIOD("Wave Period",
            "Total time in which the Pendulum Wave completes one cycle (in secs)",
            ui -> U.nf001(ui.getPendulumWave().getEffectiveWavePeriod()) + " s",
            "[Shf]-P",
            (ui, ev) -> {
                final int mod = ev.getModifiers();
                if (ev.getKeyCode() == java.awt.event.KeyEvent.VK_P && (mod == 0 || mod == Event.SHIFT) /* Only shift allowed */) {
                    ui.getPendulumWave().stepInternalWavePeriod(mod == 0, false);
                    return true;
                }

                return false;
            }, true),

    MIN_OSCILLATIONS_IN_WAVE_PERIOD("Min Osc",
            "Number of oscillations the first pendulum completes in Wave Period time",
            ui -> U.nf001(ui.getPendulumWave().getMinOscillationsInWavePeriod()),
            "[Shf]-O",
            (ui, ev) -> {
                final int mod = ev.getModifiers();
                if (ev.getKeyCode() == java.awt.event.KeyEvent.VK_O && (mod == 0 || mod == Event.SHIFT) /* Only shift allowed */) {
                    ui.getPendulumWave().stepMinOscillationsInWavePeriod(mod == 0, false);
                    return true;
                }

                return false;
            }, true),

    OSCILLATION_STEP_PER_PENDULUM("Osc Step",
            "Number of oscillations that a pendulum completes more than its predecessor",
            ui -> U.nf001(ui.getPendulumWave().getOscillationsStepPerPendulum()),
            "[Shf]-I",
            (ui, ev) -> {
                final int mod = ev.getModifiers();
                if (ev.getKeyCode() == java.awt.event.KeyEvent.VK_I && (mod == 0 || mod == Event.SHIFT) /* Only shift allowed */) {
                    ui.getPendulumWave().stepOscillationsStepPerPendulum(mod == 0, false);
                    return true;
                }

                return false;
            }, true),


    /* Camera Controls */

    CAMERA_ROTATE_X("Pitch (X)",
            "Controls the Camera PITCH (X-Axis rotation)",
            ui -> {
                final PeasyCam cam = ui.getCamera();
                return cam != null? U.nf001(U.normalizeDegrees(PApplet.degrees(cam.getRotations()[0]))) + "°": "";
            },
            "^",
            (ui, ev) -> {
                final PeasyCam cam = ui.getCamera();
                final int mod = ev.getModifiers();
                final int kc = ev.getKeyCode();
                if (cam != null && (kc == java.awt.event.KeyEvent.VK_UP || kc == java.awt.event.KeyEvent.VK_DOWN) && mod == 0) {
                    final float rx = ((kc == java.awt.event.KeyEvent.VK_UP)? 1: -1) * PApplet.HALF_PI;
                    U.rotateX(cam, rx, GLConfig.CAMERA_ROTATIONS_ANIMATION_MILLS);
                    return true;
                }

                return false;
            }, false, false, GLConfig.COLOR_X_AXIS),

    CAMERA_ROTATE_Y("Yaw (Y)",
            "Controls the Camera YAW (Y-Axis rotation)",
            ui -> {
                final PeasyCam cam = ui.getCamera();
                return cam != null? U.nf001(U.normalizeDegrees(PApplet.degrees(cam.getRotations()[1]))) + "°": "";
            },
            "< >",
            (ui, ev) -> {
                final PeasyCam cam = ui.getCamera();
                final int mod = ev.getModifiers();
                final int kc = ev.getKeyCode();
                if (cam != null && (kc == java.awt.event.KeyEvent.VK_LEFT || kc == java.awt.event.KeyEvent.VK_RIGHT) && mod == 0) {
                    final float ry = ((kc == java.awt.event.KeyEvent.VK_LEFT)? 1: -1) * PApplet.HALF_PI;
                    U.rotateY(cam, ry, GLConfig.CAMERA_ROTATIONS_ANIMATION_MILLS);
                    return true;
                }

                return false;
            }, false, false, GLConfig.COLOR_Y_AXIS),

    CAMERA_ROTATE_Z("Roll (Z)",
            "Controls the Camera ROLL (Z-Axis rotation)",
            ui -> {
                final PeasyCam cam = ui.getCamera();
                return cam != null? U.nf001(U.normalizeDegrees(PApplet.degrees(cam.getRotations()[2]))) + "°": "";
            },
            "Shf ^",
            (ui, ev) -> {
                final PeasyCam cam = ui.getCamera();
                final int mod = ev.getModifiers();
                final int kc = ev.getKeyCode();
                if (cam != null && (kc == java.awt.event.KeyEvent.VK_LEFT || kc == java.awt.event.KeyEvent.VK_RIGHT) && mod == Event.SHIFT /* With Shift */) {
                    final float rz = ((kc == java.awt.event.KeyEvent.VK_LEFT)? 1: -1) * PApplet.HALF_PI;
                    U.rotateZ(cam, rz, GLConfig.CAMERA_ROTATIONS_ANIMATION_MILLS);
                    return true;
                }

                return false;
            }, false, false, GLConfig.COLOR_Z_AXIS),

    ;

    @NotNull
    public final String label;
    @NotNull
    public final String description;
    @NotNull
    private final Function<BasePendulumWavePUi, String> valueProvider;
    @NotNull
    public final String keyEventLabel;
    @NotNull
    private final BiFunction<BasePendulumWavePUi, KeyEvent, Boolean> keyEventHandler;

    /**
     * Whether this control is continuous or discrete event
     */
    public final boolean continuousKeyEvent;

    public final boolean alwaysShowKeyBinding;

    @Nullable
    public final Color labelColorOverride;

    Control(@NotNull String label,
            @NotNull String description,
            @NotNull Function<BasePendulumWavePUi, String> valueProvider,
            @NotNull String keyEventLabel,
            @NotNull BiFunction<BasePendulumWavePUi, KeyEvent, Boolean> keyEventHandler,
            boolean continuousKeyEvent,
            boolean alwaysShowKeyBinding,
            @Nullable Color labelColorOverride) {

        this.label = label;
        this.description = description;
        this.valueProvider = valueProvider;
        this.keyEventLabel = keyEventLabel;
        this.keyEventHandler = keyEventHandler;
        this.continuousKeyEvent = continuousKeyEvent;
        this.alwaysShowKeyBinding = alwaysShowKeyBinding;
        this.labelColorOverride = labelColorOverride;
    }

    Control(@NotNull String label,
            @NotNull String description,
            @NotNull Function<BasePendulumWavePUi, String> valueProvider,
            @NotNull String keyEventLabel,
            @NotNull BiFunction<BasePendulumWavePUi, KeyEvent, Boolean> keyEventHandler,
            boolean continuousKeyEvent) {
        this(label, description, valueProvider, keyEventLabel, keyEventHandler, continuousKeyEvent, false, null);
    }


    /**
     * @return current formatted value
     */
    @NotNull
    public String getValue(@NotNull BasePendulumWavePUi baseUi) {
        return valueProvider.apply(baseUi);
    }

    /**
     * @return true oif handled, false otherwise
     */
    public boolean handleKeyEvent(@NotNull BasePendulumWavePUi baseUi, @NotNull KeyEvent event) {
        return keyEventHandler.apply(baseUi, event);
    }


    /* ............................................................................ */

    public static final Control[] CONTROLS_MAIN1 = {
            PENDULUM_COUNT,
            RESET,
            DRAW_ONLY_BOBS
    };

    public static final Control[] CONTROLS_MAIN2 = {
            SOUND,
            POLY_RHYTHM
    };

    public static final Control[] CONTROLS_MAIN3 = {
            HUD_ENABLED,
            SHOW_KEY_BINDINGS
    };

    // Controls to show even when HUD is disabled
    public static final Control[] CONTROLS_HUD_DISABLED = {
            HUD_ENABLED
    };


    public static final Control[] CONTROLS_STATUS1 = {
            SIMULATION_SPEED,
            GRAVITY,
            DRAG
    };

    public static final Control[] CONTROLS_STATUS2 = {
            PENDULUM_MASS,
            PENDULUM_START_ANGLE,
            WAVE_PERIOD,
            MIN_OSCILLATIONS_IN_WAVE_PERIOD,
            OSCILLATION_STEP_PER_PENDULUM
    };

    public static final Control[] CONTROLS_CAMERA = {
            CAMERA_ROTATE_X,
            CAMERA_ROTATE_Y,
            CAMERA_ROTATE_Z
    };


    @Nullable
    private static Control[] sValuesShared;
    private static final Object sValuesLock = new Object();

    public static Control[] getValuesShared() {
        Control[] values = sValuesShared;
        if (values == null) {
            synchronized (sValuesLock) {
                values = sValuesShared;
                if (values == null) {
                    values = values();
                    sValuesShared = values;
                }
            }
        }

        return values;
    }


    @NotNull
    public static String createDescription() {
        final Control[] controls = getValuesShared();

        final StringBuilder sj = new StringBuilder();
        boolean first = true;

        for (Control c: controls) {
            if (first) {
                first = false;
            } else {
                sj.append("\n\n");      // Delimiter
            }

            sj.append(" -> ").append(c.label).append(" : ").append(c.keyEventLabel)
                    .append("   (").append(c.continuousKeyEvent? "Continuous": "Discrete").append(')')
                    .append("\n\t ").append(c.description);
        }

        return sj.toString();
    }
}
