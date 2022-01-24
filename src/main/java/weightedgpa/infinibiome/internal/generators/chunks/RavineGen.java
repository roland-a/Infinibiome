package weightedgpa.infinibiome.internal.generators.chunks;

import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.GenerationStage;
import net.minecraft.world.gen.carver.ConfiguredCarver;
import net.minecraft.world.gen.carver.WorldCarver;
import net.minecraft.world.gen.feature.ProbabilityConfig;
import weightedgpa.infinibiome.api.Infinibiome;
import weightedgpa.infinibiome.api.generators.ChunkGenTimings;
import weightedgpa.infinibiome.api.dependency.DependencyInjector;
import weightedgpa.infinibiome.api.generators.Timing;
import weightedgpa.infinibiome.internal.misc.MCHelper;
import weightedgpa.infinibiome.api.posdata.PosDataKeys;

public final class RavineGen extends CarverWrapper {
    public RavineGen(DependencyInjector di){
        super(di, Infinibiome.MOD_ID + ":ravine");
    }

    @Override
    protected ConfiguredCarver<?> getCarver() {
        return Biome.createCarver(WorldCarver.CANYON, new ProbabilityConfig(0.02F));
    }

    @Override
    protected GenerationStage.Carving getType() {
        return GenerationStage.Carving.AIR;
    }

    @Override
    protected boolean passesExtraConditions(ChunkPos chunkPos) {
        return !posData.get(PosDataKeys.LANDMASS_TYPE, MCHelper.lowestPos(chunkPos)).isOcean();
    }

    @Override
    public Timing getChunkGenTiming() {
        return ChunkGenTimings.RAVINE;
    }

}
