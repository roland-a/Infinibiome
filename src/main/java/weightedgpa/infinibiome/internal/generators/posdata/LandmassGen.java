package weightedgpa.infinibiome.internal.generators.posdata;

import weightedgpa.infinibiome.api.generators.nonworldgen.DefaultConfig;
import weightedgpa.infinibiome.api.generators.nonworldgen.ConfigIO;
import weightedgpa.infinibiome.api.posdata.PosDataTable;
import weightedgpa.infinibiome.api.dependency.DependencyInjector;
import weightedgpa.infinibiome.api.generators.Seed;
import weightedgpa.infinibiome.api.posdata.LandmassInfo;
import weightedgpa.infinibiome.api.posdata.PosDataKeys;
import weightedgpa.infinibiome.internal.floatfunc.generators.PerlinNoise;
import weightedgpa.infinibiome.internal.floatfunc.util.Interval;
import weightedgpa.infinibiome.api.Infinibiome;
import weightedgpa.infinibiome.internal.floatfunc.util.IntervalMapper;
import weightedgpa.infinibiome.api.generators.PosDataTimings;
import weightedgpa.infinibiome.internal.minecraftImpl.commands.DebugCommand;
import weightedgpa.infinibiome.internal.misc.*;
import weightedgpa.infinibiome.internal.misc.Helper;
import weightedgpa.infinibiome.internal.floatfunc.FloatFunc;
import weightedgpa.infinibiome.internal.floatfunc.generators.SimplexNoise;
import weightedgpa.infinibiome.api.pos.BlockPos2D;

public final class LandmassGen extends DataGeneratorBase {
    private static final Interval OCEAN_TRANSITION_LENGTH = new Interval(200, 400);
    private static final Interval LAND_TRANSITION_LENGTH = new Interval(150, 300);
    private static final Interval BEACH_LENGTH = new Interval(50, 100);

    private static final Interval PERSISTENCE = new Interval(0.5, 0.65);
    private static final Interval SCALE_LIMIT = new Interval(10, 20);


    private final FloatFunc<BlockPos2D> beachLengthFunc;
    private final FloatFunc<BlockPos2D> landToBeachLengthFunc;
    private final FloatFunc<BlockPos2D> oceanToBeachLengthFunc;
    private final FloatFunc<BlockPos2D> scaleLimitFunc;
    private final FloatFunc<BlockPos2D> persistenceFunc;
    private final FloatFunc<BlockPos2D> baseFunc;

    private final Config config;

    public LandmassGen(DependencyInjector di) {
        super(
            di,
            Infinibiome.MOD_ID + ":landmass",
            PosDataTimings.LANDMASS
        );

        this.config = di.getAll(Config.class).get(0);

        this.beachLengthFunc = initBeachLength(seed);
        this.landToBeachLengthFunc = initLandTransitionLength(seed);
        this.oceanToBeachLengthFunc = initOceanTransitionLength(seed);

        this.scaleLimitFunc = initScaleLimitFunc(seed);
        this.persistenceFunc = initPersistenceFunc(seed);
        this.baseFunc = initBase(seed);

        DebugCommand.registerDebugFunc(
            "landmass",
            "type",
            this::getLandMassInfo
        );
    }

    private FloatFunc<BlockPos2D> initBase(Seed seed){
        seed = seed.newSeed("base");

        //must be constant
        double scale = config.scale;
        double lacunarity = 0.5;

        SimplexNoise<BlockPos2D> noise = new SimplexNoise<>(seed, scale, BlockPos2D.INFO);

        Interval interval = new Interval(
            0,
            1 +
            landToBeachLengthFunc.getOutputInterval().getMax() +
            beachLengthFunc.getOutputInterval().getMax() +
            oceanToBeachLengthFunc.getOutputInterval().getMax()
        );

        return new FloatFunc<BlockPos2D>() {
            @Override
            public double getOutput(BlockPos2D input) {
                double result;

                double octaves = MathHelper.scaleLimitToOctaves(
                    scale,
                    scaleLimitFunc.getOutput(input),
                    lacunarity
                );

                result = MathHelper.fractal(
                    i -> noise.getOutput(input, i),
                    noise.getOutputInterval(),
                    octaves,
                    persistenceFunc.getOutput(input),
                    lacunarity
                );

                result = noise.getOutputInterval().mapInterval(
                    result,
                    interval
                );

                return result;
            }

            @Override
            public Interval getOutputInterval() {
                return interval;
            }
        };
    }

    private FloatFunc<BlockPos2D> initPersistenceFunc(Seed seed){
        seed = seed.newSeed("persistence");

        return new PerlinNoise<>(seed, Helper.COMMON_SCALE, BlockPos2D.INFO)
            .toUniform(PerlinNoise.PERCENTILE_TABLE)
            .mapInterval(PERSISTENCE)
            ._setDebuggable(
                "landmass",
                "persistence", p -> p
            );
    }

