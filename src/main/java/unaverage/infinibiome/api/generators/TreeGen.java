package weightedgpa.infinibiome.api.generators;

import net.minecraft.world.IWorld;
import weightedgpa.infinibiome.api.dependency.MultiDep;
import weightedgpa.infinibiome.api.pos.InterChunkPos;
import net.minecraft.util.math.BlockPos;

/**
 * Objects that generates trees
 *
 * Allows for better density control with other trees
 */
public interface TreeGen extends MultiDep {
    /**
     * Returns the density that the tree will attempt to generate at a 2x2 chunk
     * If the cumulative density of every tree at a 2x2 chunk is too high, this will be used as a relative density instead
     */
    double getDensity(InterChunkPos interChunkPos);

    /**
     * Generates a tree at a position.
     */
    void generate(BlockPos treePos, IWorld world);
}
