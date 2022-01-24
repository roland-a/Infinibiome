package weightedgpa.infinibiome.api.generators;

import org.apache.commons.lang3.ArrayUtils;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.Objects;

/**
 * Determines the order which resources are to be run
 */
public final class Timing implements Comparable<Timing>{
    private static final Timing ZERO = new Timing(0);

    private final int[] timings;

    private Timing(Timing superTimer, int subTiming) {
        if (subTiming == 0){
            this.timings = superTimer.timings;
        }
        else {
            this.timings = ArrayUtils.add(superTimer.timings, subTiming);
        }
    }

    private Timing(int timing) {
        this.timings = new int[]{timing};
    }

    /**
     * Returns the default timing.
     */
    public static Timing getDefault(){
        return ZERO;
    }

    public static Builder initBuilder(Timing timing) {
        return new Builder(timing);
    }

    public static Builder initBuilder() {
        return new Builder(null);
    }

    @SuppressWarnings({"UnnecessaryThis"})
    @Override
    public int compareTo(@NotNull Timing other) {
        int length = Math.max(this.timings.length, other.timings.length);

        for (int i = 0; i < length; i++){
            int comparison = Integer.compare(
                this.getSubTiming(i),
                other.getSubTiming(i)
            );

            if (comparison == 0) continue;

            return comparison;
        }
        return 0;
    }

    private int getSubTiming(int index){
        if (index < timings.length){
            return timings[index];
        }
        return 0;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Timing timing = (Timing) o;
        return Arrays.equals(timings, timing.timings);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(timings);
    }

    /**
     * Creates sub-timings that runs later than the previously created timing
     */
    public static final class Builder {
        @Nullable
        private final Timing superTimer;

        private int timing = 0;

        private Builder(@Nullable Timing superTimer) {
            this.superTimer = superTimer;
        }

        /**
         * Creates a new timing.
         * Each timing will be later than the previous one.
         */
        public Timing getNextTiming(){
            Timing result;

            if (superTimer == null) {
                result = new Timing(
                    timing
                );
            }
            else {
                result = new Timing(
                    superTimer,
                    timing
                );
            }

            timing += 1;

            return result;
        }
    }

}