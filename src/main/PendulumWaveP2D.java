package main;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import peasy.PeasyCam;
import pendulum.Pendulum;
import pendulum.PendulumDrawStylesHolder;
import pendulum.PendulumWave;
import processing.core.PApplet;
import processing.event.KeyEvent;
import util.Config;
import util.Point3DF;
import util.U;

import java.awt.*;

/**
 * A 2D graphical engine for {@link PendulumWave Pendulum Wave} in Processing 3
 * */
public class PendulumWaveP2D extends BasePendulumWavePUi {

    // Fullscreen does not expand to entire screen with P2D and P3D renderers. See hack in setup() which solves this problem
    private static final boolean DEFAULT_FULLSCREEN = true;

    // Whether the fullscreen should be fully expanded or same as initial window size
    private static final boolean DEFAULT_INITIAL_FULLSCREEN_EXPANDED = false;

    private static final Dimension DEFAULT_INITIAL_WINDOW_SIZE = U.scaleDimension(getNativeScreenResolution(), 0.8f);


    @NotNull
    private static Point3DF pendulumChordDrawOrigin(int width, int height, int index) {
        return new Point3DF(width / 2f, min(height / 10f, 50), 0 /* 2D */);
    }



    private boolean mFullscreen = DEFAULT_FULLSCREEN;
    private boolean mInitialFullscreenExpanded = DEFAULT_INITIAL_FULLSCREEN_EXPANDED;
    @NotNull
    private Dimension mInitialWindowedSize = DEFAULT_INITIAL_WINDOW_SIZE;

    public PendulumWaveP2D(@NotNull PendulumWave pendulumWave) {
        super(pendulumWave);
        init();
    }

    public PendulumWaveP2D(int initialPendulumCount) {
        super(initialPendulumCount);
        init();
    }

    public PendulumWaveP2D() {
        super();
        init();
    }

    private void init() {
        // Handle configurations
        final Config config = R.CONFIG_2D;
        mFullscreen = config.getValueBool(R.CONFIG_KEY_FULLSCREEN, DEFAULT_FULLSCREEN);
        mInitialFullscreenExpanded = config.getValueBool(R.CONFIG_KEY_INITIAL_FULLSCREEN_EXPANDED, DEFAULT_INITIAL_FULLSCREEN_EXPANDED);
        mInitialWindowedSize = R.getConfigWindowSize(config, getNativeScreenResolution(), DEFAULT_INITIAL_WINDOW_SIZE);

        applyConfig(config);

        // AT Last
        attachPendulumWaveListener();       // very important
    }

    @Override
    public boolean isRendered3D() {
        return false;
    }

    @Override
    public boolean isFullscreen() {
        return mFullscreen;
    }

    @Override
    public boolean isInitialFullscreenExpanded() {
        return mInitialFullscreenExpanded;
    }

    @Override
    public boolean supportsSurfaceLocationSetter() {
        return super.supportsSurfaceLocationSetter();
    }

    @Override
    public boolean supportsSurfaceSizeSetter() {
        return super.supportsSurfaceSizeSetter();
    }

    @Override
    @Nullable
    public PeasyCam getCamera() {
        return null;
    }

    @Override
    public boolean shouldDrawAxesInHUD() {
        return false;
    }


    @Override
    public @NotNull Dimension getInitialWindowedSize() {
        return mInitialWindowedSize;
    }

    @Override
    public void settings() {
        super.settings();

        if (mFullscreen) {
            fullScreen(P2D);
        } else {
            size(mInitialWindowedSize.width, mInitialWindowedSize.height, P2D);
        }

//        smooth(4);
    }

    public void setup() {
        super.setup();

//        if (isFullscreen()) {
//            surface.hideCursor();
//        }

//        surface.setAlwaysOnTop(true);
    }

    @Override
    protected void drawMain() {
        super.drawMain();
    }

    @Override
    protected void beginHUD() {
        super.beginHUD();
    }

    @Override
    protected void drawHUD() {
        super.drawHUD();
    }

    @Override
    protected void endHUD() {
        super.endHUD();
    }

    @Override
    protected void preDraw() {
        super.preDraw();
    }

    @Override
    protected void postDraw() {
        super.postDraw();
    }

    @Override
    protected void onResized(int width, int height) {
        super.onResized(width, height);
    }


    @Override
    public void keyPressed(KeyEvent event) {
        super.keyPressed(event);
    }

    @Override
    public void keyReleased(KeyEvent event) {
        super.keyReleased(event);
    }

    @Override
    protected void onContinuousKeyPressed(@Nullable KeyEvent event) {
        super.onContinuousKeyPressed(event);
    }

    @Override
    protected void onSoundEnabledChanged(boolean soundEnabled) {
        super.onSoundEnabledChanged(soundEnabled);
    }



    /* Pendulum Wave Listener ............................................................. */

    @Override
    public void onPendulumCountChanged(@NotNull PendulumWave pendulumWave, int prevCount, int newCount) {
        super.onPendulumCountChanged(pendulumWave, prevCount, newCount);
    }

    @Override
    public void onSpeedChanged(@NotNull PendulumWave pendulumWave, float prevSpeed, float newSpeed) {
        super.onSpeedChanged(pendulumWave, prevSpeed, newSpeed);
    }

    @Override
    public void onIsPausedChanged(@NotNull PendulumWave pendulumWave, boolean isPaused) {
        super.onIsPausedChanged(pendulumWave, isPaused);
    }

    /* Pendulum Listener ............................................................. */

    @Override
    public void onPendulumLengthChanged(@NotNull Pendulum p, float prevLength, float newLength) {
        super.onPendulumLengthChanged(p, prevLength, newLength);
    }

    @Override
    public void onPendulumAngleChanged(@NotNull Pendulum p, float prevAngle, float newAngle) {
        super.onPendulumAngleChanged(p, prevAngle, newAngle);
    }

    @Override
    public void onPendulumHighlightChanged(@NotNull Pendulum p, boolean highlight) {
        super.onPendulumHighlightChanged(p, highlight);
    }

    /* Pendulum Draw Style ........................................................... */

    @Override
    public boolean is3D(@NotNull Pendulum p) {
        return super.is3D(p);
    }

    @Override
    public @NotNull Point3DF lineDrawOrigin(@NotNull Pendulum p) {
        return pendulumChordDrawOrigin(width, height, p.id);
    }

    @Override
    public float lengthScale(@NotNull Pendulum p) {
        return super.lengthScale(p);
    }

    @Override
    public float bobRadius(@NotNull Pendulum p) {
        return super.bobRadius(p);
    }

    @Override
    protected @NotNull PendulumDrawStylesHolder createPendulumDrawStyle(int numPendulums, int index) {
        return super.createPendulumDrawStyle(numPendulums, index);
    }



    public static void main(String[] args) {
        final PendulumWaveP2D main = new PendulumWaveP2D(16);
        PApplet.runSketch(PApplet.concat(new String[] { main.getClass().getName() }, args), main);

        main.main_cli(args);
    }
}


