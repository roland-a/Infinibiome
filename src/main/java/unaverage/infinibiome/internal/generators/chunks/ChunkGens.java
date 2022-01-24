package weightedgpa.infinibiome.internal.generators.chunks;

import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.chunk.IChunk;
import weightedgpa.infinibiome.api.dependency.DependencyInjector;
import weightedgpa.infinibiome.api.dependency.SingleDep;
import weightedgpa.infinibiome.internal.misc.DeadlockDetector;
import weightedgpa.infinibiome.api.generators.ChunkGen;

import java.util.Comparator;
import java.util.List;

public final class ChunkGens implements SingleDep {
    private final List<ChunkGen> chunkBuilders;

    private final DeadlockDetector deadlockDetector = new DeadlockDetector();

    public ChunkGens(DependencyInjector di) {
        this.chunkBuilders = di.getAll(ChunkGen.class);

        chunkBuilders.sort(
            Comparator.comparing(ChunkGen::getChunkGenTiming)
        );
    }

    public void buildChunk(ChunkPos chunkPos, IChunk world){
        for (ChunkGen chunkGen: chunkBuilders){
            perChunkBuilder(chunkGen, chunkPos, world);
        }
    }

    private void perChunkBuilder(ChunkGen chunkGen, ChunkPos chunkPos, IChunk world){
        try {
            deadlockDetector.setCurrentRunningGenerator(chunkGen);
            chunkGen.buildChunk(chunkPos, world);
            deadlockDetector.currentGeneratorFinished();
        } catch (Throwable e){
            throw new RuntimeException(chunkGen.toString(), e);
        }
    }
}
