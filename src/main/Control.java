package main;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import peasy.PeasyCam;
import processing.core.PApplet;
import processing.event.Event;
import processing.event.KeyEvent;
import util.Format;
import util.U;

import java.awt.*;
import java.util.function.BiFunction;
import java.util.function.Function;

public enum Control {

    // Only when fullscreen
    EXPAND_FULLSCREEN("Window",
            "Sets the fullscreen mode to Expanded or Windowed.",
            ui -> ui.isFullscreenExpanded()? "EXP": "WIN",
            "W",
            "",
            (ui, ev) -> {
                final int mod = ev.getModifiers();
                final int kc = ev.getKeyCode();
                if (ui.isFullscreen() && kc == java.awt.event.KeyEvent.VK_W && mod == 0) {
                    ui.toggleFullscreenExpanded(true);
                    return true;
                }

                return false;
            }, false),

    PENDULUM_COUNT("Pendulums",
            "Number of Pendulums in the Pendulum Wave.",
            ui -> String.valueOf(ui.getPendulumWave().pendulumCount()),
            "[Ctr | Shf]-N",
            "N -> Increase Count  |  Shift-N -> Decrease Count  |  Ctrl-[Shift]-N -> Change count without resetting pendulums state",
            (ui, ev) -> {
                if (ev.getKeyCode() == java.awt.event.KeyEvent.VK_N) {
                    ui.getPendulumWave().stepPendulumCount(!ev.isShiftDown(), !ev.isControlDown());
                    return true;
                }

                return false;
            }, false),

    PLAY_PAUSE("Play/Pause",
            "Play/Pause simulation.",
            ui -> ui.getPendulumWave().isPaused() ? "Paused" : "Playing",
            "SPACE",
            "",
            (ui, ev) -> {
                final int mod = ev.getModifiers();
                if (ev.getKeyCode() == java.awt.event.KeyEvent.VK_SPACE && mod == 0) {
                    ui.getPendulumWave().togglePlayPause();
                    return true;
                }

                return false;
            }, false),

    RESET("Reset",
            "Resets the Simulation.",
            ui -> "",
            "[Ctr | Shf]-R",
            "R -> Reset Pendulums State  |  Ctrl-R -> Reset Pendulum Count  |  Shift-R -> Reset Simulation Environment   |  Ctrl-Shift-R -> Reset Everything",
            (ui, ev) -> {
                final int mod = ev.getModifiers();
                if (ev.getKeyCode() == java.awt.event.KeyEvent.VK_R) {
                    if (mod == 0) {
                        ui.getPendulumWave().resetPendulumsState();
                    } else if (ev.isShiftDown()) {
                        ui.getPendulumWave().resetSimulation(ev.isControlDown(), ev.isControlDown());
                    } else if (ev.isControlDown()) {
                        ui.getPendulumWave().resetPendulumCount(true);
                    }

                    return true;
                }

                return false;
            }, false),

    DRAW_ONLY_BOBS("Bobs Only",
            "Draw pendulum bobs only (do not draw pendulum chords and support).",
            ui -> ui.isDrawOnlyBobEnabled() ? "ON" : "OFF",
            "B",
            "",
            (ui, ev) -> {
                final int mod = ev.getModifiers();
                if (ev.getKeyCode() == java.awt.event.KeyEvent.VK_B && mod == 0) {
                    ui.toggleDrawOnlyBob();
                    return true;
                }

                return false;
            }, false),

    HUD_ENABLED("HUD",
            "Show/Hide HUD.",
            ui -> ui.isHudEnabled() ? "ON" : "OFF",
            "H",
            "",
            (ui, ev) -> {
                final int mod = ev.getModifiers();
                if (ev.getKeyCode() == java.awt.event.KeyEvent.VK_H && mod == 0) {
                    ui.toggleHudEnabled();
                    return true;
                }

                return false;
            }, false, true, null),

    SHOW_KEY_BINDINGS("Controls",
            "Show/Hide Control Key Bindings.",
            ui -> ui.areKeyBindingsShown() ? "ON" : "OFF",
            "C",
            "",
            (ui, ev) -> {
                final int mod = ev.getModifiers();
                if (ev.getKeyCode() == java.awt.event.KeyEvent.VK_C && mod == 0) {
                    ui.toggleShowKeyBindings();
                    return true;
                }

                return false;
            }, false, true, null),


