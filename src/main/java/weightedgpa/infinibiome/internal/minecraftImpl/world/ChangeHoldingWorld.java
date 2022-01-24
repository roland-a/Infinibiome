package weightedgpa.infinibiome.internal.minecraftImpl.world;

import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.*;
import weightedgpa.infinibiome.internal.misc.Pair;

import java.util.*;

//currently cannot handle tile entities
//use ChangeDetectingWorld for that
public final class ChangeHoldingWorld extends WorldWrapper{
    private final Map<BlockPos, Pair<BlockState, Integer>> temp = new HashMap<>();

    public ChangeHoldingWorld(IWorld inner) {
        super(inner);
    }

    public void loadChange(){
        for (Map.Entry<BlockPos,  Pair<BlockState, Integer>> entry : temp.entrySet()){
            inner.setBlockState(
                entry.getKey(),
                entry.getValue().first,
                entry.getValue().second
            );
        }

        clearChange();
    }

    public void clearChange(){
        temp.clear();
    }

    public boolean anyChange(){
        return !temp.isEmpty();
    }

    @Override
    protected void setBlock(BlockPos pos, BlockState block, int tag) {
        if (getBlockState(pos).equals(block)) return;

        temp.put(pos.toImmutable(), new Pair<>(block, tag));
    }

    @Override
    protected BlockState getBlock(BlockPos pos) {
        Pair<BlockState, Integer> tempBlock = temp.get(pos);

        if (tempBlock != null) return tempBlock.first;

        return inner.getBlockState(pos);
    }

    public int changeCount() {
        return temp.keySet().size();
    }

    @Override
    public String toString() {
        return "ChangeHoldingWorld{" +
            "temp=" + temp +
            '}';
    }
}
