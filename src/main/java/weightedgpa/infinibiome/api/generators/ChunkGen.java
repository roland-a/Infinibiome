package weightedgpa.infinibiome.api.generators;

import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.chunk.IChunk;
import weightedgpa.infinibiome.api.dependency.MultiDep;

/**
 * Generates blocks for every chunk
 */
public interface ChunkGen extends MultiDep {
    /**
     * Determines when this runs compared to other ChunkGens
     *
     * @apiNote
     * Must always return the same Timing.
     * Must be unique compared to other running chunkGens
     */
    Timing getChunkGenTiming();

    /**
     * Reads and add blocks to a chunk
     *
     * @apiNote
     * Must do the same writes for the same position and seed
     */
    void buildChunk(ChunkPos chunkPos, IChunk chunk);
}
