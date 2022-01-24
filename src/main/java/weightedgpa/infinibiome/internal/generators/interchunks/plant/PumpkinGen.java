package weightedgpa.infinibiome.internal.generators.interchunks.plant;

import net.minecraft.block.Blocks;
import weightedgpa.infinibiome.api.Infinibiome;
import weightedgpa.infinibiome.api.dependency.DependencyInjector;
import weightedgpa.infinibiome.internal.floatfunc.util.Interval;
import weightedgpa.infinibiome.api.posdata.PosDataHelper;

//spawns only in cold climate
//spawns without regions
public final class PumpkinGen extends PlantGenBase {
    public PumpkinGen(DependencyInjector di) {
        super(
            di,
            Infinibiome.MOD_ID + ":pumpkin"
        );

        config = initConfig()
            .setPlant(Blocks.PUMPKIN)
            .setAboveWater()
            .setRate(
                new Interval(0.0, 0.01)
            )
            .setRadius(
                new Interval(5, 7)
            )
            .setDensity(
                new Interval(0.1, 0.15)
            )
            .neverInMushroomIsland()
            .setTemperature(
                PosDataHelper.COLD_INTERVAL
            )
            .anyHumidity()
            .setNoSpawnRegion()
            .setNoExtraConditions()
            .setNoGroundBoneMeal();
    }
}
