package weightedgpa.infinibiome.internal.generators.chunks;

import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.gen.GenerationStage;
import weightedgpa.infinibiome.internal.misc.MCHelper;
import weightedgpa.infinibiome.api.posdata.PosDataKeys;
import weightedgpa.infinibiome.api.posdata.PosDataProvider;

public enum CaveType {
    NORMAL(GenerationStage.Carving.AIR){
        @Override
        boolean passesCondition(PosDataProvider data, ChunkPos pos) {
            return !data.get(PosDataKeys.LANDMASS_TYPE, MCHelper.lowestPos(pos)).isOcean();
        }
    },
    UNDER_WATER(GenerationStage.Carving.LIQUID){
        @Override
        boolean passesCondition(PosDataProvider data, ChunkPos pos) {
            return !data.get(PosDataKeys.LANDMASS_TYPE, MCHelper.lowestPos(pos)).isLand();
        }
    };

    final GenerationStage.Carving carvingType;

    CaveType(GenerationStage.Carving carvingType) {
        this.carvingType = carvingType;
    }

    abstract boolean passesCondition(PosDataProvider data, ChunkPos pos);
}
