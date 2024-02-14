package util;

/**
 * A point representing a location in {@code (x,y)} coordinate space,
 * specified in float precision.
 */
public class Point2DF {
    public final float x;
    public final float y;

    public Point2DF() {
        this(0, 0);
    }

    public Point2DF(Point2DF p) {
        this(p.x, p.y);
    }

    public Point2DF(float x, float y) {
        this.x = x;
        this.y = y;
    }

    public boolean equals(Object o) {
        if (this == o)
            return true;

        if (o instanceof Point2DF pt) {
            return x == pt.x && y == pt.y;
        }

        return false;
    }

    @Override
    public int hashCode() {
        return 31 * (31 * Float.hashCode(x) + Float.hashCode(y));
    }

    public String toString() {
        return getClass().getName() + "[x=" + x + ",y=" + y + "]";
    }

}