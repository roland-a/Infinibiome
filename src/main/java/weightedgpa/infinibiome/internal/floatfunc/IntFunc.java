package weightedgpa.infinibiome.internal.floatfunc;

import weightedgpa.infinibiome.internal.floatfunc.util.Interval;

import java.util.function.Function;

/**
 * Same as {@link FloatFunc}, but returns integers instead
 */
@FunctionalInterface
public interface IntFunc<I> extends FloatFunc<I> {
    /**
     * Returns an IntFunc that returns the same number no matter what input you give it
     */
    static <I> IntFunc<I> constFunc(int value){
        Interval outputInterval = new Interval(value, value);

        return new IntFunc<I>() {
            @Override
            public int getIntOutput(I input) {
                return value;
            }

            @Override
            public Interval getOutputInterval() {
                return outputInterval;
            }
        };
    }

    /**
     * Same as {@link FloatFunc#getOutput(Object)}, but returns an int instead.
     */
    int getIntOutput(I input);

    @Override
    default double getOutput(I input) {
        return getIntOutput(input);
    }

    /**
     * Same as {@link FloatFunc#mapInput(Function)}
     */
    @Override
    default <NI> IntFunc<NI> mapInput(Function<NI, I> mapFunc) {
        return new IntFunc<NI>() {
            @Override
            public int getIntOutput(NI input) {
                I baseInput = mapFunc.apply(input);

                return IntFunc.this.getIntOutput(baseInput);
            }

            @Override
            public Interval getOutputInterval() {
                return IntFunc.this.getOutputInterval();
            }
        };
    }
}
