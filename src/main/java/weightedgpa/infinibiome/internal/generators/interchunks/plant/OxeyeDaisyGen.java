package weightedgpa.infinibiome.internal.generators.interchunks.plant;

import net.minecraft.block.Blocks;
import weightedgpa.infinibiome.api.Infinibiome;
import weightedgpa.infinibiome.api.dependency.DependencyInjector;

//can spawn in snowy climates
public final class OxeyeDaisyGen extends PlantGenBase {
    public OxeyeDaisyGen(DependencyInjector di) {
        super(
            di,
            Infinibiome.MOD_ID + ":oxeyeDaisy"
        );

        config = initConfig()
            .setPlant(Blocks.OXEYE_DAISY)
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
