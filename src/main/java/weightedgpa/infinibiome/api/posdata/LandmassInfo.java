package weightedgpa.infinibiome.api.posdata;

/**
 * A sum type representing whether a location is either land, beach, or ocean.
 */
public abstract class LandmassInfo {
    private LandmassInfo(){}

    /**
     * The percentage of how much it is transitioning into a beach.
     * If zero, then it is not transitioning into a beach.
     * The closer to one, the closer it is to becoming a beach.
     */
    public abstract double getTransitionToBeach();

    public boolean isLand(){
        return this instanceof Land;
    }

    public boolean isBeach(){
        return this instanceof Beach;
    }

    public boolean isOcean(){
        return this instanceof Ocean;
    }

    public static final class Land extends LandmassInfo {
        private final double transitionToBeach;

        public Land(double transitionToBeach) {
            this.transitionToBeach = transitionToBeach;
        }

        @Override
        public double getTransitionToBeach() {
            return transitionToBeach;
        }

        @Override
        public String toString() {
            return "Land{" +
                "transitionToBeach=" + transitionToBeach +
                '}';
        }
    }

    public static final class Beach extends LandmassInfo {
        private final double transitionToLand;

        public Beach(double transitionToLand){
            this.transitionToLand = transitionToLand;
        }

        /**
         * The closer this is to one, the closer it is to land.
         * The closer this is to zero, the closer it is to the ocean.
         */
        public double getTransitionToLand() {
            return transitionToLand;
        }

        public double getTransitionToOcean() {
            return 1 - transitionToLand;
        }

        @Override
        public double getTransitionToBeach() {
            return 1;
        }

        @Override
        public String toString() {
            return "Beach{" + "transitionToLand=" + transitionToLand + '}';
        }
    }

    public static final class Ocean extends LandmassInfo {
        private final double transitionToBeach;

        public Ocean(double transitionToBeach) {
            this.transitionToBeach = transitionToBeach;
        }

        /**
         * The percentage of how much it is transitioning into a beach.
         * If zero, then it is not transitioning into a beach.
         * The closer to one, the closer it is to becoming a beach.
         */
        @Override
        public double getTransitionToBeach() {
            return transitionToBeach;
        }

        @Override
        public String toString() {
            return "Ocean{" +
                "transitionToBeach=" + transitionToBeach +
                '}';
        }
    }
}
