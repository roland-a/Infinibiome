package weightedgpa.infinibiome.internal.generators.posdata;

import com.google.common.collect.Lists;
import net.minecraft.world.biome.Biomes;
import weightedgpa.infinibiome.api.generators.nonworldgen.DefaultConfig;
import weightedgpa.infinibiome.api.generators.nonworldgen.ConfigIO;
import weightedgpa.infinibiome.api.generators.nonworldgen.Locatable;
import weightedgpa.infinibiome.api.generators.nonworldgen.SpawnPointBlacklist;
import weightedgpa.infinibiome.api.dependency.DependencyInjector;
import weightedgpa.infinibiome.api.pointsprovider.PointsProvider;
import weightedgpa.infinibiome.internal.floatfunc.generators.SimplexNoise;
import weightedgpa.infinibiome.internal.floatfunc.util.Interval;
import weightedgpa.infinibiome.api.Infinibiome;
import weightedgpa.infinibiome.api.generators.*;
import weightedgpa.infinibiome.api.posdata.*;
import weightedgpa.infinibiome.internal.generators.utils.GenHelper;
import weightedgpa.infinibiome.internal.misc.Helper;
import weightedgpa.infinibiome.api.pos.BlockPos2D;
import weightedgpa.infinibiome.internal.floatfunc.FloatFunc;
import weightedgpa.infinibiome.internal.misc.MCHelper;
import weightedgpa.infinibiome.internal.misc.MathHelper;

import java.util.Optional;

public final class MushroomIslandGen extends DataGeneratorBase implements Locatable.HasPointsProvider, SpawnPointBlacklist {
    private static final Interval TERRAIN_AMP = new Interval(10, 50);

    //used for the shape of the island
    private static final Interval BASE_RADIUS = new Interval(300, 500);
    private static final int SCALE = 200;
    private static final int OCTAVES = 5;
    private static final double DEPTH = 1;

    private final PointsProvider<BlockPos2D> islandCenters;
    private final FloatFunc<BlockPos2D> transitionalPercentFunc;
    private final HeightMapProducer heightMapProducer;

    private final PosDataProvider justLandmassGen;

    public MushroomIslandGen(DependencyInjector di){
        super(
            di,
            Infinibiome.MOD_ID + ":mushroom_island",
            PosDataTimings.MUSHROOM_ISLAND
        );

        this.islandCenters = initIslandCenters(seed);

        this.transitionalPercentFunc = initIslandTransition(seed);

        this.heightMapProducer = initIslandHeight(seed);

        this.justLandmassGen = new PosDataProviderBase(
            Lists.newArrayList(
                di.get(LandmassGen.class)
            ),
            1
        );
    }

    private PointsProvider<BlockPos2D> initIslandCenters(Seed seed){
        seed = seed.newSeed("islandCenters");

        return GenHelper.initSeparatedLocations(
            seed,
            512,
            di.get(Config.class).rate
        )
        .filterOutput(
            this::maySpawn
        );
    }

    private boolean maySpawn(BlockPos2D islandCenter){
        return Helper.passesSurroundingTest(
            islandCenter,
            (int)BASE_RADIUS.getMin(),
            p -> justLandmassGen.get(PosDataKeys.LANDMASS_TYPE, p).isOcean(),
            BlockPos2D.INFO
        );
    }

    private FloatFunc<BlockPos2D> initIslandTransition(Seed seed) {
        seed = seed.newSeed("islandTransition");

        SimplexNoise<BlockPos2D> base = new SimplexNoise<>(seed, SCALE, BlockPos2D.INFO);

        FloatFunc<BlockPos2D> effect = new FloatFunc<BlockPos2D>() {
            @Override
            public double getOutput(BlockPos2D input) {
                return MathHelper.fractal(
                    i -> base.getOutput(input, i),
                    base.getOutputInterval(),
                    OCTAVES,
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
            BASE_RADIUS,
            effect,
            DEPTH,
            islandCenters
        );
    }

    private HeightMapProducer initIslandHeight(Seed seed){
        seed = seed.newSeed("islandHeight");

        return new HeightMapProducer(
                seed,
                FloatFunc.constFunc(MCHelper.WATER_HEIGHT),
                Helper.initUniformNoise(seed.newSeed("amp"), Helper.COMMON_SCALE)
                    .mapInterval(TERRAIN_AMP)
        );
    }

    @Override
    public void generateData(PosDataTable dataTable) {
        double transitionalPercent = transitionalPercentFunc.getOutput(dataTable.getPos());

        if (transitionalPercent == 0){
            return;
        }

        transitionalPercent = MathHelper.ease(transitionalPercent, 10);

        double height = MathHelper.lerp(
            transitionalPercent,
            dataTable.get(PosDataKeys.MAPPED_HEIGHT),
            heightMapProducer.getMappedHeight(dataTable.getPos())
        );

        dataTable.set(
            PosDataKeys.MAPPED_HEIGHT,
            height
        );

        dataTable.set(
            PosDataKeys.IS_MUSHROOM_ISLAND,
            true
        );

        dataTable.set(
            PosDataKeys.HUMIDITY,
            new ClimateValue(0.5f, 0)
        );

        if (height > MCHelper.WATER_HEIGHT){
            dataTable.set(
                PosDataKeys.LANDMASS_TYPE,
                new LandmassInfo.Land(transitionalPercent)
            );

            dataTable.set(
                PosDataKeys.OVERRIDE_BIOME,
                () -> Optional.of(Biomes.MUSHROOM_FIELDS)
            );

            dataTable.set(
                PosDataKeys.GROUND_BLOCKS,
                () -> GroundBlocks.DIRT.getSurfaceBlocks(dataTable.getPos())
            );
        }
        else if (height > MCHelper.WATER_HEIGHT - 5){
            dataTable.set(
                PosDataKeys.OVERRIDE_BIOME,
                () -> Optional.of(Biomes.MUSHROOM_FIELD_SHORE)
            );

            dataTable.set(
                PosDataKeys.GROUND_BLOCKS,
                () -> GroundBlocks.DIRT.getSurfaceBlocks(dataTable.getPos())
            );
        }
    }

    @Override
    public PointsProvider<BlockPos2D> getAllLocations() {
        return islandCenters;
    }

    @Override
    public boolean canSpawnHere(BlockPos2D pos) {
        return !posDataAfterTiming.get().get(PosDataKeys.IS_MUSHROOM_ISLAND, pos);
    }

    public static final class Config implements DefaultConfig {
        static final double DEFAULT_VALUE = 0.02;

        private final double rate;

        public Config(DependencyInjector di){
            this.rate = di.get(ConfigIO.class).subConfig("LAND").getRelativeFloat(
                "mushroom_island_rate",
                DEFAULT_VALUE,
                0,
                1,
                "Doubling/Halving this value will double/halve the rate of mushroom islands."
            );
        }
    }
}
