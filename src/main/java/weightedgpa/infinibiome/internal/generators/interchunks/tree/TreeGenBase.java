package weightedgpa.infinibiome.internal.generators.interchunks.tree;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.gen.feature.*;
import net.minecraftforge.common.Tags;
import org.apache.commons.lang3.Validate;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.IWorld;
import weightedgpa.infinibiome.internal.generators.utils.condition.Condition;
import weightedgpa.infinibiome.internal.generators.utils.condition.ConditionHelper;
import weightedgpa.infinibiome.internal.generators.utils.condition.ConditionList;
import weightedgpa.infinibiome.internal.misc.Pair;
import weightedgpa.infinibiome.api.generators.nonworldgen.Locatable;
import weightedgpa.infinibiome.api.dependency.DependencyInjector;
import weightedgpa.infinibiome.api.pointsprovider.PointsProvider;
import weightedgpa.infinibiome.api.posdata.PosDataHelper;
import weightedgpa.infinibiome.api.posdata.PosDataKeys;
import weightedgpa.infinibiome.internal.floatfunc.FloatFunc;
import weightedgpa.infinibiome.internal.floatfunc.IntFunc;
import weightedgpa.infinibiome.internal.floatfunc.generators.RandomGen;
import weightedgpa.infinibiome.api.generators.nonworldgen.SaplingController;
import weightedgpa.infinibiome.internal.generators.utils.*;
import weightedgpa.infinibiome.internal.minecraftImpl.commands.DebugCommand;
import weightedgpa.infinibiome.internal.minecraftImpl.world.ClientUpdatingWorld;
import weightedgpa.infinibiome.internal.misc.*;
import weightedgpa.infinibiome.api.generators.*;
import weightedgpa.infinibiome.api.pos.BlockPos2D;
import net.minecraft.util.math.BlockPos;
import weightedgpa.infinibiome.api.pos.InterChunkPos;
import weightedgpa.infinibiome.internal.minecraftImpl.world.ChangeHoldingWorld;

import java.util.List;
import java.util.Random;
import java.util.function.Predicate;

import static weightedgpa.infinibiome.internal.generators.utils.condition.ConditionList.*;

public abstract class TreeGenBase extends GeneratorBase implements TreeGen, SaplingController, Locatable.HasPointsProvider {
    Config<?> config;

    final PlantGrowthConfig growthConfig;

    TreeGenBase(DependencyInjector di, String seedBranch) {
        super(di, seedBranch);

        this.growthConfig = di.get(PlantGrowthConfig.class);

        DebugCommand.registerDebugFunc(
            seedBranch,
            "density",
            p -> getDensity(new InterChunkPos(p))
        );

        DebugCommand.registerDebugFunc(
            seedBranch,
            "rawDensity",
            p -> config.rawDensityFunc.getOutput(p)
        );

        DebugCommand.registerDebugFunc(
            seedBranch,
            "conditions",
            p -> config.conditions._debug(p)
        );
    }

    @Override
    public final double getDensity(InterChunkPos interChunkPos) {
        double result = 1;

        result *= config.conditions.getAllProbability(
            interChunkPos,
            StrictOption.USE_LIKE_NON_STRICT
        );

        if (result == 0) return 0;

        result *= config.rawDensityFunc.getOutput(interChunkPos.getLowestCenterBlockPos());

        return result;
    }

    @Override
    public final boolean controlsPlant(BlockState block, boolean is2x2) {
        Block sapling = (Block) config.configFunc.get(
            new BlockPos2D(0, 0),
            0,
            new Random()
        )
            .getSapling();

        return sapling.equals(block.getBlock()) && config.is2x2.test(is2x2);
    }

    @Override
    public boolean isValidGrowth(BlockPos pos, IWorld world) {
        return isValidSpace(pos, world);
    }

    @Override
    public final boolean canGrowWithBonemeal(BlockPos pos, IWorld world, Random random) {
        if (growthConfig.allowSaplingUniformGrowth){
            return true;
        }
        if (config.conditions.canBeHere(MCHelper.to2D(pos))) {
            return true;
        }
        return MathHelper.randomBool(0.25, random);
    }

