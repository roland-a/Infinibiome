package weightedgpa.infinibiome.internal.minecraftImpl.world;

import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorld;
import weightedgpa.infinibiome.internal.misc.MCHelper;

public class NoClientUpdatingWorld extends WorldWrapper {
    public NoClientUpdatingWorld(IWorld inner) {
        super(inner);
    }

    @Override
    protected BlockState getBlock(BlockPos pos) {
        return inner.getBlockState(pos);
    }

    @Override
    protected void setBlock(BlockPos pos, BlockState block, int tag) {
        inner.setBlockState(pos, block, MCHelper.DEFAULT_FLAG);
    }
}
