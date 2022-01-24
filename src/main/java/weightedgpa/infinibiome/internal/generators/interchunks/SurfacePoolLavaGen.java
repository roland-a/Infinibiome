package weightedgpa.infinibiome.internal.generators.interchunks;


import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.util.Direction;
import net.minecraft.world.IWorldReader;
import weightedgpa.infinibiome.api.Infinibiome;
import weightedgpa.infinibiome.api.dependency.DependencyInjector;
import weightedgpa.infinibiome.api.generators.InterChunkGen;
import weightedgpa.infinibiome.api.generators.InterChunkGenTimings;
import weightedgpa.infinibiome.internal.misc.*;
import weightedgpa.infinibiome.internal.floatfunc.modifiers.IntervalMapperWrapper;
import weightedgpa.infinibiome.internal.floatfunc.util.Interval;
import weightedgpa.infinibiome.api.pos.InterChunkPos;
import weightedgpa.infinibiome.api.generators.Timing;
import weightedgpa.infinibiome.api.pos.BlockPos2D;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorld;
import weightedgpa.infinibiome.internal.floatfunc.FloatFunc;
import weightedgpa.infinibiome.internal.generators.utils.GeneratorBase;
import weightedgpa.infinibiome.internal.misc.Helper;


import java.util.Random;

import static weightedgpa.infinibiome.api.posdata.PosDataHelper.*;

public final class SurfacePoolLavaGen extends GeneratorBase implements InterChunkGen {
    private static final int LAVA_BURN_RADIUS = 3;

    private static final double INIT_ROCKY_CHANCE = 0.25f;
    private static final double ROCKY_CHANCE_CHANGE_PER_Y = -0.025f;
    private static final double GRAVEL_CHANCE = 0.25f;
    private static final double MAX_CHANCE_PER_CHUNK = 0.05f;

    private final FloatFunc<BlockPos2D> chancePerChunkFunc;
    private final FloatFunc<BlockPos2D> lavaRadiusFunc;
    private final FloatFunc<BlockPos2D> stoneRadiusFunc;

    public SurfacePoolLavaGen(DependencyInjector di) {
        super(di, Infinibiome.MOD_ID + ":surfaceLavaPool");

        this.chancePerChunkFunc =
            new IntervalMapperWrapper<>(initHumidityNoise2D(di))
                .addBranch(
                    DRY_INTERVAL,
                    1 * MAX_CHANCE_PER_CHUNK,
                    0.75 * MAX_CHANCE_PER_CHUNK
                )
                .addBranch(
                    Interval.union(SEMI_DRY_INTERVAL, SEMI_WET_INTERVAL),
                    0.75 * MAX_CHANCE_PER_CHUNK,
                    0 * MAX_CHANCE_PER_CHUNK
                );

        this.lavaRadiusFunc = Helper.initUniformNoise(seed.newSeed("lavaRadius"), 9)
            .mapInterval(
                new Interval(2, 6)
            );

        this.stoneRadiusFunc = FloatFunc.constFunc(2);
    }

    @Override
    public Timing getInterChunkTiming() {
        return InterChunkGenTimings.LAVA_POOL;
    }

    @Override
    public void generate(InterChunkPos interChunkPos, IWorld interChunks) {
        Random random = randomGen.getRandom(interChunkPos.getX(), interChunkPos.getZ());

        double chancePerChunk = chancePerChunkFunc.getOutput(interChunkPos.getLowestCenterBlockPos());

        if (!MathHelper.randomBool(chancePerChunk, random)){
            return;
        }

        //generates lava pools only at the center of a interChunk to prevent fires
        BlockPos2D pos = interChunkPos.getLowestCenterBlockPos().offset(8,8);

        SurfacePool.tryGeneratePoolAt(pos, getConfig(), interChunks, posData, random);
    }

    private SurfacePool.Config getConfig(){
        return new SurfacePool.Config(
            lavaRadiusFunc,
            stoneRadiusFunc,
            (pos, world, rand) -> Blocks.LAVA.getDefaultState(),
            (pos, world, rand) -> getStoneOrGravel(rand),
            (pool, world, rand) -> isClearOfFlammables(pool, world),
            (pool, world, rand) -> {
                addRockyEffect(pool, world, rand);
                clearNearbyPlantsAndSnow(pool, world);
            }
        );
    }

    private boolean isClearOfFlammables(SurfacePool pool, IWorldReader world){
        for (BlockPos2D currPos2D: pool.iteratePoolAndEdge(LAVA_BURN_RADIUS)){
            for (int y = pool.liquidY; y <= pool.liquidY + LAVA_BURN_RADIUS; y++){
                BlockPos currPos = currPos2D.to3D(y);

                BlockState currBlock = world.getBlockState(currPos);

                //can ignore plants as they can be cleared easily
                if (currBlock.isFlammable(world, currPos, Direction.NORTH) && !MCHelper.isPlant(currBlock.getBlock())){
                    return false;
                }
            }
        }
        return true;
    }

    private void clearNearbyPlantsAndSnow(SurfacePool pool, IWorld world){
        for (BlockPos2D pos: pool.iteratePoolAndEdge(LAVA_BURN_RADIUS)){
            MCHelper.clearVertically(
                pos.to3D(pool.liquidY),
                world,
                b -> MCHelper.isPlant(b.getBlock()) || b.getBlock().equals(Blocks.SNOW)
            );
        }
    }

    private void addRockyEffect(SurfacePool pool, IWorld world, Random random){
        for (BlockPos2D pos: pool.iteratePoolAndEdge(1)){
            for (int y = pool.liquidY; y <= MCHelper.getHighestTerrainHeight(pos, world); y++){
                BlockPos currentPos = pos.to3D(y);

                double rockyChance = INIT_ROCKY_CHANCE + ROCKY_CHANCE_CHANGE_PER_Y * (y - pool.liquidY);

                if (rockyChance < 0){
                    rockyChance = 0;
                }

                if (MCHelper.isSolid(world.getBlockState(currentPos)) && MathHelper.randomBool(rockyChance, random)){
                    world.setBlockState(currentPos, Blocks.STONE.getDefaultState(), MCHelper.DEFAULT_FLAG);
                }
            }
        }
    }

    private BlockState getStoneOrGravel(Random random) {
        if (MathHelper.randomBool(GRAVEL_CHANCE, random)){
            return Blocks.GRAVEL.getDefaultState();
        }
        return Blocks.STONE.getDefaultState();
    }

    /*
    private final class Instance extends SurfacePool{


        private Instance(BlockPos2D center, IWorldReader world, Random random) {
            super(center, world, random, data);
        }

        @Override
        FloatFunc<BlockPos2D> getInnerRadius() {
            return lavaRadiusNoise;
        }

        @Override
        FloatFunc<BlockPos2D> getOuterRadius() {
            return stoneRadiusOffsetNoise;
        }

        @Override
        BlockState getInnerBlock(BlockPos2D pos, IWorldReader world, Random random) {
            return Blocks.LAVA.getDefaultState();
        }

        @Override


        @Override
        int getPlantClearingRadius() {
            return LAVA_BURN_RADIUS;
        }

        @Override
        boolean passesExtraConditions(IWorld world) {
            return isClearOfFlammables(world);
        }



        @Override
        void doExtraSteps(IWorld world, Random random) {
            addRockyEffect(world, random);
        }


    }

     */
}