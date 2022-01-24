package weightedgpa.infinibiome.internal.generators.chunks.surface;

import net.minecraft.block.Blocks;
import weightedgpa.infinibiome.api.Infinibiome;
import weightedgpa.infinibiome.api.dependency.DependencyInjector;
import weightedgpa.infinibiome.api.generators.SurfaceTimings;
import weightedgpa.infinibiome.api.generators.Timing;
import weightedgpa.infinibiome.api.posdata.PosDataHelper;

import static weightedgpa.infinibiome.internal.generators.utils.condition.ConditionHelper.*;

public final class SnowBlockSurfaceGen extends SurfaceGenBase {
    public SnowBlockSurfaceGen(DependencyInjector di){
        super(di, Infinibiome.MOD_ID + ":snowBlockSurface");

        config = initConfig()
            .setBlock(Blocks.SNOW_BLOCK.getDefaultState())
            .setPatchy()
            .setSurfaceOnly()
            .setSpawnsOnlyInDirtAndSand()
            .setNeverUnderwater()
            .neverInMushroomIsland()
            .inSpawnRegion(SurfaceHelper.COMMON_REGION_RATE)
            .addExtraConditions(
                onlyInTemperature(
                    di,
                    PosDataHelper.FREEZE_INTERVAL
                )
            );
    }

    @Override
    public Timing getTiming() {
        return SurfaceTimings.PATCHY;
    }
}
