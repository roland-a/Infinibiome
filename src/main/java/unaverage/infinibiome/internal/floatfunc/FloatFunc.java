package weightedgpa.infinibiome.internal.floatfunc;

import org.apache.commons.lang3.Validate;
import weightedgpa.infinibiome.api.generators.Seed;
import weightedgpa.infinibiome.api.pos.BlockPos2D;
import weightedgpa.infinibiome.api.pos.IntPosInfo;
import weightedgpa.infinibiome.internal.floatfunc.generators.RandomGen;
import weightedgpa.infinibiome.internal.floatfunc.util.Interval;
import weightedgpa.infinibiome.internal.floatfunc.util.PercentileTable;
import weightedgpa.infinibiome.internal.minecraftImpl.commands.DebugCommand;
import weightedgpa.infinibiome.internal.misc.MathHelper;

import java.util.Random;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;

//import weightedgpa.infinibiome.internal.command.DebugCommand;

/**
 * A function that takes in an input and returns a float value restrained to an interval
 * @param <I>
 */
@FunctionalInterface
public interface FloatFunc<I>{
    /**
     * Runs this function with an input then returns a float output.
     *
     * @apiNote
     * Output must be within the range set by {@link #getOutputInterval()}.
     * This must always return the same output for the same input.
     */
    double getOutput(I input);

    /**
     * Returns the interval that the output must be constrained to
     * This must always return the same interval.
     */
    default Interval getOutputInterval(){
        return Interval.ALL_VALUES;
    }

    /**
     * Returns a FloatFunc that returns the same value no matter what input it takes
     */
    static <I> FloatFunc<I> constFunc(double value){
        Interval outputInterval = new Interval(value, value);

        return new FloatFunc<I>() {
            @Override
            public double getOutput(I input) {
                return value;
            }

            @Override
            public Interval getOutputInterval() {
                return outputInterval;
            }
        };
    }

    default FloatFunc<I> mapInterval(Interval newInterval){
        if (newInterval.getSize() == 0) return constFunc(newInterval.getMin());

        Validate.isTrue(getOutputInterval().canMapInterval());

        return new FloatFunc<I>() {
            @Override
            public double getOutput(I input) {
                double baseOutput = FloatFunc.this.getOutput(input);

                return FloatFunc.this.getOutputInterval().mapInterval(baseOutput, newInterval);
            }

            @Override
            public Interval getOutputInterval() {
                return newInterval;
            }
        };
    }

    default IntFunc<I> mapToIntInterval(int min, int max){
        Validate.isTrue(min <= max);
        Validate.isTrue(getOutputInterval().canMapInterval());

        Interval outputInterval = new Interval(min, max);

        return new IntFunc<I>() {
            @Override
            public int getIntOutput(I input) {
                return FloatFunc.this.getOutputInterval().mapToIntInterval(
                    FloatFunc.this.getOutput(input),
                    min, max
                );
            }

            @Override
            public Interval getOutputInterval() {
                return outputInterval;
            }
        };
    }

    default FloatFunc<I> mapToSubInterval(
        FloatFunc<I> intervalPosition,
        FloatFunc<I> intervalLength
    ){
        Validate.isTrue(
            Interval.PERCENT.containsAll(intervalPosition.getOutputInterval())
        );
        Validate.isTrue(
            intervalLength.getOutputInterval().getMin() >= 0
        );
        Validate.isTrue(
            intervalLength.getOutputInterval().getMax() <= getOutputInterval().getSize()
        );

        return new FloatFunc<I>() {
            @Override
            public double getOutput(I input) {
                Interval newInterval = FloatFunc.this.getOutputInterval()
                    .subInterval(
                        intervalLength.getOutput(input),
                        intervalPosition.getOutput(input)
                    );

                return FloatFunc.this.getOutputInterval().mapInterval(
                    FloatFunc.this.getOutput(input),
                    newInterval
                );
            }

            @Override
            public Interval getOutputInterval() {
                return FloatFunc.this.getOutputInterval();
            }
        };
    }

    default <NI> FloatFunc<NI> mapInput(Function<NI, I> mapFunc){
        return new FloatFunc<NI>() {
            @Override
            public double getOutput(NI input) {
                I baseInput = mapFunc.apply(input);

                return FloatFunc.this.getOutput(baseInput);
            }

            @Override
            public Interval getOutputInterval() {
                return FloatFunc.this.getOutputInterval();
            }
        };
    }

