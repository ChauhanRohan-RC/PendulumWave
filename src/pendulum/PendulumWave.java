package pendulum;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import processing.core.PApplet;
import util.U;

import java.util.ArrayList;
import java.util.function.Consumer;


/**
 * Creates a pendulum wave. <br><br>
 * A pendulum wave is a series of pendulums, with each completing one more oscillation (in a given time, say t) than the previous one. <br>
 * So the first pendulum is the slowest (highest time period T), and the successive pendulums gets progressively faster (T decreases). <br><br>
 * EX. let t = 60s, and the first pendulum completes 20 oscillations within this time. Then the successive pendulum will complete 21, 22, 23... oscillations in 60s. <br><br>
 * Increase of 1 oscillation per pendulum is however not necessary. The increase in oscillations per pendulum (OSCILLATION STEP) can be any integer or floating point number.<br>
 * <br>
 * <strong>
 *     Effective Wave Period = Internal Wave Period / Oscillation Step
 * <strong/>
 *
 * @see #getInternalWavePeriod()
 * @see #getEffectiveWavePeriod()
 * */
public class PendulumWave implements PendulumEnvironmentProvider, Pendulum.Listener {

    public interface Listener extends Pendulum.Listener {

        void onPendulumCountChanged(@NotNull PendulumWave pendulumWave, int prevCount, int newCount);

        void onSpeedChanged(@NotNull PendulumWave pendulumWave, float prevSpeed, float newSpeed);
        
        void onIsPausedChanged(@NotNull PendulumWave pendulumWave, boolean isPaused);

    }



    /**
     * Effective Wave Period is the actual time in which a pendulum wave completes one cycle i.e. all pendulums come in sync. This depends inversely on the {@link #oscillationsStepPerPendulum oscillation_step} per pendulum. Thus, it may be different from the {@link #internalWavePeriod internal_wave_period} which is only defined for {@link #oscillationsStepPerPendulum oscillation_step} = 1.
     * <br><br>
     * <strong>
     *     Effective Wave Period = Internal Wave Period / Oscillation Step
     * </strong>
     *
     * @param internalWavePeriod internal wave period, defined only when oscillation_step_per_pendulum = 1
     * @param oscillationsStepPerPendulum oscillation_step per pendulum
     * @return The Effective Wave Period = internal_wave_period / oscillation_step
     * **/
    public static float effectiveWavePeriod(float internalWavePeriod, float oscillationsStepPerPendulum) {
        return oscillationsStepPerPendulum != 0? internalWavePeriod / Math.abs(oscillationsStepPerPendulum): 0;
    }

    /**
     * Internal Wave Period is the time in which a pendulum wave would have completed one cycle (i.e. all pendulums come in sync) if the {@link #oscillationsStepPerPendulum oscillation_step_per_pendulum} = 1.
     * <br><br>
     * <strong>
     *     Internal Wave Period = Effective Wave Period * Oscillation Step
     * </strong>
     *
     * @param effectiveWavePeriod The effective wave period
     * @param oscillationsStepPerPendulum oscillation_step per pendulum
     * @return The Internal Wave Period = effective_wave_period * oscillation_step
     * **/
    public static float internalWavePeriod(float effectiveWavePeriod, float oscillationsStepPerPendulum) {
        return effectiveWavePeriod * Math.abs(oscillationsStepPerPendulum);
    }



    public static final int DEFAULT_INITIAL_PENDULUM_COUNT = 10;
    private static final int DEFAULT_PENDULUM_COUNT__STEP = 1;

    public static final float DEFAULT_PENDULUM_MASS = 0.040f;       // mass of each pendulum bob (in kg)
    private static final float DEFAULT_PENDULUM_MASS__STEP = 0.00002f;       // unit change (in kg)

    public static final float DEFAULT_START_ANGLE = (float) Math.toRadians(30);        // Start angle of each pendulum (in radians)
    private static final float DEFAULT_START_ANGLE__STEP = (float) Math.toRadians(0.2);        // unit change (in radians)

