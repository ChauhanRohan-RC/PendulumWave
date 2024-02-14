package util;

import org.jetbrains.annotations.NotNull;
import peasy.CameraState;
import peasy.PeasyCam;
import peasy.org.apache.commons.math.geometry.Rotation;
import peasy.org.apache.commons.math.geometry.RotationOrder;
import peasy.org.apache.commons.math.geometry.Vector3D;

import java.awt.*;
import java.text.NumberFormat;

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

    @NotNull
    public static String nf(float num) {
        int inum = (int)num;
        return num == (float)inum ? String.valueOf(inum) : String.valueOf(num);
    }


    public static String nf(float num, int minIntegerDigits, int minFracDigits, int maxFracDigits) {
        final NumberFormat float_nf = NumberFormat.getInstance();
        float_nf.setGroupingUsed(false);

        if (minIntegerDigits != 0)
            float_nf.setMinimumIntegerDigits(minIntegerDigits);
        if (minFracDigits != 0) {
            float_nf.setMinimumFractionDigits(minFracDigits);
        }

        if (maxFracDigits != 0) {
            float_nf.setMaximumFractionDigits(maxFracDigits);
        }

        if (num == -0f) {
            num = 0;
        }

        return float_nf.format(num);
    }


    private static NumberFormat sNumberFormat000;
    private static NumberFormat sNumberFormat001;
    private static NumberFormat sNumberFormat002;

    public static String nf000(float num) {
        NumberFormat nf = sNumberFormat000;
        if (nf == null) {
            nf = NumberFormat.getInstance();
            nf.setGroupingUsed(false);
            sNumberFormat000 = nf;
        }

        if (num == -0f) {
            num = 0;
        }

        return nf.format(num);
    }

    public static String nf001(float num) {
        NumberFormat nf = sNumberFormat001;
        if (nf == null) {
            nf = NumberFormat.getInstance();
            nf.setGroupingUsed(false);
            nf.setMaximumFractionDigits(1);

            sNumberFormat001 = nf;
        }

        if (num == -0f) {
            num = 0;
        }

        return nf.format(num);
    }

    public static String nf002(float num) {
        NumberFormat nf = sNumberFormat002;
        if (nf == null) {
            nf = NumberFormat.getInstance();
            nf.setGroupingUsed(false);
            nf.setMaximumFractionDigits(2);

            sNumberFormat002 = nf;
        }


        if (num == -0f) {
            num = 0;
        }

        return nf.format(num);
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
            String msg = String.format("map(%s, %s, %s, %s, %s) called, which returns %s", nf(value), nf(start1), nf(stop1), nf(start2), nf(stop2), badness);
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



    /* Peasy Camera Hacks ........................................................................................  */

    public static CameraState createNewPeasyCamState(@NotNull PeasyCam cam, float delRotationX, float delRotationY, float delRotationZ, float delCenterX, float delCenterY, float delCenterZ, float delDistance) {
        final float[] rotations = cam.getRotations();
        final float[] lookAt = cam.getLookAt();

        return new CameraState(new Rotation(RotationOrder.XYZ, rotations[0] + delRotationX, rotations[1] + delRotationY, rotations[2] + delRotationZ),
                new Vector3D(lookAt[0] + delCenterX, lookAt[1] + delCenterY, lookAt[2] + delCenterZ),
                cam.getDistance() + delDistance
        );
    }

    public static CameraState createNewPeasyCamState(@NotNull PeasyCam cam, float delRotationX, float delRotationY, float delRotationZ) {
        return createNewPeasyCamState(cam, delRotationX, delRotationY, delRotationZ, 0, 0, 0, 0);
    }

    // Pitch
    public static void rotateX(@NotNull PeasyCam cam, float delRotationX, long animationMs) {
        if (animationMs > 0) {
            cam.setState(createNewPeasyCamState(cam, delRotationX, 0, 0), animationMs);
        } else {
            cam.rotateX(delRotationX);
        }
    }

    // Yaw
    public static void rotateY(@NotNull PeasyCam cam, float delRotationY, long animationMs) {
        if (animationMs > 0) {
            cam.setState(createNewPeasyCamState(cam, 0, delRotationY, 0), animationMs);
        } else {
            cam.rotateY(delRotationY);
        }
    }

    public static void rotateZ(@NotNull PeasyCam cam, float delRotationZ, long animationMs) {
        if (animationMs > 0) {
            cam.setState(createNewPeasyCamState(cam, 0, 0, delRotationZ), animationMs);
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
