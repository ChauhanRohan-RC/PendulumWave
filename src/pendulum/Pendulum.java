package pendulum;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import processing.core.PApplet;
import util.Point3DF;

import java.awt.*;

/**
 *
 * Equation of motion: <strong>ang_acc + (b/m * ang_vel) + (g/l * sin(angle)) = 0 </strong>, where<br>
 * ang_acc : angular acceleration, in rad s<sup>-2</sup> <br>
 * ang_vel : angular velocity, in rad/s <br>
 * m : mass of the pendulum bob, in kg <br>
 * g : acceleration due to gravity <br>
 * l : length of the pendulum chord, in meter <br>
 * b : drag coefficient, in kg/s (Positive -> drag, Negative -> push) <br>
 * angle: angle of the pendulum chord off the vertical
 * */
public class Pendulum {

    public static boolean shouldHighlight(float angle, float startAngle) {
        return Math.toDegrees(Math.abs(angle)) < 2;
    }

    public interface Listener {

        void onPendulumLengthChanged(@NotNull Pendulum p, float prevLength, float newLength);

        void onPendulumAngleChanged(@NotNull Pendulum p, float prevAngle, float newAngle);

        void onPendulumHighlightChanged(@NotNull Pendulum p, boolean highlight);

    }



    public final int id;

    // Mass of the pendulum bob, in kg
    private float mass;

    // Length of the pendulum chord, in meters
    private float length;

    // angle of the pendulum off the vertical axis, in radians
    private float angle;

    // Angular velocity of the pendulum, in rad/s
    private float angVel;

    private float startAngle;
    private float startAngVel;
    private boolean mStarted, mPaused;
    private boolean mHighlight;

    @Nullable
    private Listener mListener;

    private Object tag;

    public Pendulum(int id, float mass, float length, float startAngle, float startAngVel) {
        this.id = id;
        this.mass = mass;
        this.length = length;
        this.startAngle = startAngle;
        this.startAngVel = startAngVel;

        this.angle = startAngle;
        updateHighlight();
    }

    public Pendulum(int id, float mass, float length, float startAngle) {
        this(id, mass, length, startAngle, 0);
    }

    @Nullable
    public Listener getListener() {
        return mListener;
    }

    public Pendulum setListener(@Nullable Listener listener) {
        mListener = listener;
        return this;
    }

    @Nullable
    public Object getTag() {
        return tag;
    }

    public Pendulum setTag(@Nullable Object tag) {
        this.tag = tag;
        return this;
    }

    public float getMass() {
        return mass;
    }

    public Pendulum setMass(float mass) {
        this.mass = mass;
        return this;
    }


    protected void onLengthChanged(float prevLen, float newLen) {
        if (mListener != null) {
            mListener.onPendulumLengthChanged(this, prevLen, newLen);
        }
    }

    public float getLength() {
        return length;
    }

    public Pendulum setLength(float length) {
        if (this.length != length) {
            final float prev = this.length;
            this.length = length;
            onLengthChanged(prev, length);
        }

        return this;
    }

    public float getStartAngle() {
        return startAngle;
    }

    public Pendulum setStartAngle(float startAngle) {
        this.startAngle = startAngle;
        return this;
    }

    public float getStartAngVel() {
        return startAngVel;
    }

    public Pendulum setStartAngVel(float startAngVel) {
        this.startAngVel = startAngVel;
        return this;
    }

    public boolean isStarted() {
        return mStarted;
    }

    public boolean isPaused() {
        return mPaused;
    }

    public Pendulum pause() {
        mPaused = true;
        return this;
    }

    public Pendulum resume() {
        mPaused = false;
        return this;
    }

    public Pendulum resetState() {
        mStarted = false;
        angVel = startAngVel;
        setAngleInternal(startAngle);
        return this;
    }

    protected void onHighlightChanged(boolean highlight) {
        if (mListener != null) {
            mListener.onPendulumHighlightChanged(Pendulum.this, highlight);
        }
    }

    public boolean isHighlighted() {
        return mHighlight;
    }

    private void updateHighlight() {
        final boolean h = shouldHighlight(angle, startAngle);
        if (mHighlight != h) {
            mHighlight = h;
            onHighlightChanged(h);
        }
    }

