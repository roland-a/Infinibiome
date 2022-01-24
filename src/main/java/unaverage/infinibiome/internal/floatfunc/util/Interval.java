package weightedgpa.infinibiome.internal.floatfunc.util;

import com.google.common.collect.Lists;
import weightedgpa.infinibiome.internal.misc.MathHelper;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Objects;
import java.util.function.DoubleBinaryOperator;
import java.util.function.DoubleUnaryOperator;

@SuppressWarnings("PublicMethodNotExposedInInterface")
public final class Interval {
    public static final Interval ALL_VALUES = new Interval(Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY);

    public static final Interval POSITIVES = new Interval(Math.nextUp(0), Double.POSITIVE_INFINITY);
    public static final Interval NON_NEGATIVES = new Interval(0, Double.POSITIVE_INFINITY);

    public static final Interval PERCENT = new Interval(0, 1);

    private final double min;
    private final double max;

    public Interval(double value1, double value2) {
        assert !Double.isNaN(value1);
        assert !Double.isNaN(value2);

        if (value1 < value2){
            this.min = value1;
            this.max = value2;
        }
        else {
            this.min = value2;
            this.max = value1;
        }
    }

    /**
     * Applies a dynamic interval output to the FloatFunc
     *
     * @param length
     * The length of the resulting interval
     *
     * @param center
     * The relative center of the resulting interval.
     * If it is 0, then the resulting interval is at the leftmost location while still within the base interval.
     * If it is 1, then the resulting interval is at the rightmost location while still within the base interval.
     *
     * @throws IllegalArgumentException
     * If param center's is not equal or within [0,1]
     * If param length is less than 0.
     * If param length is greater than the base interval's size.
     */
    public Interval subInterval(double length, double center){
        assert PERCENT.contains(center);
        assert length >= 0;
        assert length <= getSize();

        //contains all the possible location of the resulting interval's center
        Interval resultCenterInterval = new Interval(getMin() + length / 2, getMax() - length / 2);

        double resultCenter = PERCENT.mapInterval(center, resultCenterInterval);

        return new Interval(resultCenter - length / 2, resultCenter + length / 2);
    }

    /**
     * @return The lowest value that this range contains
     */
    public double getMin() {
        return min;
    }

    /**
     * @return The highest value that this range contains
     */
    public double getMax() {
        return max;
    }

    public double getMidPoint(){
        return (max + min) / 2;
    }

    /**
     * @return
     * The distance between the max and min value of the range.
     * Is never negative.
     */
    public double getSize(){
        return max - min;
    }

    /**
     * @param value The value being tested if its inside the range.
     *
     * @return true if this range contains the value.
     */
    public boolean contains(double value){
        return getMin() <= value && getMax() >= value;
    }

    /**
     * @return
     * True if {@link #getSize()} can run without errors,
     * which is when {{@link #getSize()}} is not zero nor infinity
     */
    public boolean canMapInterval(){
        return getSize() != 0 && Double.isFinite(getSize());
    }

    public double mapInterval(double value, Interval newInterval){
        return mapInterval(value, newInterval.getMin(), newInterval.getMax());
    }

    public double mapInterval(
        double value,
        double minConvert,
        double maxConvert
    ){
        assert Double.isFinite(value);
        assert Double.isFinite(minConvert);
        assert Double.isFinite(maxConvert);

        double slope = (maxConvert - minConvert) / getSize();
        double offset = -getMin() * slope + minConvert;

        return value * slope + offset;

    }

    public int mapToIntInterval(double value, int min, int maxInclusive) {
        assert Double.isFinite(value);

        double preResult = mapInterval(
            value,
            new Interval(min, maxInclusive + 1)
        );

        int result = MathHelper.floor(preResult);

        if (result == maxInclusive + 1){
            return maxInclusive;
        }
        return result;
    }

