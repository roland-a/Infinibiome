package weightedgpa.infinibiome.internal.generators.posdata;

import weightedgpa.infinibiome.api.Infinibiome;
import weightedgpa.infinibiome.api.dependency.DependencyInjector;
import weightedgpa.infinibiome.api.generators.PosDataTimings;
import weightedgpa.infinibiome.api.generators.Seed;
import weightedgpa.infinibiome.api.generators.nonworldgen.Locatable;
import weightedgpa.infinibiome.api.pointsprovider.PointsProvider;
import weightedgpa.infinibiome.api.pos.BlockPos2D;
import weightedgpa.infinibiome.api.posdata.PosDataHelper;
import weightedgpa.infinibiome.api.posdata.PosDataKeys;

import weightedgpa.infinibiome.api.posdata.PosDataTable;
import weightedgpa.infinibiome.internal.floatfunc.FloatFunc;
import weightedgpa.infinibiome.internal.floatfunc.IntFunc;
import weightedgpa.infinibiome.internal.floatfunc.generators.SimplexNoise;
import weightedgpa.infinibiome.internal.floatfunc.modifiers.SeamlessGrid;
import weightedgpa.infinibiome.internal.floatfunc.util.Interval;
import weightedgpa.infinibiome.internal.floatfunc.util.IntervalMapper;
import weightedgpa.infinibiome.internal.generators.utils.PredicateSearcher;
import weightedgpa.infinibiome.internal.minecraftImpl.commands.DebugCommand;
import weightedgpa.infinibiome.internal.misc.*;
import weightedgpa.infinibiome.internal.pointsprovider.GridRandomPoints;
import weightedgpa.infinibiome.internal.pos.DistortedPos;
import weightedgpa.infinibiome.internal.pos.PosDistorter;

public final class RiverGen extends DataGeneratorBase implements Locatable.HasPointsProvider {
    private static final double VORONOI_RATE_PER_GRID = 0.4f;
    private static final int VORONOI_GRID_SIZE = 1024;

    private static final int LONG_WINDING_SCALE = 2000;
    private static final int LONG_WINDING_AMP = 500;

    private static final Interval LOCAL_WINDING_AMP = new Interval(30, 60);
    private static final Interval LOCAL_WINDING_SCALE = new Interval(50, 100);

    private static final Interval CENTER_TO_SHORE_WIDTH = new Interval(5, 20);
    private static final Interval SHORE_TO_OUTSIDE_WIDTH = new Interval(40, 200);

    private static final double MAX_DEPTH_TO_WIDTH_RATIO = 0.25f;

    private static final double MAX_HUMIDITY_BONUS = 0.1f;

    private static final Interval TRANSITION_EASE = new Interval(2, 10);
    private static final SeamlessGrid TABLE = PregeneratedSeamlessGrid.TABLE_256_256;

    //returns the length in blocks from the center of the river to its shore
    //in other words, it returns half the length of the river
    private final FloatFunc<BlockPos2D> centerToShoreLengthFunc;

    //returns the length in blocks of the river's shore to outside the river
    //in other words, it returns the length of the river's bank
    private final FloatFunc<BlockPos2D> shoreToOutsideLengthFunc;

    //returns how deep the river can be for a given width
    //0 means the most shallow as possible
    //1 means the deepest possible for a given river width
    //also used for determining how much to increase the humidity around the river
    private final FloatFunc<BlockPos2D> depthPercentFunc;

    //returns how deep a river should be at a position
    //depends on depthPercent
    private final FloatFunc<BlockPos2D> depthFunc;

    //returns scale used for the short winding
    private final FloatFunc<BlockPos2D> localWindingScaleFunc;

    private final FloatFunc<BlockPos2D> easeFunc;

    //returns amp used for short winding
    private final FloatFunc<BlockPos2D> localWindingAmpFunc;

    //returns how far a position is from the closest river center
    private final FloatFunc<BlockPos2D> distanceToRiverFunc;

    public RiverGen(DependencyInjector di) {
        super(
            di,
            Infinibiome.MOD_ID + ":river",
            PosDataTimings.RIVER
        );

        this.centerToShoreLengthFunc = initCenterToShoreLength(seed);
        this.shoreToOutsideLengthFunc = initShoreToOutsideLength(seed);
        this.depthPercentFunc = initRiverDepthPercent(seed);
        this.depthFunc = initRiverDepth();
        this.easeFunc = initEaseFunc(seed);
        this.localWindingScaleFunc = initLocalWindingScale(seed);
        this.localWindingAmpFunc = initLocalWindingAmp(seed);
        this.distanceToRiverFunc = initDistanceToRiverCenter(seed);

        DebugCommand.registerDebugFunc(
            "river",
            "isRiver",
            p -> posDataAfterTiming.get().get(PosDataKeys.HEIGHT_MODIFIED_BY_RIVER, p)
        );
    }

