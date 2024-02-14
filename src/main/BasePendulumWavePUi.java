package main;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import peasy.PeasyCam;
import pendulum.*;
import processing.core.PApplet;
import processing.core.PFont;
import processing.event.KeyEvent;
import processing.opengl.PJOGL;
import sound.MidiNotePlayer;
import util.U;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;


/**
 * A base graphical UI for {@link PendulumWave Pendulum Wave} in Processing 3
 * */
public abstract class BasePendulumWavePUi extends PApplet implements PendulumStyleProvider, PendulumWave.Listener {

    private int _w, _h;
    @Nullable
    private KeyEvent mKeyEvent;
    protected PFont pdSans, pdSansMedium;

    @NotNull
    protected final PendulumWave pendulumWave;

    /* Draw Styles */
    private boolean mDrawOnlyBob = GLConfig.DEFAULT_DRAW_ONLY_BOB;
    private final List<PendulumDrawStylesHolder> drawStyles = new ArrayList<>();

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
        this.pendulumWave.setListener(this);

        updatePendulumDrawStyles();
    }

    public BasePendulumWavePUi(int initialPendulumCount) {
        this(new PendulumWave(initialPendulumCount));
    }

    public BasePendulumWavePUi() {
        this(new PendulumWave());
    }

    /* Camera */

    @Nullable
    public PeasyCam getCamera() {
        return null;
    }

    public boolean shouldDrawAxesInHUD() {
        return false;
    }

    @Override
    public void settings() {
        if (R.IMAGE_PENDULUM_WAVE_ICON != null) {
            PJOGL.setIcon(R.IMAGE_PENDULUM_WAVE_ICON.toString());       // icon
        }
    }


    public void setup() {
        _w = width;
        _h = height;

        surface.setTitle(GLConfig.TITLE);

//        if (sketchFullScreen()) {
//            surface.setAlwaysOnTop(true);       // Keyboard focus, since already fullscreen so no problem in this
//        } else {
//            surface.setResizable(true);
//        }

        frameRate(GLConfig.FRAME_RATE);

        pdSans = createFont(R.FONT_PD_SANS_REGULAR.toString(), 20);
        pdSansMedium = createFont(R.FONT_PD_SANS_MEDIUM.toString(), 20);
        textFont(pdSans);       // Default
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
            final String val = controls[i].getValue(this);
            values[i] = val;

            max_val_width = max(max_val_width, textWidth(val));
        }

        max_val_width += textWidth(label_value_delimiter);

        final float x1 = width - padx;
        final float x2 = x1 - max_val_width - hgap;

        for (int i=0; i < controls.length; i++) {
            final Control control = controls[i];
            final String label = control.label;
            final String keyLabel = label_key_label_delimiter + control.keyEventLabel;
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
            final String label = c.label + (showKeyBindingsFilter.test(c)? label_key_label_delimiter + c.keyEventLabel: "");

            max_label_width = max(max_label_width, textWidth(label));
        }

        final float x2 = padx + max_label_width + hgap;

        for (int i=0; i < controls.length; i++) {
            final Control control = controls[i];
            final String label = control.label;
            final String keyLabel = label_key_label_delimiter + control.keyEventLabel;
            final String val = control.getValue(this);

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
            values1[i] = c.label + " : " + c.getValue(this);
        }

        for (int i=0; i < controls2.length; i++) {
            Control c = controls2[i];
            values2[i] = c.label + " : " + c.getValue(this);
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
                text(controls1[i].keyEventLabel, center1Pos[i], bottomY);
            }

            for (int i=0; i < controls2.length; i++) {
                text(controls2[i].keyEventLabel, center2Pos[i], bottomY);
            }

            // final top y
            statusTopY = bottomY - (textAscent() + textDescent());
        }


        return statusTopY;
    }

    private void drawHUDAxes(@Nullable PeasyCam cam, float topX, float topY, float lenAxis) {
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
            drawHUDAxes(cam, padx, pady, lenAxis);
            y_1 = (lenAxis * 2) + (pady * 2);
        }

        if (cam != null && hudEnabled) {
            drawMainControlsAlignLeft(Control.CONTROLS_CAMERA, y_1 + pady, padx, 0, vgap_main, keyBindingsFilter);
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

//    @Override
//    public boolean is3D(@NotNull Pendulum p);

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




    /* ...................................  MAIN CLI  ............................................ */

    public static final String DESCRIPTION_CONTROLS =
            "\n.......................  CONTROLS  ........................\n" +
            Control.createDescription() +
            "\n.............................................................\n";


    public void main_cli(String[] args) {
        println(DESCRIPTION_CONTROLS);

        // TODO: Command Line Interface
    }
}