    public static final float DEFAULT_INTERNAL_WAVE_PERIOD_SECS = 30f;             // total time (in s) for the entire wave to complete one cycle, ex 60s
    private static final float DEFAULT_INTERNAL_WAVE_PERIOD_SECS__STEP = 0.02f;       // unit step (in s)

    public static final float DEFAULT_OSCILLATIONS_MIN = 4f;                //  number of oscillations of the first pendulum in wave period time
    private static final float DEFAULT_OSCILLATIONS_MIN__STEP = 0.05f;       //  unit change

    public static final float DEFAULT_OSCILLATIONS_STEP_PER_PENDULUM = 0.2f;       // number of oscillations that a certain pendulum completes more that the previous pendulum i.e = num_oscillation(i + 1) - num_oscillation(i)
    private static final float DEFAULT_OSCILLATIONS_STEP_PER_PENDULUM__STEP = 0.01f;       // unit change

    public static final float DEFAULT_EFFECTIVE_WAVE_PERIOD_SECS = effectiveWavePeriod(DEFAULT_INTERNAL_WAVE_PERIOD_SECS, DEFAULT_OSCILLATIONS_STEP_PER_PENDULUM);
    private static final float DEFAULT_EFFECTIVE_WAVE_PERIOD_SECS__STEP = DEFAULT_INTERNAL_WAVE_PERIOD_SECS__STEP;       // unit step (in s)

    public static final float DEFAULT_GRAVITY = 9.8f;               // Acceleration due to gravity, in ms-2
    private static final float DEFAULT_GRAVITY__STEP = 0.002f;               // discrete step in gravity, in ms-2

    public static final float DEFAULT_DRAG = 0.0f;                  // drag coefficient, in kg/s. Positive -> drag, negative -> push
    private static final float DEFAULT_DRAG__STEP = 0.00002f;                  // drag coefficient step, in kg/s. Positive -> drag, negative -> push

    public static final float SPEED_MIN = 0.02f;
    public static final float SPEED_MAX = 50f;
    public static final float DEFAULT_SPEED = 1f;
    public static final float DEFAULT_SPEED__STEP = 0.005f;            // speed step

    public static float speedToPercent(float speed) {
        return U.norm(speed, SPEED_MIN, SPEED_MAX) * 100;
    }

    public static float percentToSpeed(float percent) {
        return U.lerp(SPEED_MIN, SPEED_MAX, percent / 100);
    }


    private final int initialPendulumCount;

    /**
     * Total time (in s) for the entire wave to complete one cycle, ex 60s
     * */
    private float internalWavePeriod = DEFAULT_INTERNAL_WAVE_PERIOD_SECS;

    /**
     * number of oscillations of the first pendulum in {@link #internalWavePeriod wave period} time
     * */
    private float minOscillationsInWavePeriod = DEFAULT_OSCILLATIONS_MIN;

    /**
     * number of oscillations that a pendulum completes more than its predecessor i.e. num_oscillations(i'th pendulum) - num_oscillations(i - 1 'th pendulum)
     * */
    private float oscillationsStepPerPendulum = DEFAULT_OSCILLATIONS_STEP_PER_PENDULUM;

    /**
     * Acceleration due to gravity, in ms-2
     * */
    private float gravity = DEFAULT_GRAVITY;     // Acceleration due to gravity, in ms-2

    /**
     * Drag coefficient, in kg/s. Positive -> drag, negative -> push
     * */
    private float drag = DEFAULT_DRAG;           // drag coefficient, in kg/s

    /**
     * Mass of each pendulum bob, in kg
     * */
    private float pendulumMass = DEFAULT_PENDULUM_MASS;

    /**
     * Starting angle of each pendulum, in radians
     * */
    private float pendulumStartAngle = DEFAULT_START_ANGLE;

    /**
     * Pendulums in the wave
     * */
    @NotNull
    private final ArrayList<Pendulum> pendulums = new ArrayList<>();

    /**
     * Speed of the simulation. This is multiplied to the time step
     * */
    private float speed = DEFAULT_SPEED;


    // Minimum and maximum Pendulum lengths
    private Pendulum shortestPendulum, longestPendulum;

    // Last update time (in ns)
    private long mLastUpdateNs = -1;
    private boolean mPaused;

