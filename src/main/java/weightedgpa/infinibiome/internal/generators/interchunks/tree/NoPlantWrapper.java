package weightedgpa.infinibiome.internal.generators.interchunks.tree;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorld;
import weightedgpa.infinibiome.internal.minecraftImpl.world.WorldWrapper;
import weightedgpa.infinibiome.internal.misc.MCHelper;

//allows trees to override saplings and ignore other plants by having them be read as air
public class NoPlantWrapper extends WorldWrapper {
    public NoPlantWrapper(IWorld inner) {
        super(inner);
    }

    @Override
    protected BlockState getBlock(BlockPos pos) {
        if (isPlant(pos)) {
            return Blocks.AIR.getDefaultState();
        }

        return inner.getBlockState(pos);
    }

    @Override
    protected void setBlock(BlockPos pos, BlockState block, int tag) {
        inner.setBlockState(pos, block, tag);
    }

    private boolean isPlant(BlockPos pos) {
        return MCHelper.isPlant(
            inner.getBlockState(pos).getBlock()
        );
    }
}
