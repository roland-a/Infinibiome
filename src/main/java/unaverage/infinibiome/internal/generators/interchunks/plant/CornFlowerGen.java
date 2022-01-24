package weightedgpa.infinibiome.internal.generators.interchunks.plant;

import net.minecraft.block.Blocks;
import weightedgpa.infinibiome.api.Infinibiome;
import weightedgpa.infinibiome.api.dependency.DependencyInjector;

public final class CornFlowerGen extends PlantGenBase {
    public CornFlowerGen(DependencyInjector di) {
        super(
            di,
            Infinibiome.MOD_ID + ":cornFlower"
        );

        config = initConfig()
            .setPlant(Blocks.CORNFLOWER)
            .setAboveWater()
            .setWithCommonRate()
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