    SOUND("Sound",
            "Toggle Sounds.",
            ui -> ui.isSoundEnabled() ? "ON" : "OFF",
            "S",
            "",
            (ui, ev) -> {
                final int mod = ev.getModifiers();
                if (ev.getKeyCode() == java.awt.event.KeyEvent.VK_S && mod == 0) {
                    ui.toggleSoundEnabled();
                    return true;
                }

                return false;
            }, false),

    POLY_RHYTHM("Poly Rhythm",
            "Toggle Poly Rhythm (play multiple notes at once).",
            ui -> ui.isPolyRhythmEnabled() ? "ON" : "OFF",
            "Shf-S",
            "",
            (ui, ev) -> {
                final int mod = ev.getModifiers();
                if (ev.getKeyCode() == java.awt.event.KeyEvent.VK_S && mod == Event.SHIFT /* Shift only */) {
                    ui.togglePolyRhythmEnabled();
                    return true;
                }

                return false;
            }, false),

    SAVE_FRAME("Save Frame",
            "Save Current graphics frame in a png file.",
            ui -> "",
            "Ctrl-S",
            "",
            (ui, ev) -> {
                final int mod = ev.getModifiers();
                if (ev.getKeyCode() == java.awt.event.KeyEvent.VK_S && mod == Event.CTRL /* Ctrl only */) {
                    ui.snapshot();
                    return true;
                }

                return false;
            }, false),

    SIMULATION_SPEED("Sim Speed",
            "Simulation Speed, in both multiples and percentage.",
            ui -> String.format("%sx (%s%%)",
                    Format.nf001(ui.getPendulumWave().getSpeed()),
                    Format.nf001(ui.getPendulumWave().getSpeedPercent())),
            "[Shf]-/",
            "/ -> Increase Speed  |  Shift-/ -> Decrease Speed",
            (ui, ev) -> {
                final int mod = ev.getModifiers();
                if (ev.getKeyCode() == java.awt.event.KeyEvent.VK_SLASH && (mod == 0 || mod == Event.SHIFT) /* Only shift allowed */) {
                    ui.getPendulumWave().stepSpeed(mod == 0);
                    return true;
                }

                return false;
            }, true),

    GRAVITY("Gravity",
            "Acceleration due to Gravity (in ms-2).",
            ui -> Format.nf002(ui.getPendulumWave().gravity()) + " ms-2",
            "[Shf]-G",
            "G -> Increase Gravity  |  Shift-G -> Decrease Gravity",
            (ui, ev) -> {
                final int mod = ev.getModifiers();
                if (ev.getKeyCode() == java.awt.event.KeyEvent.VK_G && (mod == 0 || mod == Event.SHIFT) /* Only shift allowed */) {
                    ui.getPendulumWave().stepGravity(mod == 0, false);
                    return true;
                }

                return false;
            }, true),

    DRAG("Drag",
            "Drag Coefficient (in g/s), Positive value corresponds to drag, negative to push.",
            ui -> Format.nf002(ui.getPendulumWave().drag() * 1000) + " g/s",
            "[Shf]-D",
            "D -> Increase Drag  |  Shift-D -> Decrease Drag",
            (ui, ev) -> {
                final int mod = ev.getModifiers();
                if (ev.getKeyCode() == java.awt.event.KeyEvent.VK_D && (mod == 0 || mod == Event.SHIFT) /* Only shift allowed */) {
                    ui.getPendulumWave().stepDrag(mod == 0, false);
                    return true;
                }

                return false;
            }, true),

    PENDULUM_MASS("Mass",
            "Mass of each pendulum Bob (in grams).",
            ui -> Format.nf002(ui.getPendulumWave().getPendulumMass() * 1000) + " g",
            "[Shf]-M",
            "M -> Increase Mass  |  Shift-M -> Decrease Mass",
            (ui, ev) -> {
                final int mod = ev.getModifiers();
                if (ev.getKeyCode() == java.awt.event.KeyEvent.VK_M && (mod == 0 || mod == Event.SHIFT) /* Only shift allowed */) {
                    ui.getPendulumWave().stepPendulumMass(mod == 0, false);
                    return true;
                }

                return false;
            }, true),

