package util;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import peasy.CameraState;
import peasy.PeasyCam;
import peasy.org.apache.commons.math.geometry.Rotation;
import peasy.org.apache.commons.math.geometry.RotationOrder;
import peasy.org.apache.commons.math.geometry.Vector3D;

import java.awt.*;
import java.util.Collection;
import java.util.function.Function;

public class U {

    /**
     * Native screen resolution, ex. 1920x1080
     * */
    @NotNull
    public static final Dimension SCREEN_RESOLUTION_NATIVE;

    /**
     * Screen resolution with scaling. Normally, the scaling is 125% (1.25).<br>
     * Hence, for a native 1920x1080 screen, the apparent resolution will be 1920/1.25 , 1080/1.25 = 1536x864<br><br>
     * */
    @NotNull
    public static final Dimension SCREEN_RESOLUTION_SCALED;

    static {
        final DisplayMode displayMode = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDisplayMode();

        SCREEN_RESOLUTION_NATIVE = new Dimension(displayMode.getWidth(), displayMode.getHeight());
        SCREEN_RESOLUTION_SCALED = Toolkit.getDefaultToolkit().getScreenSize();
    }

    @NotNull
    public static Dimension scaleDimension(@NotNull Dimension o, float scale) {
        return new Dimension(Math.round(o.width * scale), Math.round(o.height * scale));
    }


    public static <T extends Enum<T>> T cycleEnum(@NotNull Class<T> clazz, int curOrdinal) {
        final T[] values = clazz.getEnumConstants();

        if (values.length == 0)
            return null;

        int nextI = curOrdinal + 1;
        if (nextI >= values.length) {
            nextI = 0;
        }

        return values[nextI];
    }

    public static <T extends Enum<T>> T cycleEnum(@NotNull Class<T> clazz, T current) {
        return cycleEnum(clazz, current != null ? current.ordinal() : -1);
    }


    public static int alpha255(int argb) {
        return (argb >>> 24) & 0xff;
    }

    public static int red255(int argb) {
        return (argb >>> 16) & 0xff;
    }

    public static int green255(int argb) {
        return (argb >>> 8) & 0xff;
    }

    public static int blue255(int argb) {
        return argb & 0xff;
    }

    public static float alpha01(int argb) {
        return alpha255(argb) / 255.0f;
    }
    
    public static int rgb255(int argb) {
        return argb & 0xffffff;
    }

    public static int withAlpha(int argb, int alpha) {
        return ((alpha & 0xff) << 24) | rgb255(argb);
    }

    @NotNull
    public static String hex(int rgb) {
        return String.format("#%02x%02x%02x", red255(rgb), green255(rgb), blue255(rgb));
    }

    public static float luminance255(int r, int g, int b) {
        return (0.2126f * r) + (0.7152f * g) + (0.0722f * b);
    }

    public static float luminance255(int rgb) {
        return luminance255(red255(rgb), green255(rgb), blue255(rgb));
    }


    public static float sq(float n) {
        return n * n;
    }



    public static float lerp(float start, float stop, float amt) {
        return start + (stop - start) * amt;
    }

    public static float norm(float value, float start, float stop) {
        return (value - start) / (stop - start);
    }

    public static float map(float value, float start1, float stop1, float start2, float stop2) {
        float outgoing = start2 + (stop2 - start2) * ((value - start1) / (stop1 - start1));
        String badness = null;
        if (outgoing != outgoing) {
            badness = "NaN (not a number)";
        } else if (outgoing == Float.NEGATIVE_INFINITY || outgoing == Float.POSITIVE_INFINITY) {
            badness = "infinity";
        }

        if (badness != null) {
            String msg = String.format("map(%s, %s, %s, %s, %s) called, which returns %s", Format.nf(value), Format.nf(start1), Format.nf(stop1), Format.nf(start2), Format.nf(stop2), badness);
            System.err.println(msg);
        }

        return outgoing;
    }

    public static int constrain(int amt, int low, int high) {
        return (amt < low)? low: Math.min(amt, high);
    }


