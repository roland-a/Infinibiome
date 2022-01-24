package weightedgpa.infinibiome.internal.generators.interchunks;

import net.minecraft.block.Blocks;
import net.minecraft.block.SnowyDirtBlock;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.biome.Biome;
import weightedgpa.infinibiome.api.dependency.DependencyInjector;
import weightedgpa.infinibiome.api.posdata.ClimateValue;
import weightedgpa.infinibiome.api.posdata.PosDataHelper;
import weightedgpa.infinibiome.api.posdata.PosDataKeys;
import weightedgpa.infinibiome.internal.misc.MCHelper;
import weightedgpa.infinibiome.api.generators.*;
import weightedgpa.infinibiome.api.posdata.PosDataProvider;
import weightedgpa.infinibiome.api.generators.InterChunkGenTimings;
import net.minecraft.block.BlockState;
import weightedgpa.infinibiome.api.pos.BlockPos2D;
import net.minecraft.world.IWorld;
import weightedgpa.infinibiome.api.pos.InterChunkPos;
import weightedgpa.infinibiome.internal.minecraftImpl.IBBiomes;

public final class SnowGen implements InterChunkGen {
    private final PosDataProvider posData;

    public SnowGen(DependencyInjector di) {
        this.posData = di.get(PosDataProvider.class);
    }

    @Override
    public Timing getInterChunkTiming() {
        return InterChunkGenTimings.SNOW;
    }

    @Override
    public void generate(InterChunkPos interChunkPos, IWorld interChunks) {
        interChunkPos.forEachAllPos(
            p -> {
                if (!couldSnowAtAnyHeight(p)) return;

                BlockPos snowPos = p.to3D(
                    MCHelper.getHighestY(
                        p,
                        interChunks,
                        b -> !b.getBlock().equals(Blocks.SNOW)
                    ) + 1
                );

                removeCoveredSnow(snowPos, interChunks);

                tryPlaceSnow(snowPos, interChunks);
            }
        );
    }

    private boolean couldSnowAtAnyHeight(BlockPos2D pos){
        ClimateValue temp = posData.get(PosDataKeys.TEMPERATURE, pos);

        if (PosDataHelper.FREEZE_INTERVAL.contains(temp.fromHeight(0))) return true;

        if (PosDataHelper.FREEZE_INTERVAL.contains(temp.fromHeight(255))) return true;

        return false;
    }

    private void tryPlaceSnow(BlockPos snowPos, IWorld world){
        Biome biome = IBBiomes.getBiome(MCHelper.to2D(snowPos), posData);

        if (biome.doesSnowGenerate(world, snowPos)) {
            world.setBlockState(snowPos, Blocks.SNOW.getDefaultState(), MCHelper.DEFAULT_FLAG);

            BlockState snowyDirt = world.getBlockState(snowPos.down());

            if (snowyDirt.has(SnowyDirtBlock.SNOWY)) {
                world.setBlockState(snowPos.down(), snowyDirt.with(SnowyDirtBlock.SNOWY, true), MCHelper.DEFAULT_FLAG);
            }
        }
        else if (biome.doesWaterFreeze(world, snowPos.down())){
            world.setBlockState(snowPos.down(), Blocks.ICE.getDefaultState(), MCHelper.DEFAULT_FLAG);
        }
    }

    private void removeCoveredSnow(BlockPos snowPos, IWorld world){
        int maxY = snowPos.getY() - 1;

        int minY = MCHelper.getHighestTerrainHeight(
            MCHelper.to2D(snowPos),
            world
        );

        //prevents unfreezing glaciers
        boolean inOcean = posData.get(PosDataKeys.LANDMASS_TYPE, MCHelper.to2D(snowPos)).isOcean();

        BlockPos.Mutable mutPos = new BlockPos.Mutable(snowPos);

        for (int y = maxY; y >= minY; y--){
            mutPos.setY(y);

            if (tryUnfreeze(mutPos, world, inOcean)){
                break;
            }
        }
    }

    private boolean tryUnfreeze(BlockPos snowPos, IWorld world, boolean inOcean){
        BlockState snowBlock = world.getBlockState(snowPos);

        BlockPos iceOrDirtPos = snowPos.down();
        BlockState iceOrDirt = world.getBlockState(iceOrDirtPos);

        if (snowBlock.getBlock().equals(Blocks.SNOW)){
            world.setBlockState(snowPos, Blocks.AIR.getDefaultState(), MCHelper.DEFAULT_FLAG);

            if (iceOrDirt.has(SnowyDirtBlock.SNOWY) && iceOrDirt.get(SnowyDirtBlock.SNOWY)){
                world.setBlockState(iceOrDirtPos, iceOrDirt.with(SnowyDirtBlock.SNOWY, false), MCHelper.DEFAULT_FLAG);
            }
            return true;
        }

        //prevents unfreezing ice directly below any solid blocks
        boolean underneathSolid = MCHelper.isSolid(
            world.getBlockState(iceOrDirtPos.up())
        );

        if (!underneathSolid && !inOcean && iceOrDirt.getBlock().equals(Blocks.ICE)){
            world.setBlockState(iceOrDirtPos, Blocks.WATER.getDefaultState(), MCHelper.DEFAULT_FLAG);
            return true;
        }
        return false;
    }

}
