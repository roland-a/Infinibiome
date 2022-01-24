package weightedgpa.infinibiome.internal.generators.interchunks.plant;

import net.minecraft.block.Blocks;
import weightedgpa.infinibiome.api.Infinibiome;
import weightedgpa.infinibiome.internal.floatfunc.util.Interval;
import weightedgpa.infinibiome.api.dependency.DependencyInjector;
import weightedgpa.infinibiome.internal.generators.utils.GenHelper;

import static weightedgpa.infinibiome.internal.generators.interchunks.plant.PlantHelper.*;
import static weightedgpa.infinibiome.internal.generators.utils.condition.ConditionHelper.*;


//plant with large and dense clusters
//spawns only in sparse tree density
public final class AlliumGen extends PlantGenBase {
    public AlliumGen(DependencyInjector di){
        super(
            di,
            Infinibiome.MOD_ID + ":allium"
        );

        config = initConfig()
            .setPlant(Blocks.ALLIUM)
            .setAboveWater()
            .setRate(
                new Interval(0.8, 1.0)
            )
            .setRadius(
                new Interval(5, 7)
            )
            .setDensity(
                new Interval(0.7, 0.8)
            )
            .neverInMushroomIsland()
            .anyNonFreezingTemperature()
            .anyHumidity()
            .setSpawnRegion(
                COMMON_REGION_RATE / 4
            )
            .setExtraConditions(
                onlyInTreeDensity(
                    di,
                    GenHelper.NON_FORESTED_TREE_INTERVAL
                )
            );
    }
}