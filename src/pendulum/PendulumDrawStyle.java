package pendulum;

import org.jetbrains.annotations.Nullable;
import java.awt.Color;

public record PendulumDrawStyle(float lineStrokeWeight,
                                @Nullable Color lineStrokeColor,
                                float bobStrokeWeight,
                                @Nullable Color bobStrokeColor,
                                boolean fillBob,
                                @Nullable Color bobFillColor,
                                float extraRadius) {

}