    private double mElapsedSecs = 0;

//    private long mStartNs = -1;
//    private long mPausedNs = -1;

    @Nullable
    private Listener mListener;

    public PendulumWave(int initialPendulumCount) {
        this.initialPendulumCount = initialPendulumCount;
        setPendulumCount(initialPendulumCount, false);
    }

    public PendulumWave() {
        this(DEFAULT_INITIAL_PENDULUM_COUNT);
    }


    public int getInitialPendulumCount() {
        return initialPendulumCount;
    }

    public Listener getListener() {
        return mListener;
    }

    public PendulumWave setListener(@Nullable Listener listener) {
        mListener = listener;
        return this;
    }

    public PendulumWave updatePendulums() {
        if (isPaused())
            return this;
        
        final long lastNs = mLastUpdateNs;
        if (lastNs != -1) {
            final float dt = (System.nanoTime() - lastNs) * 1e-9f * speed;
            mElapsedSecs += dt;

            pendulums.forEach(p -> p.update(this, dt));
        }

        final long nowNs = System.nanoTime();
        mLastUpdateNs = nowNs;
//        if (mStartNs == -1) {
//            // start
//            mStartNs = nowNs;
//        }

        return this;
    }

    public PendulumWave drawPendulums(@NotNull PApplet p, @NotNull PendulumStyleProvider styleProvider) {
        for (int i = pendulums.size() - 1; i >= 0; i--) {
            pendulums.get(i).draw(p, styleProvider);
        }

        return this;
    }




    /* Pendulum Physics ........................................................... */

    /**
     * @return time stamp of the last call to {@link #updatePendulums()}, in nanoseconds or {@code -1}
     *
     * @see System#nanoTime()
     * */
    public long getLastUpdateNanoTime() {
        return mLastUpdateNs;
    }

//    /**
//     * @return elapsed time in nanoseconds, or {@code 0} if not started yet
//     * */
//    public long getElapsedNanoSeconds() {
//        if (mStartNs == -1)
//            return 0;
//
//        if (isPaused() && mPausedNs != -1) {
//            return mPausedNs - mStartNs;
//        }
//
//        return System.nanoTime() - mStartNs;
//    }

    /**
     * @return elapsed time in seconds, or {@code 0} if not started yet
     * */
    public double getElapsedSeconds() {
        return mElapsedSecs;
    }

    @Nullable
    public Pendulum getShortestPendulum() {
        return shortestPendulum;
    }

    @Nullable
    public Pendulum getLongestPendulum() {
        return longestPendulum;
    }

    private float calculatePendulumLength(int i) {
        return U.sq(internalWavePeriod / (2 * ((float) Math.PI) * (minOscillationsInWavePeriod + i * oscillationsStepPerPendulum))) * gravity;
    }

    private void updatePendulumsLength(boolean calculateAndSet) {
        Pendulum shortest = null, longest = null;

        for (int i=0; i < pendulums.size(); i++) {
            final Pendulum p = pendulums.get(i);
            final float len;

            if (calculateAndSet) {
                len = calculatePendulumLength(i);
                p.setLength(len);
            } else {
                len = p.getLength();
            }

            if (i == 0) {
                shortest = longest = p;
            } else if (len < shortest.getLength()) {
                shortest = p;
            } else if (len > longest.getLength()) {
                longest = p;
            }
        }

        shortestPendulum = shortest;
        longestPendulum = longest;
    }

    private void updatePendulumsLength() {
        updatePendulumsLength(true);
    }

    private void updatePendulumsMass() {
        pendulums.forEach(p -> p.setMass(pendulumMass));
    }

    private void updatePendulumsStartAngle() {
        pendulums.forEach(p -> p.setStartAngle(pendulumStartAngle));
    }


    private void onPendulumMassChanged(float prev, float mass, boolean resetPendulumsState) {
        updatePendulumsMass();

        if (resetPendulumsState) {
            resetPendulumsState();
        }
    }

    public float getPendulumMass() {
        return pendulumMass;
    }

