package weightedgpa.infinibiome.internal.generators.interchunks.plant;

import net.minecraft.block.Blocks;
import weightedgpa.infinibiome.api.Infinibiome;
import weightedgpa.infinibiome.api.dependency.DependencyInjector;
import weightedgpa.infinibiome.api.posdata.LandmassInfo;
import weightedgpa.infinibiome.internal.floatfunc.util.Interval;

import static weightedgpa.infinibiome.internal.generators.utils.condition.ConditionHelper.*;

public final class LilyGen extends PlantGenBase {
    public LilyGen(DependencyInjector di) {
        super(
            di,
            Infinibiome.MOD_ID + ":lily"
        );

        config = initConfig()
            .setPlant(
                Blocks.LILY_PAD
            )
            .setAboveWater()
            .setScatteredRate(
                new Interval(0, 32)
            )
            .neverInMushroomIsland()
            .anyNonFreezingTemperature()
            .anyHumidity()
            .setSpawnRegion(
                PlantHelper.COMMON_REGION_RATE/3
            )
            .setExtraConditions(
                onlyInLandMass(
                    di,
                    LandmassInfo::isLand
                )
            );
    }

}
