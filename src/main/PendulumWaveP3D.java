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
 *
 * TODO: pendulum support as cylinder, switchable spot lighting
 * */
public class PendulumWaveP3D extends BasePendulumWavePUi {

    // Although fullscreen does not work normally with P3D, Resize works horribly outside fullscreen in P3D. So fullscreen is better
    private static final boolean DEFAULT_FULLSCREEN = true;
    private static final Dimension DEFAULT_WINDOW_SIZE = U.scaleDimension(U.SCREEN_RESOLUTION_NATIVE, 0.9f, 0.86f);

    /**
     * Separation between pendulums, in pixels
     * */
    private static final float PENDULUM_SEPARATION_PIXELS = 50;

    @NotNull
    private static Point3DF pendulumChordDrawOrigin(int width, int height, int index) {
        return new Point3DF(width / 2f, min(height / 10f, 50), -index * PENDULUM_SEPARATION_PIXELS);
    }


    private PeasyCam cam;

    private boolean mFullscreen = DEFAULT_FULLSCREEN;
    @NotNull
    private Dimension mInitialWindowSize = DEFAULT_WINDOW_SIZE;

    public PendulumWaveP3D(@NotNull PendulumWave pendulumWave) {
        super(pendulumWave);
        init();
    }

    public PendulumWaveP3D(int initialPendulumCount) {
        super(initialPendulumCount);
        init();
    }

    public PendulumWaveP3D() {
        super();
        init();
    }

    private void init() {
        // Handle configurations
        final Config config = R.CONFIG_3D;
        mFullscreen = config.getValueBool(R.CONFIG_KEY_FULLSCREEN, DEFAULT_FULLSCREEN);
        mInitialWindowSize = R.getConfigWindowSize(config, U.SCREEN_RESOLUTION_NATIVE, DEFAULT_WINDOW_SIZE);

        applyConfig(config);

        // AT Last
        attachPendulumWaveListener();       // very important
    }

    @Override
    public boolean isRendered3D() {
        return true;
    }

    @Override
    public boolean isFullscreen() {
        return mFullscreen;
    }

    @Override
    public boolean supportsSurfaceLocationSetter() {
        return true;
    }

    @Override
    public boolean supportsSurfaceSizeSetter() {
        return false;
    }

    @Override
    public @NotNull Dimension getInitialSurfaceDimensions() {
        return mInitialWindowSize;
    }

    @Override
    public void settings() {
        super.settings();

        if (mFullscreen) {
            fullScreen(P3D);
        } else {
            size(mInitialWindowSize.width, mInitialWindowSize.height, P3D);
        }

        smooth(4);
    }

    public void setup() {
        super.setup();

        if (!mFullscreen) {
            surface.setResizable(false);    // Resize works horribly outside fullscreen in P3D renderer
        }

//        surface.setCursor(Cursor.MOVE_CURSOR);       // PeasyCam crashes when cursor is hidden
//        surface.setResizable(true);
//        surface.setAlwaysOnTop(true);

        recreateCamera(false);
    }

    private void drawSupport() {
        if (isDrawOnlyBobEnabled())
            return;

        final int count = pendulumWave.pendulumCount();
        final Point3DF o = pendulumChordDrawOrigin(width, height, 0);
        final float len = ((count - 1) * PENDULUM_SEPARATION_PIXELS);
        final float w = 20, h = 20, d = 20;

        pushMatrix();
        pushStyle();

//        strokeWeight(2);
//        stroke(GLConfig.PENDULUMS_SUPPORT_STROKE.getRGB());
        noStroke();
        fill(GLConfig.PENDULUMS_SUPPORT_FILL.getRGB());


//        translate(o.x, o.y, d);
//        rotateX(-PI / 2);
//        cylinder(10, 10, (len + (d * 2)), 20);

        translate(o.x, o.y, -len / 2);
        rectMode(CENTER);
        box(w, h, len + (d * 2));

        popStyle();
        popMatrix();
    }

