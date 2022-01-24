package weightedgpa.infinibiome.internal.generators.chunks.surface;

import net.minecraft.block.Blocks;
import weightedgpa.infinibiome.api.Infinibiome;
import weightedgpa.infinibiome.api.dependency.DependencyInjector;
import weightedgpa.infinibiome.api.generators.SurfaceTimings;
import weightedgpa.infinibiome.api.generators.Timing;

public final class GrassSurfaceGen extends SurfaceGenBase {
    public GrassSurfaceGen(DependencyInjector di) {
        super(di, Infinibiome.MOD_ID + ":grassSurface");

        config = initConfig()
            .setBlock(Blocks.GRASS_BLOCK.getDefaultState())
            .setFull()
            .setSurfaceOnly()
            .setSpawnsOnlyInDirt()
            .setNeverUnderwater()
            .neverInMushroomIsland()
            .noSpawnRegion()
            .noExtraConditions();
    }

    @Override
    public Timing getTiming() {
        return SurfaceTimings.GRASS;
    }
}
