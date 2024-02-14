package pendulum;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public record PendulumDrawStylesHolder(@NotNull PendulumDrawStyle normalDrawStyle,
                                       @Nullable PendulumDrawStyle highlightDrawStyle) {
}