    protected void onAngleChanged(float prevAngle, float newAngle) {
        if (mListener != null) {
            mListener.onPendulumAngleChanged(Pendulum.this, prevAngle, newAngle);
        }

        updateHighlight();
    }

    private void setAngleInternal(float angle) {
        if (this.angle == angle)
            return;

        final float prev = this.angle;
        this.angle = angle;
        onAngleChanged(prev, angle);
    }

    public Pendulum update(@NotNull PendulumEnvironmentProvider environmentProvider, float dt /* secs */) {
        if (!mStarted) {
            angVel = startAngVel;
            setAngleInternal(startAngle);
            mStarted = true;        // start
        } else if (mPaused) {
            return this;
        }

        final float acc = - ((environmentProvider.gravity() * PApplet.sin(angle) / length) + (environmentProvider.drag() / mass * angVel));

        angVel += acc * dt;
        setAngleInternal(angle + (angVel * dt));
        return this;
    }

    public Pendulum draw(@NotNull PApplet p, @NotNull PendulumStyleProvider styleProvider) {
        p.pushMatrix();

        final boolean is3d = styleProvider.is3D(this);
        final Point3DF origin = styleProvider.lineDrawOrigin(this);

        if (is3d) {
            p.translate(origin.x, origin.y, origin.z);
        } else {
            p.translate(origin.x, origin.y);
        }

        final float len = length * styleProvider.lengthScale(this);
        final float x = len * PApplet.sin(angle), y = len * PApplet.cos(angle);

        // Style
        final boolean highlight = isHighlighted();
        PendulumDrawStyle style = null;

        if (highlight) {
            style = styleProvider.drawStyleHighlight(this);
        }

        if (style == null) {
            style = styleProvider.drawStyle(this);
        }

        // Line
        if (styleProvider.drawLine()) {
            p.pushStyle();

            // draw line
            if (is3d) {
                p.noStroke();

                if (style.lineStrokeWeight() == 0 || style.lineStrokeColor() == null) {
                    p.noFill();
                } else {
                    p.fill(style.lineStrokeColor().getRGB());
                }

                // Box interacts with lights, line does not
                p.pushMatrix();
                p.rotateZ(-angle);
                p.translate(0, len/2, 0);       // center draw mode
                p.box(style.lineStrokeWeight(), len, style.lineStrokeWeight());
                p.popMatrix();

//                p.line(0, 0, 0, x, y, 0);
            } else {
                // Line stroke
                if (style.lineStrokeWeight() == 0 || style.lineStrokeColor() == null) {
                    p.noStroke();
                } else {
                    p.strokeWeight(style.lineStrokeWeight());
                    p.stroke(style.lineStrokeColor().getRGB());
                }

                p.line(0, 0, x, y);
            }

            p.popStyle();
        }


        if (styleProvider.drawBob()) {
            p.pushStyle();

            // Bon stroke
            if (style.bobStrokeWeight() == 0 || style.bobStrokeColor() == null) {
                p.noStroke();
            } else {
                p.strokeWeight(style.bobStrokeWeight());
                p.stroke(style.bobStrokeColor().getRGB());
            }

            // bob fill
            if (style.fillBob() && style.bobFillColor() != null) {
                p.fill(style.bobFillColor().getRGB());
            } else {
                p.noFill();
            }

            // draw bob
            final float radius = styleProvider.bobRadius(this) + style.extraRadius();

            if (is3d) {
                p.translate(x, y, 0);

//                p.lights();
//                p.noStroke();
                p.sphere(radius);
            } else {
                p.ellipse(x, y, radius, radius);
            }

            p.popStyle();
        }

        p.popMatrix();
        return this;
    }


    public static final PendulumDrawStyle DEFAULT_DRAW_STYLE = new PendulumDrawStyle(
            2,
            Color.GRAY,
            2,
            Color.GRAY,
            true,
            Color.WHITE,
            0
    );

    public static final PendulumDrawStyle DEFAULT_DRAW_STYLE_HIGHLIGHT = new PendulumDrawStyle(
            2,
            Color.WHITE,
            2,
            Color.WHITE,
            true,
            Color.ORANGE,
            0
    );

}