    @Override
    public final void generate(BlockPos treePos, IWorld world) {
        tryGenerateTree(
            treePos,
            world,//new NoClientUpdatingWorld(world),
            randomGen.getRandom(treePos.getX(), treePos.getZ()),
            false
        );
    }

    @Override
    public final void growFromSapling(BlockPos pos, IWorld world, Random random) {
        tryGenerateTree(
            pos,
            new ClientUpdatingWorld(world),
            random,
            true
        );
    }

    private void tryGenerateTree(BlockPos treePos, IWorld world, Random random, boolean isSapling){
        BlockPos2D treePos2D = MCHelper.to2D(treePos);

        if (!isSapling) {
            if (nearPathOrFence(treePos2D, world)) return;

            if (collidesWithOtherTree(treePos2D, world)) return;
        }

        int trunkHeight = config.heightFunc.getIntOutput(treePos2D);

        BaseTreeFeatureConfig treeConfig = config.configFunc.get(treePos2D, trunkHeight, random);

        if (isSapling){
            treeConfig.forcePlacement = true;
        }

        ChangeHoldingWorld worldWrapper = new ChangeHoldingWorld(
            new NoPlantWrapper(world)
        );

        removeStoneOrGravel(treePos.down(), worldWrapper);

        //noinspection unchecked,rawtypes,rawtypes
        ((Feature) config.feature).place(
            worldWrapper,
            chunkGenerator,
            random,
            treePos,
            treeConfig
        );

        if (worldWrapper.changeCount() > 4){
            worldWrapper.loadChange();
        }

        if (!isSapling){
            TreeHelper.fixTwoByTwoTrees(treePos, world);
        }

        if (isSapling){
            treeConfig.forcePlacement = false;
        }
    }

    private void removeStoneOrGravel(BlockPos groundPos, IWorld world){
        //makes sure trees wont generate on cave surfaces
        if (groundPos.getY() != (int)posData.get(PosDataKeys.MAPPED_HEIGHT, MCHelper.to2D(groundPos))) return;

        if (!originallyDirt(MCHelper.to2D(groundPos))) return;

        world.setBlockState(groundPos, Blocks.DIRT.getDefaultState(), MCHelper.DEFAULT_FLAG);
    }

    private boolean originallyDirt(BlockPos2D pos){
        List<BlockState> groundBlocks = posData.get(PosDataKeys.GROUND_BLOCKS, pos);

        if (groundBlocks.isEmpty()) return false;

        if (!groundBlocks.get(0).equals(Blocks.DIRT.getDefaultState())) return false;

        return true;
    }

    private boolean nearPathOrFence(BlockPos2D pos, IWorldReader world){
        for (int xOffset = -3; xOffset <= 3; xOffset++){
            for (int zOffset = -3; zOffset <= 3; zOffset++){
                BlockPos currGroundPos = pos
                    .offset(xOffset, zOffset)
                    .to3D(p -> MCHelper.getHighestTerrainHeight(p, world)
                );

                if (world.getBlockState(currGroundPos).getBlock().equals(Blocks.GRASS_PATH)) return true;

                if (world.getBlockState(currGroundPos.up()).isIn(Tags.Blocks.FENCES)) return true;
            }
        }
        return false;
    }

    private boolean collidesWithOtherTree(BlockPos2D pos, IWorldReader world){
        int radius = config.isolationRadius;

        for (int x = -radius; x <= radius; x++){
            for (int z = -radius; z <= radius; z++){
                BlockPos2D currPos2D = pos.offset(x, z);

                int checkHeight = MCHelper.getHighestTerrainHeight(currPos2D, world) + 1;

                Block block = world.getBlockState(currPos2D.to3D(checkHeight)).getBlock();

                if (BlockTags.LOGS.contains(block)) return true;
            }
        }
        return false;
    }

    private boolean isValidSpace(BlockPos pos, IWorld world){
        Random random = new Random(0);

        ChangeHoldingWorld changeHoldingWorld = new ChangeHoldingWorld(new NoPlantWrapper(world));

        tryGenerateTree(pos, changeHoldingWorld, random, true);

        return changeHoldingWorld.anyChange();
    }

