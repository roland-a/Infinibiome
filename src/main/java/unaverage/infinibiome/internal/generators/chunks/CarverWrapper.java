package weightedgpa.infinibiome.internal.generators.chunks;

import net.minecraft.util.SharedSeedRandom;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.chunk.IChunk;
import net.minecraft.world.gen.GenerationStage;
import net.minecraft.world.gen.carver.ConfiguredCarver;
import weightedgpa.infinibiome.api.generators.ChunkGen;
import weightedgpa.infinibiome.api.dependency.DependencyInjector;
import weightedgpa.infinibiome.internal.generators.utils.GeneratorBase;
import weightedgpa.infinibiome.internal.misc.MCHelper;
import weightedgpa.infinibiome.internal.minecraftImpl.IBBiomes;

import java.util.BitSet;

public abstract class CarverWrapper extends GeneratorBase implements ChunkGen {
    private final int sharedRandomSeed;

    protected CarverWrapper(DependencyInjector di, String seedBranch){
        super(di, seedBranch);

        sharedRandomSeed = seed.newSeed("sharedRandomSeed").getAsInt();
    }

    protected abstract GenerationStage.Carving getType();

    protected abstract ConfiguredCarver<?> getCarver();

    protected boolean passesExtraConditions(ChunkPos chunkPos){
        return true;
    }

    @Override
    public final void buildChunk(ChunkPos chunkPos, IChunk chunk) {
        if (!passesExtraConditions(chunkPos)) return;

        BitSet bitset = chunk.getCarvingMask(getType());

        SharedSeedRandom sharedSeedRandom = new SharedSeedRandom();

        for(int xOffset = chunkPos.x - 8; xOffset <= chunkPos.x + 8; ++xOffset) {
            for(int zOffset = chunkPos.z - 8; zOffset <= chunkPos.z + 8; ++zOffset) {

                sharedSeedRandom.setLargeFeatureSeed(sharedRandomSeed, xOffset, zOffset);

                if (getCarver().shouldCarve(sharedSeedRandom, xOffset, zOffset)) {
                    getCarver().carveRegion(
                        chunk,
                        p -> IBBiomes.getBiome(MCHelper.to2D(p), posData),
                        sharedSeedRandom,
                        63,
                        xOffset,
                        zOffset,
                        chunkPos.x,
                        chunkPos.z,
                        bitset
                    );
                }
            }
        }
    }
}
