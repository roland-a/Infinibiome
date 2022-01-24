package weightedgpa.infinibiome.internal.generators.utils.condition;

import net.minecraft.util.math.ChunkPos;
import org.apache.commons.lang3.Validate;
import weightedgpa.infinibiome.api.dependency.MultiDep;
import weightedgpa.infinibiome.api.dependency.DependencyInjector;
import weightedgpa.infinibiome.api.generators.Seed;
import weightedgpa.infinibiome.api.pointsprovider.PointsProvider;
import weightedgpa.infinibiome.internal.floatfunc.generators.RandomGen;
import weightedgpa.infinibiome.internal.misc.*;
import weightedgpa.infinibiome.internal.pointsprovider.*;
import weightedgpa.infinibiome.api.pos.InterChunkPos;
import weightedgpa.infinibiome.api.posdata.LandmassInfo;
import weightedgpa.infinibiome.api.posdata.PosDataHelper;
import weightedgpa.infinibiome.internal.floatfunc.FloatFunc;
import weightedgpa.infinibiome.internal.floatfunc.modifiers.IntervalMapperWrapper;
import weightedgpa.infinibiome.internal.floatfunc.util.Interval;
import weightedgpa.infinibiome.api.pos.BlockPos2D;
import weightedgpa.infinibiome.api.posdata.PosDataKeys;
import weightedgpa.infinibiome.api.posdata.PosDataProvider;
import weightedgpa.infinibiome.internal.minecraftImpl.commands.DebugCommand;
import weightedgpa.infinibiome.internal.generators.interchunks.tree.TreeGens;
import weightedgpa.infinibiome.internal.misc.Helper;

import java.util.*;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.function.Predicate;

@SuppressWarnings("WeakerAccess")
public final class ConditionHelper {
    private ConditionHelper(){}

    public static Condition chancePerChunk(double rate){
        return new Condition() {
            @Override
            public double getProbability(BlockPos2D pos) {
                return rate;
            }

            @Override
            public String toString() {
                return "ChancePerChunk{chance=" + rate + "}";
            }
        };
    }

    public static Condition onlyInRegion(Seed seed, double rate) {
        return onlyInRegion(seed, rate, 0.5f);
    }

    public static Condition onlyInRegion(Seed seed, double rate, double fade){
        seed = seed.newSeed("spawnRegion");

        Validate.isTrue(rate >= 0);
        Validate.isTrue(Interval.PERCENT.contains(fade));

        if (rate == 0){
            return new Condition.BoolInterpolated() {
                @Override
                public boolean passes(BlockPos2D pos){
                    return false;
                }

                @Override
                public String toString() {
                    return "AlwaysNoRegion{}";
                }
            };
        }

        FloatFunc<BlockPos2D> radiusFunc = initRegionRadius(seed);
        PointsProvider<BlockPos2D> centers = initRegionCenters(seed, rate);

        FloatFunc<BlockPos2D> noise = new IntervalMapperWrapper<BlockPos2D>(
            input -> MathHelper.gradientTowardsPoint(
                input,
                radiusFunc.getOutput(input),
                centers
            )
        )
        .addBranch(
            new Interval(0, fade),
            0, 1
        )
        .addBranch(
            new Interval(fade, 1),
            1, 1
        );

        return new Condition() {
            @Override
            public double getProbability(BlockPos2D pos){
                return noise.getOutput(pos);
            }

            @Override
            public String toString() {
                return "OnlyInRegion{}";
            }
        };
    }

    private static FloatFunc<BlockPos2D> initRegionRadius(Seed seed){
        seed = seed.newSeed("radius");

        return Helper.initUniformNoise(seed, Helper.COMMON_SCALE)
            .mapInterval(
                new Interval(100, 500)
            );
    }

    private static PointsProvider<BlockPos2D> initRegionCenters(Seed seed, double rate){
        seed = seed.newSeed("regionCenter");

        return new GridRandomPoints<>(
            seed,
            FloatFunc.<BlockPos2D>constFunc(rate).randomRound(seed, BlockPos2D.INFO),
            512,
            BlockPos2D.INFO
        );
    }

    public static Condition onlyInTemperature(DependencyInjector di, Interval interval){
        return new Condition() {
            final FloatFunc<BlockPos2D> tempFunc = initClimateNoise(
                PosDataHelper.initTemperatureNoise2D(di),
                0.05f,
                interval
            );

            @Override
            public double getProbability(BlockPos2D pos){
                return tempFunc.getOutput(pos);
            }

            @Override
            public String toString() {
                return "OnlyInTemperature{}";
            }
        };
    }

    public static Condition onlyInHumidity(DependencyInjector di, Interval interval){
        return new Condition() {
            final FloatFunc<BlockPos2D> humidityFunc = initClimateNoise(
                PosDataHelper.initHumidityNoise2D(di),
                0.05f,
                interval
            );

            @Override
            public double getProbability(BlockPos2D pos){
                return humidityFunc.getOutput(pos);
            }

            @Override
            public String toString() {
                return "OnlyInHumidity{}";
            }
        };
    }