    private FloatFunc<BlockPos2D> initScaleLimitFunc(Seed seed){
        seed = seed.newSeed("scaleLimit");

        return new PerlinNoise<>(seed, Helper.COMMON_SCALE, BlockPos2D.INFO)
            .toUniform(
                PerlinNoise.PERCENTILE_TABLE
            )
            .mapInterval(SCALE_LIMIT)
            ._setDebuggable(
                "landmass",
                "scaleLimit", p->p
            );
    }

    private FloatFunc<BlockPos2D> initOceanTransitionLength(Seed seed){
        seed = seed.newSeed("OceanTransitionLength");

        return initLengthBase(seed, OCEAN_TRANSITION_LENGTH,"oceanTransitionLength");
    }

    private FloatFunc<BlockPos2D> initLandTransitionLength(Seed seed){
        seed = seed.newSeed("landTransitionLength");

        return initLengthBase(seed, LAND_TRANSITION_LENGTH, "landTransitionLength");
    }

    private FloatFunc<BlockPos2D> initBeachLength(Seed seed){
        seed = seed.newSeed("beachLength");

        return initLengthBase(seed, BEACH_LENGTH, "beachLength");
    }

    private FloatFunc<BlockPos2D> initLengthBase(Seed seed, Interval lengthInterval, String debugName){
        //divides the range by landMassScale so that the length would be the same with different landMassScale
        Interval scaledLength = lengthInterval.applyOp(
            n -> n / config.scale
        );

        FloatFunc<BlockPos2D> result = new PerlinNoise<>(seed, Helper.COMMON_SCALE, BlockPos2D.INFO)
            .toUniform(
                PerlinNoise.PERCENTILE_TABLE
            )
            .mapInterval(scaledLength);

        result.mapInterval(lengthInterval)._setDebuggable(
            "landmass",
            debugName,
            p -> p
        );

        return result;
    }

    private void fillTable(PosDataTable dataOutput) {
        dataOutput.set(
            PosDataKeys.LANDMASS_TYPE,
            getLandMassInfo(dataOutput.getPos())
        );
    }
    
    @SuppressWarnings("OverlyLongMethod")
    private LandmassInfo getLandMassInfo(BlockPos2D pos){
        double baseValue = baseFunc.getOutput(pos);
        
        return new IntervalMapper<LandmassInfo>(
            () -> {throw new RuntimeException("should never happen");}
        )
        .addBranch(
            __ -> {
                double min = baseFunc.getOutputInterval().getMin();
                double max = min + config.size;

                return new Interval(min, max);
            },
            __ -> {
                return new LandmassInfo.Land(0);
            }
        )
        .addBranch(
            n -> {
                if (n.getPrevInterval() == null) return null;

                double landToBeachLength = this.landToBeachLengthFunc.getOutput(pos);

                return n.getPrevInterval().initAhead(landToBeachLength);
            },
            i -> {
                double transitionTowardsBeach = i.mapInterval(baseValue, Interval.PERCENT);

                return new LandmassInfo.Land(transitionTowardsBeach);
            }
        )
        .addBranch(
            n -> {
                if (n.getPrevInterval() == null) return null;

                double beachLength = this.beachLengthFunc.getOutput(pos);

                return n.getPrevInterval().initAhead(beachLength);
            },
            i -> {
                double transitionTowardsLand = 1 - i.mapInterval(baseValue, Interval.PERCENT);

                return new LandmassInfo.Beach(transitionTowardsLand);
            }
        )
        .addBranch(
            n -> {
                if (n.getPrevInterval() == null) return null;

                double oceanToBeachLength = oceanToBeachLengthFunc.getOutput(pos);

                return n.getPrevInterval().initAhead(oceanToBeachLength);
            },
            i -> {
                double transitionTowardsBeach = 1 - i.mapInterval(baseValue, Interval.PERCENT);

                return new LandmassInfo.Ocean(transitionTowardsBeach);
            }
        )
        .addBranch(
            n -> {
                if (n.getPrevInterval() == null) return null;

                double min = n.getPrevInterval().getMax();
                double max = baseFunc.getOutputInterval().getMax();

                return new Interval(min, max);
            },
            i -> {
                return new LandmassInfo.Ocean(0);
            }
        )
        .run(
            baseValue
        );
    }

    @Override
    public void generateData(PosDataTable data) {
        fillTable(data);
    }

    public static final class Config implements DefaultConfig {
        private final double scale;
        private final double size;

        public Config(DependencyInjector di){
            ConfigIO config = di.get(ConfigIO.class).subConfig("LAND");

            this.scale = config.getRelativeFloat(
                "landmass_scale",
                12000,
                1000,
                Integer.MAX_VALUE,
                "Doubling/Halving this value will double/halve landmass and oceanmass size."
            );

            this.size = config.getFloat(
                "landmass_size",
                0.45,
                0,
                1,
                "Increasing/Decreasing this value will increase/decrease the land to ocean ratio. This is not a percentage of landmass yet."
            );
        }
    }

}
