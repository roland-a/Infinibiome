package weightedgpa.infinibiome.internal.minecraftImpl.world;

import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorld;
import weightedgpa.infinibiome.internal.misc.MCHelper;

public final class ChangeDetectingWorld extends WorldWrapper {
    private boolean anyChange = false;

    public ChangeDetectingWorld(IWorld inner) {
        super(inner);
    }

    public boolean anyChange(){
        return anyChange;
    }

    @Override
    protected BlockState getBlock(BlockPos pos) {
        return inner.getBlockState(pos);
    }

    @Override
    protected void setBlock(BlockPos pos, BlockState block, int tag) {
        if (getBlock(pos).equals(block)) return;

        anyChange = true;

        inner.setBlockState(pos, block, tag);
    }
}
