package weightedgpa.infinibiome.internal.generators.interchunks;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorld;
import net.minecraft.world.gen.feature.ConfiguredFeature;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.IFeatureConfig;
import net.minecraft.world.gen.feature.NoFeatureConfig;
import org.apache.commons.lang3.Validate;
import weightedgpa.infinibiome.api.dependency.DependencyInjector;
import weightedgpa.infinibiome.api.generators.InterChunkGenTimings;
import weightedgpa.infinibiome.api.generators.Timing;
import weightedgpa.infinibiome.internal.floatfunc.FloatFunc;
import weightedgpa.infinibiome.api.generators.InterChunkGen;
import weightedgpa.infinibiome.internal.floatfunc.util.Interval;
import weightedgpa.infinibiome.internal.generators.utils.*;
import weightedgpa.infinibiome.internal.generators.utils.condition.Condition;
import weightedgpa.infinibiome.internal.generators.utils.condition.ConditionHelper;
import weightedgpa.infinibiome.internal.generators.utils.condition.ConditionList;
import weightedgpa.infinibiome.internal.minecraftImpl.commands.DebugCommand;
import weightedgpa.infinibiome.internal.misc.MCHelper;
import weightedgpa.infinibiome.internal.misc.MathHelper;
import weightedgpa.infinibiome.api.pointsprovider.PointsProvider;
import weightedgpa.infinibiome.api.pos.BlockPos2D;
import weightedgpa.infinibiome.api.pos.InterChunkPos;
import weightedgpa.infinibiome.internal.minecraftImpl.world.ChangeDetectingWorld;

import java.util.Random;

import static weightedgpa.infinibiome.internal.generators.utils.condition.ConditionList.*;

public abstract class SmallObjectGenBase extends GeneratorBase implements InterChunkGen {
    public Config config;

    protected SmallObjectGenBase(DependencyInjector di, String seedBranch) {
        super(di, seedBranch);

        DebugCommand.registerDebugFunc(
            seedBranch,
            "count",
            p -> config.countFunc.getOutput(p)
        );

        DebugCommand.registerDebugFunc(
            seedBranch,
            "actualCount",
            p -> getActualCount(new InterChunkPos(p))
        );

        DebugCommand.registerDebugFunc(
            seedBranch,
            "conditions",
            p -> config.conditions._debug(p)
        );
    }

    @Override
    public Timing getInterChunkTiming() {
        return InterChunkGenTimings.STRUCTS;
    }

    @Override
    public final void generate(InterChunkPos interChunkPos, IWorld world) {
        //world = new NoClientUpdatingWorld(world);

        int count = getActualCount(interChunkPos);

        Random random = randomGen.getRandom(interChunkPos.getX(), interChunkPos.getZ());

        for (int i = 0; i < count; i++){
            for (int j = 0; j < config.maxAttempts; j++) {
                BlockPos2D randomPos = interChunkPos.getRandomCenterPos(random);

                //stops attempting to prevent higher count around prohibited places;
                if (!canGenerateHere(randomPos, random)) break;

                int height = config.getHeightFunc.getHeight(randomPos, world, random);

                ChangeDetectingWorld worldWrapper = new ChangeDetectingWorld(world);

                config.generateObjectFunc.run(
                    randomPos.to3D(height),
                    worldWrapper,
                    random
                );

                if (worldWrapper.anyChange()) break;
            }
        }
    }

    private boolean canGenerateHere(BlockPos2D pos, Random random){
        double specificProbability = config.conditions.getAllProbability(
            pos,
            StrictOption.ONLY
        );

        return MathHelper.randomBool(specificProbability, random);
    }

    private int getActualCount(InterChunkPos interChunkPos){
        double avgCount = 1;

        avgCount *= config.countFunc.getOutput(interChunkPos.getLowestCenterBlockPos());

        avgCount *= config.conditions.getAllProbability(
            interChunkPos,
            StrictOption.EXCLUDE
        );

        Random random = randomGen.getRandom(interChunkPos.getX(), interChunkPos.getZ());

        return MathHelper.randomRound(avgCount, random);
    }
    
