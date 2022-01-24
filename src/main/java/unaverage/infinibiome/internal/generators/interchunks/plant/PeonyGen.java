package weightedgpa.infinibiome.internal.generators.interchunks.plant;

import net.minecraft.block.Blocks;
import weightedgpa.infinibiome.api.Infinibiome;
import weightedgpa.infinibiome.internal.floatfunc.util.Interval;
import weightedgpa.infinibiome.api.dependency.DependencyInjector;

public final class PeonyGen extends PlantGenBase {
    public PeonyGen(DependencyInjector di) {
        super(
            di,
            Infinibiome.MOD_ID + ":peony"
        );

        config = initConfig()
            .setPlant(Blocks.PEONY)
            .setAboveWater()
            .setRate(
                new Interval(0.10, 0.30)
            )
            .setWithCommonRadius()
            .setWithCommonDensity()
            .neverInMushroomIsland()
            .anyNonFreezingTemperature()
            .anyHumidity()
            .setSpawnRegion(
                PlantHelper.COMMON_REGION_RATE
            )
            .setNoExtraConditions();
    }
}
