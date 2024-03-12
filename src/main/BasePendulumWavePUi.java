package main;

import org.apache.commons.math3.exception.OutOfRangeException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.tritonus.share.ArraySet;
import peasy.PeasyCam;
import pendulum.*;
import processing.core.PApplet;
import processing.core.PFont;
import processing.event.KeyEvent;
import processing.opengl.PJOGL;
import sound.MidiNotePlayer;
import util.Config;
import util.Format;
import util.U;

import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.function.Predicate;


/**
 * A base graphical UI for {@link PendulumWave Pendulum Wave} in Processing 3
 * */
public abstract class BasePendulumWavePUi extends PApplet implements PendulumStyleProvider, PendulumWave.Listener {

    private static final boolean CREATE_README = false;

    /**
     * Custom event with a {@link Runnable} task payload to be executed on the UI thread
     *
     * @see #enqueueTask(Runnable)
     * @see #handleKeyEvent(KeyEvent)
     * */
    private static final int ACTION_EXECUTE_RUNNABLE = 121230123;

    @NotNull
    public static Dimension getNativeScreenResolution() {
        return U.SCREEN_RESOLUTION_NATIVE;
    }


    private int _w, _h;
    @Nullable
    private KeyEvent mKeyEvent;
    protected PFont pdSans, pdSansMedium;

    @NotNull
    protected final PendulumWave pendulumWave;

    /* Draw Styles */
    private boolean mDrawOnlyBob = GLConfig.DEFAULT_DRAW_ONLY_BOB;
    private final List<PendulumDrawStylesHolder> drawStyles = Collections.synchronizedList(new ArrayList<>());

    /* Sound */
    private boolean mSoundEnabled = GLConfig.DEFAULT_SOUND_ENABLED;
    private boolean mPolyRhythmEnabled = GLConfig.DEFAULT_POLY_RHYTHM_ENABLED;
    @Nullable
    private volatile MidiNotePlayer mSoundPlayer;

    /* HUD and Controls */
    private boolean mHudEnabled = GLConfig.DEFAULT_HUD_ENABLED;
    private boolean mShowKeyBindings = GLConfig.DEFAULT_SHOW_KEY_BINDINGS;

    private int mPausedDrawTrigger;

    public BasePendulumWavePUi(@NotNull PendulumWave pendulumWave) {
        this.pendulumWave = pendulumWave;
        updatePendulumDrawStyles();
    }

    public BasePendulumWavePUi(int initialPendulumCount) {
        this(new PendulumWave(initialPendulumCount));
    }

    public BasePendulumWavePUi() {
        this(new PendulumWave());
    }

    public final void attachPendulumWaveListener() {
        pendulumWave.setListener(this);
    }

    public final void detachPendulumWaveListener() {
        if (pendulumWave.getListener() == this) {
            pendulumWave.setListener(null);
        }
    }


    public abstract boolean isRendered3D();

    public abstract boolean isFullscreen();

    public abstract boolean isInitialFullscreenExpanded();

    @NotNull
    public abstract Dimension getInitialWindowedSize();

    /* Camera */

    @Nullable
    public PeasyCam getCamera() {
        return null;
    }

    public final boolean cameraSupported() {
        return getCamera() != null;
    }

    public boolean shouldDrawAxesInHUD() {
        return isRendered3D();
    }

    public boolean supportsSurfaceLocationSetter() {
        return true;
    }

    public boolean supportsSurfaceSizeSetter() {
        return true;
    }

    @Override
    public void settings() {
        // icon
        if (R.IMAGE_PENDULUM_WAVE_ICON != null) {
            PJOGL.setIcon(R.IMAGE_PENDULUM_WAVE_ICON.toString());
        }
    }


    public void setup() {
//        println("-> UI Thread: " + Thread.currentThread().getName());

        _w = width;
        _h = height;

        // Surface methods  ..........................................................................
        surface.setTitle(isRendered3D()? R.TITLE_3D: R.TITLE_2D);
        if (supportsSurfaceSizeSetter()) {
            surface.setResizable(true);
        }

        // Fullscreen does not expand to entire screen with P2D and P3D renderers. THis hack solves the problem
        if (isFullscreen()) {       // (P2D.equals(sketchRenderer()) || P3D.equals(sketchRenderer()))
            // A Hack to force window to fullscreen with P2D and P3D. only works after a certain delay, so post an event
            enqueueTask(() -> setFullscreenExpanded(isInitialFullscreenExpanded(), false));
        }

        if (GLConfig.DEFAULT_WINDOW_IN_SCREEN_CENTER && !isFullscreen()) {
            setSurfaceLocationCenter(false);
        }

//        if (sketchFullScreen()) {
//            surface.setAlwaysOnTop(true);       // Keyboard focus, since already fullscreen so no problem in this
//        } else {
//            surface.setResizable(true);
//        }

        frameRate(GLConfig.FRAME_RATE);

        // Fonts  .............................................................................
        pdSans = createFont(R.FONT_PD_SANS_REGULAR.toString(), 20);
        pdSansMedium = createFont(R.FONT_PD_SANS_MEDIUM.toString(), 20);
        textFont(pdSans);       // Default
    }


    public final void applyConfig(@NotNull Config config) {
        // Sounds
        mSoundEnabled = config.getValueBool(R.CONFIG_KEY_SOUND, mSoundEnabled);
        mPolyRhythmEnabled = config.getValueBool(R.CONFIG_KEY_POLY_RHYTHM, mPolyRhythmEnabled);

        // Simulation Environment
        final float speed = config.getValueFloat(R.CONFIG_KEY_SPEED, -1f);
        if (speed > 0)
            pendulumWave.setSpeed(speed);

        if (config.containsKey(R.CONFIG_KEY_GRAVITY)) {
            final float gravity = config.getValueFloat(R.CONFIG_KEY_GRAVITY, pendulumWave.gravity());
            pendulumWave.setGravity(gravity, false);
        }

        if (config.containsKey(R.CONFIG_KEY_DRAG)) {
            final float drag = config.getValueFloat(R.CONFIG_KEY_DRAG, pendulumWave.drag() * 1000 /* in gram/s */);
            pendulumWave.setDrag(drag / 1000 /* in kg/s */, false);
        }

        // Pendulum
        final float mass = config.getValueFloat(R.CONFIG_KEY_MASS, -1f);
        if (mass > 0)
            pendulumWave.setPendulumMass(mass / 1000 /* in kg */, false);

        if (config.containsKey(R.CONFIG_KEY_START_ANGLE)) {
            final float startAngle = config.getValueFloat(R.CONFIG_KEY_START_ANGLE, degrees(pendulumWave.getPendulumStartAngle()));
            pendulumWave.setPendulumStartAngle(radians(startAngle), false);
        }

        // Pendulum Wave
        final int count = config.getValueInt(R.CONFIG_KEY_PENDULUM_COUNT, -1);
        if (count > 0)
            pendulumWave.setPendulumCount(count, false);

        final float minOsc = config.getValueFloat(R.CONFIG_KEY_MIN_OSC, -1f);
        if (minOsc > 0)
            pendulumWave.setMinOscillationsInWavePeriod(minOsc, false);

        final float osc_step = config.getValueFloat(R.CONFIG_KEY_OSC_STEP, -1f);
        if (osc_step > 0)
            pendulumWave.setOscillationsStepPerPendulum(osc_step, false);

        final float wp = config.getValueFloat(R.CONFIG_KEY_WAVE_PERIOD, -1f);
        if (wp > 0)
            pendulumWave.setEffectiveWavePeriod(wp, false);     // should be last
    }

    public final boolean setSurfaceLocation(int x, int y, boolean verbose) {
        if (supportsSurfaceLocationSetter()) {
            surface.setLocation(x, y);
            return true;
        }

        if (verbose) {
            printErrln(R.SHELL_WINDOW + String.format("Current %s renderer does not support changing window position on screen!!\n\tRenderer: %s\n\tFullscreen: %b", isRendered3D()? "3D": "2D", sketchRenderer(), isFullscreen()));
        }

        return false;
    }

