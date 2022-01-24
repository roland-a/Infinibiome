package weightedgpa.infinibiome.internal.generators.chunks.surface;

/*
import org.apache.commons.lang3.ArrayUtils;
import net.minecraft.block.BlockState;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.IWorld;
import weightedgpa.infinibiome.api.generator.StartUpLoader;
import weightedgpa.infinibiome.api.pointsprovider.PointsProvider;
import weightedgpa.infinibiome.api.pos.BlockPos2D;
import net.minecraft.util.math.BlockPos;
import weightedgpa.infinibiome.api.generator.Locatable;
import weightedgpa.infinibiome.api.generator.Timing;
import weightedgpa.infinibiome.api.posdata.PosDataProvider;
import weightedgpa.infinibiome.api.generator.SurfaceGen;
import weightedgpa.infinibiome.api.minecraft12.CommonBlocks;
import weightedgpa.infinibiome.internal.generator.utils.PredicateSearcher;

import java.util.List;

public abstract class SurfaceBase implements SurfaceGen, Locatable {
    private static final BlockState[] DEFAULT_SPAWN_BLOCKS = {
        CommonBlocks.DIRT
    };
    protected final PosDataProvider data;

    protected SurfaceBase(StartUpLoader loader) {
        this.data = di.getOne(DefaultStartups.DATA);
    }

    abstract BlockState getBlock();
    abstract boolean canBePlacedAt(BlockPos2D pos);

    boolean isValidAboveBlock(Block block){
        return true;
    }

    BlockState[] getValidSpawnBlocks(){
        return DEFAULT_SPAWN_BLOCKS;
    }

    boolean extendsDown(){
        return true;
    }

    @Override
    public Timing getTiming() {
        return SurfaceTiming.PATCHY;
    }

    @Override
    public final boolean tryGenerateGroundAt(BlockPos groundPos, IWorld world) {
        if (!canBePlacedAt(MCHelper.to2D(groundPos))){
            return false;
        }

        final BlockState aboveBlock = world.getBlockState(groundPos.up());

        if (!isValidAboveBlock(aboveBlock)){
            return false;
        }

        for (int y = 0;; y--){
            final BlockPos placePos = groundPos.up(y);

            if (groundPos.getY() <= 0){
                break;
            }

            final BlockState spawnBlock = world.getBlockState(placePos);

            if (!ArrayUtils.contains(getValidSpawnBlocks(), spawnBlock)){
                break;
            }

            world.setBlockState(groundPos.up(y), getBlock(), MCHelper.DEFAULT_BOOL);

            if (!extendsDown()){
                break;
            }
        }

        return true;
    }


}

 */