    private static FloatFunc<BlockPos2D> initClimateNoise(FloatFunc<BlockPos2D> base, double dist, Interval interval){
        Validate.isTrue(
            Interval.PERCENT.containsAll(interval)
        );
        Validate.isTrue(dist > 0);

        if (interval.getSize() < dist * 2){
           dist = interval.getSize() * .2;
        }

        IntervalMapperWrapper<BlockPos2D> intervalMapper = new IntervalMapperWrapper<>(base);

        Interval baseIntervalShrunk = shrinkInterval(interval, dist);

        intervalMapper.addBranch(
            baseIntervalShrunk.initBehind(dist),
            0, 1
        );

        intervalMapper.addBranch(
            baseIntervalShrunk,
            1, 1
        );

        intervalMapper.addBranch(
            baseIntervalShrunk.initAhead(dist),
            1, 0
        );

        return intervalMapper;
    }

    private static Interval shrinkInterval(Interval interval, double dist){
        double min;

        if (interval.getMin() == 0){
            min = 0;
        }
        else {
            min = interval.getMin() + dist;
        }

        double max;

        if (interval.getMax() == 1){
            max = 1;
        }
        else {
            max = interval.getMax() - dist;
        }

        return new Interval(min, max);
    }

    public static Condition onlyInAmp(DependencyInjector di, Interval validAmp){
        DebugCommand.registerDebugFunc(
            "posData",
            "amp",
            p -> di.get(PosDataProvider.class).get(PosDataKeys.AMP, p)
        );

        return new Condition.BoolInterpolated() {
            final PosDataProvider posData = di.get(PosDataProvider.class);

            @Override
            public boolean passes(BlockPos2D pos){
                return validAmp.contains(posData.get(PosDataKeys.AMP, pos));
            }

            @Override
            public String toString() {
                return "OnlyInAmp{}";
            }
        };
    }

    public static Condition onlyInHeight(DependencyInjector di, int radius, Interval validHeight){
        return new Condition.BoolInterpolated() {
            final PosDataProvider posData = di.get(PosDataProvider.class);
            
            @Override
            public boolean passes(BlockPos2D pos){
                return Helper.passesSurroundingTest(
                    pos,
                    radius,
                    p -> validHeight.contains(posData.get(PosDataKeys.MAPPED_HEIGHT, p)),
                    BlockPos2D.INFO
                );
            }

            @Override
            public boolean isSlow() {
                return radius != 0;
            }

            @Override
            public String toString() {
                return "OnlyInHeight{}";
            }
        };
    }

    public static Condition onlyInHeight(DependencyInjector di, Interval validHeight){
        return onlyInHeight(di, 0, validHeight);
    }

    public static Condition onlyInLandMass(DependencyInjector di, Predicate<LandmassInfo> predicate){
        return onlyInLandMass(di, 0, predicate);
    }

    public static Condition onlyInLandMass(DependencyInjector di, int radius, Predicate<LandmassInfo> predicate){
        return new Condition.BoolStrict() {
            final PosDataProvider posData = di.get(PosDataProvider.class);

            @Override
            public boolean passes(BlockPos2D pos){
                return Helper.passesSurroundingTest(
                    pos,
                    radius,
                    p -> predicate.test(posData.get(PosDataKeys.LANDMASS_TYPE, p)),
                    BlockPos2D.INFO
                );
            }

            @Override
            public boolean isSlow() {
                return radius != 0;
            }

            @Override
            public String toString() {
                return "OnlyInLandmass{}";
            }
        };
    }

    public static Condition onlyInTreeDensity(DependencyInjector di, Interval allowedTreeDensity){
        return new Condition.BoolInterpolated() {
            private final TreeGens treeGen = di.get(TreeGens.class);

            private final PosDataProvider posData = di.get(PosDataProvider.class);

            @Override
            public boolean passes(BlockPos2D pos){
                double treeDensity;

                double humidity = PosDataHelper.getHumidity(pos, posData);

                if (PosDataHelper.DRY_INTERVAL.contains(humidity) || posData.get(PosDataKeys.LANDMASS_TYPE, pos).isOcean()){
                    treeDensity = 0;
                }
                else {
                    treeDensity = treeGen.getApproxDensity(new InterChunkPos(pos));
                }

                return allowedTreeDensity.contains(treeDensity);
            }

            @Override
            public boolean isSlow() {
                return true;
            }

            @Override
            public String toString() {
                return "OnlyInTreeDensity{}";
            }
        };
    }

    public static Condition onlyInSlope(DependencyInjector di, int radius, Interval validSlope){
        PosDataProvider posData = di.get(PosDataProvider.class);

        DebugCommand.registerDebugFunc(
            "slope",
            "10",
            p -> String.valueOf(
                PosDataHelper.getAverageSlope(
                    p,
                    10,
                    posData
                )
            )
        );

        DebugCommand.registerDebugFunc(
            "slope",
            "20",
            p -> String.valueOf(
                PosDataHelper.getAverageSlope(
                    p,
                    20,
                    posData
                )
            )
        );
        
        return onlyInSlope(
            p -> posData.get(PosDataKeys.MAPPED_HEIGHT, p),
            p -> !PosDataHelper.isUnderwaterPortionOfLakeOrRiver(p, posData),
            radius,
            validSlope
        );
    }