    public final boolean setSurfaceSize(int w, int h, boolean verbose) {
        if (supportsSurfaceSizeSetter()) {
            surface.setSize(w, h);
            return true;
        }

        if (verbose) {
            printErrln(R.SHELL_WINDOW + String.format("Current %s renderer does not support changing window size!!\n\tRenderer: %s\n\tFullscreen: %b", isRendered3D()? "3D": "2D", sketchRenderer(), isFullscreen()));
        }

        return false;
    }

    public final boolean setSurfaceLocationCenter(boolean verbose) {
        final Dimension screen_res = getNativeScreenResolution();
        return setSurfaceLocation((screen_res.width - width) / 2, (screen_res.height - height) / 2, verbose);
    }

    public final void setSurfaceToInitialWindowedSize(boolean centerOnScreen, boolean verbose) {
        final Dimension def = getInitialWindowedSize();
        setSurfaceSize(def.width, def.height, verbose);

        if (centerOnScreen) {
            setSurfaceLocationCenter(verbose);
        }
    }

    // A Hack to force window to fullscreen with P2D and P3D.
    private void setSurfaceToNativeFullscreenSize() {
        final Dimension screen_res = getNativeScreenResolution();
        surface.setSize(screen_res.width, screen_res.height);
        surface.setLocation(0, 0);
    }

    /**
     * @return true if this window is in fullscreen mode and is expanded on the entire screen native resolution
     *
     * @see #isFullscreen()
     * @see #getNativeScreenResolution()
     * */
    public final boolean isFullscreenExpanded() {
        final Dimension screen_res;
        return isFullscreen() && (screen_res = getNativeScreenResolution()).width == width && screen_res.height == height;
    }

    /**
     * Expand or window a fullscreen PGraphics surface<br>
     * <br>
     * Expand: Fill the entire screen native resolution<br>
     * Window: Set the surface size to initial windowed size, as given by {@link #getInitialWindowedSize()} and center on screen
     * */
    public final void setFullscreenExpanded(boolean expanded, boolean verbose) {
        if (!isFullscreen())
            return;

        if (expanded) {
            setSurfaceToNativeFullscreenSize();
        } else {
            setSurfaceToInitialWindowedSize(true, verbose);
        }
    }

    public final void toggleFullscreenExpanded(boolean verbose) {
        if (!isFullscreen())
            return;

        setFullscreenExpanded(!isFullscreenExpanded(), verbose);
    }

    public final void resetSurfaceSizeAndPos(boolean verbose) {
        if (isFullscreen()) {
            setFullscreenExpanded(isInitialFullscreenExpanded(), verbose);
        } else {
            setSurfaceToInitialWindowedSize(true, verbose);
        }
    }


    protected void drawMain() {
        background(GLConfig.BG.getRGB());

        pendulumWave.updatePendulums();
        pendulumWave.drawPendulums(this, this);
    }


    protected void beginHUD() {
    }

    protected void endHUD() {
    }


    /**
     * @return bottomY of the bounding box
     * */
    private float drawMainControlsAlignRight(@NotNull Control[] controls, float topy, float padx, float hgap, float vgap, @NotNull Predicate<Control> showKeyBindingsFilter) {
        final String label_value_delimiter = "      ";
        final String label_key_label_delimiter = "   ";

        textSize(getTextSize(max(GLConfig.TEXT_SIZE_MAIN_CONTROLS_LABEL, GLConfig.TEXT_SIZE_MAIN_CONTROLS_KEY_LABEL, GLConfig.TEXT_SIZE_MAIN_CONTROLS_VALUE)));
        final float entry_height = textAscent() + textDescent();
        float max_val_width = 0;

        final String[] values = new String[controls.length];
        for (int i=0; i < controls.length; i++) {
            final String val = controls[i].getFormattedValue(this);
            values[i] = val;

            max_val_width = max(max_val_width, textWidth(val));
        }

        max_val_width += textWidth(label_value_delimiter);

        final float x1 = width - padx;
        final float x2 = x1 - max_val_width - hgap;

        for (int i=0; i < controls.length; i++) {
            final Control control = controls[i];
            final String label = control.label;
            final String keyLabel = label_key_label_delimiter + control.keyBindingLabel;
            final String val = values[i];

            final float y1 = topy + (i * (entry_height + vgap));

            // Value
            textSize(getTextSize(GLConfig.TEXT_SIZE_MAIN_CONTROLS_VALUE));
            fill(GLConfig.FG_MAIN_CONTROLS_VALUE.getRGB());
            textAlign(RIGHT, TOP);
            text(label_value_delimiter + val, x1, y1);

            // Key Label
            final float keyLabelW;
            if (showKeyBindingsFilter.test(control)) {
                textSize(getTextSize(GLConfig.TEXT_SIZE_MAIN_CONTROLS_KEY_LABEL));
                fill(GLConfig.FG_MAIN_CONTROLS_KEY_LABEL.getRGB());
                textAlign(RIGHT, BOTTOM);
                text(keyLabel, x2, y1 + entry_height);
                keyLabelW = textWidth(keyLabel);
            } else {
                keyLabelW = 0;
            }

            // Label
            textSize(getTextSize(GLConfig.TEXT_SIZE_MAIN_CONTROLS_LABEL));
            fill((control.labelColorOverride != null? control.labelColorOverride: GLConfig.FG_MAIN_CONTROLS_LABEL).getRGB());
            textAlign(RIGHT, BOTTOM);
            text(label, x2 - keyLabelW, y1 + entry_height);
        }

        return topy + ((controls.length - 1) * (entry_height + vgap)) + entry_height;
    }

    private float drawMainControlsAlignLeft(@NotNull Control[] controls, float topy, float padx, float hgap, float vgap, @NotNull Predicate<Control> showKeyBindingsFilter) {
        final String label_value_delimiter = "      ";
        final String label_key_label_delimiter = "   ";

        textSize(getTextSize(max(GLConfig.TEXT_SIZE_MAIN_CONTROLS_LABEL, GLConfig.TEXT_SIZE_MAIN_CONTROLS_KEY_LABEL, GLConfig.TEXT_SIZE_MAIN_CONTROLS_VALUE)));
        final float entry_height = textAscent() + textDescent();

        textSize(getTextSize(max(GLConfig.TEXT_SIZE_MAIN_CONTROLS_LABEL, GLConfig.TEXT_SIZE_MAIN_CONTROLS_KEY_LABEL)));
        float max_label_width = 0;
        for (Control c: controls) {
            final String label = c.label + (showKeyBindingsFilter.test(c)? label_key_label_delimiter + c.keyBindingLabel : "");

            max_label_width = max(max_label_width, textWidth(label));
        }

        final float x2 = padx + max_label_width + hgap;

        for (int i=0; i < controls.length; i++) {
            final Control control = controls[i];
            final String label = control.label;
            final String keyLabel = label_key_label_delimiter + control.keyBindingLabel;
            final String val = control.getFormattedValue(this);

            final float y1 = topy + (i * (entry_height + vgap));

            // Label
            textSize(getTextSize(GLConfig.TEXT_SIZE_MAIN_CONTROLS_LABEL));
            fill((control.labelColorOverride != null? control.labelColorOverride: GLConfig.FG_MAIN_CONTROLS_LABEL).getRGB());
            textAlign(LEFT, BOTTOM);
            text(label, padx, y1 + entry_height);
            final float labelW = textWidth(label);

            // Key Label
            if (showKeyBindingsFilter.test(control)) {
                textSize(getTextSize(GLConfig.TEXT_SIZE_MAIN_CONTROLS_KEY_LABEL));
                fill(GLConfig.FG_MAIN_CONTROLS_KEY_LABEL.getRGB());
                textAlign(LEFT, BOTTOM);
                text(keyLabel, padx + labelW, y1 + entry_height);
            }

            // Value
            textSize(getTextSize(GLConfig.TEXT_SIZE_MAIN_CONTROLS_VALUE));
            fill(GLConfig.FG_MAIN_CONTROLS_VALUE.getRGB());
            textAlign(LEFT, TOP);
            text(label_value_delimiter + val, x2, y1);
        }

        return topy + ((controls.length - 1) * (entry_height + vgap)) + entry_height;
    }

