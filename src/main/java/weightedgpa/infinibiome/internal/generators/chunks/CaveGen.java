package weightedgpa.infinibiome.internal.generators.chunks;

import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.GenerationStage;
import net.minecraft.world.gen.carver.CaveWorldCarver;
import net.minecraft.world.gen.carver.ConfiguredCarver;
import net.minecraft.world.gen.feature.ProbabilityConfig;
import weightedgpa.infinibiome.api.Infinibiome;
import weightedgpa.infinibiome.api.dependency.DependencyInjector;
import weightedgpa.infinibiome.api.generators.ChunkGenTimings;
import weightedgpa.infinibiome.internal.misc.*;
import weightedgpa.infinibiome.api.generators.Timing;
import weightedgpa.infinibiome.api.posdata.PosDataKeys;

public final class CaveGen extends CarverWrapper {
    public CaveGen(DependencyInjector di){
        super(di, Infinibiome.MOD_ID + ":cave");
    }

    @Override
    protected ConfiguredCarver<?> getCarver() {
        return Biome.createCarver(CaveWorldCarver.CAVE, new ProbabilityConfig(0.14285715F));
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
        return ChunkGenTimings.CAVE;
    }
}
