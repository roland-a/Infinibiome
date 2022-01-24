package weightedgpa.infinibiome.internal.generators.interchunks.mob;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.passive.ChickenEntity;
import weightedgpa.infinibiome.api.Infinibiome;
import weightedgpa.infinibiome.api.dependency.DependencyInjector;

import static weightedgpa.infinibiome.internal.generators.utils.condition.ConditionHelper.*;

//spawns in any area that's not a desert nor freezing
public final class ChickenGen extends MobGenBase {
    public ChickenGen(DependencyInjector di) {
        super(di, Infinibiome.MOD_ID + ":chicken");

        config = initConfig()
            .getEntity(w -> new ChickenEntity(EntityType.CHICKEN, w.getWorld()))
            .setGroupCount(4, 5)
            .setBabyChance(MobHelper.COMMON_BABY_RATE)
            .alwaysAboveWater()
            .neverInMushroomIsland()
            .anyNonHighSlope()
            .onlyOnNonBeachLand()
            .anyNonFreezingTemp()
            .anyNonDesertHumidity()
            .setChancePerChunk(MobHelper.COMMON_RATE)
            .addExtraConditions(
                onlyIfNotNear(
                    di,
                    2,
                    MobGenBase::canSpawnAtInterChunk,
                    WolfGen.class,
                    FoxGen.class,
                    OcelotGen.class
                )
            );
    }
}
