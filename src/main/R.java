package main;

import org.apache.commons.math3.util.Pair;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import util.Config;

import java.awt.*;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;

public class R {

    public static final boolean FROZEN = false;         // TODO: set true before packaging

    // Title
    public static final String TITLE = "Pendulum Wave";
    public static final String TITLE_2D = "Pendulum Wave 2D";
    public static final String TITLE_3D = "Pendulum Wave 3D";

    // Dir structure
    public static final Path DIR_MAIN = (FROZEN? Path.of("app") : Path.of("")).toAbsolutePath();
    public static final Path DIR_RES = DIR_MAIN.resolve("res");
    public static final Path DIR_FONT = DIR_RES.resolve("font");
    public static final Path DIR_IMAGE = DIR_RES.resolve("image");

    // Images
    public static final Path IMAGE_PENDULUM_WAVE_ICON = DIR_IMAGE.resolve("pendulum_wave_icon3.png");

    // Fonts
    public static final Path FONT_PD_SANS_REGULAR = DIR_FONT.resolve("product_sans_regular.ttf");
    public static final Path FONT_PD_SANS_MEDIUM = DIR_FONT.resolve("product_sans_medium.ttf");

    // Configurations

    public static final Path FILE_CONFIG_2D = DIR_MAIN.resolve("config-2D.ini");
    public static final Path FILE_CONFIG_3D = DIR_MAIN.resolve("config-3D.ini");

    public static final Config CONFIG_2D = Config.obtain(FILE_CONFIG_2D);       // Since configs are lazily loaded, this does not have any cost
    public static final Config CONFIG_3D = Config.obtain(FILE_CONFIG_3D);       // Since configs are lazily loaded, this does not have any cost

    public static final String CONFIG_KEY_FULLSCREEN = "fullscreen";
    public static final String CONFIG_KEY_WIN_WIDTH_PIXELS = "win_width";   // pixels has precedence over ratio
    public static final String CONFIG_KEY_WIN_HEIGHT_PIXELS = "win_height"; // pixels has precedence over ratio
    public static final String CONFIG_KEY_WIN_WIDTH_RATIO = "win_width_ratio";
    public static final String CONFIG_KEY_WIN_HEIGHT_RATIO = "win_height_ratio";
    public static final String CONFIG_KEY_SOUND = "sound";
    public static final String CONFIG_KEY_POLY_RHYTHM = "poly_rhythm";
    public static final String CONFIG_KEY_SPEED = "speed";
    public static final String CONFIG_KEY_GRAVITY = "gravity";
    public static final String CONFIG_KEY_DRAG = "drag";
    public static final String CONFIG_KEY_MASS = "mass";
    public static final String CONFIG_KEY_START_ANGLE = "start_angle";
    public static final String CONFIG_KEY_PENDULUM_COUNT = "pendulum_count";
    public static final String CONFIG_KEY_WAVE_PERIOD = "wave_period";
    public static final String CONFIG_KEY_MIN_OSC = "min_osc";
    public static final String CONFIG_KEY_OSC_STEP = "osc_step";

    public static int getConfigWindowWidth(@NotNull Config config, int screenWidth, int defaultValue) {
        int w = config.getValueInt(CONFIG_KEY_WIN_WIDTH_PIXELS, -1);
        if (w <= 0) {
            final float ratio = config.getValueFloat(CONFIG_KEY_WIN_WIDTH_RATIO, -1);
            if (ratio > 0) {
                w = Math.round(screenWidth * ratio);
            }
        }

        if (w <= 0) {
            w = defaultValue;
        }

        return w;
    }

    public static int getConfigWindowHeight(@NotNull Config config, int screenHeight, int defaultValue) {
        int h = config.getValueInt(CONFIG_KEY_WIN_HEIGHT_PIXELS, -1);
        if (h <= 0) {
            final float ratio = config.getValueFloat(CONFIG_KEY_WIN_HEIGHT_RATIO, -1);
            if (ratio > 0) {
                h = Math.round(screenHeight * ratio);
            }
        }

        if (h <= 0) {
            h = defaultValue;
        }

        return h;
    }

    @NotNull
    public static Dimension getConfigWindowSize(@NotNull Config config, @NotNull Dimension screenSize, @NotNull Dimension defaultValue) {
        return new Dimension(
                getConfigWindowWidth(config, screenSize.width, defaultValue.width),
                getConfigWindowHeight(config, screenSize.height, defaultValue.height)
        );
    }


    // Shell
    private static final String SHELL_ROOT_NS = "wave";       // Name Space

    @NotNull
    public static String shellPath(@Nullable String child) {
        return ((child == null || child.isEmpty())? SHELL_ROOT_NS: SHELL_ROOT_NS + "\\" + child) + "> ";
    }

    public static final String SHELL_ROOT = shellPath(null);
    public static final String SHELL_WINDOW = shellPath("win");
    public static final String SHELL_PENDULUM_COUNT = shellPath("count");
    public static final String SHELL_RESET = shellPath("reset");
    public static final String SHELL_SPEED = shellPath("speed");
    public static final String SHELL_GRAVITY = shellPath("gravity");
    public static final String SHELL_DRAG = shellPath("drag");
    public static final String SHELL_MASS = shellPath("mass");
    public static final String SHELL_ANGLE = shellPath("angle");
    public static final String SHELL_WAVE_PERIOD = shellPath("wp");
    public static final String SHELL_MIN_OSC = shellPath("min-osc");
    public static final String SHELL_OSC_STEP = shellPath("osc-step");

