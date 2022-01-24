package weightedgpa.infinibiome.internal.generators.interchunks.mob;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.passive.WolfEntity;
import weightedgpa.infinibiome.api.Infinibiome;
import weightedgpa.infinibiome.api.dependency.DependencyInjector;
import weightedgpa.infinibiome.internal.generators.utils.GenHelper;

import static weightedgpa.infinibiome.internal.generators.utils.condition.ConditionHelper.*;

//spawns in coldish climate
public final class WolfGen extends MobGenBase {
    public WolfGen(DependencyInjector di) {
        super(di, Infinibiome.MOD_ID + ":wolf");

        config = initConfig()
            .getEntity(w -> new WolfEntity(EntityType.WOLF, w.getWorld()))
            .setGroupCount(4, 5)
            .setBabyChance(MobHelper.COMMON_BABY_RATE)
            .alwaysAboveWater()
            .neverInMushroomIsland()
            .anyNonHighSlope()
            .onlyOnNonBeachLand()
            .setTemperature(
                GenHelper.COLDISH
            )
            .anyNonDesertHumidity()
            .setChancePerChunk(1/200d)
            .addExtraConditions(
                onlyInTreeDensity(
                    di,
                    GenHelper.FORESTED_TREE_INTERVAL
                ),
                onlyIfNotNear(
                    di,
                    2,
                    MobGenBase::canSpawnAtInterChunk,
                    LlamaGen.class
                )
            );
    }
}