    private FloatFunc<BlockPos2D> initCenterToShoreLength(Seed seed){
        seed = seed.newSeed("centerToShoreLength");

        return Helper.initUniformNoise(seed, Helper.COMMON_SCALE)
            .mapInterval(CENTER_TO_SHORE_WIDTH)
            ._setDebuggable(
                "river",
                "halfLength",
                p -> p
            );
    }

    private FloatFunc<BlockPos2D> initShoreToOutsideLength(Seed seed){
        seed = seed.newSeed("shoreToOutsideLength");

        return Helper.initUniformNoise(seed, Helper.COMMON_SCALE)
            .mapInterval(SHORE_TO_OUTSIDE_WIDTH)
            ._setDebuggable(
                "river",
                "bankLength",
                p -> p
            );
    }

    private FloatFunc<BlockPos2D> initRiverDepthPercent(Seed seed){
        seed = seed.newSeed("riverDepthPercent");

        return Helper.initUniformNoise(seed, Helper.COMMON_SCALE)
            ._setDebuggable(
                "river",
                "depthPercent",
                p -> p
            );
    }

    private FloatFunc<BlockPos2D> initRiverDepth(){
        return new FloatFunc<BlockPos2D>() {
            @Override
            public double getOutput(BlockPos2D input) {
                double riverWidth = centerToShoreLengthFunc.getOutput(input) * 2;

                double maxDepth = riverWidth * MAX_DEPTH_TO_WIDTH_RATIO;

                double depth = maxDepth * depthPercentFunc.getOutput(input);

                if (depth < 1) {
                    return 1;
                }
                return depth;
            }
        }._setDebuggable(
            "river",
            "depth",
            p->p
        );
    }

    private FloatFunc<BlockPos2D> initEaseFunc(Seed seed){
        seed = seed.newSeed("ease");

        return Helper.initUniformNoise(seed, Helper.COMMON_SCALE)
            .mapInterval(
                TRANSITION_EASE
            )
            ._setDebuggable(
                "river",
                "ease",
                p -> p
            );
    }

    private FloatFunc<BlockPos2D> initLocalWindingScale(Seed seed){
        seed = seed.newSeed("windingScale");

        return Helper.initUniformNoise(seed, Helper.COMMON_SCALE)
            .mapInterval(LOCAL_WINDING_SCALE)
            ._setDebuggable(
                "river",
                "scale",
                (t, p) -> PregeneratedSeamlessGrid.TABLE_256_256._debugValue(p, t::getOutput)
            );
    }

    private FloatFunc<BlockPos2D> initLocalWindingAmp(Seed seed){
        seed = seed.newSeed("windingAmp");

        return Helper.initUniformNoise(seed, Helper.COMMON_SCALE)
            .mapInterval(
                LOCAL_WINDING_AMP
            )
            ._setDebuggable(
                "river",
                "amp",
                p -> p
            );
    }

    private FloatFunc<BlockPos2D> initLocalWinding(Seed seed){
        seed = seed.newSeed("localDistortion");

        SimplexNoise<BlockPos2D> noise = new SimplexNoise<>(seed, 1, BlockPos2D.INFO);

        return input -> {
            double result;

            result = TABLE.getValue(
                input,
                gridPos -> MathHelper.fractal(
                    i -> {
                        double localScale = localWindingScaleFunc.getOutput(gridPos);

                        return noise.getOutput(input, i * localScale);
                    },
                    noise.getOutputInterval(),
                    4,
                    0.5,
                    0.5
                ),
                BlockPos2D.INFO
            );

            //converts the noise interval to [-1,1]
            result = noise.getOutputInterval().mapInterval(result, new Interval(-1, 1));

            //converts [-1,1] to [-amp,amp]
            result *= localWindingAmpFunc.getOutput(input);

            return result;
        };
    }

    private FloatFunc<BlockPos2D> initLongWinding(Seed seed){
        seed = seed.newSeed("longDistortion");

        SimplexNoise<BlockPos2D> base = new SimplexNoise<>(seed, LONG_WINDING_SCALE, BlockPos2D.INFO);

        return input -> {
            double result;

            result = MathHelper.fractal(
                i -> base.getOutput(input, i),
                base.getOutputInterval(),
                4,
                0.5,
                0.5
            );

            result = base.getOutputInterval().mapInterval(
                result,
                new Interval(-LONG_WINDING_AMP, LONG_WINDING_AMP)
            );

            return result;
        };
    }

    private FloatFunc<BlockPos2D> initDistanceToRiverCenter(Seed seed){
        seed = seed.newSeed("distanceToRiver");

        PosDistorter<BlockPos2D> distorter = new PosDistorter<>(
            seed.newSeed("localWindingDistorter"),
            s -> FloatFunc.sum(
                initLocalWinding(s),
                initLongWinding(s)
            ),
            BlockPos2D.INFO
        );

        PointsProvider<DistortedPos> voronoiPoints = initVoronoiPoints(seed);

        return input -> {
            DistortedPos distortedInput = distorter.distortPos(input);

            return voronoiPoints.getDistanceToVoronoiBorder(
                distortedInput
            );
        };
    }