    /**
     * @return top y coordinate of the bounding rectangle
     * */
    private float drawStatusControls(@NotNull Control[] controls1, @NotNull Control[] controls2, @NotNull String delimiter, float padx, float pady, float vgap, boolean showKeyBindings) {
        final String[] values1 = new String[controls1.length];
        final String[] values2 = new String[controls2.length];

        for (int i=0; i < controls1.length; i++) {
            Control c = controls1[i];
            values1[i] = c.label + " : " + c.getFormattedValue(this);
        }

        for (int i=0; i < controls2.length; i++) {
            Control c = controls2[i];
            values2[i] = c.label + " : " + c.getFormattedValue(this);
        }

        final String status1 = join(values1, delimiter);
        final String status2 = join(values2, delimiter);

        final float statusBottomY = height - pady;

        textSize(getTextSize(GLConfig.TEXT_SIZE__STATUS_CONTROLS));

        // Status1
        fill(GLConfig.FG__STATUS_CONTROLS.getRGB());
        textAlign(LEFT, BOTTOM);
        text(status1, padx, statusBottomY);

        // Status 2
        textAlign(RIGHT, BOTTOM);
        text(status2, width - padx, statusBottomY);

        final float statusTextHeight = textAscent() + textDescent();
        float statusTopY = statusBottomY - statusTextHeight;        // Top Y to return

        if (showKeyBindings) {
//            textSize(getTextSize(GLConfig.TEXT_SIZE__STATUS_CONTROLS));         // redundant

            final float bottomY = statusTopY - vgap;

            final float delW = textWidth(delimiter);
            final float[] center1Pos = new float[values1.length];
            final float[] center2Pos = new float[values2.length];

            float xs1 = padx;
            for (int i=0; i < values1.length; i++) {
                if (i != 0)
                    xs1 += delW;

                float tw = textWidth(values1[i]);
                center1Pos[i] = xs1 + (tw / 2);

                xs1 += tw;
            }

            final float status2W = textWidth(status2);
            float xs2 = width - padx - status2W;
            for (int i=0; i < values2.length; i++) {
                if (i != 0)
                    xs2 += delW;

                float tw = textWidth(values2[i]);
                center2Pos[i] = xs2 + (tw / 2);

                xs2 += tw;
            }

            textSize(getTextSize(GLConfig.TEXT_SIZE_CONTROL_KEY_BINDING_LABEL));
            fill(GLConfig.FG_CONTROL_KEY_BINDING_LABEL.getRGB());
            textAlign(CENTER, BOTTOM);

            for (int i=0; i < controls1.length; i++) {
                text(controls1[i].keyBindingLabel, center1Pos[i], bottomY);
            }

            for (int i=0; i < controls2.length; i++) {
                text(controls2[i].keyBindingLabel, center2Pos[i], bottomY);
            }

            // final top y
            statusTopY = bottomY - (textAscent() + textDescent());
        }


        return statusTopY;
    }

    private float drawHUDAxes(@Nullable PeasyCam cam, float topX, float topY, float lenAxis) {
        pushMatrix();
        pushStyle();

        translate(topX + lenAxis, topY + lenAxis, 0);       // go to center
        if (cam != null) {
            float[] r = cam.getRotations();
            rotateX(r[0]);
            rotateY(r[1]);
            rotateZ(r[2]);
        }

        strokeWeight(2);

        // X-Axis
        stroke(GLConfig.COLOR_X_AXIS.getRGB());
        line(0, 0, 0, lenAxis, 0, 0);
//        text('X', lenAxis + 20, 0, 0);

        // Y-Axis
        stroke(GLConfig.COLOR_Y_AXIS.getRGB());
        line(0, 0, 0, 0, lenAxis, 0);
//        text('Y', 0, lenAxis + 20, 0);

        // Z-Axis
        stroke(GLConfig.COLOR_Z_AXIS.getRGB());
        line(0, 0, 0, 0, 0, lenAxis);
//        text('Z',0, 0, lenAxis + 20);

        popStyle();
        popMatrix();

        return topY + (lenAxis * 2);
    }

    protected void drawHUD() {
        /* ................................... HUD .............................. */

        pushStyle();

        final boolean hudEnabled = isHudEnabled();
        final boolean showKeyBindings = areKeyBindingsShown();
        final Predicate<Control> keyBindingsFilter = c -> showKeyBindings || c.alwaysShowKeyBinding;

        final float padx = 20;
        final float pady = 20;
        final float vgap_main = pady / 3;
        final float vgap_status = pady / 2;
        final float lenAxis = 50;

        /* TOP-LEFT SIDE: Axes and Camera ................................................................... */
        final PeasyCam cam = getCamera();
        final boolean drawAxes = shouldDrawAxesInHUD();
        float y_1 = pady;

        if (drawAxes) {
            y_1 = drawHUDAxes(cam, padx, y_1, lenAxis);
            y_1 += (pady * 2);      // vgap
        }

        if (hudEnabled) {
            if (cam != null) {
                y_1 = drawMainControlsAlignLeft(Control.CONTROLS_CAMERA, y_1, padx, 0, vgap_main, keyBindingsFilter);
                y_1 += (pady * 1.5f);   // vgap
            }

            if (isFullscreen()) {
                drawMainControlsAlignLeft(Control.CONTROLS_FULLSCREEN_WINDOW, y_1, padx, 0, vgap_main, keyBindingsFilter);
            }
        }

        /* TOP-RIGHT and BOTTOM: HUD and Status ................................................... */
        final float statusTopY;
        if (hudEnabled) {
            // TOP-RIGHT: Main Controls 1
            final float bottomYMain1 = drawMainControlsAlignRight(Control.CONTROLS_MAIN1, pady, padx, 0, vgap_main, keyBindingsFilter);

            // TOP-RIGHT: Main Controls 2
            final float bottomYMain2 = drawMainControlsAlignRight(Control.CONTROLS_MAIN2, bottomYMain1 + (pady * 1.5f), padx, 0, vgap_main, keyBindingsFilter);

            // TOP-RIGHT: Main Controls 3
            final float bottomYMain3 = drawMainControlsAlignRight(Control.CONTROLS_MAIN3, bottomYMain2 + (pady * 1.5f), padx, 0, vgap_main, keyBindingsFilter);

            // BOTTOM-LEFT and BOTTOM-RIGHT: status 1 and status 2
            final String delimiter = "   |   ";
            statusTopY = drawStatusControls(Control.CONTROLS_STATUS1, Control.CONTROLS_STATUS2, delimiter, padx, pady, vgap_status, showKeyBindings);
        } else {
            // TOP-RIGHT: Controls to show even when HUD is disabled
            drawMainControlsAlignRight(Control.CONTROLS_HUD_DISABLED, pady, padx, 0, vgap_main, keyBindingsFilter);

            statusTopY = height;
        }


        /* BOTTOM-LEFT SIDE: Pause State ......................................................*/
        final boolean paused = pendulumWave.isPaused();
        final int mills = millis();

        final long pauseDelta = mills - mPausedDrawTrigger;
        final boolean draw = !paused || (pauseDelta > 0 && pauseDelta < 500 /* ms to draw */);
        if (draw) {
            // Draw things which depends on Play/Pause state

            final String elapsedFormatted = String.valueOf((long) Math.floor(pendulumWave.getElapsedSeconds()));
            String text = "Time : " + elapsedFormatted + " s";
            if (paused) {
                text += "   |   PAUSED";
                fill(GLConfig.ACCENT_HIGHLIGHT.getRGB());
            } else {
                fill(GLConfig.ACCENT.getRGB());
            }

            textSize(getTextSize(GLConfig.TEXT_SIZE_NORMAL));
            textAlign(LEFT, BOTTOM);
            text(text, padx, statusTopY - pady);
        } else if (pauseDelta > 0) {
            mPausedDrawTrigger = mills + 400 /* ms to hide */;
        }

        popStyle();
    }

    public final void draw() {
        preDrawInternal();

        // Main
        drawMain();

        // GUD
        beginHUD();
        drawHUD();
        endHUD();

        postDrawInternal();
    }


