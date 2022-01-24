package weightedgpa.infinibiome.internal.floatfunc.modifiers;

import weightedgpa.infinibiome.internal.floatfunc.util.Interval;
import weightedgpa.infinibiome.internal.floatfunc.FloatFunc;
import weightedgpa.infinibiome.internal.floatfunc.util.IntervalMapper;
import weightedgpa.infinibiome.internal.misc.MathHelper;

import java.util.ArrayList;
import java.util.List;

public final class IntervalMapperWrapper<I> implements FloatFunc<I> {
    private final FloatFunc<I> base;
    private final double defaultValue;
    private final List<Interval> intervals = new ArrayList<>();
    private final List<Outputs> outputs = new ArrayList<>();

    private Interval outputInterval;

    public IntervalMapperWrapper(FloatFunc<I> base) {
        this(base, 0f);
    }

    public IntervalMapperWrapper(FloatFunc<I> base, double defaultValue) {
        this.base = base;
        this.defaultValue = defaultValue;
        this.outputInterval = new Interval(defaultValue, defaultValue);
    }

    public IntervalMapperWrapper<I> addBranch(Interval captureInterval, double captureRangeMinConvert, double captureRangeMaxConvert){
        intervals.add(
            captureInterval
        );

        outputs.add(
            new Outputs(
                captureRangeMinConvert,
                captureRangeMaxConvert
            )
        );

        outputInterval = outputInterval.union(
            new Interval(captureRangeMinConvert, captureRangeMaxConvert)
        );

        return this;
    }

    @Override
    public double getOutput(I input) {
        IntervalMapper<Double> intervalMapper = new IntervalMapper(() -> defaultValue);

        double baseValue = base.getOutput(input);

        for (int i = 0; i < intervals.size(); i++){
            int i_ = i;

            intervalMapper.addBranch(
                __ -> intervals.get(i_),
                interval -> outputs.get(i_).convert(baseValue, interval)
            );
        }

        return intervalMapper.run(baseValue);
    }

    @Override
    public Interval getOutputInterval() {
        return outputInterval;
    }

    private static final class Outputs {
        private final double lowerValueConvert;
        private final double upperValueConvert;
    
        Outputs(double lowerValueConvert, double upperValueConvert) {
            this.lowerValueConvert = lowerValueConvert;
            this.upperValueConvert = upperValueConvert;
        }

        private double convert(double baseValue, Interval captureInterval) {
            return captureInterval.mapInterval(
                baseValue,
                lowerValueConvert,
                upperValueConvert
            );
        }
    }
}
