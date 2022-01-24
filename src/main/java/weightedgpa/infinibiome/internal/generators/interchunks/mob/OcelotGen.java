package weightedgpa.infinibiome.internal.generators.interchunks.mob;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.passive.OcelotEntity;
import weightedgpa.infinibiome.api.Infinibiome;
import weightedgpa.infinibiome.api.dependency.DependencyInjector;
import weightedgpa.infinibiome.internal.floatfunc.util.Interval;

import static weightedgpa.infinibiome.api.posdata.PosDataHelper.*;
import static weightedgpa.infinibiome.internal.generators.utils.condition.ConditionHelper.*;

//spawns in hot climate with high tree density
public final class OcelotGen extends MobGenBase {
    public OcelotGen(DependencyInjector di) {
        super(di, Infinibiome.MOD_ID + ":ocelot");

        config = initConfig()
            .getEntity(
                w -> new OcelotEntity(EntityType.OCELOT, w.getWorld())
            )
            .setGroupCount(1)
            .setBabyChance(MobHelper.COMMON_BABY_RATE)
            .alwaysAboveWater()
            .neverInMushroomIsland()
            .anySlopeIncludingHigh()
            .onlyOnNonBeachLand()
            .setTemperature(
                HOT_INTERVAL
            )
            .anyNonDesertHumidity()
            .setChancePerChunk(
                1/100d
            )
            .addExtraConditions(
                onlyInTreeDensity(
                    di,
                    new Interval(0.5, 1)
                )
            );
    }
}
