package weightedgpa.infinibiome.api.generators.nonworldgen;

import weightedgpa.infinibiome.api.dependency.MultiDep;
import weightedgpa.infinibiome.api.pos.BlockPos2D;

/**
 * Used for preventing certain locations from being a player spawn-point
 */
public interface SpawnPointBlacklist extends MultiDep {
    boolean canSpawnHere(BlockPos2D pos);
}
