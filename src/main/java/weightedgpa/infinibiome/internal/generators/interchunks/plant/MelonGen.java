package weightedgpa.infinibiome.internal.generators.interchunks.plant;

import net.minecraft.block.Blocks;
import weightedgpa.infinibiome.api.Infinibiome;
import weightedgpa.infinibiome.api.dependency.DependencyInjector;
import weightedgpa.infinibiome.api.posdata.PosDataHelper;
import weightedgpa.infinibiome.internal.floatfunc.util.Interval;

//spawns in hot and wet climate
//spawns without regions
public final class MelonGen extends PlantGenBase {
    public MelonGen(DependencyInjector di) {
        super(
            di,
            Infinibiome.MOD_ID + ":melon"
        );

        config = initConfig()
            .setPlant(
                Blocks.MELON
            )
            .setAboveWater()
            .setRate(
                new Interval(0.01, 0.05)
            )
            .setRadius(
                new Interval(5, 7)
            )
            .setDensity(
                new Interval(0.05, 0.1)
            )
            .neverInMushroomIsland()
            .setTemperature(
                PosDataHelper.HOT_INTERVAL
            )
            .setHumdity(
                PosDataHelper.WET_INTERVAL
            )
            .setNoSpawnRegion()
            .setNoExtraConditions()
            .setNoGroundBoneMeal();
    }
}
