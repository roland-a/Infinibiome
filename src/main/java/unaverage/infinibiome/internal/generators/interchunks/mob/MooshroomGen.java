package weightedgpa.infinibiome.internal.generators.interchunks.mob;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.passive.MooshroomEntity;
import weightedgpa.infinibiome.api.Infinibiome;
import weightedgpa.infinibiome.api.dependency.DependencyInjector;

//spawns only in mushroom island
public final class MooshroomGen extends MobGenBase {
    public MooshroomGen(DependencyInjector di) {
        super(di, Infinibiome.MOD_ID + ":mooshroom");

        config = initConfig()
            .getEntity(
                w -> new MooshroomEntity(EntityType.MOOSHROOM, w.getWorld())
            )
            .setGroupCount(2, 3)
            .setBabyChance(MobHelper.COMMON_BABY_RATE)
            .alwaysAboveWater()
            .onlyInMushroomIsland()
            .anyNonHighSlope()
            .onlyOnNonBeachLand()
            .anyTemperatureIncludingFreezing()
            .anyHumidityIncludingDesert()
            .setChancePerChunk(1/8d)
            .noExtraConditions();
    }
}
