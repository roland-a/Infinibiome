package weightedgpa.infinibiome.internal.generators.interchunks;

import net.minecraft.world.IWorld;
import net.minecraft.world.gen.feature.ConfiguredFeature;
import weightedgpa.infinibiome.api.dependency.DependencyInjector;
import weightedgpa.infinibiome.api.generators.InterChunkGen;
import weightedgpa.infinibiome.api.pos.InterChunkPos;
import weightedgpa.infinibiome.internal.generators.utils.GeneratorBase;

public abstract class MCFeatureGenBase extends GeneratorBase implements InterChunkGen {
    protected MCFeatureGenBase(DependencyInjector di, String seedBranch) {
        super(di, seedBranch);
    }

    abstract ConfiguredFeature<?, ?> getFeature();

    @Override
    public void generate(InterChunkPos interChunkPos, IWorld interChunks) {
        getFeature().place(
            interChunks,
            chunkGenerator,
            randomGen.getRandom(interChunkPos.getX(), interChunkPos.getZ()),
            interChunkPos.getLowestCenterBlockPos().to3D(0)
        );
    }
}
