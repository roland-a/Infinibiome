package weightedgpa.infinibiome.api.posdata;

import weightedgpa.infinibiome.api.generators.ClimateConfig;
import weightedgpa.infinibiome.api.dependency.DependencyInjector;
import weightedgpa.infinibiome.api.generators.Seed;
import weightedgpa.infinibiome.internal.floatfunc.FloatFunc;
import weightedgpa.infinibiome.internal.floatfunc.generators.RandomGen;
import weightedgpa.infinibiome.internal.floatfunc.util.Interval;
import weightedgpa.infinibiome.internal.misc.MCHelper;
import weightedgpa.infinibiome.internal.misc.MathHelper;
import weightedgpa.infinibiome.api.pos.BlockPos2D;
import net.minecraft.util.math.BlockPos;

import java.util.Random;
import java.util.function.Function;
import java.util.function.Predicate;

public final class PosDataHelper {
    private PosDataHelper(){}

    public static final Interval FREEZE_INTERVAL = new Interval(0/6d, 1/6d);
    public static final Interval COLD_INTERVAL = new Interval(1/6d, 3/6d);
    public static final Interval WARM_INTERVAL = new Interval(3/6d, 5/6d);
    public static final Interval HOT_INTERVAL = new Interval(5/6d, 6/6d);

    public static final Interval DRY_INTERVAL = new Interval(0/6d, 1/6d);
    public static final Interval SEMI_DRY_INTERVAL = new Interval(1/6d, 3/6d);
    public static final Interval SEMI_WET_INTERVAL = new Interval(3/6d, 5/6d);
    public static final Interval WET_INTERVAL = new Interval(5/6d, 6/6d);

    private static final Seed BASE_SEED = Seed.ROOT.newSeed("climateFuzziness");
    private static final RandomGen TEMPERATURE_FUZZY_RANDOM = new RandomGen(BASE_SEED.newSeed("temperature"));
    private static final RandomGen HUMIDITY_FUZZY_RANDOM = new RandomGen(BASE_SEED.newSeed("humidity"));

    public static FloatFunc<BlockPos2D> initHumidityNoise2D(DependencyInjector di){
        PosDataProvider data = di.get(PosDataProvider.class);

        return new FloatFunc<BlockPos2D>() {
            @Override
            public double getOutput(BlockPos2D input) {
                return getHumidity(input, data);
            }

            @Override
            public Interval getOutputInterval() {
                return Interval.PERCENT;
            }
        };
    }

    public static FloatFunc<BlockPos> initHumidityNoise3D(DependencyInjector di){
        PosDataProvider data = di.get(PosDataProvider.class);

        return new FloatFunc<BlockPos>() {
            @Override
            public double getOutput(BlockPos input) {
                return getHumidity(input, data);
            }

            @Override
            public Interval getOutputInterval() {
                return Interval.PERCENT;
            }
        };
    }

    public static FloatFunc<BlockPos2D> initTemperatureNoise2D(DependencyInjector di){
        PosDataProvider data = di.get(PosDataProvider.class);

        return new FloatFunc<BlockPos2D>() {
            @Override
            public double getOutput(BlockPos2D input) {
                return getTemperature(input, data);
            }

            @Override
            public Interval getOutputInterval() {
                return Interval.PERCENT;
            }
        };
    }

    public static FloatFunc<BlockPos> initTemperatureNoise3D(DependencyInjector di){
        PosDataProvider data = di.get(PosDataProvider.class);

        return new FloatFunc<BlockPos>() {
            @Override
            public double getOutput(BlockPos input) {
                return getTemperature(input, data);
            }

            @Override
            public Interval getOutputInterval() {
                return Interval.PERCENT;
            }
        };
    }

    public static double getHumidity(BlockPos2D pos, PosDataProvider data){
        int mappedHeight = (int)data.get(PosDataKeys.MAPPED_HEIGHT, pos);

        return getHumidity(pos.to3D(mappedHeight), data);
    }

    public static double getHumidity(BlockPos pos, PosDataProvider data){
        return data.get(PosDataKeys.HUMIDITY, MCHelper.to2D(pos)).fromHeight(pos.getY());
    }

