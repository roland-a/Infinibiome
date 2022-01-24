package weightedgpa.infinibiome.internal.generators.interchunks.plant;

import net.minecraft.block.Blocks;
import weightedgpa.infinibiome.api.Infinibiome;
import weightedgpa.infinibiome.api.dependency.DependencyInjector;
import weightedgpa.infinibiome.api.posdata.LandmassInfo;
import weightedgpa.infinibiome.internal.floatfunc.util.Interval;
import weightedgpa.infinibiome.internal.generators.utils.GenHelper;

import static weightedgpa.infinibiome.internal.generators.utils.condition.ConditionHelper.*;


public final class DeadBushGen extends PlantGenBase {
    public DeadBushGen(DependencyInjector di) {
        super(
            di,
            Infinibiome.MOD_ID + ":deadBush"
        );

        config = initConfig()
            .setPlant(Blocks.DEAD_BUSH)
            .setAboveWater()
            .setScatteredRate(
                new Interval(0, 20)
            )
            .neverInMushroomIsland()
            .anyTemperatureIncludingFreezing()
            .setHumdity(
                GenHelper.DRYISH
            )
            .setSpawnRegion(
                PlantHelper.COMMON_REGION_RATE*3
            )
            .setExtraConditions(
                onlyInLandMass(
                    di,
                    LandmassInfo::isLand
                )
            )
            .setNoGroundBoneMeal();
    }

}