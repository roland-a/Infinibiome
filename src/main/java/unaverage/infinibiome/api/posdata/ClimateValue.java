package weightedgpa.infinibiome.api.posdata;

import weightedgpa.infinibiome.internal.floatfunc.util.Interval;
import weightedgpa.infinibiome.internal.misc.MCHelper;

/**
 * Represents all the vertical values of either temperature or humidity at an xz position.
 */
public final class ClimateValue {
    private final double baseValue;
    private final int baseHeight;
    private final double changePerY;

    public ClimateValue(double baseValue, double changePerY) {
        this(
            MCHelper.WATER_HEIGHT,
            baseValue,
            changePerY
        );
    }

    private ClimateValue(int baseHeight, double baseValue, double changePerY) {
        this.baseHeight = baseHeight;
        this.baseValue = baseValue;
        this.changePerY = changePerY;
    }

    /**
     * @return
     * The temperature/humidity at the y position clamped between 0 and 1
     */
    public double fromHeight(double y){
        double heightFromBase = y - baseHeight;

        double result;

        if (heightFromBase < 0){
            result = baseValue;
        }
        else {
            result = baseValue + (heightFromBase * changePerY);
        }

        return Interval.PERCENT.clamp(result);
    }

    public ClimateValue increase(double offset){
        return new ClimateValue(
            baseHeight,
            baseValue + offset,
            changePerY
        );
    }
}
