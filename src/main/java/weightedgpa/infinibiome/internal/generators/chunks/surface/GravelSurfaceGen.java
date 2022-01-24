package weightedgpa.infinibiome.internal.generators.chunks.surface;

import net.minecraft.block.Blocks;
import weightedgpa.infinibiome.api.Infinibiome;
import weightedgpa.infinibiome.api.dependency.DependencyInjector;
import weightedgpa.infinibiome.api.generators.SurfaceTimings;
import weightedgpa.infinibiome.api.generators.Timing;

public final class GravelSurfaceGen extends SurfaceGenBase {
    public GravelSurfaceGen(DependencyInjector di){
        super(di, Infinibiome.MOD_ID + ":gravelSurface");

        config = initConfig()
            .setBlock(
                Blocks.GRAVEL.getDefaultState()
            )
            .setPatchy()
            .setExtendsDown()
            .setSpawnsOnlyInDirtAndSand()
            .setAlsoUnderwater()
            .neverInMushroomIsland()
            .inSpawnRegion(
                SurfaceHelper.COMMON_REGION_RATE
            )
            .noExtraConditions();
    }

    @Override
    public Timing getTiming() {
        return SurfaceTimings.PATCHY;
    }
}
