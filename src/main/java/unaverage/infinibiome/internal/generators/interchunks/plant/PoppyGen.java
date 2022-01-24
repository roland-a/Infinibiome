package weightedgpa.infinibiome.internal.generators.interchunks.plant;

import net.minecraft.block.Blocks;
import weightedgpa.infinibiome.api.Infinibiome;
import weightedgpa.infinibiome.api.posdata.PosDataHelper;
import weightedgpa.infinibiome.internal.floatfunc.util.Interval;
import weightedgpa.infinibiome.api.dependency.DependencyInjector;

public final class PoppyGen extends PlantGenBase {
    public PoppyGen(DependencyInjector di) {
        super(
            di,
            Infinibiome.MOD_ID + ":poppy"
        );

        config = initConfig()
            .setPlant(Blocks.POPPY)
            .setAboveWater()
            .setWithCommonRate()
            .setWithCommonRadius()
            .setDensity(
                new Interval(0.1, 0.2)
            )
            .neverInMushroomIsland()
            .setTemperature(
                PosDataHelper.HOT_INTERVAL
            )
            .anyHumidity()
            .setSpawnRegion(
                PlantHelper.COMMON_REGION_RATE
            )
            .setNoExtraConditions();
    }

}
