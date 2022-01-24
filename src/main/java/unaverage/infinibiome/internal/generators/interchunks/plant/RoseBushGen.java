package weightedgpa.infinibiome.internal.generators.interchunks.plant;

import net.minecraft.block.Blocks;
import weightedgpa.infinibiome.api.Infinibiome;
import weightedgpa.infinibiome.internal.floatfunc.util.Interval;
import weightedgpa.infinibiome.api.dependency.DependencyInjector;
import weightedgpa.infinibiome.internal.generators.utils.GenHelper;

import static weightedgpa.infinibiome.internal.generators.utils.condition.ConditionHelper.*;

//spawns in small but dense sparse clusters
//spawns only in moderate tree density
public final class RoseBushGen extends PlantGenBase {
    public RoseBushGen(DependencyInjector di) {
        super(
            di,
            Infinibiome.MOD_ID + ":roseBush"
        );

        config = initConfig()
            .setPlant(Blocks.ROSE_BUSH)
            .setAboveWater()
            .setRate(
                new Interval(0.0, 0.1)
            )
            .setRadius(
                new Interval(2, 3)
            )
            .setDensity(
                new Interval(0.8, 0.9)
            )
            .neverInMushroomIsland()
            .anyNonFreezingTemperature()
            .anyHumidity()
            .setSpawnRegion(
                PlantHelper.COMMON_REGION_RATE
            )
            .setExtraConditions(
                onlyInTreeDensity(
                    di,
                    GenHelper.FORESTED_TREE_INTERVAL
                )
            );
    }
}
