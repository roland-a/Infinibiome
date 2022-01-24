package weightedgpa.infinibiome.internal.generators.interchunks.plant;

import net.minecraft.block.Blocks;
import weightedgpa.infinibiome.api.Infinibiome;
import weightedgpa.infinibiome.internal.floatfunc.util.Interval;
import weightedgpa.infinibiome.api.dependency.DependencyInjector;
import weightedgpa.infinibiome.internal.misc.MCHelper;

import static weightedgpa.infinibiome.internal.generators.utils.condition.ConditionHelper.*;

//spawns in mountainous areas
//spawns without regions
public final class ValleyLilyGen extends PlantGenBase {
    public ValleyLilyGen(DependencyInjector di) {
        super(
            di,
            Infinibiome.MOD_ID + ":valleyLily"
        );

        config = initConfig()
            .setPlant(Blocks.LILY_OF_THE_VALLEY)
            .setAboveWater()
            .setRate(
                new Interval(0.5, 1)
            )
            .setWithCommonRadius()
            .setWithCommonDensity()
            .neverInMushroomIsland()
            .anyNonFreezingTemperature()
            .anyHumidity()
            .setNoSpawnRegion()
            .setExtraConditions(
                onlyInHeight(
                    di,
                    new Interval(MCHelper.WATER_HEIGHT, MCHelper.WATER_HEIGHT +20)
                ),
                onlyInAmp(
                    di,
                    new Interval(60, Double.POSITIVE_INFINITY)
                )
            );
    }

}