    public PendulumWave setPendulumMass(float pendulumMass, boolean resetPendulumsState) {
        if (pendulumMass <= 0) {
            throw new IllegalArgumentException("Pendulum mass should be > 0, given: " + pendulumMass);
        }

        if (this.pendulumMass == pendulumMass)
            return this;

        final float prev = this.pendulumMass;
        this.pendulumMass = pendulumMass;
        onPendulumMassChanged(prev, pendulumMass, resetPendulumsState);
        return this;
    }

    public PendulumWave stepPendulumMass(boolean inc, boolean resetPendulumsState) {
        float _new = getPendulumMass() + (inc? 1: -1) * DEFAULT_PENDULUM_MASS__STEP;
        if (_new > 0)
            setPendulumMass(_new, resetPendulumsState);

        return this;
    }


    private void onPendulumStartAngleChanged(float prev, float startAngle, boolean resetPendulumsState) {
        updatePendulumsStartAngle();

        if (resetPendulumsState) {
            resetPendulumsState();
        }
    }

    public float getPendulumStartAngle() {
        return pendulumStartAngle;
    }

    public PendulumWave setPendulumStartAngle(float pendulumStartAngle, boolean resetPendulumsState) {
        pendulumStartAngle %= (float) (Math.PI * 2);

        if (this.pendulumStartAngle == pendulumStartAngle)
            return this;

        final float prev = this.pendulumStartAngle;
        this.pendulumStartAngle = pendulumStartAngle;
        onPendulumStartAngleChanged(prev, pendulumStartAngle, resetPendulumsState);
        return this;
    }

    public PendulumWave stepPendulumStartAngle(boolean inc, boolean resetPendulumsState) {
        return setPendulumStartAngle(getPendulumStartAngle() + (inc? 1: -1) * DEFAULT_START_ANGLE__STEP, resetPendulumsState);
    }


    private void onGravityChanged(float prev, float gravity, boolean resetPendulumsState) {
        updatePendulumsLength();

        if (resetPendulumsState) {
            resetPendulumsState();
        }
    }

    @Override
    public float gravity() {
        return gravity;
    }

    public PendulumWave setGravity(float gravity, boolean resetPendulumsState) {
        if (this.gravity == gravity)
            return this;

        final float prev = this.gravity;
        this.gravity = gravity;
        onGravityChanged(prev, gravity, resetPendulumsState);
        return this;
    }

    public PendulumWave stepGravity(boolean inc, boolean resetPendulumsState) {
        return setGravity(gravity() + (inc? 1: -1) * DEFAULT_GRAVITY__STEP, resetPendulumsState);
    }

    private void onDragChanged(float prev, float drag, boolean resetPendulumsState) {
        if (resetPendulumsState) {
            resetPendulumsState();
        }
    }

    @Override
    public float drag() {
        return drag;
    }

    public PendulumWave setDrag(float drag, boolean resetPendulumsState) {
        if (this.drag == drag)
            return this;

        final float prev = this.drag;
        this.drag = drag;
        onDragChanged(prev, drag, resetPendulumsState);
        return this;
    }

    public PendulumWave stepDrag(boolean inc, boolean resetPendulumsState) {
        return setDrag(drag() + (inc? 1: -1) * DEFAULT_DRAG__STEP, resetPendulumsState);
    }


    private void onInternalWavePeriodChanged(float prev, float wavePeriod, boolean resetPendulumsState) {
        updatePendulumsLength();

        if (resetPendulumsState) {
            resetPendulumsState();
        }
    }

    /**
     * @return Internal Wave Period (in s), corresponding to {@link #oscillationsStepPerPendulum oscillation_step} = 1
     *
     * @see #getEffectiveWavePeriod()
     * */
    public float getInternalWavePeriod() {
        return internalWavePeriod;
    }

    /**
     * Sets the internal Wave Period, which is defined when {@link #oscillationsStepPerPendulum oscillation_step} = 1.
     *
     * @param internalWavePeriod Wave Period (in s), corresponding to {@link #oscillationsStepPerPendulum oscillation_step} = 1
     * @param resetPendulumsState true to reset all pendulums
     *
     * @see #getEffectiveWavePeriod()
     * */
    public PendulumWave setInternalWavePeriod(float internalWavePeriod, boolean resetPendulumsState) {
        if (internalWavePeriod <= 0) {
            throw new IllegalArgumentException("Internal Wave period must be > 0, given: " + internalWavePeriod);
        }

        if (this.internalWavePeriod == internalWavePeriod)
            return this;

        final float prev = this.internalWavePeriod;
        this.internalWavePeriod = internalWavePeriod;
        onInternalWavePeriodChanged(prev, internalWavePeriod, resetPendulumsState);
        return this;
    }

