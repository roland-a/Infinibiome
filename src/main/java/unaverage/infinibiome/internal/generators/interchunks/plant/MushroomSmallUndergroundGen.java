package weightedgpa.infinibiome.internal.generators.interchunks.plant;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.block.Blocks;
import weightedgpa.infinibiome.api.Infinibiome;
import weightedgpa.infinibiome.api.dependency.DependencyInjector;
import weightedgpa.infinibiome.internal.generators.utils.GeneratorBase;
import weightedgpa.infinibiome.internal.misc.*;
import weightedgpa.infinibiome.api.generators.*;
import weightedgpa.infinibiome.api.pos.InterChunkPos;
import net.minecraft.block.BlockState;
import weightedgpa.infinibiome.api.pos.BlockPos2D;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.IWorld;
import weightedgpa.infinibiome.internal.floatfunc.FloatFunc;
import weightedgpa.infinibiome.internal.misc.Helper;


import javax.annotation.Nullable;
import java.util.Random;

public final class MushroomSmallUndergroundGen extends GeneratorBase implements InterChunkGen {
    private static final int MIN_HEIGHT = 16;

    private static final double CHANCE_PER_CHUNK = 0.8f;
    private static final double DENSITY = 0.25f;

    private static final FloatFunc<BlockPos2D> radiusFunc = FloatFunc.constFunc(2);

    public MushroomSmallUndergroundGen(DependencyInjector di) {
        super(di, Infinibiome.MOD_ID + ":mushroomSmallUnderground");
    }

    @Override
    public Timing getInterChunkTiming() {
        return InterChunkGenTimings.PLANTS;
    }

    @Override
    public void generate(InterChunkPos interChunkPos, IWorld interChunks) {
        Random random = randomGen.getRandom(interChunkPos.getX(), interChunkPos.getZ());

        if (MathHelper.randomBool(CHANCE_PER_CHUNK, random)) {
            return ;
        }

        BlockPos2D centerPos = interChunkPos.getRandomCenterPos(random);

        generatePlant(centerPos, interChunks, random);
    }

    private void generatePlant(BlockPos2D center2D, IWorld world, Random random){
        Integer centerHeight = getClusterCenterHeight(center2D, world, random);

        if (centerHeight == null) return ;

        BlockPos center = center2D.to3D(centerHeight);

        BlockState plantBlock = getPlantBlock(random);

        Helper.placeClusterWithUnknownHeight(
            center,
            radiusFunc,
            world,
            p -> {
                if (p.getY() >= MCHelper.getHighestTerrainHeight(MCHelper.to2D(p), world)) return ;

                if (!MathHelper.randomBool(DENSITY, random)) return ;

                world.setBlockState(p, plantBlock, MCHelper.DEFAULT_FLAG);
            }
        );

    }

    @Nullable
    private Integer getClusterCenterHeight(BlockPos2D centerPos, IWorldReader world, Random random){
        IntList validPos = new IntArrayList();

        for (int y = MIN_HEIGHT; y <= MCHelper.getHighestNonAirY(centerPos, world); y++){
            BlockPos scanPos = centerPos.to3D(y);

            if (
                MCHelper.isMostlyAir(world.getBlockState(scanPos)) &&
                MCHelper.isSolid(world.getBlockState(scanPos.down()))
            ){
                validPos.add(y);
            }
        }

        if (validPos.isEmpty()){
            return null;
        }

        return validPos.getInt(random.nextInt(validPos.size()));
    }

    private BlockState getPlantBlock(Random random){
        if (random.nextBoolean()){
            return Blocks.RED_MUSHROOM.getDefaultState();
        }
        return Blocks.BROWN_MUSHROOM.getDefaultState();
    }
}
