package weightedgpa.infinibiome.internal.generators.interchunks.plant;

import net.minecraft.block.Blocks;
import weightedgpa.infinibiome.api.Infinibiome;
import weightedgpa.infinibiome.internal.floatfunc.util.Interval;
import weightedgpa.infinibiome.api.dependency.DependencyInjector;

//spawns in large, thin and sparse clusters
public final class DandelionGen extends PlantGenBase {
    public DandelionGen(DependencyInjector di) {
        super(
            di,
            Infinibiome.MOD_ID + ":dandelion"
        );

        config = initConfig()
            .setPlant(Blocks.DANDELION)
            .setAboveWater()
            .setRate(
                new Interval(0.1, 0.2)
            )
            .setRadius(
                new Interval(4, 7)
            )
            .setDensity(
                new Interval(0.1, 0.3)
            )
            .neverInMushroomIsland()
            .anyNonFreezingTemperature()
            .anyHumidity()
            .setSpawnRegion(
                PlantHelper.COMMON_REGION_RATE
            )
            .setNoExtraConditions();
    }

}