    private void preDrawInternal() {
        if (_w != width || _h != height) {
            _w = width;
            _h = height;
            onResized(width, height);
        }

        /* Handle Keys [Continuous] */
        if (keyPressed && mKeyEvent != null) {
            onContinuousKeyPressed(mKeyEvent);
        }

        // Specific implementations
        preDraw();
    }

    private void postDrawInternal() {
        // Specific implementations
        postDraw();
    }

    protected void preDraw() {

    }

    protected void postDraw() {

    }

    protected void onResized(int width, int height) {

    }

    public final float getTextSize(float size) {
        return GLConfig.getTextSize(width, height, size);
    }



    /* Events ............................................ */

    /**
     * Enqueue a custom task to be executed on the UI thread
     * */
    public final void enqueueTask(@NotNull Runnable task) {
        postEvent(new KeyEvent(task, millis(), ACTION_EXECUTE_RUNNABLE, 0, (char) 0, 0, false));
    }


    public final void enqueueTasks(@Nullable Collection<? extends Runnable> tasks) {
        final Runnable chain = U.chainRunnables(tasks);     // Merge tasks to a single task

        if (chain != null) {
            enqueueTask(chain);
        }
    }

    @Override
    protected final void handleKeyEvent(KeyEvent event) {
        // If this is an ESC key down event, mask it to be able to handle it myself
        if (event.getAction() == KeyEvent.PRESS && (event.getKeyCode() == java.awt.event.KeyEvent.VK_ESCAPE)) {
            event = Control.changeKeyCode(event, Control.ESCAPE_KEY_CODE_SUBSTITUTE, Control.ESCAPE_KEY_SUBSTITUTE);
        }

        super.handleKeyEvent(event);

        // Handle Custom Events
        if (event.getAction() == ACTION_EXECUTE_RUNNABLE && (event.getNative() instanceof Runnable task)) {
            task.run();
        }
    }

    @Override
    public void keyPressed(KeyEvent event) {
        super.keyPressed(event);
        mKeyEvent = event;

        // Handle Discrete Controls
        for (Control control: Control.getValuesShared()) {
            if (control.continuousKeyEvent)
                continue;

            if (control.handleKeyEvent(this, event))
                break;
        }
    }

    @Override
    public void keyReleased(KeyEvent event) {
        super.keyReleased(event);

        if (mKeyEvent != null && mKeyEvent.getKeyCode() == event.getKeyCode()) {
            mKeyEvent = null;
        }
    }

    protected void onContinuousKeyPressed(@Nullable KeyEvent event) {
        if (event == null)
            return;

        // Handle Continuous Controls
        for (Control control: Control.getValuesShared()) {
            if (!control.continuousKeyEvent)
                continue;

            if (control.handleKeyEvent(this, event))
                break;
        }
    }


    /* Sounds  ..................................... */

    @NotNull
    private MidiNotePlayer createSoundPlayer() {
        return new MidiNotePlayer(this)
                .setPolyRhythmEnabled(mPolyRhythmEnabled)
                .setAmplitude(0.5f)
                .setAttackTime(0.01f)
                .setSustainTime(0.1f)
                .setSustainLevel(0.5f)
                .setReleaseTime(0.25f);
    }

    @NotNull
    private MidiNotePlayer getSoundPlayer() {
        MidiNotePlayer player = mSoundPlayer;
        if (player == null) {
            synchronized (this) {
                player = mSoundPlayer;
                if (player == null) {
                    player = createSoundPlayer();
                    mSoundPlayer = player;
                }
            }
        }

        // Fast Configurations
        player.setPolyRhythmEnabled(mPolyRhythmEnabled);
        return player;
    }

    private void playMidiNote(float midiNote) {
        if (!isSoundEnabled())
            return;

        getSoundPlayer().play(midiNote);
    }


    protected void onSoundEnabledChanged(boolean soundEnabled) {

    }

    public final boolean isSoundEnabled() {
        return mSoundEnabled;
    }

    public final void setSoundEnabled(boolean soundEnabled) {
        if (mSoundEnabled == soundEnabled)
            return;

        mSoundEnabled = soundEnabled;
        onSoundEnabledChanged(soundEnabled);
    }

    public final void toggleSoundEnabled() {
        setSoundEnabled(!isSoundEnabled());
    }


    protected void onPolyRhythmEnabledChanged(boolean polyRhythmEnabled) {

    }

    public final boolean isPolyRhythmEnabled() {
        return mPolyRhythmEnabled;
    }

    public final void setPolyRhythmEnabled(boolean polyRhythmEnabled) {
        if (mPolyRhythmEnabled == polyRhythmEnabled)
            return;

        mPolyRhythmEnabled = polyRhythmEnabled;
        onPolyRhythmEnabledChanged(polyRhythmEnabled);
    }

    public final void togglePolyRhythmEnabled() {
        setPolyRhythmEnabled(!isPolyRhythmEnabled());
    }



    /* Pendulum Wave Listener ............................................................. */

    @NotNull
    public PendulumWave getPendulumWave() {
        return pendulumWave;
    }

    @Override
    public void onPendulumCountChanged(@NotNull PendulumWave pendulumWave, int prevCount, int newCount) {
        updatePendulumDrawStyles();
    }

    @Override
    public void onSpeedChanged(@NotNull PendulumWave pendulumWave, float prevSpeed, float newSpeed) {

    }

    @Override
    public void onIsPausedChanged(@NotNull PendulumWave pendulumWave, boolean isPaused) {

    }

    /* Pendulum Listener ............................................................. */

    @Override
    public void onPendulumLengthChanged(@NotNull Pendulum p, float prevLength, float newLength) {

    }

    @Override
    public void onPendulumAngleChanged(@NotNull Pendulum p, float prevAngle, float newAngle) {

    }


    @Override
    public void onPendulumHighlightChanged(@NotNull Pendulum p, boolean highlight) {
        if (highlight && isSoundEnabled()) {
            final float midiNote = map(p.id, 0, pendulumWave.pendulumCount(), 85, 100);
            playMidiNote(midiNote);
        }
    }

    @Override
    public boolean is3D(@NotNull Pendulum p) {
        return isRendered3D();
    }

//    @Override
//    public @NotNull Point3DF chordDrawOrigin(@NotNull Pendulum p) {
//        return pendulumChordDrawOrigin(width, height, p.id);
//    }

    @Override
    public float lengthScale(@NotNull Pendulum p) {
        final Pendulum longest = pendulumWave.getLongestPendulum();
        final float lenMax;

        return longest != null && (lenMax = longest.getLength()) > 0? (min(width, height) * 0.8f) / lenMax: 0;
    }

    @Override
    public float bobRadius(@NotNull Pendulum p) {
        return p.getMass() * 400;
    }


    protected void onDrawOnlyBobChanged(boolean drawOnlyBob) {

    }

    public final boolean isDrawOnlyBobEnabled() {
        return mDrawOnlyBob;
    }

    public final BasePendulumWavePUi setDrawOnlyBob(boolean drawOnlyBob) {
        if (mDrawOnlyBob != drawOnlyBob) {
            mDrawOnlyBob = drawOnlyBob;
            onDrawOnlyBobChanged(drawOnlyBob);
        }

        return this;
    }

    public final BasePendulumWavePUi toggleDrawOnlyBob() {
        return setDrawOnlyBob(!isDrawOnlyBobEnabled());
    }

    @Override
    public final boolean drawLine() {
        return !mDrawOnlyBob;
    }

    @Override
    public final boolean drawBob() {
        return true;
    }


    /* Pendulum Draw Styles ........................................................... */

    @NotNull
    protected PendulumDrawStylesHolder createPendulumDrawStyle(int numPendulums, int index) {
        if (isRendered3D()) {
            return new PendulumDrawStylesHolder(
                    GLConfig.createHueCycleDrawStyle3D(numPendulums, index,false),   // Normal style
                    GLConfig.createHueCycleDrawStyle3D(numPendulums, index,true)     // Highlight style
            );
        }

        return new PendulumDrawStylesHolder(
                GLConfig.createHueCycleDrawStyle2D(numPendulums, index,false),   // Normal style
                GLConfig.createHueCycleDrawStyle2D(numPendulums, index,true)     // Highlight style
        );
    }