    public PendulumWave stepInternalWavePeriod(boolean inc, boolean resetPendulumsState) {
        final float _new = getInternalWavePeriod() + (inc? 1: -1) * DEFAULT_INTERNAL_WAVE_PERIOD_SECS__STEP;
        if (_new > 0) {
            setInternalWavePeriod(_new, resetPendulumsState);
        }

        return this;
    }



    private void onMinOscillationsInWavePeriodChanged(float prev, float minOscillationsInWavePeriod, boolean resetPendulumsState) {
        updatePendulumsLength();

        if (resetPendulumsState) {
            resetPendulumsState();
        }
    }

    public float getMinOscillationsInWavePeriod() {
        return minOscillationsInWavePeriod;
    }

    public PendulumWave setMinOscillationsInWavePeriod(float minOscillationsInWavePeriod, boolean resetPendulumsState) {
        if (minOscillationsInWavePeriod <= 0) {
            throw new IllegalArgumentException("Minimum Oscillation In Wave Period must be > 0, given: " + minOscillationsInWavePeriod);
        }

        if (this.minOscillationsInWavePeriod == minOscillationsInWavePeriod)
            return this;

        final float prev = this.minOscillationsInWavePeriod;
        this.minOscillationsInWavePeriod = minOscillationsInWavePeriod;
        onMinOscillationsInWavePeriodChanged(prev, minOscillationsInWavePeriod, resetPendulumsState);
        return this;
    }

    public PendulumWave stepMinOscillationsInWavePeriod(boolean inc, boolean resetPendulumsState) {
        final float _new = getMinOscillationsInWavePeriod() + (inc? 1: -1) * DEFAULT_OSCILLATIONS_MIN__STEP;
        if (_new > 0) {
            setMinOscillationsInWavePeriod(_new, resetPendulumsState);
        }

        return this;
    }



    private void onOscillationsStepPerPendulumChanged(float prev, float oscillationsStepPerPendulum, boolean resetPendulumsState) {
        updatePendulumsLength();

        if (resetPendulumsState) {
            resetPendulumsState();
        }
    }

    public float getOscillationsStepPerPendulum() {
        return oscillationsStepPerPendulum;
    }

    public PendulumWave setOscillationsStepPerPendulum(float oscillationsStepPerPendulum, boolean resetPendulumsState) {
        if (oscillationsStepPerPendulum <= 0) {
            throw new IllegalArgumentException("Oscillation step per pendulum must be > 0, given: " + oscillationsStepPerPendulum);
        }

        if (this.oscillationsStepPerPendulum == oscillationsStepPerPendulum)
            return this;

        final float prev = this.oscillationsStepPerPendulum;
        this.oscillationsStepPerPendulum = oscillationsStepPerPendulum;
        onOscillationsStepPerPendulumChanged(prev, oscillationsStepPerPendulum, resetPendulumsState);
        return this;
    }

    public PendulumWave stepOscillationsStepPerPendulum(boolean inc, boolean resetPendulumsState) {
        final float _new = getOscillationsStepPerPendulum() + (inc? 1: -1) * DEFAULT_OSCILLATIONS_STEP_PER_PENDULUM__STEP;
        if (_new > 0) {
            setOscillationsStepPerPendulum(_new, resetPendulumsState);
        }

        return this;
    }