    public static float constrain(float amt, float low, float high) {
        return (amt < low)? low: Math.min(amt, high);
    }



    @Nullable
    public static Runnable chainRunnables(@Nullable Collection<? extends Runnable> tasks) {
        if (tasks == null || tasks.isEmpty())
            return null;

        Runnable merged = null;
        for (Runnable task: tasks) {
            if (merged == null) {
                merged = task;
            } else {
                final Runnable old = merged;
                merged = () -> { old.run(); task.run(); };
            }
        }

        return merged;
    }


    /* Peasy Camera Hacks ........................................................................................  */

    @NotNull
    public static CameraState transformPeasyCamState(@NotNull PeasyCam cam, @NotNull Function<float[], float[]> rotationTransform, @NotNull Function<float[], float[]> lookAtTransform, @NotNull Function<Double, Double> distanceProvider) {
        final float[] newRotations = rotationTransform.apply(cam.getRotations());
        final float[] newLookAt = lookAtTransform.apply(cam.getLookAt());
        final double newDistance = distanceProvider.apply(cam.getDistance());

        return new CameraState(new Rotation(RotationOrder.XYZ, newRotations[0], newRotations[1], newRotations[2]),
                new Vector3D(newLookAt[0], newLookAt[1], newLookAt[2]),
                newDistance
        );
    }

    @NotNull
    public static CameraState changePeasyCamStateBy(@NotNull PeasyCam cam, float delRotationX, float delRotationY, float delRotationZ, float delLookAtX, float delLookAtY, float delLookAtZ, float delDistance) {
        return transformPeasyCamState(cam, rot -> new float[] { rot[0] + delRotationX, rot[1] + delRotationY, rot[2] + delRotationZ }, la -> new float[] { la[0] + delLookAtX, la[1] + delLookAtY, la[2] + delLookAtZ }, dis -> dis + delDistance);
    }

    @NotNull
    public static CameraState changePeasyCamStateBy(@NotNull PeasyCam cam, float delRotationX, float delRotationY, float delRotationZ) {
        return changePeasyCamStateBy(cam, delRotationX, delRotationY, delRotationZ, 0, 0, 0, 0);
    }

    // Pitch
    public static void rotateXTo(@NotNull PeasyCam cam, float rotationX, long animationMs) {
        cam.setState(transformPeasyCamState(cam, rot -> new float[] { rotationX, rot[1], rot[2] }, la -> la, dis -> dis), animationMs);
    }

    public static void rotateXBy(@NotNull PeasyCam cam, float delRotationX, long animationMs) {
        if (animationMs > 0) {
            cam.setState(changePeasyCamStateBy(cam, delRotationX, 0, 0), animationMs);
        } else {
            cam.rotateX(delRotationX);
        }
    }

    // Yaw
    public static void rotateYTo(@NotNull PeasyCam cam, float rotationY, long animationMs) {
        cam.setState(transformPeasyCamState(cam, rot -> new float[] { rot[0], rotationY, rot[2] }, la -> la, dis -> dis), animationMs);
    }

    public static void rotateYBy(@NotNull PeasyCam cam, float delRotationY, long animationMs) {
        if (animationMs > 0) {
            cam.setState(changePeasyCamStateBy(cam, 0, delRotationY, 0), animationMs);
        } else {
            cam.rotateY(delRotationY);
        }
    }

    // Roll
    public static void rotateZTo(@NotNull PeasyCam cam, float rotationZ, long animationMs) {
        cam.setState(transformPeasyCamState(cam, rot -> new float[] { rot[0], rot[1], rotationZ }, la -> la, dis -> dis), animationMs);
    }

    public static void rotateZBy(@NotNull PeasyCam cam, float delRotationZ, long animationMs) {
        if (animationMs > 0) {
            cam.setState(changePeasyCamStateBy(cam, 0, 0, delRotationZ), animationMs);
        } else {
            cam.rotateZ(delRotationZ);
        }
    }

    public static float normalizeDegrees(float degrees) {
        degrees %= 360;

        if (degrees < 0) {
            degrees += 360;
        }

        return degrees;
    }


}