    PENDULUM_START_ANGLE("Start Angle",
            "Start angle for each pendulum (in degrees).",
            ui -> Format.nf001(U.normalizeDegrees(PApplet.degrees(ui.getPendulumWave().getPendulumStartAngle()))) + "°",
            "[Shf]-A",
            "A -> Increase Start Angle  |  Shift-A -> Decrease Start Angle",
            (ui, ev) -> {
                final int mod = ev.getModifiers();
                if (ev.getKeyCode() == java.awt.event.KeyEvent.VK_A && (mod == 0 || mod == Event.SHIFT) /* Only shift allowed */) {
                    ui.getPendulumWave().stepPendulumStartAngle(mod == 0, false);
                    return true;
                }

                return false;
            }, true),

    WAVE_PERIOD("Wave Period",
            "Total time in which the Pendulum Wave completes one cycle (in secs).",
            ui -> Format.nf001(ui.getPendulumWave().getEffectiveWavePeriod()) + " s",
            "[Shf]-P",
            "P -> Increase Wave Period  |  Shift-P -> Decrease Wave Period",
            (ui, ev) -> {
                final int mod = ev.getModifiers();
                if (ev.getKeyCode() == java.awt.event.KeyEvent.VK_P && (mod == 0 || mod == Event.SHIFT) /* Only shift allowed */) {
                    ui.getPendulumWave().stepEffectiveWavePeriod(mod == 0, false);
                    return true;
                }

                return false;
            }, true),

    MIN_OSCILLATIONS_IN_WAVE_PERIOD("Min Osc",
            "Number of oscillations the first pendulum completes in Wave Period time.",
            ui -> Format.nf001(ui.getPendulumWave().getMinOscillationsInWavePeriod()),
            "[Shf]-O",
            "O -> Increase Min Osc  |  Shift-O -> Decrease Min Osc",
            (ui, ev) -> {
                final int mod = ev.getModifiers();
                if (ev.getKeyCode() == java.awt.event.KeyEvent.VK_O && (mod == 0 || mod == Event.SHIFT) /* Only shift allowed */) {
                    ui.getPendulumWave().stepMinOscillationsInWavePeriod(mod == 0, false);
                    return true;
                }

                return false;
            }, true),

    OSCILLATION_STEP_PER_PENDULUM("Osc Step",
            "Number of oscillations that a pendulum completes more than its predecessor.",
            ui -> Format.nf001(ui.getPendulumWave().getOscillationsStepPerPendulum()),
            "[Shf]-I",
            "I -> Increase Osc Step  |  Shift-I -> Decrease Osc Step",
            (ui, ev) -> {
                final int mod = ev.getModifiers();
                if (ev.getKeyCode() == java.awt.event.KeyEvent.VK_I && (mod == 0 || mod == Event.SHIFT) /* Only shift allowed */) {
                    ui.getPendulumWave().stepOscillationsStepPerPendulum(mod == 0, false);
                    return true;
                }

                return false;
            }, true),


    /* Camera Controls */

    CAMERA_ROTATE_X("Pitch-X",
            "Controls the Camera PITCH (rotation about X-Axis).",
            ui -> {
                final PeasyCam cam = ui.getCamera();
                return cam != null? Format.nf001(U.normalizeDegrees(PApplet.degrees(cam.getRotations()[0]))) + "°": "N/A";
            },
            "Up/Down",
            "[UP | DOWN] arrow keys",
            (ui, ev) -> {
                final int mod = ev.getModifiers();
                final int kc = ev.getKeyCode();
                if (ui.cameraSupported() && (kc == java.awt.event.KeyEvent.VK_UP || kc == java.awt.event.KeyEvent.VK_DOWN) && (mod == 0 || mod == Event.CTRL)) {
                    ui.rotateCameraXByUnit(kc == java.awt.event.KeyEvent.VK_UP, mod == 0);
                    return true;
                }

                return false;
            }, false, false, GLConfig.COLOR_X_AXIS),

    CAMERA_ROTATE_Y("Yaw-Y",
            "Controls the Camera YAW (rotation about Y-Axis).",
            ui -> {
                final PeasyCam cam = ui.getCamera();
                return cam != null? Format.nf001(U.normalizeDegrees(PApplet.degrees(cam.getRotations()[1]))) + "°": "N/A";
            },
            "Left/Right",
            "[LEFT | RIGHT] arrow keys",
            (ui, ev) -> {
                final int mod = ev.getModifiers();
                final int kc = ev.getKeyCode();
                if (ui.cameraSupported() && (kc == java.awt.event.KeyEvent.VK_LEFT || kc == java.awt.event.KeyEvent.VK_RIGHT) && (mod == 0 || mod == Event.CTRL)) {
                    ui.rotateCameraYByUnit(kc == java.awt.event.KeyEvent.VK_LEFT, mod == 0);
                    return true;
                }

                return false;
            }, false, false, GLConfig.COLOR_Y_AXIS),

