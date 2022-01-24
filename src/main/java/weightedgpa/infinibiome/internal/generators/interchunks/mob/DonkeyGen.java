package weightedgpa.infinibiome.internal.generators.interchunks.mob;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.passive.horse.DonkeyEntity;
import weightedgpa.infinibiome.api.Infinibiome;
import weightedgpa.infinibiome.api.dependency.DependencyInjector;
import weightedgpa.infinibiome.api.posdata.PosDataHelper;
import weightedgpa.infinibiome.internal.floatfunc.util.Interval;
import weightedgpa.infinibiome.internal.generators.utils.GenHelper;

import static weightedgpa.infinibiome.internal.generators.utils.condition.ConditionHelper.*;

//spawns in flat dryish climate with low tree density
//should be more common than horses
public final class DonkeyGen extends MobGenBase {
    public DonkeyGen(DependencyInjector di) {
        super(di, Infinibiome.MOD_ID + ":donkey");

        config = initConfig()
            .getEntity(
                w -> new DonkeyEntity(EntityType.DONKEY, w.getWorld())
            )
            .setGroupCount(4, 6)
            .setBabyChance(MobHelper.COMMON_BABY_RATE)
            .alwaysAboveWater()
            .neverInMushroomIsland()
            .setSlope(
                new Interval(0, 1/2d)
            )
            .onlyOnNonBeachLand()
            .anyTemperatureIncludingFreezing()
            .setHumidity(
                Interval.union(
                    PosDataHelper.DRY_INTERVAL,
                    PosDataHelper.SEMI_DRY_INTERVAL,
                    PosDataHelper.SEMI_WET_INTERVAL
                )
            )
            .setChancePerChunk(1/250d)
            .addExtraConditions(
                onlyInTreeDensity(
                    di,
                    GenHelper.NON_FORESTED_TREE_INTERVAL
                ),
                onlyIfNotNear(
                    di,
                    2,
                    MobGenBase::canSpawnAtInterChunk,
                    PolarBearGen.class
                )
            );
    }
}
