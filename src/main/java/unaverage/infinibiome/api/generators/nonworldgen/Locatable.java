package weightedgpa.infinibiome.api.generators.nonworldgen;

import weightedgpa.infinibiome.api.dependency.MultiDep;
import weightedgpa.infinibiome.api.pointsprovider.PointsProvider;
import weightedgpa.infinibiome.api.pos.BlockPos2D;

import javax.annotation.Nullable;

/**
 * Allows the user to find the closest instance of this generator through the /ib locate command
 */
public interface Locatable extends MultiDep {
    @Nullable
    BlockPos2D getClosestInstance(BlockPos2D pos);

    /**
     * Returns the pointsProvider that contains every possible location this object will spawn at
     */
    interface HasPointsProvider extends Locatable {
        PointsProvider<BlockPos2D> getAllLocations();

        @Override
        default BlockPos2D getClosestInstance(BlockPos2D pos){
            return getAllLocations().getClosestPoint(pos);
        }
    }
}
