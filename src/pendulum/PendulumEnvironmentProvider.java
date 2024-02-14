package pendulum;

public interface PendulumEnvironmentProvider {
        
        /**
         * @return acceleration due to gravity, in ms<sup>-2</sup> (SI units)
         * */
        float gravity();

        /**
         * @return medium drag coefficient, in kg/s (SI units). Positive value -> drag, Negative value -> push
         * */
        float drag();
        
    }