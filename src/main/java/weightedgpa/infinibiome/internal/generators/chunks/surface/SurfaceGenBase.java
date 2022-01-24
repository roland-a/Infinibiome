package weightedgpa.infinibiome.internal.generators.chunks.surface;

import com.google.common.collect.Lists;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;
import net.minecraft.block.BlockState;
import org.apache.commons.lang3.Validate;
import weightedgpa.infinibiome.api.dependency.DependencyInjector;
import weightedgpa.infinibiome.api.generators.nonworldgen.Locatable;
import weightedgpa.infinibiome.api.pointsprovider.PointsProvider;
import weightedgpa.infinibiome.api.posdata.LandmassInfo;
import weightedgpa.infinibiome.internal.generators.utils.*;
import weightedgpa.infinibiome.internal.generators.utils.condition.Condition;
import weightedgpa.infinibiome.internal.generators.utils.condition.ConditionHelper;
import weightedgpa.infinibiome.internal.generators.utils.condition.ConditionList;
import weightedgpa.infinibiome.internal.misc.*;
import weightedgpa.infinibiome.api.generators.*;
import weightedgpa.infinibiome.api.pos.BlockPos2D;
import net.minecraft.util.math.BlockPos;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Random;

public abstract class SurfaceGenBase extends GeneratorBase implements SurfaceGen, Locatable.HasPointsProvider {
    protected Config config;

    private final Patches patches;

    SurfaceGenBase(DependencyInjector di, String seedBranch) {
        super(di, seedBranch);

        patches = new Patches(seed);
    }

    @Override
    public void checkIsValid() {
        Validate.notNull(config);
    }

    @Override
    public void generate(BlockPos2D pos, IWorld world, Random random) {
        double probability;

        probability = config.conditions.getAllProbability(
            pos,
            ConditionList.StrictOption.USE_LIKE_NON_STRICT
        );

        if (!MathHelper.randomBool(probability, random)) return;

        BlockPos groundPos = pos.to3D(
            MCHelper.getHighestTerrainHeight(pos, world)
        );

        if (!validAboveGround(groundPos, world)) return;

        if (config.isPatchy && !patches.canPlaceAt(pos, probability)) return;

        placeSurface(groundPos, world);
    }

    private void placeSurface(BlockPos groundPos, IWorld world){
        for (int y = 0; y < 4; y++){
            BlockPos placePos = groundPos.down(y);

            if (placePos.getY() <= 1) break;

            if (!isValidGround(placePos, world)) break;

            world.setBlockState(placePos, config.block, MCHelper.DEFAULT_FLAG);

            if (config.isSurfaceOnly) break;
        }
    }

    private boolean validAboveGround(BlockPos groundPos, IBlockReader world){
        BlockState aboveGroundBlock = world.getBlockState(groundPos.up());

        boolean validUnderwater =
            config.isAlsoUnderwater &&
            MCHelper.isMostlyWater(aboveGroundBlock);

        return MCHelper.isMostlyAir(aboveGroundBlock) || validUnderwater;
    }

    private boolean isValidGround(BlockPos pos, IBlockReader world){
        return config.validBlock.contains(
            world.getBlockState(pos).getBlock()
        );
    }

    @Override
    public PointsProvider getAllLocations() {
        return new PredicateSearcher<>(
            32,
            p -> config.conditions.canBeHere(p),
            BlockPos2D.INFO
        );
    }

    Config.BlockStep initConfig(){
        return new Config().new BlockStep();
    }

    @SuppressWarnings("InstanceVariableMayNotBeInitialized")
    class Config{
        private Config(){}

        private BlockState block;
        private boolean isPatchy;
        private boolean isSurfaceOnly;
        private boolean isAlsoUnderwater;
        private Collection<Block> validBlock;

        private ConditionList conditions = new ConditionList(
            ConditionHelper.onlyInLandMass(
                di,
                LandmassInfo::isLand
            ),
            ConditionHelper.onlyWhereInfibiomeGenAllowed(di)
        );

        class BlockStep{
            IsPatchyStep setBlock(BlockState block){
                Config.this.block = block;

                return new IsPatchyStep();
            }
        }

        class IsPatchyStep {
            DepthStep setFull(){
               return setPatchy(false);
            }

            DepthStep setPatchy(){
                return setPatchy(true);
            }

            DepthStep setPatchy(boolean isPatchy){
                Config.this.isPatchy = isPatchy;

                return new DepthStep();
            }
        }

        class DepthStep {
            ValidReplacementStep setSurfaceOnly(){
                Config.this.isSurfaceOnly = true;

                return new ValidReplacementStep();
            }

            ValidReplacementStep setExtendsDown(){
                Config.this.isSurfaceOnly = false;

                return new ValidReplacementStep();
            }
        }

        class ValidReplacementStep {
           ValidAboveBlockStep setSpawnsOnlyInDirt(){
                validBlock = Lists.newArrayList(Blocks.DIRT);

                return new ValidAboveBlockStep();
            }

            ValidAboveBlockStep setSpawnsOnlyInSand(){
                validBlock = Lists.newArrayList(Blocks.SAND);

                return new ValidAboveBlockStep();
            }

            ValidAboveBlockStep setSpawnsOnlyInDirtAndSand(){
                validBlock = Lists.newArrayList(
                    Blocks.DIRT,
                    Blocks.SAND
                );

                return new ValidAboveBlockStep();
            }

            ValidAboveBlockStep setValidBlocks(Collection<Block> validBlocks){
                Config.this.validBlock = new ArrayList<>(validBlocks);

                return new ValidAboveBlockStep();
            }
        }

        class ValidAboveBlockStep {
            MushroomIslandStep setNeverUnderwater(){
                Config.this.isAlsoUnderwater = false;

                return new MushroomIslandStep();
            }

            MushroomIslandStep setAlsoUnderwater(){
                Config.this.isAlsoUnderwater = true;

                return new MushroomIslandStep();
            }
        }

        class MushroomIslandStep {
            RegionStep neverInMushroomIsland(){
                conditions = conditions.add(
                    ConditionHelper.onlyInMushroomIsland(di).invert()
                );

                return new RegionStep();
            }

            RegionStep onlyInMushroomIsland(){
                conditions = conditions.add(
                    ConditionHelper.onlyInMushroomIsland(di)
                );

                return new RegionStep();
            }
        }

        class RegionStep{
            ConditionStep inSpawnRegion(double rate){
                Validate.isTrue(rate > 0);

                double fade;

                if (!isPatchy){
                    fade = 0.25;
                }
                else {
                    fade = 0.5;
                }

                conditions = conditions.add(
                    ConditionHelper.onlyInRegion(
                        seed,
                        rate,
                        fade
                    )
                );

                return new ConditionStep();
            }

            ConditionStep noSpawnRegion() {
                return new ConditionStep();
            }
        }

        class ConditionStep{
            Config addExtraConditions(Condition extraCondition0, Condition... extraConditions){
                conditions = conditions.add(extraCondition0);
                conditions = conditions.add(extraConditions);

                return Config.this;
            }

            Config noExtraConditions(){
                return Config.this;
            }
        }
    }
}
