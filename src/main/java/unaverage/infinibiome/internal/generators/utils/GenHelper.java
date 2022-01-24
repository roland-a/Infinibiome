package weightedgpa.infinibiome.internal.generators.utils;

import org.apache.commons.lang3.Validate;
import weightedgpa.infinibiome.api.generators.Seed;
import weightedgpa.infinibiome.api.pointsprovider.PointsProvider;
import weightedgpa.infinibiome.api.pos.BlockPos2D;
import weightedgpa.infinibiome.api.posdata.PosDataKeys;
import weightedgpa.infinibiome.api.posdata.PosDataProvider;
import weightedgpa.infinibiome.internal.floatfunc.FloatFunc;
import weightedgpa.infinibiome.internal.floatfunc.generators.RandomGen;
import weightedgpa.infinibiome.internal.floatfunc.util.Interval;
import weightedgpa.infinibiome.internal.generators.utils.condition.ConditionList;
import weightedgpa.infinibiome.internal.misc.MathHelper;
import weightedgpa.infinibiome.internal.pointsprovider.EmptyPointsProvider;
import weightedgpa.infinibiome.internal.pointsprovider.GridLowestPoint;

import static weightedgpa.infinibiome.api.posdata.PosDataHelper.*;

public final class GenHelper {
    private GenHelper(){}

    //region temp
    public static final Interval WARMISH = Interval.union(
        HOT_INTERVAL,
        WARM_INTERVAL
    );

    public static final Interval COLDISH = Interval.union(
        COLD_INTERVAL,
        FREEZE_INTERVAL
    );

    public static final Interval NOT_FREEZING = Interval.union(
        HOT_INTERVAL,
        WARM_INTERVAL,
        COLD_INTERVAL
    );

    public static final Interval UPPER_HOT_INTERVAL = new Interval(
        HOT_INTERVAL.getMax(),
        HOT_INTERVAL.getMidPoint()
    );

    public static final Interval LOWER_HOT_INTERVAL = new Interval(
        HOT_INTERVAL.getMidPoint(),
        HOT_INTERVAL.getMin()
    );

    public static final Interval UPPER_FREEZE_INTERVAL = new Interval(
        FREEZE_INTERVAL.getMax(),
        FREEZE_INTERVAL.getMidPoint()
    );

    public static final Interval LOWER_FREEZE_INTERVAL = new Interval(
        FREEZE_INTERVAL.getMidPoint(),
        FREEZE_INTERVAL.getMin()
    );
    //endregion

    //region humidity
    public static final Interval WETISH = Interval.union(
        WET_INTERVAL,
        SEMI_WET_INTERVAL
    );

    public static final Interval DRYISH = Interval.union(
        DRY_INTERVAL,
        SEMI_DRY_INTERVAL
    );

    public static final Interval NOT_DESERT = Interval.union(
        WET_INTERVAL,
        SEMI_WET_INTERVAL,
        SEMI_DRY_INTERVAL
    );
    //endregion

    public static final Interval NON_FORESTED_TREE_INTERVAL = new Interval(0, 0.25);
    public static final Interval FORESTED_TREE_INTERVAL = new Interval(0.25, Double.POSITIVE_INFINITY);

    public static PointsProvider<BlockPos2D> getCommonPredicateSearcher(int gridSize, boolean isUnderwater, ConditionList conditions, PosDataProvider posData) {
        return new PredicateSearcher<>(
            gridSize,
            p -> {
                if (!conditions.canBeHere(p)) return false;

                if (isUnderwater){
                    return
                        isUnderwaterPortionOfLakeOrRiver(p, posData) ||
                        posData.get(PosDataKeys.LANDMASS_TYPE, p).isOcean();
                }
                return posData.get(PosDataKeys.LANDMASS_TYPE, p).isLand();
            },
            BlockPos2D.INFO
        );
    }

    public static FloatFunc<BlockPos2D> initComplexCluster(
        Seed seed,
        Interval primaryRadiusSize,
        FloatFunc<BlockPos2D> secondaryRadius,
        double secondaryRadiusDepth,
        PointsProvider<BlockPos2D> centers
    ) {
        Validate.isTrue(
            Interval.PERCENT.contains(secondaryRadiusDepth)
        );

        seed = seed.newSeed("complexCluster");

        secondaryRadius = secondaryRadius.mapInterval(
            new Interval(1 - secondaryRadiusDepth, 1)
        );

        FloatFunc<BlockPos2D> baseRadiusFunc = new RandomGen(seed.newSeed("baseRadius"))
            .asPercentFloatFunc(BlockPos2D.INFO)
            .mapInterval(
                primaryRadiusSize
            );


        Interval outputInterval = new Interval(0, primaryRadiusSize.getMax());

        FloatFunc<BlockPos2D> effectFunc_ = secondaryRadius;

        return input -> {
            double effectFuncOutput = effectFunc_.getOutput(input);

            FloatFunc<BlockPos2D> clusterCenterToRadius = new FloatFunc<BlockPos2D>() {
                @Override
                public double getOutput(BlockPos2D clusterCenter) {
                    double result;

                    //base radius should always be the same around the same clusterCenter
                    result = baseRadiusFunc.getOutput(clusterCenter);

                    result *= effectFuncOutput;

                    return result;
                }

                @Override
                public Interval getOutputInterval() {
                    return outputInterval;
                }
            };

            return MathHelper.gradientTowardsPoint(
                input,
                clusterCenterToRadius,
                centers
            );
        };
    }

    public static PointsProvider<BlockPos2D> initSeparatedLocations(Seed seed, int gridSize, double rate){
        Validate.isTrue(gridSize > 0);
        Validate.isTrue(
            Interval.PERCENT.contains(rate)
        );

        seed = seed.newSeed("blockPosLocations");

        if (rate == 0){
            return new EmptyPointsProvider<>(BlockPos2D.INFO);
        }

        return new GridLowestPoint<>(
            gridSize,
            BlockPos2D.INFO
        )
        .filterOutput(
            FloatFunc.<BlockPos2D>constFunc(rate).randomBool(BlockPos2D.INFO, seed)
        );
    }
}