    /**
     * Clamps a single value to a specified range
     *
     * @param base
     * The value that will be clamped if outside of the range.
     *
     * @return
     * The clamped value
     */
    public double clamp(double base){
        assert !Double.isNaN(base);

        if (base > getMax()){
            return getMax();
        }
        if (base < getMin()){
            return getMin();
        }
        return base;
    }

    public double foldClamp(double base){
        assert Double.isFinite(base): base;

        if (contains(base)) return base;

        if (base < getMin()){
            return foldClamp(getMin() + (getMin() - base));
        }
        assert base > getMax();

        return foldClamp(getMax() - (base - getMax()));
    }

    /**
     * @return A range that contains all of the values of both ranges.
     */
    public Interval union(Interval other){
        return new Interval(
            Math.min(getMin(), other.getMin()),
            Math.max(getMax(), other.getMax())
        );
    }

    /**
     * @return
     * A range that contains only the value that both ranges share.
     * null when neither range share any value.
     */
    @Nullable
    public Interval intersection(Interval interval2){
        double resultMin = Math.max(getMin(), interval2.getMin());
        double resultMax = Math.min(getMax(), interval2.getMax());

        if (resultMin > resultMax){
            return null;
        }

        return new Interval(resultMin, resultMax);
    }

    public Interval initBehind(double length){
        assert length >= 0: length;

        return new Interval(getMin() - length, getMin());
    }

    public Interval initAhead(double length){
        assert length >= 0: length;

        return new Interval(getMax(), getMax() + length);
    }

    /**
     * Applies an operation to every value in the interval
     * The function MUST be monotonic for this to work properly
     *
     * @param function
     * the unary function being applied to the values of the interval
     *
     * @return
     * interval containing every value after operation
     */
    public Interval applyOp(DoubleUnaryOperator function){
        double minApplied = function.applyAsDouble(getMin());
        double maxApplied = function.applyAsDouble(getMax());

        return new Interval(minApplied, maxApplied);
    }

    public static Interval union(Interval interval0, Interval... intervals){
        Interval result = interval0;

        for (Interval interval: intervals){
            result = result.union(interval);
        }
        return result;
    }

    /**
     * Applies an operation to every two values in both intervals
     * The function MUST be monotonic for this to work properly
     *
     * @param interval1,range2
     * The ranges that the operation will apply to
     *
     * @param function
     * The binary function that will be applied to the values of both intervals.
     *
     * @return
     * Interval containing every value after operation
     */
    @SuppressWarnings("OptionalGetWithoutIsPresent")
    public static Interval applyOp(Interval interval1, Interval interval2, DoubleBinaryOperator function){
        List<Double> list = Lists.newArrayList(
            function.applyAsDouble(interval1.getMin(), interval2.getMin()),
            function.applyAsDouble(interval1.getMax(), interval2.getMin()),
            function.applyAsDouble(interval1.getMin(), interval2.getMax()),
            function.applyAsDouble(interval1.getMax(), interval2.getMax())
        );

        double upperBound = list.stream()
            .max(Double::compare)
            .get();

        double lowerBound = list.stream()
            .min(Double::compare)
            .get();

        return new Interval(lowerBound, upperBound);
    }


    public boolean containsAll(Interval other){
        return getMax() >= other.getMax() && getMin() <= other.getMin();
    }

    public boolean containsAny(Interval other){
        return this.intersection(other) != null;
    }

    @Override
    public String toString() {
        return String.format("Interval[min=%s, max=%s]", getMin(), getMax());
    }

    @SuppressWarnings("all")
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || this.getClass() != o.getClass()) {
            return false;
        }
        Interval interval = (Interval)o;
        return Double.compare(interval.getMin(), this.getMin()) == 0 &&
            Double.compare(interval.getMax(), this.getMax()) == 0;
    }

    @SuppressWarnings("all")
    @Override
    public int hashCode() {
        return Objects.hash(this.getMin(), this.getMax());
    }

    public boolean isConstant() {
        return getMax() == getMin();
    }
}