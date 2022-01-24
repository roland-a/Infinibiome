package weightedgpa.infinibiome.internal.generators.posdata;

import com.google.common.collect.Lists;
import weightedgpa.infinibiome.api.Infinibiome;
import weightedgpa.infinibiome.api.dependency.DependencyInjector;
import weightedgpa.infinibiome.api.generators.Seed;
import weightedgpa.infinibiome.api.generators.nonworldgen.Locatable;
import weightedgpa.infinibiome.api.pointsprovider.PointsProvider;
import weightedgpa.infinibiome.internal.floatfunc.FloatFunc;
import weightedgpa.infinibiome.internal.floatfunc.generators.SimplexNoise;
import weightedgpa.infinibiome.internal.floatfunc.util.Interval;
import weightedgpa.infinibiome.api.generators.PosDataTimings;
import weightedgpa.infinibiome.internal.generators.utils.GenHelper;
import weightedgpa.infinibiome.internal.minecraftImpl.commands.DebugCommand;
import weightedgpa.infinibiome.internal.misc.*;
import weightedgpa.infinibiome.internal.pointsprovider.GridRandomPoints;
import weightedgpa.infinibiome.api.pos.BlockPos2D;
import weightedgpa.infinibiome.api.posdata.*;
import weightedgpa.infinibiome.internal.misc.Helper;

public final class LakeGen extends DataGeneratorBase implements Locatable.HasPointsProvider {
    private static final int GRID_LENGTH = 1024;
    private static final double CHANCE_PER_GRID = 0.6f;

    private static final Interval LAKE_DEPTH = new Interval(2, 6);

    //shape of lake
    private static final Interval PRIMARY_RADIUS = new Interval(400, 800);
    private static final int SECONDARY_RADIUS_SCALE = 200;
    private static final double SECONDARY_RADIUS_DEPTH = 0.5f;

    private static final double HUMIDITY_BONUS = 0.2f;

    private final FloatFunc<BlockPos2D> transitionalPercentFunc;
    private final FloatFunc<BlockPos2D> lakeDepthFunc;
    private final PointsProvider<BlockPos2D> lakeCenters;

    //temporary fix to speed up lake generation without breaking previous 0.4 world
    private final PosDataProvider justLandmassGen;

    public LakeGen(DependencyInjector di) {
        super(
            di,
            Infinibiome.MOD_ID + ":lake",
            PosDataTimings.LAKE
        );

        this.lakeCenters = initLakesCenter(seed);
        this.transitionalPercentFunc = initLakeTransition(seed);
        this.lakeDepthFunc = initLakeDepth(seed);

        this.justLandmassGen = new PosDataProviderBase(
            Lists.newArrayList(
                di.get(LandmassGen.class)
            ),
            1
        );

        DebugCommand.registerDebugFunc(
            "lake",
            "isLake",
            p -> posDataAfterTiming.get().get(PosDataKeys.HEIGHT_MODIFIED_BY_LAKE, p)
        );
    }

    private PointsProvider<BlockPos2D> initLakesCenter(Seed seed){
        seed = seed.newSeed("lakesCenter");

        return new GridRandomPoints<>(
            seed,
            FloatFunc.<BlockPos2D>constFunc(CHANCE_PER_GRID).randomRound(seed, BlockPos2D.INFO),
            GRID_LENGTH,
            BlockPos2D.INFO
        )
        .filterOutput(
            this::maySpawn
        );
    }

    private FloatFunc<BlockPos2D> initLakeTransition(Seed seed){
        seed = seed.newSeed("lakeTransition");

        SimplexNoise<BlockPos2D> base = new SimplexNoise<>(seed, SECONDARY_RADIUS_SCALE, BlockPos2D.INFO);

        FloatFunc<BlockPos2D> secondaryRadius = new FloatFunc<BlockPos2D>() {
            @Override
            public double getOutput(BlockPos2D input) {
                return MathHelper.fractal(
                    i -> base.getOutput(input, i),
                    base.getOutputInterval(),
                    4,
                    0.5,
                    0.5
                );
            }

            @Override
            public Interval getOutputInterval() {
                return base.getOutputInterval();
            }
        };

        return GenHelper.initComplexCluster(
            seed,
            PRIMARY_RADIUS,
            secondaryRadius,
            SECONDARY_RADIUS_DEPTH,
            lakeCenters
        );
    }


    private FloatFunc<BlockPos2D> initLakeDepth(Seed seed){
        seed = seed.newSeed("lakeDepth");

        return Helper.initUniformNoise(seed, Helper.COMMON_SCALE)
            .mapInterval(
                LAKE_DEPTH
            );
    }

    @Override
    public void generateData(PosDataTable dataTable) {
        double transitionToLake = transitionalPercentFunc.getOutput(dataTable.getPos());

        transitionToLake = MathHelper.ease(transitionToLake, 10);

        if (transitionToLake == 0){
            return;
        }

        increaseHumidity(dataTable, transitionToLake);

        setNewHeight(dataTable, transitionToLake);

        dataTable.set(
            PosDataKeys.HEIGHT_MODIFIED_BY_LAKE,
            true
        );
    }

    private void increaseHumidity(PosDataTable posDataTable, double transitionToLake){
        if (transitionToLake < 0.5){
            return;
        }

        transitionToLake = new Interval(0.5, 1).mapInterval(transitionToLake, Interval.PERCENT);

        posDataTable.set(
            PosDataKeys.HUMIDITY,
            posDataTable.get(PosDataKeys.HUMIDITY).increase(HUMIDITY_BONUS * transitionToLake)
        );
    }

    private void setNewHeight(PosDataTable posDataTable, double transitionToLake){
        double previousHeight = posDataTable.get(PosDataKeys.MAPPED_HEIGHT);

        double lakeHeight = MCHelper.WATER_HEIGHT - lakeDepthFunc.getOutput(posDataTable.getPos());

        if (lakeHeight > previousHeight){
            return;
        }

        double newHeight = MathHelper.lerp(
            transitionToLake,
            previousHeight,
            lakeHeight
        );

        newHeight = PosDataGenHelper.fixHeight(newHeight, previousHeight, posDataTable);

        posDataTable.set(
            PosDataKeys.MAPPED_HEIGHT,
            newHeight
        );
    }

    @Override
    public PointsProvider<BlockPos2D> getAllLocations() {
        return lakeCenters;
    }

    private boolean maySpawn(BlockPos2D lakeCenter){
        return Helper.passesSurroundingTest(
            lakeCenter,
            (int)PRIMARY_RADIUS.getMin(),
            p -> justLandmassGen.get(PosDataKeys.LANDMASS_TYPE, p).isLand(),
            BlockPos2D.INFO
        );
    }
}
