package weightedgpa.infinibiome.internal.generators.chunks.surface;

import net.minecraft.block.Blocks;
import weightedgpa.infinibiome.api.Infinibiome;
import weightedgpa.infinibiome.api.dependency.DependencyInjector;
import weightedgpa.infinibiome.api.generators.SurfaceTimings;
import weightedgpa.infinibiome.api.generators.Timing;

public final class MyceliumSurfaceGen extends SurfaceGenBase {
    public MyceliumSurfaceGen(DependencyInjector di) {
        super(di, Infinibiome.MOD_ID + ":myceliumSurface");

        config = initConfig()
            .setBlock(
                Blocks.MYCELIUM.getDefaultState()
            )
            .setFull()
            .setSurfaceOnly()
            .setSpawnsOnlyInDirt()
            .setNeverUnderwater()
            .onlyInMushroomIsland()
            .noSpawnRegion()
            .noExtraConditions();
    }

    @Override
    public Timing getTiming() {
        return SurfaceTimings.MYCELIUM;
    }
}