    /**
     * Effective Wave Period is the actual time in which this pendulum wave completes one cycle i.e. all pendulums come in sync. This depends inversely on the {@link #oscillationsStepPerPendulum oscillation_step} per pendulum. Thus, it may be different from the {@link #internalWavePeriod internal_wave_period} which is only defined for {@link #oscillationsStepPerPendulum oscillation_step} = 1.
     * <br><br>
     * <strong>
     *     Effective Wave Period = Internal Wave Period / Oscillation Step
     * </strong>
     *
     * @return The effective wave period of this pendulum wave, in seconds.
     *
     * @see #effectiveWavePeriod(float, float) effectiveWavePeriod(internalWavePeriod, oscillationsStepPerPendulum)
     * */
    public float getEffectiveWavePeriod() {
        return effectiveWavePeriod(internalWavePeriod, oscillationsStepPerPendulum);
    }

    /**
     * Sets the effective wave period. It just calls {@link #setInternalWavePeriod(float, boolean)}.
     *
     * @param effectiveWavePeriod effective wave period, in seconds
     * @param resetPendulumsState true to reset all pendulums
     *
     * @see #internalWavePeriod(float, float) internalWavePeriod(effectiveWavePeriod, oscillationsStepPerPendulum)
     * @see #getEffectiveWavePeriod()
     * @see #setInternalWavePeriod(float, boolean)
     * */
    public PendulumWave setEffectiveWavePeriod(float effectiveWavePeriod, boolean resetPendulumsState) {
        if (effectiveWavePeriod <= 0) {
            throw new IllegalArgumentException("Effective Wave period must be > 0, given: " + internalWavePeriod);
        }

        return setInternalWavePeriod(internalWavePeriod(effectiveWavePeriod, oscillationsStepPerPendulum), resetPendulumsState);
    }

    public PendulumWave stepEffectiveWavePeriod(boolean inc, boolean resetPendulumsState) {
        final float _new = getEffectiveWavePeriod() + (inc? 1: -1) * DEFAULT_EFFECTIVE_WAVE_PERIOD_SECS__STEP;
        if (_new > 0) {
            setEffectiveWavePeriod(_new, resetPendulumsState);
        }

        return this;
    }



    private void onSpeedChanged(float prevSpeed, float newSpeed) {
        if (mListener != null) {
            mListener.onSpeedChanged(this, prevSpeed, newSpeed);
        }
    }

    public float getSpeed() {
        return speed;
    }

    public float getSpeedPercent() {
        return speedToPercent(speed);
    }

    /**
     * @param speed speed to set
     * @return the new speed, which may be different from the requested speed
     * */
    public float setSpeed(float speed) {
        speed = U.constrain(speed, SPEED_MIN, SPEED_MAX);
        if (this.speed == speed)
            return speed;

        final float prev = this.speed;
        this.speed = speed;
        onSpeedChanged(prev, speed);
        return speed;
    }

    /**
     * @param percent percentage of speed, from 0-100%
     * @return the new speed percentage, which may be different from the requested speed percent
     * */
    public float setSpeedPercent(float percent) {
        setSpeed(percentToSpeed(percent));
        return getSpeedPercent();
    }

    public PendulumWave stepSpeed(boolean inc) {
        setSpeed(getSpeed() + (inc? 1: -1) * DEFAULT_SPEED__STEP);
        return this;
    }

    
    private void onIsPausedChanged(boolean isPaused) {
        mLastUpdateNs = -1;     // Invalidate

//        if (mStartNs != -1) {
//            if (isPaused) {
//                mPausedNs = System.nanoTime();
//            } else if (mPausedNs != -1) {
//                mStartNs += (System.nanoTime() - mPausedNs);
//                mPausedNs = -1;
//            }
//        } else {
//            mPausedNs = -1;
//        }
        
        if (mListener != null) {
            mListener.onIsPausedChanged(this, isPaused);
        }
    }

    public boolean isPaused() {
        return mPaused;
    }
    
    public PendulumWave setPause(boolean pause) {
        if (mPaused == pause)
            return this;
        
        pendulums.forEach(pause? Pendulum::pause: Pendulum::resume);
        mPaused = pause;
        onIsPausedChanged(pause);
        return this;
    }
    
    public PendulumWave togglePlayPause() {
        return setPause(!isPaused());
    }
    
    
    private void onPendulumsStateReset() {
        mLastUpdateNs = -1;         // invalidate, very imp
        mElapsedSecs = 0;           // reset elapsed secs

//        mStartNs = -1;
//        mPausedNs = -1;
    }