    @Override
    public final void checkIsValid() {
        Validate.notNull(config);
    }

    @Override
    public final PointsProvider<BlockPos2D> getAllLocations() {
        return GenHelper.getCommonPredicateSearcher(
            32,
            false,
            config.conditions.add(
                new Condition.BoolInterpolated() {
                    @Override
                    public boolean passes(BlockPos2D pos) {
                    double humidity = PosDataHelper.getHumidity(pos, posData);

                    return !PosDataHelper.DRY_INTERVAL.contains(humidity);
                    }
                }
            ),
            posData
        );
    }

    interface ConfigFunc<T extends BaseTreeFeatureConfig>{
        T get(BlockPos2D pos, int height, Random random);
    }

    <T extends BaseTreeFeatureConfig> Config<T>.FeatureStep initConfig(){
        return new Config<T>().new FeatureStep();
    }

    class Config<T extends BaseTreeFeatureConfig>{
        private Config(){}

        Feature<T> feature;
        ConfigFunc<T> configFunc;
        IntFunc<BlockPos2D> heightFunc;

        Predicate<Boolean> is2x2;
        int isolationRadius;
        FloatFunc<BlockPos2D> rawDensityFunc;

        ConditionList conditions = new ConditionList(
            ConditionHelper.onlyInMushroomIsland(di).invert(),
            ConditionHelper.onlyWhereInfibiomeGenAllowed(di)
        );

        class FeatureStep{
            ConfigStep setFeature(Feature<T> feature_){
                feature = feature_;

                return new ConfigStep();
            }
        }

        class ConfigStep{
            HeightStep setConfigFunc(ConfigFunc<T> configFunc_){
                configFunc = configFunc_;

                return new HeightStep();
            }
        }

        class HeightStep{
            IsolationStep setHeightFunc(int min, int max){
                heightFunc = new RandomGen(seed).<BlockPos2D>asPercentFloatFunc(BlockPos2D.INFO)
                    .mapToIntInterval(
                        min, max
                    );

                return new IsolationStep();
            }

            IsolationStep setHeightFunc(Pair<Integer, Integer> heightInterval){
                return setHeightFunc(
                    heightInterval.first,
                    heightInterval.second
                );
            }
        }

        class IsolationStep{
            _2x2Step setIsolationRadius(int radius){
                isolationRadius = radius;

                return new _2x2Step();
            }
        }

        class _2x2Step{
            DensityStep set2x2ConfigOption(Predicate<Boolean> is2x2){
                Config.this.is2x2 = is2x2;

                return new DensityStep();
            }

            DensityStep onlyGrowIn2x2Config(){
                return set2x2ConfigOption(is2x2 -> is2x2);
            }

            DensityStep onlyGrowIn1x1Config(){
                return set2x2ConfigOption(is2x2 -> !is2x2);
            }

            DensityStep growInAnySaplingConfig(){
                return set2x2ConfigOption(is2x2 -> true);
            }
        }

        class DensityStep{
            RegionStep setWithCommonDensity(){
                return setDensity(TreeHelper.initCommonDensity(seed));
            }

            RegionStep setDensity(FloatFunc<BlockPos2D> density){
                rawDensityFunc = density;

                return new RegionStep();
            }
        }

        class RegionStep{
            ConditionStep setRegionRate(double rate){
                conditions = conditions.add(
                    ConditionHelper.onlyInRegion(
                        seed,
                        rate
                    )
                );

                return new ConditionStep();
            }
        }

        class ConditionStep{
            Config<?> addExtraConditions(Condition extraCondition0, Condition... extraConditions){
                conditions = conditions.add(extraCondition0);
                conditions = conditions.add(extraConditions);

                return Config.this;
            }

            Config<?> addExtraConditions(List<Condition> extraConditions){
                conditions = conditions.add(extraConditions);

                return Config.this;
            }

            Config<?> noExtraConditions(){
                return Config.this;
            }
        }

    }
}
