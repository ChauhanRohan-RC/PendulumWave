=======================  Pendulum Wave  =======================
This is an interactive Pendulum Wave physics engine, with both 2D and 3D renderers and a real-time simulation environment

## CONTROLS ----------------------------------------------------
-> [Ctr | Shf]-N : Pendulums  [Discrete]
	Number of Pendulums in the Pendulum Wave.
	<Keys> : N -> Increase Count  |  Shift-N -> Decrease Count  |  Ctrl-[Shift]-N -> Change count without resetting pendulums state

-> SPACE : Play/Pause  [Discrete]
	Play/Pause simulation.

-> [Ctr | Shf]-R : Reset  [Discrete]
	Resets the Simulation.
	<Keys> : R -> Reset Pendulums State  |  Ctrl-R -> Reset Pendulum Count  |  Shift-R -> Reset Simulation Environment   |  Ctrl-Shift-R -> Reset Everything

-> B : Bobs Only  [Discrete]
	Draw pendulum bobs only (do not draw pendulum chords and support).

-> H : HUD  [Discrete]
	Show/Hide HUD.

-> C : Controls  [Discrete]
	Show/Hide Control Key Bindings.

-> S : Sound  [Discrete]
	Toggle Sounds.

-> Shf-S : Poly Rhythm  [Discrete]
	Toggle Poly Rhythm (play multiple notes at once).

-> Ctrl-S : Save Frame  [Discrete]
	Save Current graphics frame in a png file.

-> [Shf]-/ : Sim Speed  [Continuous]
	Simulation Speed, in both multiples and percentage.
	<Keys> : / -> Increase Speed  |  Shift-/ -> Decrease Speed

-> [Shf]-G : Gravity  [Continuous]
	Acceleration due to Gravity (in ms-2).
	<Keys> : G -> Increase Gravity  |  Shift-G -> Decrease Gravity

-> [Shf]-D : Drag  [Continuous]
	Drag Coefficient (in g/s), Positive value corresponds to drag, negative to push.
	<Keys> : D -> Increase Drag  |  Shift-D -> Decrease Drag

-> [Shf]-M : Mass  [Continuous]
	Mass of each pendulum Bob (in grams).
	<Keys> : M -> Increase Mass  |  Shift-M -> Decrease Mass

-> [Shf]-A : Start Angle  [Continuous]
	Start angle for each pendulum (in degrees).
	<Keys> : A -> Increase Start Angle  |  Shift-A -> Decrease Start Angle

-> [Shf]-P : Wave Period  [Continuous]
	Total time in which the Pendulum Wave completes one cycle (in secs).
	<Keys> : P -> Increase Wave Period  |  Shift-P -> Decrease Wave Period

-> [Shf]-O : Min Osc  [Continuous]
	Number of oscillations the first pendulum completes in Wave Period time.
	<Keys> : O -> Increase Min Osc  |  Shift-O -> Decrease Min Osc

-> [Shf]-I : Osc Step  [Continuous]
	Number of oscillations that a pendulum completes more than its predecessor.
	<Keys> : I -> Increase Osc Step  |  Shift-I -> Decrease Osc Step

-> Up/Down : Pitch-X  [Discrete]
	Controls the Camera PITCH (rotation about X-Axis).
	<Keys> : [UP | DOWN] arrow keys

-> Left/Right : Yaw-Y  [Discrete]
	Controls the Camera YAW (rotation about Y-Axis).
	<Keys> : [LEFT | RIGHT] arrow keys

-> Shf-Up/Down : Roll-Z  [Discrete]
	Controls the Camera ROLL (rotation about Z-Axis).
	<Keys> : Shift-[LEFT | RIGHT] arrow keys


## COMMANDS ----------------------------------------------------
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
