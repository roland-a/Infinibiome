package weightedgpa.infinibiome.internal.generators.chunks.surface;

import net.minecraft.block.Blocks;
import weightedgpa.infinibiome.api.Infinibiome;
import weightedgpa.infinibiome.api.dependency.DependencyInjector;
import weightedgpa.infinibiome.api.generators.SurfaceTimings;
import weightedgpa.infinibiome.api.generators.Timing;

import static weightedgpa.infinibiome.internal.generators.chunks.surface.SurfaceHelper.*;

public final class CoarseDirtSurfaceGen extends SurfaceGenBase {
    private final Type type;

    public CoarseDirtSurfaceGen(Type type, DependencyInjector di){
        super(di, Infinibiome.MOD_ID + ":coarseDirt" + type.name());

        this.type = type;

        config = initConfig()
            .setBlock(Blocks.COARSE_DIRT.getDefaultState())
            .setPatchy(
                type == Type.PATCHY
            )
            .setExtendsDown()
            .setSpawnsOnlyInDirt()
            .setAlsoUnderwater()
            .neverInMushroomIsland()
            .inSpawnRegion(type.spawnRegion)
            .noExtraConditions();
    }

    @Override
    public Timing getTiming() {
        return type.timing;
    }

    @Override
    public String toString() {
        return "CoarseDirt{" +
            "type=" + type +
            '}';
    }

    public enum Type {
        FULL(
            COMMON_REGION_RATE / 2,
            SurfaceTimings.FULL
        ),
        PATCHY(
            COMMON_REGION_RATE,
            SurfaceTimings.PATCHY
        );

        private final double spawnRegion;
        private final Timing timing;

        Type(double spawnRegion, Timing timing) {
            this.spawnRegion = spawnRegion;
            this.timing = timing;
        }
    }
}
