package weightedgpa.infinibiome.internal.generators.interchunks.plant;

import net.minecraft.block.Blocks;
import weightedgpa.infinibiome.api.Infinibiome;
import weightedgpa.infinibiome.api.dependency.DependencyInjector;
import weightedgpa.infinibiome.api.posdata.PosDataHelper;

//spawns only in wet climate
public final class BlueOrchidGen extends PlantGenBase {
    public BlueOrchidGen(DependencyInjector di) {
        super(
            di,
            Infinibiome.MOD_ID + ":blueOrchid"
        );
        config = initConfig()
            .setPlant(Blocks.BLUE_ORCHID)
            .setAboveWater()
            .setWithCommonRate()
            .setWithCommonRadius()
            .setWithCommonDensity()
            .neverInMushroomIsland()
            .anyNonFreezingTemperature()
            .setHumdity(
                PosDataHelper.WET_INTERVAL
            )
            .setSpawnRegion(
                PlantHelper.COMMON_REGION_RATE/2f
            )
            .setNoExtraConditions();
    }

}