    @FunctionalInterface
    interface GenerateObjectFunc{
        void run(BlockPos pos, IWorld world, Random random);
    }
    
    @FunctionalInterface
    protected interface GetHeightFunc{
        int getHeight(BlockPos2D pos, IWorld world, Random random);
    }
    
    protected final Config.GenStep initConfig(){
        return new Config().new GenStep();
    }
    
    protected class Config{
        private Config(){}
        
        private GenerateObjectFunc generateObjectFunc;
        
        private FloatFunc<BlockPos2D> countFunc;
        private int maxAttempts;

        private GetHeightFunc getHeightFunc;
        
        public ConditionList conditions = new ConditionList(
            ConditionHelper.onlyWhereInfibiomeGenAllowed(di)
        );

        public class GenStep{
            public CountStep getGenerateFunc(GenerateObjectFunc func){
                generateObjectFunc = func;
                
                return new CountStep();
            }

            public CountStep setWithFeature(ConfiguredFeature<?, ?> feature){
                return getGenerateFunc(
                    (pos, world, random) -> feature.place(
                        world,
                        chunkGenerator,
                        random,
                        pos
                    )
                );
            }

            public CountStep setWithFeature(Feature<NoFeatureConfig> feature){
                return setWithFeature(
                    feature.withConfiguration(IFeatureConfig.NO_FEATURE_CONFIG)
                );
            }

            public <I extends IFeatureConfig> CountStep setWithFeature(Feature<I> feature, I config){
                return setWithFeature(
                    feature.withConfiguration(config)
                );
            }
        }

        public class CountStep{
            public AttemptsStep setCount(FloatFunc<BlockPos2D> count){
                Validate.isTrue(Interval.NON_NEGATIVES.containsAll(count.getOutputInterval()));
                
                countFunc = count;
                
                return new AttemptsStep();
            }


            public AttemptsStep setCount(double count){
                return setCount(FloatFunc.constFunc(count));
            }
        }

        public  class AttemptsStep{
            public HeightStep setAttemptsPerCount(int attempts){
                maxAttempts = attempts;
                
                return new HeightStep();
            }
        }

        public class HeightStep{
            public ChanceStep aboveHighestTerrainBlock(){
                return customHeightFunc(
                    (p, w, r) -> MCHelper.getHighestTerrainHeight(p, w) + 1
                );
            }

            public ChanceStep atHighestTerrainBlock(){
                return customHeightFunc(
                    (p, w, r) -> MCHelper.getHighestTerrainHeight(p, w)
                );
            }

            public ChanceStep randomUnderHeight(int max){
                return randomBetweenHeight(0, max);
            }

            public ChanceStep randomBetweenHeight(int min, int max){
                return customHeightFunc(
                    (p, w, r) -> MathHelper.randomInt(
                        min,
                        max,
                        r
                    )
                );
            }

            public ChanceStep customHeightFunc(GetHeightFunc getHeight){
                getHeightFunc = getHeight;

                return new ChanceStep();
            }
        }

        public class ChanceStep{
            public ConditionStep noChancePerChunk(){
                return new ConditionStep();
            }

            public ConditionStep setChancePerChunk(double chance){
                conditions = conditions.add(
                    ConditionHelper.chancePerChunk(chance)
                );

                return new ConditionStep();
            }

            public ConditionStep onlyInRegion(double rate){
                conditions = conditions.add(
                    ConditionHelper.onlyInRegion(
                        seed,
                        rate
                    )
                );

                return new ConditionStep();
            }
        }

        public class ConditionStep{
            public Config addExtraConditions(Condition extraCondition0, Condition... extraConditions){
                conditions = conditions.add(extraCondition0);
                conditions = conditions.add(extraConditions);
                
                return Config.this;
            }

            public Config noExtraConditions() {
                return Config.this;
            }
        }
    }

    @Override
    public void checkIsValid() {
        Validate.notNull(config);
    }

    public PointsProvider<BlockPos2D> getAllLocations(){
        return new PredicateSearcher<>(
            1,
            p -> getActualCount(p) > 0 && config.conditions.canBeHere(p),
            InterChunkPos.INFO
        )
        .mapPoints(
            BlockPos2D.INFO
        );
    }
}