    private PointsProvider<DistortedPos> initVoronoiPoints(Seed seed){
        seed = seed.newSeed("voronoiPoints");

        IntFunc<DistortedPos> voronoiPointsGridCount =
            FloatFunc.<DistortedPos>constFunc(VORONOI_RATE_PER_GRID)
                .randomRound(
                    seed,
                    DistortedPos.INFO.floor()
                );

        return new GridRandomPoints<>(
            seed,
            voronoiPointsGridCount,
            VORONOI_GRID_SIZE,
            DistortedPos.INFO
        );
    }

    private void fillData(PosDataTable dataTable){
        double distanceToRiver = distanceToRiverFunc.getOutput(dataTable.getPos());

        new IntervalMapper<>(
            () -> Unit.INSTANCE
        )
        .addBranch(
            __ -> {
                double centerToShoreLength = centerToShoreLengthFunc.getOutput(dataTable.getPos());

                return new Interval(0, centerToShoreLength);
            },
            i -> {
                double transitionToBank = i.mapInterval(distanceToRiver, Interval.PERCENT);

                centerToShore(dataTable, transitionToBank);
                return Unit.INSTANCE;
            }
        )
        .addBranch(
            n -> {
                if (n.getPrevInterval() == null) return null;

                double shoreToOutsideLength = shoreToOutsideLengthFunc.getOutput(dataTable.getPos());

                return n.getPrevInterval().initAhead(
                    shoreToOutsideLength
                );
            },
            i -> {
                double transitionToOutside = i.mapInterval(distanceToRiver, Interval.PERCENT);

                shoreToOutside(dataTable, transitionToOutside);
                return Unit.INSTANCE;
            }
        )
        .run(
            distanceToRiver
        );
    }

    private void centerToShore(PosDataTable posDataTable, double centerToShorePercent){
        centerToShorePercent = MathHelper.ease(
            centerToShorePercent,
            easeFunc.getOutput(posDataTable.getPos())
        );

        double shoreHeight = MCHelper.WATER_HEIGHT;

        double centerHeight = shoreHeight - depthFunc.getOutput(posDataTable.getPos());

        double newHeight = MathHelper.lerp(
            centerToShorePercent,
            centerHeight,
            shoreHeight
        );

        double outsideHeight = posDataTable.get(PosDataKeys.MAPPED_HEIGHT);

        if (newHeight > outsideHeight){
            return;
        }

        posDataTable.set(
            PosDataKeys.MAPPED_HEIGHT,
            newHeight
        );

        markAsRiver(posDataTable);
        increaseHumidity(posDataTable, 0);
    }

    @SuppressWarnings("UnnecessaryLocalVariable")
    private void shoreToOutside(PosDataTable posDataTable, double shoreToOutsidePercent){
        shoreToOutsidePercent = MathHelper.ease(
            shoreToOutsidePercent,
            easeFunc.getOutput(posDataTable.getPos())
        );

        double previousHeight = posDataTable.get(PosDataKeys.MAPPED_HEIGHT);

        double shoreHeight = MCHelper.WATER_HEIGHT;

        double newHeight = MathHelper.lerp(
            shoreToOutsidePercent,
            shoreHeight,
            previousHeight
        );

        if (newHeight > previousHeight){
            return;
        }

        newHeight = PosDataGenHelper.fixHeight(newHeight, previousHeight, posDataTable);
        
        posDataTable.set(
            PosDataKeys.MAPPED_HEIGHT,
            newHeight
        );

        increaseHumidity(posDataTable, shoreToOutsidePercent);
    }

    private void markAsRiver(PosDataTable dataTable) {
        //should not mark as river if its in the middle of the ocean
        if (dataTable.get(PosDataKeys.LANDMASS_TYPE).isOcean()){
            return;
        }

        dataTable.set(
            PosDataKeys.HEIGHT_MODIFIED_BY_RIVER,
            true
        );
    }

    private void increaseHumidity(PosDataTable posDataTable, double shoreToOutsidePercent){
        if (posDataTable.get(PosDataKeys.LANDMASS_TYPE).isOcean()){
            return;
        }

        double humidityIncrease;

        humidityIncrease = MAX_HUMIDITY_BONUS;

        //the deeper the river, the higher the humidity
        humidityIncrease *= depthPercentFunc.getOutput(posDataTable.getPos());

        //the closer to outside, the lower the humidity
        humidityIncrease *= 1 - shoreToOutsidePercent;

        //increases the humidity
        posDataTable.set(
            PosDataKeys.HUMIDITY,
            posDataTable.get(PosDataKeys.HUMIDITY).increase(humidityIncrease)
        );
    }

    @Override
    public void generateData(PosDataTable dataTable) {
        fillData(dataTable);
    }

    @Override
    public PointsProvider<BlockPos2D> getAllLocations() {
        return new PredicateSearcher<>(
            16,
            p -> posDataAfterTiming.get().get(PosDataKeys.HEIGHT_MODIFIED_BY_RIVER, p) && PosDataHelper.isUnderWaterAt(p, posDataAfterTiming.get()),
            BlockPos2D.INFO
        );
    }
}