    protected final void updatePendulumDrawStyles() {
        drawStyles.clear();
        final int n = pendulumWave.pendulumCount();

        for (int i=0; i < n; i++) {
            drawStyles.add(createPendulumDrawStyle(n, i));
        }
    }

    @Override
    @NotNull
    public final PendulumDrawStyle drawStyle(@NotNull Pendulum p) {
        return drawStyles.get(p.id).normalDrawStyle();
    }

    @Override
    @Nullable
    public final PendulumDrawStyle drawStyleHighlight(@NotNull Pendulum p) {
        return drawStyles.get(p.id).highlightDrawStyle();
    }


    /* HUD and Controls  .......................................................... */

    protected void onHudEnabledChanged(boolean hudEnabled) {

    }

    public final boolean isHudEnabled() {
        return mHudEnabled;
    }

    public final void setHudEnabled(boolean hudEnabled) {
        if (mHudEnabled == hudEnabled)
            return;

        mHudEnabled = hudEnabled;
        onHudEnabledChanged(hudEnabled);
    }

    public final void toggleHudEnabled() {
        setHudEnabled(!isHudEnabled());
    }


    protected void onShowKeyBindingsChanged(boolean showKeyBindings) {

    }

    public final boolean areKeyBindingsShown() {
        return mShowKeyBindings;
    }

    public final void setShowKeyBindings(boolean showKeyBindings) {
        if (mShowKeyBindings == showKeyBindings)
            return;

        mShowKeyBindings = showKeyBindings;
        onShowKeyBindingsChanged(showKeyBindings);
    }

    public final void toggleShowKeyBindings() {
        setShowKeyBindings(!areKeyBindingsShown());
    }


    /* Camera Methods ........................................................ */

    public final boolean resetCamera(boolean animate) {
        final PeasyCam cam = getCamera();
        if (cam == null)
            return false;

        cam.reset(Math.max(animate? GLConfig.CAMERA_EXPLICIT_RESET_ANIMATION_MILLS: 0, 1));
        return true;
    }

    public final boolean rotateCameraXTo(float rotationX, boolean animate) {
        final PeasyCam cam = getCamera();
        if (cam == null)
            return false;

        U.rotateXTo(cam, rotationX, animate? GLConfig.CAMERA_ROTATIONS_ANIMATION_MILLS: 0);
        return true;
    }

    public final boolean rotateCameraYTo(float rotationY, boolean animate) {
        final PeasyCam cam = getCamera();
        if (cam == null)
            return false;

        U.rotateYTo(cam, rotationY, animate? GLConfig.CAMERA_ROTATIONS_ANIMATION_MILLS: 0);
        return true;
    }

    public final boolean rotateCameraZTo(float rotationZ, boolean animate) {
        final PeasyCam cam = getCamera();
        if (cam == null)
            return false;

        U.rotateZTo(cam, rotationZ, animate? GLConfig.CAMERA_ROTATIONS_ANIMATION_MILLS: 0);
        return true;
    }

    public final boolean rotateCameraXBy(float rotationXBy, boolean animate) {
        final PeasyCam cam = getCamera();
        if (cam == null)
            return false;

        U.rotateXBy(cam, rotationXBy, animate? GLConfig.CAMERA_ROTATIONS_ANIMATION_MILLS: 0);
        return true;
    }

    public final boolean rotateCameraXByUnit(boolean up, boolean animate) {
        return rotateCameraXBy((up? 1: -1) * HALF_PI, animate);
    }


    public final boolean rotateCameraYBy(float rotationYBy, boolean animate) {
        final PeasyCam cam = getCamera();
        if (cam == null)
            return false;

        U.rotateYBy(cam, rotationYBy, animate? GLConfig.CAMERA_ROTATIONS_ANIMATION_MILLS: 0);
        return true;
    }

    public final boolean rotateCameraYByUnit(boolean left, boolean animate) {
        return rotateCameraYBy((left? 1: -1) * HALF_PI, animate);
    }

    public final boolean rotateCameraZBy(float rotationZBy, boolean animate) {
        final PeasyCam cam = getCamera();
        if (cam == null)
            return false;

        U.rotateZBy(cam, rotationZBy, animate? GLConfig.CAMERA_ROTATIONS_ANIMATION_MILLS: 0);
        return true;
    }

    public final boolean rotateCameraZByUnit(boolean left, boolean animate) {
        return rotateCameraZBy((left? 1: -1) * HALF_PI, animate);
    }


    public final void snapshot() {
        String file_name = String.format("pendulum-wave-%dD_count-%d_time-%.2fs_mass-%.2fg_g-%.2f_drag-%.2f.png", isRendered3D()? 3: 2,
                pendulumWave.pendulumCount(),
                pendulumWave.getElapsedSeconds(),
                pendulumWave.getPendulumMass() * 1000,
                pendulumWave.gravity(),
                pendulumWave.drag() * 1000);

//        file_name = Format.replaceAllWhiteSpaces(file_name.toLowerCase(), "_");

        saveFrame(file_name);
        println(R.SHELL_ROOT + "Frame saved to file: " + file_name);
    }

    /* ...................................  MAIN CLI  ............................................ */

    public static void printErr(Object o) {
        System.err.print(o);
        System.err.flush();
    }

    public static void printErrln(Object o) {
        System.err.println(o);
        System.err.flush();
    }

    public void printErrCameraUnsupported() {
        printErrln(R.SHELL_CAMERA + "Camera is not supported by the current Renderer: " + sketchRenderer() + "\nThis may happen in a 2D renderer like JAVA2D or P2D. Launch the 3D version for camera support");
    }


    protected void main_init(String[] args) {
        if (CREATE_README) {
            R.createFullDescriptionReadme(true);
        }
    }

