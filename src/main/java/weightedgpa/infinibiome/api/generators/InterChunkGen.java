package weightedgpa.infinibiome.api.generators;

import net.minecraft.world.IWorld;
import weightedgpa.infinibiome.api.dependency.MultiDep;
import weightedgpa.infinibiome.api.pos.InterChunkPos;

/**
 * Objects that generates structures that can exist between chunks
 * Does this by generating blocks in every 2x2 chunk
 */
public interface InterChunkGen extends MultiDep {
    /**
     * Determines when this run compared to other InterChunkGens
     *
     * @apiNote
     * Must always return the same Timing.
     * Can share timings with other interChunkGens
     */
    Timing getInterChunkTiming();

    /**
     * Reads and writes blocks inside a 2x2 chunk
     *
     * @apiNote
     * Allowed to do different writes for the same location and seed
     */
    void generate(InterChunkPos interChunkPos, IWorld interChunks);
}
