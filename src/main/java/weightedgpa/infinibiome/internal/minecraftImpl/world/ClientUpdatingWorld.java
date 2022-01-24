package weightedgpa.infinibiome.internal.minecraftImpl.world;

import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorld;

public class ClientUpdatingWorld extends WorldWrapper {
    public ClientUpdatingWorld(IWorld inner) {
        super(inner);
    }

    @Override
    protected BlockState getBlock(BlockPos pos) {
        return inner.getBlockState(pos);
    }

    @Override
    protected void setBlock(BlockPos pos, BlockState block, int tag) {
        inner.setBlockState(pos, block, 2);//(tag | 2) & ~4);
    }
}