    public static double getTemperature(BlockPos2D pos, PosDataProvider data){
        double mappedHeight = data.get(PosDataKeys.MAPPED_HEIGHT, pos);

        return getTemperature(pos.to3D((int)mappedHeight), data);
    }

    public static double getTemperature(BlockPos pos, PosDataProvider data){
        return data.get(PosDataKeys.TEMPERATURE, MCHelper.to2D(pos)).fromHeight(pos.getY());
    }

    public static double fuzzHumidity(double humidity, BlockPos2D pos, ClimateConfig climateConfig){
        return fuzz(
            humidity,
            climateConfig.desertTransitionBorder,
            HUMIDITY_FUZZY_RANDOM.getRandom(pos.getBlockX(), pos.getBlockZ()),
            climateConfig
        );
    }

    public static double fuzzTemperature(double temperature, BlockPos2D pos, ClimateConfig climateConfig){
        return fuzz(
            temperature,
            climateConfig.frozenTransitionBorder,
            TEMPERATURE_FUZZY_RANDOM.getRandom(pos.getBlockX(), pos.getBlockZ()),
            climateConfig
        );
    }

    public static double fuzz(double climateValue, double size, Random random, ClimateConfig climateConfig){
        assert Interval.PERCENT.contains(climateValue): climateValue;
        assert size > 0: size;

        double trueSize = size/climateConfig.scale;

        if (trueSize > .2) trueSize = .2;

        double offset = MathHelper.lerp(
            random.nextFloat(),
            -trueSize,
            trueSize
        );

        double result;

        result = climateValue;

        result += offset;

        result = Interval.PERCENT.clamp(result);

        return result;
    }

    public static double getAverageSlope(BlockPos2D pos, int radius, PosDataProvider data){
        return getAverageSlope(
            pos,
            radius,
            p -> data.get(PosDataKeys.MAPPED_HEIGHT, p),
            p -> true
        );
    }

    public static double getAverageSlope(BlockPos2D pos, int radius, Function<BlockPos2D, Double> toHeightFunc, Predicate<BlockPos2D> predicate) {
        assert radius > 0;

        double total = 0;
        int count = 0;

        for (SearchRay searchRay: SearchRay.values()) {
            double currentHeight = Double.NaN;
            double previousHeight;

            for (int i = -radius; i <= radius; i++) {
                BlockPos2D scannedPos = searchRay.newPos(pos, i);

                previousHeight = currentHeight;
                currentHeight = toHeightFunc.apply(scannedPos);

                if (Double.isNaN(previousHeight)) continue;

                if (!predicate.test(scannedPos)) continue;

                total += Math.abs(currentHeight - previousHeight);
                count += 1;
            }
        }

        return total/count;
    }

    public static double getAverageSlope(BlockPos2D pos, int radius, Function<BlockPos2D, Double> toHeightFunc) {
        return getAverageSlope(
            pos,
            radius,
            toHeightFunc,
            p -> true
        );
    }

    public static boolean isUnderwaterPortionOfLakeOrRiver(BlockPos2D pos2D, PosDataProvider data) {
        if (!isUnderWaterAt(pos2D, data)){
            return false;
        }
        
        return data.get(PosDataKeys.HEIGHT_MODIFIED_BY_LAKE, pos2D) || data.get(PosDataKeys.HEIGHT_MODIFIED_BY_RIVER, pos2D);
    }

    public static boolean isUnderWaterAt(BlockPos2D p, PosDataProvider data) {
        return (int)data.get(PosDataKeys.MAPPED_HEIGHT, p) < MCHelper.WATER_HEIGHT;
    }

    private enum SearchRay{
        ORTHOGONAL_1{
            @Override
            BlockPos2D newPos(BlockPos2D center, int distance) {
                return center.offset(distance, 0);
            }
        },
        ORTHOGONAL_2{
            @Override
            BlockPos2D newPos(BlockPos2D center, int distance) {
                return center.offset(0, distance);
            }
        },
        DIAGONAL_1{
            @Override
            BlockPos2D newPos(BlockPos2D center, int distance) {
                return center.offset(distance, distance);
            }
        },
        DIAGONAL_2{
            @Override
            BlockPos2D newPos(BlockPos2D center, int distance) {
                return center.offset(-distance, distance);
            }
        },;

        abstract BlockPos2D newPos(BlockPos2D center, int distance);
    }
}
