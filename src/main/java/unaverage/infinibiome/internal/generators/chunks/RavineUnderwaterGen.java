package weightedgpa.infinibiome.internal.generators.chunks;

import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.GenerationStage;
import net.minecraft.world.gen.carver.ConfiguredCarver;
import net.minecraft.world.gen.carver.WorldCarver;
import net.minecraft.world.gen.feature.ProbabilityConfig;
import weightedgpa.infinibiome.api.Infinibiome;
import weightedgpa.infinibiome.api.generators.Timing;
import weightedgpa.infinibiome.api.generators.ChunkGenTimings;
import weightedgpa.infinibiome.api.dependency.DependencyInjector;
import weightedgpa.infinibiome.internal.misc.MCHelper;
import weightedgpa.infinibiome.api.posdata.PosDataKeys;

public final class RavineUnderwaterGen extends CarverWrapper {
    public RavineUnderwaterGen(DependencyInjector di){
        super(di, Infinibiome.MOD_ID + ":ravineUnderwater");
    }

    @Override
    protected ConfiguredCarver<?> getCarver() {
        return Biome.createCarver(WorldCarver.UNDERWATER_CANYON, new ProbabilityConfig(0.02F));
    }

    @Override
    protected GenerationStage.Carving getType() {
        return GenerationStage.Carving.LIQUID;
    }

    @Override
    protected boolean passesExtraConditions(ChunkPos chunkPos) {
        return !posData.get(PosDataKeys.LANDMASS_TYPE, MCHelper.lowestPos(chunkPos)).isLand();
    }

    @Override
    public Timing getChunkGenTiming() {
        return ChunkGenTimings.RAVINE_UNDERWATER;
    }

}
