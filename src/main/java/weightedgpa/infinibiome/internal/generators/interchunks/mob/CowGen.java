package weightedgpa.infinibiome.internal.generators.interchunks.mob;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.passive.CowEntity;
import weightedgpa.infinibiome.api.Infinibiome;
import weightedgpa.infinibiome.api.dependency.DependencyInjector;
import weightedgpa.infinibiome.internal.generators.utils.GenHelper;

import static weightedgpa.infinibiome.internal.generators.utils.condition.ConditionHelper.*;

//spawns in low tree density that's not a desert nor freezing
public final class CowGen extends MobGenBase {
    public CowGen(DependencyInjector di) {
        super(di, Infinibiome.MOD_ID + ":cow");

        config = initConfig()
            .getEntity(
                w -> new CowEntity(EntityType.COW, w.getWorld())
            )
            .setGroupCount(2, 3)
            .setBabyChance(MobHelper.COMMON_BABY_RATE)
            .alwaysAboveWater()
            .neverInMushroomIsland()
            .anyNonHighSlope()
            .onlyOnNonBeachLand()
            .anyTemperatureIncludingFreezing()
            .anyNonDesertHumidity()
            .setChancePerChunk(MobHelper.COMMON_RATE * 2)
            .addExtraConditions(
                chancePerChunk(0.5).activeOutside(
                    onlyInTreeDensity(
                        di,
                        GenHelper.NON_FORESTED_TREE_INTERVAL
                    )
                ),
                onlyIfNotNear(
                    di,
                    2,
                    MobGenBase::canSpawnAtInterChunk,
                    WolfGen.class
                )
            );
    }
}
