package weightedgpa.infinibiome.internal.generators.interchunks.tree;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorld;
import net.minecraftforge.common.IPlantable;
import weightedgpa.infinibiome.api.pos.BlockPos2D;
import weightedgpa.infinibiome.api.pos.InterChunkPos;
import weightedgpa.infinibiome.internal.minecraftImpl.world.WorldWrapper;
import weightedgpa.infinibiome.internal.misc.MCHelper;
import weightedgpa.infinibiome.internal.misc.Log2helper;

import java.util.Arrays;

final class TreeDensityCounter extends WorldWrapper {
    private static final int INVALID_GROUND = -1;
    private static final int GROUND = 0;
    private static final int LEAF = 1;

    private final int[][] leafMap = new int[16][16];

    private final InterChunkPos interChunkPos;

    TreeDensityCounter(IWorld inner, InterChunkPos interChunkPos) {
        super(inner);

        this.interChunkPos = interChunkPos;

        interChunkPos.forEachCenterPos(
            pos2D -> {
                {
                    int leafHeight = MCHelper.getHighestY(
                        pos2D,
                        inner,
                        b -> !b.getBlock().equals(Blocks.SNOW)
                    );

                    BlockPos leafPos = pos2D.to3D(leafHeight);

                    if (BlockTags.LEAVES.contains(inner.getBlockState(leafPos).getBlock())){
                        setMap(
                            pos2D,
                            LEAF
                        );
                        return;
                    }
                }
                {
                    int groundHeight = MCHelper.getHighestTerrainHeight(pos2D, inner);

                    BlockPos groundPos = pos2D.to3D(groundHeight);
                    BlockState groundBlock = inner.getBlockState(groundPos);

                    if (!groundBlock.canSustainPlant(inner, groundPos.up(), Direction.DOWN, (IPlantable) Blocks.OAK_SAPLING)) {
                        setMap(
                            pos2D,
                            INVALID_GROUND
                        );
                    }
                }
            }
        );
    }

    double getCurrentDensity(){
        int groundCount = 0;
        int leafCount = 0;

        for (int[] row: leafMap){
            for (int num: row){
                if (num == GROUND) {
                    groundCount++;
                }
                else if (num == LEAF) {
                    leafCount++;
                }
            }
        }

        if (groundCount + leafCount <= 10) return 0;

        return leafCount / (double)(groundCount + leafCount);
    }

    String debugInner(){
        String result = "\n";

        for (int[] row: leafMap){
            result += Arrays.toString(row) + "\n";
        }
        return result;
    }

    @Override
    protected BlockState getBlock(BlockPos pos) {
        return inner.getBlockState(pos);
    }

    @Override
    protected void setBlock(BlockPos pos, BlockState block, int tag) {
        if (BlockTags.LEAVES.contains(block.getBlock())){
            setMap(
                MCHelper.to2D(pos),
                LEAF
            );
        }

        inner.setBlockState(pos, block, tag);
    }

    private void setMap(BlockPos2D pos, int num){
        if (!interChunkPos.containsCenterPos(pos)) return;

        int x = Log2helper.mod(pos.getBlockX(), 4);
        int z = Log2helper.mod(pos.getBlockZ(), 4);

        leafMap[x][z] = num;
    }
}