    /** Applies {@link MathHelper#skew(float, float)}
     */
    default FloatFunc<I> skew(FloatFunc<I> skewFunc){
        Validate.isTrue(getOutputInterval().canMapInterval());

        if (skewFunc.getOutputInterval().isConstant() && skewFunc.getOutputInterval().getMin() == 0){
            return this;
        }

        if (getOutputInterval().equals(Interval.PERCENT)){
            return new FloatFunc<I>() {
                @Override
                public double getOutput(I input) {
                    return MathHelper.skew(
                        FloatFunc.this.getOutput(input),
                        skewFunc.getOutput(input)
                    );

                }

                @Override
                public Interval getOutputInterval() {
                    return FloatFunc.this.getOutputInterval();
                }
            };
        }

        return new FloatFunc<I>() {
            @Override
            public double getOutput(I input) {
                double baseOutput = FloatFunc.this.getOutput(input);

                double baseOutputPercent;

                baseOutputPercent = FloatFunc.this.getOutputInterval().mapInterval(baseOutput, Interval.PERCENT);

                baseOutputPercent = MathHelper.skew(baseOutputPercent, skewFunc.getOutput(input));

                return Interval.PERCENT.mapInterval(baseOutputPercent, FloatFunc.this.getOutputInterval());
            }

            @Override
            public Interval getOutputInterval() {
                return FloatFunc.this.getOutputInterval();
            }
        };
    }

    /**
     * Applies {@link MathHelper#randomRound(float, Random)} on a FloatFunc's output
     *
     * The Random parameter is generated from a {@link RandomGen}
     */
    default IntFunc<I> randomRound(Seed seed, IntPosInfo<I> posInfo){
        seed = seed.newSeed("randomRound");

        Validate.isTrue(
            getOutputInterval().getMin() >= 0
        );

        RandomGen randomProducer = new RandomGen(seed);

        Interval outputInterval = new Interval(
            0,
            StrictMath.ceil(getOutputInterval().getMax() * 2)
        );

        return new IntFunc<I>() {
            @Override
            public int getIntOutput(I input) {
                Random random = randomProducer.getRandom(
                    posInfo.getIntX(input),
                    posInfo.getIntZ(input)
                );

                return MathHelper.randomRound(
                    FloatFunc.this.getOutput(input),
                    random
                );
            }

            @Override
            public Interval getOutputInterval() {
                return outputInterval;
            }
        };
    }

    /**
     * Applies {@link MathHelper#randomBool(float, Random)} to the FloatFunc's output.
     */
    default Predicate<I> randomBool(IntPosInfo<I> posInfo, Seed seed){
        Validate.isTrue(
            Interval.PERCENT.containsAll(getOutputInterval())
        );

        seed = seed.newSeed("probability");

        RandomGen randomProducer = new RandomGen(seed);

        return input -> {
            double chance = getOutput(input);

            Random random = randomProducer.getRandom(
                posInfo.getIntX(input),
                posInfo.getIntZ(input)
            );

            return MathHelper.randomBool(
                chance,
                random
            );
        };
    }


    /**
     * Returns a FloatFunc that returns the product of every inner FloatFunc's values
     */
    @SafeVarargs
    static <I> FloatFunc<I> multiply(FloatFunc<I>... floatFuncs){
        Interval outputInterval = _applyOp((a, b) -> a * b, floatFuncs);

        return new FloatFunc<I>(){
            @Override
            public double getOutput(I input) {
                double result = 1;

                for (FloatFunc<I> floatFunc: floatFuncs){
                    result *= floatFunc.getOutput(input);

                    if (result == 0){
                        return 0;
                    }
                }
                return result;
            }

            @Override
            public Interval getOutputInterval() {
                return outputInterval;
            }
        };
    }

    /**
     * Returns a FloatFunc that returns the sum of every inner FloatFunc's values
     */
    @SafeVarargs
    static <I> FloatFunc<I> sum(FloatFunc<I>... floatFuncs){
        Interval outputInterval = _applyOp(
            (a, b) -> a + b,
            floatFuncs
        );

        return new FloatFunc<I>(){
            @Override
            public double getOutput(I input) {
                double result = 0;

                for (FloatFunc<I> floatFunc: floatFuncs){
                    result += floatFunc.getOutput(input);
                }
                return result;
            }

            @Override
            public Interval getOutputInterval() {
                return outputInterval;
            }
        };
    }

    default FloatFunc<I> toUniform(PercentileTable table){
        return new FloatFunc<I>(){
            @Override
            public double getOutput(I input) {
                double output = FloatFunc.this.getOutput(input);

                return table.rawValueToPercentile(output);
            }

            @Override
            public Interval getOutputInterval() {
                return Interval.PERCENT;
            }
        };
    }

    default FloatFunc<I> _setDebuggable(String group, String name, Function<BlockPos2D, I> blockPosToI){
        DebugCommand.registerDebugFunc(
            group,
            name,
            p -> String.valueOf(getOutput(blockPosToI.apply(p)))
        );

        return this;
    }

    default FloatFunc<I> _setDebuggable(String group, String name, BiFunction<FloatFunc<I>, BlockPos2D, String> display){
        DebugCommand.registerDebugFunc(
            group,
            name,
            p -> display.apply(this, p)
        );

        return this;
    }

    static <I> Interval _applyOp(BiFunction<Double, Double, Double> func, FloatFunc<I>... floatFuncs){
        Interval result = floatFuncs[0].getOutputInterval();

        for (int i = 1; i < floatFuncs.length; i++) {
            FloatFunc<I> floatFunc = floatFuncs[i];

            result = Interval.applyOp(
                result,
                floatFunc.getOutputInterval(),
                func::apply
            );
        }

        return result;
    }
}
