package weightedgpa.infinibiome.internal.generators.chunks;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.chunk.IChunk;
import weightedgpa.infinibiome.api.Infinibiome;
import weightedgpa.infinibiome.api.generators.ChunkGenTimings;
import weightedgpa.infinibiome.api.dependency.DependencyInjector;
import weightedgpa.infinibiome.internal.generators.utils.GeneratorBase;
import weightedgpa.infinibiome.internal.misc.MCHelper;
import weightedgpa.infinibiome.api.pos.BlockPos2D;
import net.minecraft.util.math.BlockPos;
import weightedgpa.infinibiome.api.posdata.PosDataKeys;
import weightedgpa.infinibiome.api.generators.ChunkGen;
import weightedgpa.infinibiome.api.generators.Timing;

import java.util.List;


@SuppressWarnings("MethodMayBeStatic")
public final class BaseTerrainGen extends GeneratorBase implements ChunkGen {
    public BaseTerrainGen(DependencyInjector di) {
        super(di, Infinibiome.MOD_ID + ":baseTerrain");
    }

    @Override
    public Timing getChunkGenTiming() {
        return ChunkGenTimings.BASE_TERRAIN;
    }

    @Override
    public void buildChunk(ChunkPos chunkPos, IChunk chunk) {
        MCHelper.forEachPos(
            chunkPos,
            p -> {
                placeStone(p, chunk);

                placeGroundBlocks(p, chunk);

                placeWaterIfNeeded(p, chunk);
            }
        );
    }

    private void placeStone(BlockPos2D pos2D, IChunk world){
        int maxHeight = (int)posData.get(PosDataKeys.MAPPED_HEIGHT, pos2D);

        BlockPos.Mutable mutPos = new BlockPos.Mutable(pos2D.getBlockX(), 0, pos2D.getBlockZ());

        for (int blockY = 0; blockY <= maxHeight; blockY++){
            mutPos.setY(blockY);

            world.setBlockState(mutPos, Blocks.STONE.getDefaultState(), MCHelper.DEFAULT_IS_MOVING);
        }
    }

    private void placeGroundBlocks(BlockPos2D pos, IChunk world){
        List<BlockState> groundBlocks = posData.get(PosDataKeys.GROUND_BLOCKS, pos);

        int height = (int)posData.get(PosDataKeys.MAPPED_HEIGHT, pos);

        for (int i = 0; i < groundBlocks.size(); i++){
            BlockPos pos3D = pos.to3D(height - i);

            world.setBlockState(pos3D, groundBlocks.get(i), MCHelper.DEFAULT_IS_MOVING);
        }
    }

    private void placeWaterIfNeeded(BlockPos2D pos2D, IChunk world){
        int groundHeight = (int)posData.get(PosDataKeys.MAPPED_HEIGHT, pos2D);

        BlockPos.Mutable mutPos = new BlockPos.Mutable(pos2D.getBlockX(), 0, pos2D.getBlockZ());

        for (int blockY = groundHeight + 1; blockY <= MCHelper.WATER_HEIGHT; blockY++) {
            mutPos.setY(blockY);

            world.setBlockState(
                mutPos,
                Blocks.WATER.getDefaultState(),
                MCHelper.DEFAULT_IS_MOVING
            );
        }
    }
}