    public void main_cli(String[] args) {
        main_init(args);

        println(R.DESCRIPTION_GENERAL_WITH_HELP);
//        println("-> Command Line Thread: " + Thread.currentThread().getName() + "\n");
        boolean running = true;
        Scanner sc;

        final ArrayList<String> main_cmds = new ArrayList<>();
        final Set<String> ops = new ArraySet<>();
        final ArrayList<Runnable> tasks = new ArrayList<>();

        while (running) {
            sc = new Scanner(System.in);
            print(R.SHELL_ROOT);

            main_cmds.clear();
            ops.clear();
            tasks.clear();

            final String cmd = sc.nextLine().trim().toLowerCase();
            if (cmd.isEmpty())
                continue;

            switch (cmd) {
                case "exit", "quit" -> running = false;
                case "play", "start" -> tasks.add(() -> pendulumWave.setPause(false));
                case "pause", "stop" -> tasks.add(() -> pendulumWave.setPause(true));
                case "toggle play", "toggle pause" -> tasks.add(pendulumWave::togglePlayPause);
                case "sound", "toggle sound" -> tasks.add(this::toggleSoundEnabled);
                case "poly-rhythm", "toggle poly-rhythm" -> tasks.add(this::togglePolyRhythmEnabled);
                case "hud", "toggle hud" -> tasks.add(this::toggleHudEnabled);
                case "controls", "toggle controls", "keys", "toggle keys" -> tasks.add(this::toggleShowKeyBindings);
                case "bobs", "toggle bobs", "bobs-only", "toggle bobs-only", "only-bobs", "toggle only-bobs" -> tasks.add(this::toggleDrawOnlyBob);
                case "save", "saveframe", "snap", "snapshot" -> tasks.add(this::snapshot);
                default -> {
                    final String[] tokens = splitTokens(cmd);
                    for (String s : tokens) {
                        if (s.isEmpty())
                            continue;

                        if (s.length() > 1 && s.charAt(0) == '-' && !Character.isDigit(s.charAt(1))) {
                            ops.add(s);
                        } else {
                            main_cmds.add(s);
                        }
                    }
                    if (main_cmds.isEmpty()) {
                        continue;
                    }

                    final String main_cmd = main_cmds.get(0);       // main command
                    final boolean forceFlag = ops.contains("-f");
                    final boolean resetFlag = ops.contains("-reset");

                    switch (main_cmd) {
                        case "help", "usage" -> {
                            final Runnable usage_pr = () -> println(R.SHELL_ROOT + "Usage: help [-controls | -commands | -all]");

                            if (ops.contains("-all")) {
                                println("\n" + R.getFullDescription(true, true));
                                continue;
                            }

                            boolean done = false;

                            if (ops.contains("-control") || ops.contains("-controls") || ops.contains("-key") || ops.contains("-keys") || ops.contains("-keybindings") || ops.contains("-key-bindings")) {
                                println("\n" + R.getControlsDescription());
                                done = true;
                            }

                            if (ops.contains("-cmd") || ops.contains("-command") || ops.contains("-commands")) {
                                println("\n" + R.DESCRIPTION_COMMANDS);
                                done = true;
                            }

                            if (!done) {
                                usage_pr.run();
                            }
                        }

                        case "win", "window" -> {
                            final Runnable usage_pr = () -> println(R.SHELL_WINDOW + "Sets the window size or screen location.\nUsage: win [-size | -pos] <x> <y>\nWildcards: w -> initial windowed size (to be used with -size)  |  c -> center window on screen (to be used with -pos)\nExample: win -size 200 400  |  win -pos 10 20  |  win -pos center  |  win -size w\n");
                            final int mode;

                            // Mode: 0 -> Size, 1 -> Position
                            if (ops.contains("-size")) {
                                mode = 0;
                            } else if (ops.contains("-pos") || ops.contains("-position") || ops.contains("-loc") || ops.contains("-location")) {
                                mode = 1;
                            } else {
                                usage_pr.run();
                                continue;
                            }

                            // Wildcards
                            final String wildcard = main_cmds.size() == 2? main_cmds.get(1): null;
                            boolean wildcardDone = false;

                            if (Format.notEmpty(wildcard)) {
                                if (mode == 0 && (wildcard.equals("w") || wildcard.equals("win") || wildcard.equals("window") || wildcard.equals("windowed"))) {
                                    tasks.add(() -> setSurfaceToInitialWindowedSize(false, true));
                                    wildcardDone = true;
                                } else if (mode == 1 && (wildcard.equals("c") || wildcard.equals("center"))) {
                                    tasks.add(() -> setSurfaceLocationCenter(true));
                                    wildcardDone = true;
                                }
                            }

                            // Main commands
                            if (!wildcardDone) {
                                if (main_cmds.size() < 3) {
                                    usage_pr.run();
                                    continue;
                                }

                                final String v1_str = main_cmds.get(1);
                                final String v2_str = main_cmds.get(2);

                                try {
                                    final int v1 = Integer.parseInt(v1_str);
                                    final int v2 = Integer.parseInt(v2_str);

                                    if (mode == 0) {
                                        tasks.add(() -> setSurfaceSize(v1, v2, true));
                                    } else {
                                        tasks.add(() -> setSurfaceLocation(v1, v2, true));
                                    }
                                } catch (NumberFormatException n_exc) {
                                    printErrln(R.SHELL_WINDOW + String.format("Invalid arguments supplied to window %s. %s must only be integers. GIven: %s, %s", mode == 0? "size": "position", mode == 0? "Width and Height": "Screen X and Y coordinates", v1_str, v2_str));
                                    usage_pr.run();
                                } catch (Exception exc) {
                                    printErrln(R.SHELL_WINDOW + "Failed to set window " + (mode == 0? "size": "position") + ".\nException: " + exc);
                                    usage_pr.run();
                                }
                            }
                        }

                        case "n", "num", "count" -> {
                            final Runnable usage_pr = () -> println(R.SHELL_PENDULUM_COUNT + "Usage: count [-soft] <pendulum count>\nExample: count 12\n");

                            final String count_str = main_cmds.size() > 1 ? main_cmds.get(1) : "";
                            if (count_str.isEmpty()) {
                                println(R.SHELL_PENDULUM_COUNT + "Current: " + pendulumWave.pendulumCount() + " | Default: " + pendulumWave.getInitialPendulumCount());
                                usage_pr.run();
                                continue;
                            }

                            try {
                                final int count = Integer.parseInt(count_str);
                                final boolean resetState = !ops.contains("-soft");
                                tasks.add(() -> {
                                    pendulumWave.setPendulumCount(count, resetState);
                                    println("\n" + R.SHELL_PENDULUM_COUNT + " Pendulum count set to " + pendulumWave.pendulumCount());
                                });
                            } catch (IllegalArgumentException arg_exc) {
                                printErrln(R.SHELL_PENDULUM_COUNT + "Invalid count: " + arg_exc.getMessage());
                            } catch (Exception exc) {
                                printErrln(R.SHELL_PENDULUM_COUNT + "Failed to parse pendulum count\n" + exc);
                                usage_pr.run();
                            }
                        }

                        case "reset" -> {
                            final Runnable usage_pr = () -> println(R.SHELL_RESET + "Usage: reset [-state | -env | -count | -cam | -win | -all]\nExample: reset -env -state  |  Default: reset -cam -count\n");

                            boolean done = false;

                            if (ops.contains("-all")) {
                                tasks.add(() -> {
                                    pendulumWave.resetSimulation(true, true);
//                            resetCamera(!force);
                                    resetSurfaceSizeAndPos(false);
                                });

                                done = true;
                            } else {
                                if (ops.contains("-env")) {
                                    tasks.add(() -> pendulumWave.resetSimulation(false, false));
                                    done = true;
                                }

                                if (ops.contains("-count")) {
                                    tasks.add(() -> pendulumWave.resetPendulumCount(false));
                                    done = true;
                                }

                                if (ops.contains("-cam") || ops.contains("-camera")) {
                                    tasks.add(() -> resetCamera(!forceFlag));
                                    done = true;
                                }

                                if (ops.contains("-win") || ops.contains("-window")) {
                                    tasks.add(() -> resetSurfaceSizeAndPos(true));
                                    done = true;
                                }

                                if (ops.contains("-state")) {
                                    tasks.add(pendulumWave::resetPendulumsState);
                                    done = true;
                                }
                            }

                            if (!done) {        // Default
                                tasks.add(pendulumWave::resetPendulumsState);
                                usage_pr.run();
                            }
                        }

                        case "speed" -> {
                            int mode = 0;       // 0: -x, 1: -percent

                            final Runnable cur_val_pr = () -> println(R.SHELL_SPEED + String.format("Current: %sx (%s%%)  |  Default: %sx (%s%%)", Format.nf001(pendulumWave.getSpeed()), Format.nf001(pendulumWave.getSpeedPercent()), Format.nf001(PendulumWave.DEFAULT_SPEED), Format.nf001(PendulumWave.speedToPercent(PendulumWave.DEFAULT_SPEED))));
                            final Runnable usage_pr = () -> println(R.SHELL_SPEED + "Usage: speed [-x | -p] <value>. Modes: -x -> Multiple | -p -> Percentage. Defaults to multiple (-x) values\nExample: speed -x 2.5 | speed -p 50\n");

                            final String val_str = main_cmds.size() > 1 ? main_cmds.get(1) : "";
                            if (val_str.isEmpty()) {
                                cur_val_pr.run();
                                usage_pr.run();
                                continue;
                            }

                            boolean done = false;
                            try {
                                final float val = Float.parseFloat(val_str);

                                if (ops.contains("-p") || ops.contains("-percent")) {
                                    mode = 1;

                                    if (val < 0 || val > 100) {
                                        throw new OutOfRangeException(val, 0, 100);
                                    }

                                    tasks.add(() -> pendulumWave.setSpeedPercent(val));
                                    done = true;
                                } else {
                                    mode = 0;

                                    if (val < PendulumWave.SPEED_MIN || val > PendulumWave.SPEED_MAX) {
                                        throw new OutOfRangeException(val, PendulumWave.SPEED_MIN, PendulumWave.SPEED_MAX);
                                    }

                                    tasks.add(() -> pendulumWave.setSpeed(val));
                                    done = true;
                                }
                            } catch (OutOfRangeException oor) {
                                printErrln(R.SHELL_SPEED + String.format("Speed %s must be within range [%s, %s], given: %s%s", mode == 0 ? "multiple" : "percent", Format.nf001(oor.getLo().floatValue()), Format.nf001(oor.getHi().floatValue()), Format.nf000(oor.getArgument().floatValue()), mode == 0 ? "x" : "%"));
                                usage_pr.run();
                            } catch (NumberFormatException ignored) {
                                printErrln(R.SHELL_SPEED + "Speed must be an integer or a floating point number, given: " + val_str);
                                usage_pr.run();
                            }

                            if (done) {
                                tasks.add(() -> println("\n" + R.SHELL_SPEED + "Speed set to " + Control.SIMULATION_SPEED.getFormattedValue(this)));
                            }
                        }

                        case "gravity", "g" -> {
                            final Runnable cur_val_pr = () -> println(R.SHELL_GRAVITY + String.format("Acceleration due to gravity. Current: %s ms-2  |  Default: %s ms-2", Format.nf002(pendulumWave.gravity()), Format.nf002(PendulumWave.DEFAULT_GRAVITY)));
                            final Runnable usage_pr = () -> println(R.SHELL_GRAVITY + "Usage: g [-reset] <value in ms-2>. \nExample: g 9.8  |  g -reset 12.4\n");

                            final String val_str = main_cmds.size() > 1 ? main_cmds.get(1) : "";
                            if (val_str.isEmpty()) {
                                cur_val_pr.run();
                                usage_pr.run();
                                continue;
                            }

                            try {
                                final float val = Float.parseFloat(val_str);
                                tasks.add(() -> {
                                    pendulumWave.setGravity(val, resetFlag);
                                    println("\n" + R.SHELL_GRAVITY + "Gravity set to " + Format.nf002(pendulumWave.gravity()) + " ms-2");
                                });
                            } catch (NumberFormatException exc) {
                                printErrln(R.SHELL_GRAVITY + "Gravity must be an integer or a floating point number, given: " + val_str);
                                usage_pr.run();
                            }
                        }

                        case "drag" -> {
                            final Runnable cur_val_pr = () -> println(R.SHELL_DRAG + String.format("Drag coefficient (in gram/s). Current: %s g/s  |  Default: %s g/s", Format.nf002(pendulumWave.drag() * 1000), Format.nf002(PendulumWave.DEFAULT_DRAG * 1000)));
                            final Runnable usage_pr = () -> println(R.SHELL_DRAG + "Usage: drag [-reset] <value in g/s>. \nExample: drag 1.2  |  drag -reset 2.1\n");

                            final String val_str = main_cmds.size() > 1 ? main_cmds.get(1) : "";
                            if (val_str.isEmpty()) {
                                cur_val_pr.run();
                                usage_pr.run();
                                continue;
                            }

                            try {
                                final float val = Float.parseFloat(val_str);
                                tasks.add(() -> {
                                    pendulumWave.setDrag(val / 1000, resetFlag);
                                    println("\n" + R.SHELL_DRAG + "Drag set to " + Format.nf002(pendulumWave.drag() * 1000) + " g/s");
                                });
                            } catch (NumberFormatException exc) {
                                printErrln(R.SHELL_DRAG + "Drag must be an integer or a floating point number, given: " + val_str);
                                usage_pr.run();
                            }
                        }

                        case "mass" -> {
                            final Runnable cur_val_pr = () -> println(R.SHELL_MASS + String.format("Pendulum Mass (in grams). Current: %s g  |  Default: %s g", Format.nf002(pendulumWave.getPendulumMass() * 1000), Format.nf002(PendulumWave.DEFAULT_PENDULUM_MASS * 1000)));
                            final Runnable usage_pr = () -> println(R.SHELL_MASS + "Usage: mass [-reset] <value in g>. \nExample: mass 50  |  mass -reset 24.6\n");

                            final String val_str = main_cmds.size() > 1 ? main_cmds.get(1) : "";
                            if (val_str.isEmpty()) {
                                cur_val_pr.run();
                                usage_pr.run();
                                continue;
                            }

                            try {
                                final float val = Float.parseFloat(val_str);
                                tasks.add(() -> {
                                    pendulumWave.setPendulumMass(val / 1000, resetFlag);
                                    println("\n" + R.SHELL_MASS + "Pendulum Mass set to " + Format.nf002(pendulumWave.getPendulumMass() * 1000) + " g");
                                });
                            } catch (NumberFormatException exc) {
                                printErrln(R.SHELL_MASS + "Pendulum Mass must be an integer or a floating point number, given: " + val_str);
                                usage_pr.run();
                            } catch (IllegalArgumentException arg_exc) {
                                printErrln(R.SHELL_MASS + arg_exc.getMessage());
                                usage_pr.run();
                            }
                        }

                        case "angle" -> {
                            final Runnable cur_val_pr = () -> println(R.SHELL_ANGLE + String.format("Pendulum Start Angle (in deg). Current: %s°  |  Default: %s°", Format.nf002(U.normalizeDegrees(degrees(pendulumWave.getPendulumStartAngle()))), Format.nf002(U.normalizeDegrees(degrees(PendulumWave.DEFAULT_START_ANGLE)))));
                            final Runnable usage_pr = () -> println(R.SHELL_ANGLE + "Usage: angle [-reset] <value in deg>. \nExample: angle 30  |  angle -reset 45.7\n");

                            final String val_str = main_cmds.size() > 1 ? main_cmds.get(1) : "";
                            if (val_str.isEmpty()) {
                                cur_val_pr.run();
                                usage_pr.run();
                                continue;
                            }

                            try {
                                final float val = Float.parseFloat(val_str);
                                tasks.add(() -> {
                                    pendulumWave.setPendulumStartAngle(radians(val), resetFlag);
                                    println("\n" + R.SHELL_ANGLE + "Pendulum Start Angle set to " + Format.nf002(U.normalizeDegrees(degrees(pendulumWave.getPendulumStartAngle()))) + "°");
                                });
                            } catch (NumberFormatException exc) {
                                printErrln(R.SHELL_ANGLE + "Pendulum Start Angle must be an integer or a floating point number, given: " + val_str);
                                usage_pr.run();
                            }
                        }

                        case "period", "waveperiod", "wp" -> {
                            final Runnable cur_val_pr = () -> println(R.SHELL_WAVE_PERIOD + String.format("Wave Period (in secs). Current: %s s  |  Default: %s s", Format.nf001(pendulumWave.getEffectiveWavePeriod()), Format.nf001(PendulumWave.DEFAULT_EFFECTIVE_WAVE_PERIOD_SECS)));
                            final Runnable usage_pr = () -> println(R.SHELL_WAVE_PERIOD + "Usage: wp [-reset] <value in secs>. \nExample: wp 150  |  wp -reset 78\n");

                            final String val_str = main_cmds.size() > 1 ? main_cmds.get(1) : "";
                            if (val_str.isEmpty()) {
                                cur_val_pr.run();
                                usage_pr.run();
                                continue;
                            }

                            try {
                                final float val = Float.parseFloat(val_str);
                                tasks.add(() -> {
                                    pendulumWave.setEffectiveWavePeriod(val, resetFlag);
                                    println("\n" + R.SHELL_WAVE_PERIOD + "Pendulum Wave Period set to " + Format.nf001(pendulumWave.getEffectiveWavePeriod()) + " s");
                                });
                            } catch (NumberFormatException exc) {
                                printErrln(R.SHELL_WAVE_PERIOD + "Pendulum Wave Period must be an integer or a floating point number, given: " + val_str);
                                usage_pr.run();
                            } catch (IllegalArgumentException arg_exc) {
                                printErrln(R.SHELL_WAVE_PERIOD + arg_exc.getMessage());
                                usage_pr.run();
                            }
                        }

                        case "minosc", "min-osc", "osc", "mosc" -> {
                            final Runnable cur_val_pr = () -> println(R.SHELL_MIN_OSC + String.format("Minimum Oscillations in Wave Period. Current: %s  |  Default: %s", Format.nf001(pendulumWave.getMinOscillationsInWavePeriod()), Format.nf001(PendulumWave.DEFAULT_OSCILLATIONS_MIN)));
                            final Runnable usage_pr = () -> println(R.SHELL_MIN_OSC + "Usage: mosc [-reset] <value>. \nExample: mosc 4.67  |  minosc -reset 8.57\n");

                            final String val_str = main_cmds.size() > 1 ? main_cmds.get(1) : "";
                            if (val_str.isEmpty()) {
                                cur_val_pr.run();
                                usage_pr.run();
                                continue;
                            }

                            try {
                                final float val = Float.parseFloat(val_str);
                                tasks.add(() -> {
                                    pendulumWave.setMinOscillationsInWavePeriod(val, resetFlag);
                                    println("\n" + R.SHELL_MIN_OSC + "Min Oscillations in wave period set to " + Format.nf001(pendulumWave.getMinOscillationsInWavePeriod()));
                                });
                            } catch (NumberFormatException exc) {
                                printErrln(R.SHELL_MIN_OSC + "Minimum Oscillations must be an integer or a floating point number, given: " + val_str);
                                usage_pr.run();
                            } catch (IllegalArgumentException arg_exc) {
                                printErrln(R.SHELL_MIN_OSC + arg_exc.getMessage());
                                usage_pr.run();
                            }
                        }

                        case "oscstep", "osc-step", "ostep", "step" -> {
                            final Runnable cur_val_pr = () -> println(R.SHELL_OSC_STEP + String.format("Oscillation Step per pendulum. Current: %s  |  Default: %s", Format.nf001(pendulumWave.getOscillationsStepPerPendulum()), Format.nf001(PendulumWave.DEFAULT_OSCILLATIONS_STEP_PER_PENDULUM)));
                            final Runnable usage_pr = () -> println(R.SHELL_OSC_STEP + "Usage: ostep [-reset] <value>. \nExample: ostep 0.2  |  step -reset 0.42\n");

                            final String val_str = main_cmds.size() > 1 ? main_cmds.get(1) : "";
                            if (val_str.isEmpty()) {
                                cur_val_pr.run();
                                usage_pr.run();
                                continue;
                            }

                            try {
                                final float val = Float.parseFloat(val_str);
                                tasks.add(() -> {
                                    pendulumWave.setOscillationsStepPerPendulum(val, resetFlag);
                                    println("\n" + R.SHELL_OSC_STEP + "Oscillation Step per pendulum set to " + Format.nf001(pendulumWave.getOscillationsStepPerPendulum()));
                                });
                            } catch (NumberFormatException exc) {
                                printErrln(R.SHELL_OSC_STEP + "Oscillation Step must be an integer or a floating point number, given: " + val_str);
                                usage_pr.run();
                            } catch (IllegalArgumentException arg_exc) {
                                printErrln(R.SHELL_OSC_STEP + arg_exc.getMessage());
                                usage_pr.run();
                            }
                        }

                        case "rotationx", "rotx", "rx", "pitch" -> {
                            if (!cameraSupported()) {
                                printErrCameraUnsupported();
                                continue;
                            }

                            final Runnable cur_val_pr = () -> println(R.SHELL_ROTATION_X + String.format("Pitch (rotation about X-axis). Current: %s", Control.CAMERA_ROTATE_X.getFormattedValue(this)));
                            final Runnable usage_pr = () -> println(R.SHELL_ROTATION_X + "Usage: pitch [-by | -f] <+ | - | value_in_deg>. Options: -by -> change rotation by  |  -f -> Force without animations\nExample: pitch 90  |  rx -by 10.5  |  pitch +\n");

                            final String val_str = main_cmds.size() > 1 ? main_cmds.get(1) : "";
                            if (val_str.isEmpty()) {
                                cur_val_pr.run();
                                usage_pr.run();
                                continue;
                            }

                            if (val_str.equals("+") || val_str.equals("up")) {
                                tasks.add(() -> rotateCameraXByUnit(true, !forceFlag));
                            } else if (val_str.equals("-") || val_str.equals("down")) {
                                tasks.add(() -> rotateCameraXByUnit(false, !forceFlag));
                            } else {
                                try {
                                    final float val = radians(Float.parseFloat(val_str));
                                    if (ops.contains("-by")) {
                                        tasks.add(() -> rotateCameraXBy(val, !forceFlag));
                                    } else {
                                        tasks.add(() -> rotateCameraXTo(val, !forceFlag));
                                    }
                                } catch (NumberFormatException exc) {
                                    printErrln(R.SHELL_ROTATION_X + "Pitch (X-Rotation) must be an integer or a floating point number, given: " + val_str);
                                    usage_pr.run();
                                }
                            }
                        }

                        case "rotationy", "roty", "ry", "yaw" -> {
                            if (!cameraSupported()) {
                                printErrCameraUnsupported();
                                continue;
                            }

                            final Runnable cur_val_pr = () -> println(R.SHELL_ROTATION_Y + String.format("Yaw (rotation about Y-axis). Current: %s", Control.CAMERA_ROTATE_Y.getFormattedValue(this)));
                            final Runnable usage_pr = () -> println(R.SHELL_ROTATION_Y + "Usage: yaw [-by | -f] <+ | - | value_in_deg>. Options: -by -> change rotation by  |  -f -> Force without animations\nExample: yaw 90  |  ry -by 10.5  |  yaw -\n");

                            final String val_str = main_cmds.size() > 1 ? main_cmds.get(1) : "";
                            if (val_str.isEmpty()) {
                                cur_val_pr.run();
                                usage_pr.run();
                                continue;
                            }

                            if (val_str.equals("+") || val_str.equals("left")) {
                                tasks.add(() -> rotateCameraYByUnit(true, !forceFlag));
                            } else if (val_str.equals("-") || val_str.equals("right")) {
                                tasks.add(() -> rotateCameraYByUnit(false, !forceFlag));
                            } else {
                                try {
                                    final float val = radians(Float.parseFloat(val_str));
                                    if (ops.contains("-by")) {
                                        tasks.add(() -> rotateCameraYBy(val, !forceFlag));
                                    } else {
                                        tasks.add(() -> rotateCameraYTo(val, !forceFlag));
                                    }
                                } catch (NumberFormatException exc) {
                                    printErrln(R.SHELL_ROTATION_Y + "Yaw (Y-Rotation) must be an integer or a floating point number, given: " + val_str);
                                    usage_pr.run();
                                }
                            }
                        }

                        case "rotationz", "rotz", "rz", "roll" -> {
                            if (!cameraSupported()) {
                                printErrCameraUnsupported();
                                continue;
                            }

                            final Runnable cur_val_pr = () -> println(R.SHELL_ROTATION_Z + String.format("Roll (rotation about Z-axis). Current: %s", Control.CAMERA_ROTATE_Z.getFormattedValue(this)));
                            final Runnable usage_pr = () -> println(R.SHELL_ROTATION_Z + "Usage: roll [-by | -f] <+ | - | value_in_deg>. Options: -by -> change rotation by  |  -f -> Force without animations\nExample: roll 90  |  rz -by 10.5  |  roll right\n");

                            final String val_str = main_cmds.size() > 1 ? main_cmds.get(1) : "";
                            if (val_str.isEmpty()) {
                                cur_val_pr.run();
                                usage_pr.run();
                                continue;
                            }

                            if (val_str.equals("+") || val_str.equals("left")) {
                                tasks.add(() -> rotateCameraZByUnit(true, !forceFlag));
                            } else if (val_str.equals("-") || val_str.equals("right")) {
                                tasks.add(() -> rotateCameraZByUnit(false, !forceFlag));
                            } else {
                                try {
                                    final float val = radians(Float.parseFloat(val_str));
                                    if (ops.contains("-by")) {
                                        tasks.add(() -> rotateCameraZBy(val, !forceFlag));
                                    } else {
                                        tasks.add(() -> rotateCameraZTo(val, !forceFlag));
                                    }
                                } catch (NumberFormatException exc) {
                                    printErrln(R.SHELL_ROTATION_Z + "Roll (Z-Rotation) must be an integer or a floating point number, given: " + val_str);
                                    usage_pr.run();
                                }
                            }
                        }

                        default -> printErrln(R.SHELL_ROOT + "Unknown Command: " + main_cmd);      // Unknown command
                    }
                }
            }

            // Enqueue UI tasks of this command
            enqueueTasks(tasks);
        }

        exit();
    }





//    public static void main(String[] args) {
//
//    }
}