    CAMERA_ROTATE_Z("Roll-Z",
            "Controls the Camera ROLL (rotation about Z-Axis).",
            ui -> {
                final PeasyCam cam = ui.getCamera();
                return cam != null? Format.nf001(U.normalizeDegrees(PApplet.degrees(cam.getRotations()[2]))) + "°": "N/A";
            },
            "Shf-Up/Down",
            "Shift-[LEFT | RIGHT] arrow keys",
            (ui, ev) -> {
//                final int mod = ev.getModifiers();
                final int kc = ev.getKeyCode();
                if (ui.cameraSupported() && (kc == java.awt.event.KeyEvent.VK_LEFT || kc == java.awt.event.KeyEvent.VK_RIGHT) && ev.isShiftDown() /* With Shift */) {
                    ui.rotateCameraZByUnit(kc == java.awt.event.KeyEvent.VK_LEFT, !ev.isControlDown());
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
    public final String keyBindingLabel;
    @NotNull
    public final String keyBindingDescription;
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
            @NotNull String keyBindingLabel,
            @NotNull String keyBindingDescription,
            @NotNull BiFunction<BasePendulumWavePUi, KeyEvent, Boolean> keyEventHandler,
            boolean continuousKeyEvent,
            boolean alwaysShowKeyBinding,
            @Nullable Color labelColorOverride) {

        this.label = label;
        this.description = description;
        this.valueProvider = valueProvider;
        this.keyBindingLabel = keyBindingLabel;
        this.keyBindingDescription = keyBindingDescription;
        this.keyEventHandler = keyEventHandler;
        this.continuousKeyEvent = continuousKeyEvent;
        this.alwaysShowKeyBinding = alwaysShowKeyBinding;
        this.labelColorOverride = labelColorOverride;
    }

    Control(@NotNull String label,
            @NotNull String description,
            @NotNull Function<BasePendulumWavePUi, String> valueProvider,
            @NotNull String keyBindingLabel,
            @NotNull String keyBindingDescription,
            @NotNull BiFunction<BasePendulumWavePUi, KeyEvent, Boolean> keyEventHandler,
            boolean continuousKeyEvent) {
        this(label, description, valueProvider, keyBindingLabel, keyBindingDescription, keyEventHandler, continuousKeyEvent, false, null);
    }


    /**
     * @return current formatted value
     */
    @NotNull
    public String getFormattedValue(@NotNull BasePendulumWavePUi baseUi) {
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

    public static final Control[] CONTROLS_FULLSCREEN_WINDOW = {
            EXPAND_FULLSCREEN
    };


    @Nullable
    private static volatile Control[] sValuesShared;
    private static final Object sValuesLock = new Object();

    @Nullable
    private static volatile String sControlsDescription;

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
    private static String createControlsDescription() {
        final Control[] controls = getValuesShared();

        final StringBuilder sj = new StringBuilder();
        boolean firstControl = true;

        for (Control c: controls) {
            if (firstControl) {
                firstControl = false;
            } else {
                sj.append("\n\n");      // Delimiter
            }

            sj.append("-> ").append(c.keyBindingLabel).append(" : ").append(c.label)
                    .append("  [").append(c.continuousKeyEvent? "Continuous": "Discrete").append(']');

            // Descriptions
            for (String line: c.description.split("\n")) {
                if (line == null || line.isEmpty())
                    continue;

                sj.append("\n\t").append(line);
            }

            boolean firstKeyBind = true;
            // Key bindings Description
            for (String line: c.keyBindingDescription.split("\n")) {
                if (line == null || line.isEmpty())
                    continue;

                if (firstKeyBind) {
                    sj.append("\n\t<Keys> : ");
                    firstKeyBind = false;
                } else {
                    sj.append("\n\t              ");
                }

                sj.append(line);
            }
        }

        return sj.toString();
    }

    public static String getControlsDescription() {
        String des = sControlsDescription;

        if (des == null) {
            synchronized (Control.class) {
                des = sControlsDescription;
                if (des == null) {
                    des = createControlsDescription();
                    sControlsDescription = des;
                }
            }
        }

        return des;
    }
}
