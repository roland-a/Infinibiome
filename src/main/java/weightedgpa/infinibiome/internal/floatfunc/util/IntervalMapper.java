package weightedgpa.infinibiome.internal.floatfunc.util;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

public final class IntervalMapper<T> {
    private final Supplier<T> defaultOutput;
    private final List<Function<Neighbors, Interval>> intervalProducers = new ArrayList<>();
    private final List<Function<Interval, T>> outputProducers = new ArrayList<>();

    public IntervalMapper(Supplier<T> defaultOutput) {
        this.defaultOutput = defaultOutput;
    }

    public IntervalMapper<T> addBranch(
        @Nullable
        Function<Neighbors, Interval> intervalProducer,
        Function<Interval, T> outputProducer
    ){
        intervalProducers.add(intervalProducer);
        outputProducers.add(outputProducer);

        return this;
    }

    public T run(double value){
        Interval[] intervals = new Interval[outputProducers.size()];

        while (!allInit(intervals)) {
            for (int i = 0; i < intervals.length; i++) {
                intervals[i] = intervalProducers.get(i)
                    .apply(
                        new Neighbors(i, intervals)
                    );

                if (intervals[i] != null && intervals[i].contains(value)) {
                    return outputProducers.get(i).apply(intervals[i]);
                }
            }
        }
        return defaultOutput.get();
    }

    private boolean allInit(Interval[] intervals){
        for (Interval i: intervals){
            if (i == null) return false;
        }
        return true;
    }


    public final class Neighbors{
        final int currPosition;
        final Interval[] intervals;

        private Neighbors(int currPosition, Interval[] intervals) {
            this.currPosition = currPosition;
            this.intervals = intervals;
        }

        @Nullable
        public Interval between() {
            Interval prev = getPrevInterval();

            if (prev == null) return null;

            Interval next = getNextInterval();

            if (next == null) return null;

            return new Interval(prev.getMax(), next.getMin());
        }

        @Nullable
        public Interval getNextInterval(){
            if (currPosition == intervals.length - 1) throw new RuntimeException("cant get between");

            if (intervals[currPosition + 1] == null) return null;

            return intervals[currPosition + 1];
        }

        @Nullable
        public Interval getPrevInterval(){
            if (currPosition == 0) throw new RuntimeException("cant get between");

            if (intervals[currPosition - 1] == null) return null;

            return intervals[currPosition - 1];
        }
    }
}
