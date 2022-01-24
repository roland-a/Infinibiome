package weightedgpa.infinibiome.internal.generators.interchunks.mob;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.passive.PandaEntity;
import weightedgpa.infinibiome.api.Infinibiome;
import weightedgpa.infinibiome.api.dependency.DependencyInjector;
import weightedgpa.infinibiome.api.posdata.PosDataHelper;
import weightedgpa.infinibiome.internal.generators.interchunks.plant.BambooGen;

import static weightedgpa.infinibiome.internal.generators.utils.condition.ConditionHelper.*;

//spawns near bamboos
public final class PandaGen extends MobGenBase {
    public PandaGen(DependencyInjector di) {
        super(di, Infinibiome.MOD_ID + ":panda");

        config = initConfig()
            .getEntity(
                w -> new PandaEntity(EntityType.PANDA, w.getWorld())
            )
            .setGroupCount(1)
            .setBabyChance(MobHelper.COMMON_BABY_RATE)
            .alwaysAboveWater()
            .neverInMushroomIsland()
            .anyNonHighSlope()
            .onlyOnNonBeachLand()
            .setTemperature(
                PosDataHelper.HOT_INTERVAL
            )
            .setHumidity(
                PosDataHelper.WET_INTERVAL
            )
            .setChancePerChunk(1/50d)
            .addExtraConditions(
                //todo find a better way to do this
                onlyIfNotNear(
                    di,
                    3,
                    (m, p) -> m.getConditions().canBeHere(p),
                    BambooGen.class
                )
                .invert()
            );
    }
}