    @Override
    protected void drawMain() {
        background(GLConfig.BG.getRGB());

        // Lights
//        ambientLight(255, 255, 255);
//        directionalLight();
        final float len = ((pendulumWave.pendulumCount() - 1) * PENDULUM_SEPARATION_PIXELS);
        spotLight(255, 255, 255, 0, height / 2f, -len / 2f, 1, 0, 0, PI / 3, 2);     // left
        spotLight(255, 255, 255, width, height / 2f, -len / 2f, -1, 0, 0, PI / 3, 2);    // Right

        spotLight(255, 255, 255, width / 2f, height / 2f, (height / 2f) / tan(radians(26)), 0, 0, -1, PI / 4, 2);    // Front
        spotLight(255, 255, 255, width / 2f, height / 2f, -(len + (height / 2f) / tan(radians(26))), 0, 0, 1, PI / 4, 2);    // Back

        spotLight(255, 255, 255, width / 2f, 0, -len / 2f, 0, 1, 0, PI / 4, 2);    // Top
        spotLight(255, 255, 255, width / 2f, height, -len / 2f, 0, -1, 0, PI / 4, 2);    // Bottom

        drawSupport();

        pendulumWave.updatePendulums();
        pendulumWave.drawPendulums(this, this);
    }

    @Override
    protected void beginHUD() {
        super.beginHUD();

        if (cam != null) {
            cam.beginHUD();
        }
    }

    @Override
    public boolean shouldDrawAxesInHUD() {
        return true;
    }

    @Override
    protected void drawHUD() {
        super.drawHUD();
    }

    @Override
    protected void endHUD() {
        super.endHUD();

        if (cam != null) {
            cam.endHUD();
        }
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

        recreateCamera(true);
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
        return p.getMass() * 500;
    }

    @Override
    protected @NotNull PendulumDrawStylesHolder createPendulumDrawStyle(int numPendulums, int index) {
        return super.createPendulumDrawStyle(numPendulums, index);
    }

    /* Camera ...........................................  */

    @Override
    @Nullable
    public PeasyCam getCamera() {
        return cam;
    }

    private void recreateCamera(boolean preserveRotations) {
        float[] rotations = null;

        if (cam != null) {
            if (preserveRotations) {
                rotations = cam.getRotations();
            }

            cam.setActive(false);
            cam = null;     // gc
        }

        final int count = pendulumWave.pendulumCount();
        final float total_len = (count - 1) * PENDULUM_SEPARATION_PIXELS;

        final float x_center = width / 2f;
        final float y_center = height / 2f;
        final float z_center = -total_len / 2f;
        cam = new PeasyCam(this, x_center, y_center, z_center, abs(z_center) + (y_center / tan(radians(26))));
        if (rotations != null) {
            cam.rotateX(rotations[0]);
            cam.rotateY(rotations[1]);
            cam.rotateZ(rotations[2]);
        } else {
            cam.reset(GLConfig.CAMERA_EXPLICIT_RESET_ANIMATION_MILLS);
        }
    }

    // TODO: cylinder lighting (normal vectors)
    void cylinder(float bottom, float top, float h, int sides) {
        float angle;
        float[] x = new float[sides+1];
        float[] z = new float[sides+1];

        float[] x2 = new float[sides+1];
        float[] z2 = new float[sides+1];

        //get the x and z position on a circle for all the sides
        for(int i=0; i < x.length; i++) {
            angle = (TWO_PI / sides) * i;
            x[i] = sin(angle) * bottom;
            z[i] = cos(angle) * bottom;
            x2[i] = sin(angle) * top;
            z2[i] = cos(angle) * top;
        }

        //draw the bottom of the cylinder
        beginShape(TRIANGLE_FAN);

        normal(0, -1, 0);
        vertex(0,   0,    0);

        for(int i=0; i < x.length; i++){
            vertex(x[i], 0, z[i]);
        }

        endShape();

        //draw the center of the cylinder
        beginShape(QUAD_STRIP);

        for(int i=0; i < x.length; i++){
            normal(-x[i], 0, z[i]);
            vertex(x[i], 0, z[i]);
            vertex(x2[i], h, z2[i]);
        }

        endShape();

        //draw the top of the cylinder
        beginShape(TRIANGLE_FAN);

        normal(0, 1, 0);
        vertex(0,   h,    0);

        for(int i=0; i < x.length; i++) {
            vertex(x2[i], h, z2[i]);
        }

        endShape();
    }

    public static void main(String[] args) {
        final PendulumWaveP3D main = new PendulumWaveP3D(16);
        PApplet.runSketch(PApplet.concat(new String[] { main.getClass().getName() }, args), main);

        main.main_cli(args);
    }
}


