package weightedgpa.infinibiome.internal.generators.chunks.surface;

import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.chunk.IChunk;
import weightedgpa.infinibiome.api.generators.*;
import weightedgpa.infinibiome.api.dependency.DependencyInjector;
import weightedgpa.infinibiome.internal.floatfunc.generators.RandomGen;
import weightedgpa.infinibiome.internal.generators.utils.SortedRandomizedList;
import weightedgpa.infinibiome.internal.minecraftImpl.world.ChangeDetectingWorld;
import weightedgpa.infinibiome.internal.minecraftImpl.world.ChunkAsWorld;
import weightedgpa.infinibiome.internal.misc.MCHelper;

import java.util.Comparator;
import java.util.Random;

public final class SurfaceGens implements ChunkGen {
    private final SortedRandomizedList<SurfaceGen> surfaceGens;

    private final RandomGen randomGen;

    public SurfaceGens(DependencyInjector di) {
        this.surfaceGens = new SortedRandomizedList<>(
            di.getAll(SurfaceGen.class),
            Comparator.comparing(SurfaceGen::getTiming)
        );

        this.randomGen = new RandomGen(
            di.get(Seed.class).newSeed("surfaceGens")
        );
    }

    @Override
    public Timing getChunkGenTiming() {
        return ChunkGenTimings.SURFACE;
    }

    @Override
    public void buildChunk(ChunkPos chunkPos, IChunk chunk) {
        MCHelper.forEachPos(
            chunkPos,
            p -> {
                ChangeDetectingWorld worldWrapper = new ChangeDetectingWorld(
                    new ChunkAsWorld(chunk)
                );

                Random random = randomGen.getRandom(p.getBlockX(), p.getBlockZ());

                surfaceGens.forEachItem(
                    surfaceGen -> {
                        surfaceGen.generate(
                            p,
                            worldWrapper,
                            random
                        );

                        if (!worldWrapper.anyChange()) return true;

                        return false;
                    }
                );
            }
        );
    }
}
