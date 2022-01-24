package weightedgpa.infinibiome.internal.generators.interchunks.mob;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.passive.PigEntity;
import weightedgpa.infinibiome.api.Infinibiome;
import weightedgpa.infinibiome.api.dependency.DependencyInjector;
import weightedgpa.infinibiome.internal.generators.utils.condition.ConditionHelper;

//spawns everywhere that's not a desert nor freezing
public final class PigGen extends MobGenBase {
    public PigGen(DependencyInjector di) {
        super(di, Infinibiome.MOD_ID + ":pig");

        config = initConfig()
            .getEntity(
                w -> new PigEntity(EntityType.PIG, w.getWorld())
            )
            .setGroupCount(4, 5)
            .setBabyChance(MobHelper.COMMON_BABY_RATE)
            .alwaysAboveWater()
            .neverInMushroomIsland()
            .anyNonHighSlope()
            .inLandOrBeach()
            .anyNonFreezingTemp()
            .anyNonDesertHumidity()
            .setChancePerChunk(
                MobHelper.COMMON_RATE
            )
            .addExtraConditions(
                ConditionHelper.onlyIfNotNear(
                    di,
                    2,
                    MobGenBase::canSpawnAtInterChunk,
                    WolfGen.class
                )
            );
    }
}
