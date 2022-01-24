package weightedgpa.infinibiome.internal.generators.chunks.surface;

import net.minecraft.block.Blocks;
import weightedgpa.infinibiome.api.Infinibiome;
import weightedgpa.infinibiome.api.dependency.DependencyInjector;

import weightedgpa.infinibiome.api.generators.SurfaceTimings;
import weightedgpa.infinibiome.api.generators.Timing;
import weightedgpa.infinibiome.internal.floatfunc.util.Interval;

import static weightedgpa.infinibiome.internal.generators.utils.condition.ConditionHelper.*;

public final class PodzolSurfaceGen extends SurfaceGenBase {
    private final Type type;

    public PodzolSurfaceGen(Type type, DependencyInjector di) {
        super(di, Infinibiome.MOD_ID + ":podzol" + type.name());

        this.type = type;

        config = initConfig()
            .setBlock(
                Blocks.PODZOL.getDefaultState()
            )
            .setPatchy(
                type == Type.PATCHY
            )
            .setSurfaceOnly()
            .setSpawnsOnlyInDirt()
            .setNeverUnderwater()
            .neverInMushroomIsland()
            .inSpawnRegion(type.spawnRegionRate)
            .addExtraConditions(
                /*onlyInHumidity(
                    di,
                    GenHelper.WETISH
                ), */
                onlyInTreeDensity(
                    di,
                    new Interval(0.3f, 1)
                )
            );
    }

    @Override
    public Timing getTiming() {
        return type.timing;
    }

    @Override
    public String toString() {
        return "Podzol{" +
            "type=" + type +
            '}';
    }

    public enum Type{
        FULL(
            SurfaceHelper.COMMON_REGION_RATE / 2,
            SurfaceTimings.FULL
        ),
        PATCHY(
            SurfaceHelper.COMMON_REGION_RATE,
            SurfaceTimings.PATCHY
        );

        private final double spawnRegionRate;
        private final Timing timing;

        Type(double spawnRegionRate, Timing timing) {
            this.spawnRegionRate = spawnRegionRate;
            this.timing = timing;
        }
    }
}