    public static Condition onlyInSlope(
        Function<BlockPos2D, Double> heightFunc,
        Predicate<BlockPos2D> shouldCount,
        int radius,
        Interval validSlope
    ){
        return new Condition.BoolInterpolated() {
            @Override
            public boolean passes(BlockPos2D pos){
                return validSlope.contains(
                    PosDataHelper.getAverageSlope(
                        pos,
                        radius,
                        heightFunc,
                        shouldCount
                    )
                );
            }

            @Override
            public boolean isSlow() {
                return true;
            }

            @Override
            public String toString() {
                return "OnlyInSlope{}";
            }
        };
    }

    public static Condition onlyInMushroomIsland(DependencyInjector di){
        return new Condition.BoolStrict() {
            final PosDataProvider posData = di.get(PosDataProvider.class);

            @Override
            public boolean passes(BlockPos2D pos) {
                return posData.get(PosDataKeys.IS_MUSHROOM_ISLAND, pos);
            }

            @Override
            public String toString() {
                return "OnlyInMushroomIsland{}";
            }
        };
    }

    @SafeVarargs
    public static <T extends MultiDep> Condition onlyIfNotNear(
        DependencyInjector di,
        int radius,
        BiPredicate<T, InterChunkPos> spawnsIn,
        Class<? extends T>... generatorBase
    ){
        List<T> genBases = new ArrayList<>();

        for (Class<? extends T> mobClass : generatorBase) {
            genBases.addAll(di.getAll(mobClass));
        }

        return new Condition.BoolInterpolated() {
            @Override
            public boolean passes(BlockPos2D pos){
                InterChunkPos interChunkPos = new InterChunkPos(pos);
                
                for (T base: genBases) {
                    for (int x = -radius; x <= radius; x++) {
                        for (int z = -radius; z <= radius; z++) {
                            if (spawnsIn.test(base, interChunkPos.offset(x, z))) return false;
                        }
                    }
                }
                return true;
            }

            @Override
            public boolean isSlow() {
                return true;
            }

            @Override
            public String toString() {
                return "OnlyNotNear{list=" + Arrays.asList(generatorBase) + "}";
            }
        };
    }

    public static Condition onlyInPoints(PointsProvider<InterChunkPos> pointsProvider){
        return new Condition.BoolStrict() {
            @Override
            public boolean passes(BlockPos2D pos){
                InterChunkPos interChunkPos = new InterChunkPos(pos);
                
                return pointsProvider.hasPoint(interChunkPos);
            }

            @Override
            public String toString() {
                return "OnlyInPoints{}";
            }
        };
    }

    public static PointsProvider<ChunkPos> initSeparatedChunkLocations(Seed seed, int gridSize, double rate){
        Validate.isTrue(gridSize > 0);
        Validate.isTrue(
            Interval.PERCENT.contains(rate)
        );

        seed = seed.newSeed("chunkPosLocations");

        if (rate == 0){
            return new EmptyPointsProvider<>(MCHelper.CHUNK_POS_INFO);
        }

        return new GridLowestPoint<>(
            gridSize,
            MCHelper.CHUNK_POS_INFO
        )
        .filterOutput(
            FloatFunc.<ChunkPos>constFunc(rate).randomBool(MCHelper.CHUNK_POS_INFO, seed)
        );
    }

    public static Condition switchBetweenConditions(
        Condition chooser,
        Condition runWhen1,
        Condition runWhen0
    ){
        return new Condition() {
            @Override
            public double getProbability(BlockPos2D pos){
                double chooserOutput = chooser.getProbability(pos);

                if (chooserOutput == 0) return runWhen0.getProbability(pos);

                if (chooserOutput == 1) return runWhen1.getProbability(pos);

                return MathHelper.lerp(
                    chooserOutput,
                    runWhen0.getProbability(pos),
                    runWhen1.getProbability(pos)
                );
            }

            @Override
            public boolean isSlow() {
                return
                    chooser.isSlow() ||
                    runWhen1.isSlow() ||
                    runWhen0.isSlow();
            }

            @Override
            public boolean likelyConflicts(Condition other) {
                return false;
            }

            @Override
            public String toString() {
                return "Switch{chooser=" + chooser + ", runWhen1=" + runWhen1 + ", runWhen0=" + runWhen0 + "}";
            }
        };
    }

    public static Condition onlyWhereInfibiomeGenAllowed(DependencyInjector di){
        PosDataProvider posData = di.get(PosDataProvider.class);

        return new Condition.BoolStrict() {
            @Override
            public boolean passes(BlockPos2D pos) {
                return posData.get(PosDataKeys.AllOW_INFINIBIOME_GEN, pos);
            }

            @Override
            public String toString() {
                return "OnlyWhereIBGenAllowed{}";
            }
        };
    }
}
