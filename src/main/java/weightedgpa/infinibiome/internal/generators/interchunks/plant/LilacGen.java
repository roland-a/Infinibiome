package weightedgpa.infinibiome.internal.generators.interchunks.plant;

import net.minecraft.block.Blocks;
import weightedgpa.infinibiome.api.Infinibiome;
import weightedgpa.infinibiome.internal.floatfunc.util.Interval;
import weightedgpa.infinibiome.api.dependency.DependencyInjector;

import static weightedgpa.infinibiome.internal.generators.interchunks.plant.PlantHelper.*;

public final class LilacGen extends PlantGenBase {
    public LilacGen(DependencyInjector di) {
        super(
            di,
            Infinibiome.MOD_ID + ":lilac"
        );

        config = initConfig()
            .setPlant(Blocks.LILAC)
            .setAboveWater()
            .setRate(new Interval(0.1, 0.3))
            .setWithCommonRadius()
            .setWithCommonDensity()
            .neverInMushroomIsland()
            .anyNonFreezingTemperature()
            .anyHumidity()
            .setSpawnRegion(
                COMMON_REGION_RATE
            )
            .setNoExtraConditions();
    }
}
