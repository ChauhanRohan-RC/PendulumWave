package pendulum;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import util.Point3DF;

public interface PendulumStyleProvider {

    boolean is3D(@NotNull Pendulum p);

    @NotNull
    Point3DF lineDrawOrigin(@NotNull Pendulum p);

    float lengthScale(@NotNull Pendulum p);

    float bobRadius(@NotNull Pendulum p);

    boolean drawLine();

    boolean drawBob();

    @NotNull
    PendulumDrawStyle drawStyle(@NotNull Pendulum p);

    @Nullable
    PendulumDrawStyle drawStyleHighlight(@NotNull Pendulum p);

}