    public static final String SHELL_CAMERA = shellPath("cam");
    public static final String SHELL_ROTATION_X = shellPath("pitch");
    public static final String SHELL_ROTATION_Y = shellPath("yaw");
    public static final String SHELL_ROTATION_Z = shellPath("roll");


    public static final String DESCRIPTION_GENERAL =
            """
            =======================  Pendulum Wave  =======================
            This is an interactive Pendulum Wave physics engine, with both 2D and 3D renderers and a real-time simulation environment
            """;

    public static final String DESCRIPTION_COMMANDS =
            """
            -> help [-controls | -commands] : Print usage information
               Options
               1. -controls -> print controls information
               2. -commands -> print commands information
               
            -> count [-soft] <pendulum count> : Sets the number of pendulums in the wave.
               Alias: num, n
               Options
               1. -soft -> Do not reset pendulums state
               
            -> reset [-state | -env | -count | -cam | -win | -all] : Resets the given scope
               Scopes
               1. -state -> reset pendulums state
               2. -env -> reset simulation environment
               3. -count -> reset pendulum count
               4. -cam -> reset camera (pitch, yaw and roll)
               5. -win -> reset window size and position
               6. -all -> reset everything
            
            -> play : Start the simulation. Alias: start
            -> pause : Pause the simulation. Alias: stop
            -> toggle play : Toggle simulation play/pause state
            -> save : Save current frame to a png file
               Alias: snap, snapshot, saveframe
               
            -> speed [-x | -p] <value> : Sets the simulation speed, in multipliers or percentage
               Modes
               1. -x -> Multiples or times (Default)
               2. -p -> percentage, in range [0, 100]
               
            -> gravity [-reset] <value in ms-2> : Sets the acceleration due to gravity (in ms-2)
            -> drag [-reset] <value in g/s> : Sets the drag coefficient (in gram/s). positive value -> drag, negative -> push
            -> mass [-reset] <value in g> : Sets the mass of each pendulum (in grams). Must be > 0
            -> angle [-reset] <value in deg> : Sets the start angle of each pendulum (in degrees)
            
            -> wp [-reset] <value in secs> : Sets the wave period (in secs). Must be > 0
               Alias: period, waveperiod
               
            -> minosc [-reset] <value> : Sets the minimum oscillations in wave period. Must be > 0
               Alias: osc, mosc
            
            -> oscstep [-reset] <value> : Sets the oscillation step per pendulum. Must be > 0
               Alias: step, ostep
            
            -> bob-only : Toggle draw bobs-only mode
               Alias: bobs, toggle bobs, toggle bobs-only
               
            -> sound : Toggle sounds
            -> poly-rhythm : Toggle Poly Rhythm mode. If enabled, it allows playing multiple notes at once
            
            -> hud : Toggle HUD overlay
            -> keys : Toggle control key bindings
               Alias: toggle keys, controls, toggle controls
               
            -> win [-size | -pos] <x> <y> : Sets the window size or location on screen
               Options
               1. -size -> set window size
               2. -pos -> set window location on screen
            
            -> pitch [-by | -f] <+ | - | value_in_deg> : Sets the camera pitch (rotation about X-axis)
               Alias: rx, rotx, rotationx
               Wildcards: + or up, - or down
               Options
               1. -by -> change current pitch by the given value
               2. -f -> force without animations
               
            -> yaw [-by | -f] <+ | - | value_in_deg> : Sets the camera yaw (rotation about Y-axis)
               Alias: ry, roty, rotationy
               Wildcards: + or left, - or right
               Options
               1. -by -> change current yaw by the given value
               2. -f -> force without animations
            
            -> roll [-by | -f] <+ | - | value_in_deg> : Sets the camera roll (rotation about Z-axis)
               Alias: rz, rotz, rotationz
               Wildcards: + or left, - or right
               Options
               1. -by -> change current roll by the given value
               2. -f -> force without animations
            """;

    public static void main(String[] args) {
        System.out.println(DESCRIPTION_COMMANDS);
    }

    /* ...................................  Utility functions  ................................ */

    @NotNull
    public static Pair<String, String> splitNameExt(@NotNull String fullName, boolean withDot) {
        final int i = fullName.lastIndexOf('.');

        if (i != 1) {
            return new Pair<>(fullName.substring(0, i), fullName.substring(withDot? i: i + 1));
        }

        return new Pair<>(fullName, "");
    }

    @NotNull
    public static String getName(@NotNull String fullName) {
        final int i = fullName.lastIndexOf('.');
        return i != -1? fullName.substring(0, i): fullName;
    }


    @Nullable
    public static Path findFileWithNameTokenSuffix(@NotNull Path dir, @NotNull String fileNameTokenSuffix) {
        // Finding any file in IMG folder with name == IMAGE_ASCII_ART_INPUT_FILE_NAME

        try (DirectoryStream<Path> stream = Files.newDirectoryStream(dir, Files::isRegularFile)) {
            for (Path p: stream) {
                final String name = getName(p.getFileName().toString());
                if (name.endsWith(fileNameTokenSuffix)) {
                    return p;
                }
            }
        } catch (Exception exc) {
        }

        return null;
    }


    // Readme

    public static boolean createReadme(@NotNull String instructions) {
        try (PrintWriter w = new PrintWriter("readme.txt", StandardCharsets.UTF_8)) {
            w.print(instructions);
            w.flush();
            return true;
        } catch (Throwable exc) {
            exc.printStackTrace();
        }

        return false;
    }


}