    public PendulumWave resetPendulumsState() {
        pendulums.forEach(Pendulum::resetState);
        onPendulumsStateReset();
        return this;
    }

    public PendulumWave resetPendulumCount(boolean resetPendulumsState) {
        return setPendulumCount(initialPendulumCount, resetPendulumsState);
    }

    public PendulumWave resetSimulation(boolean resetPendulumCount, boolean resetPendulumsState) {
        // Environment
        setSpeed(DEFAULT_SPEED);
        setGravity(DEFAULT_GRAVITY, false);
        setDrag(DEFAULT_DRAG, false);

        // Pendulum Wave
        setPendulumMass(DEFAULT_PENDULUM_MASS, false);
        setPendulumStartAngle(DEFAULT_START_ANGLE, false);
        setInternalWavePeriod(DEFAULT_INTERNAL_WAVE_PERIOD_SECS, false);
        setMinOscillationsInWavePeriod(DEFAULT_OSCILLATIONS_MIN, false);
        setOscillationsStepPerPendulum(DEFAULT_OSCILLATIONS_STEP_PER_PENDULUM, false);

        // Pendulums Count
        if (resetPendulumCount) {
            resetPendulumCount(false);
        }

        // Pendulums State
        if (resetPendulumsState) {
            resetPendulumsState();
        }

        return this;
    }


    /* Pendulums Count ............................................. */

    @NotNull
    private Pendulum createPendulum(int id, float length) {
        return new Pendulum(id, pendulumMass, length, pendulumStartAngle).setListener(this);
    }

    private void detachPendulum(@Nullable Pendulum p) {
        if (p == null)
            return;

        if (p.getListener() == this) {
            p.setListener(null);
        }
    }

    public int pendulumCount() {
        return pendulums.size();
    }

    public Pendulum pendulumAt(int index) {
        return pendulums.get(index);
    }

    public PendulumWave forEachPendulum(@NotNull Consumer<? super Pendulum> action) {
        pendulums.forEach(action);
        return this;
    }

    private void onPendulumCountChanged(int prevCount, int newCount, boolean resetPendulumsState) {
        updatePendulumsLength();

        if (mListener != null) {
            mListener.onPendulumCountChanged(this, prevCount, newCount);
        }

        if (resetPendulumsState) {
            resetPendulumsState();
        }
    }

    public PendulumWave setPendulumCount(int count, boolean resetPendulumsState) {
        if (count < 1) {
            throw new IllegalArgumentException("Total number of pendulums must be >= 1. Given: " + count);
        }

        final int prev = pendulums.size();
        if (prev == count)
            return this;

        if (prev < count) {
            // add pendulums
            for (int i=prev; i < count; i++) {
                pendulums.add(createPendulum(i, 0 /* to be updated */));
            }
        } else {
            for (int i=prev - 1; i >= count; i--) {
                detachPendulum(pendulums.remove(i));
            }
        }

        onPendulumCountChanged(prev, pendulums.size(), resetPendulumsState);
        return this;
    }

    public PendulumWave setPendulumCount(int count) {
        return setPendulumCount(count, true);
    }

    public PendulumWave stepPendulumCount(boolean inc, boolean resetPendulumsState) {
        final int _new = pendulumCount() + (inc? 1: -1) * DEFAULT_PENDULUM_COUNT__STEP;
        if (_new >= 1) {
            setPendulumCount(_new, resetPendulumsState);
        }

        return this;
    }


    /* Pendulum Listener ............................................................. */

    @Override
    public void onPendulumLengthChanged(@NotNull Pendulum p, float prevLength, float newLength) {
        if (mListener != null) {
            mListener.onPendulumLengthChanged(p, prevLength, newLength);
        }
    }

    @Override
    public void onPendulumAngleChanged(@NotNull Pendulum p, float prevAngle, float newAngle) {
        if (mListener != null) {
            mListener.onPendulumAngleChanged(p, prevAngle, newAngle);
        }
    }

    @Override
    public void onPendulumHighlightChanged(@NotNull Pendulum p, boolean highlight) {
        if (mListener != null) {
            mListener.onPendulumHighlightChanged(p, highlight);
        }
    }

}


