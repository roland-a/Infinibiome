package weightedgpa.infinibiome.internal.generators.interchunks.mob;


import net.minecraft.entity.EntityType;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.passive.horse.HorseEntity;
import net.minecraft.world.IWorld;
import weightedgpa.infinibiome.api.Infinibiome;
import weightedgpa.infinibiome.api.dependency.DependencyInjector;
import weightedgpa.infinibiome.api.posdata.PosDataHelper;
import weightedgpa.infinibiome.internal.floatfunc.util.Interval;
import weightedgpa.infinibiome.internal.generators.utils.GenHelper;
import weightedgpa.infinibiome.api.pos.InterChunkPos;
import net.minecraft.util.math.BlockPos;

import java.util.Random;

import static weightedgpa.infinibiome.internal.generators.utils.condition.ConditionHelper.*;

//spawns in dryish climate with low tree density that's not a desert nor cold
public final class HorseGen extends MobGenBase {
    public HorseGen(DependencyInjector di) {
        super(di, Infinibiome.MOD_ID + ":horse");

        config = initConfig()
            .getEntity(this::getEntity)
            .setGroupCount(4, 5)
            .setBabyChance(1/5d)
            .alwaysAboveWater()
            .neverInMushroomIsland()
            .setSlope(
                new Interval(0, 1/2d)
            )
            .onlyOnNonBeachLand()
            .anyTemperatureIncludingFreezing()
            .setHumidity(
                Interval.union(
                    PosDataHelper.SEMI_DRY_INTERVAL,
                    PosDataHelper.SEMI_WET_INTERVAL
                )
            )
            .setChancePerChunk(1/500d)
            .addExtraConditions(
                onlyInTreeDensity(
                    di,
                    GenHelper.NON_FORESTED_TREE_INTERVAL
                )
            );
    }

    private AnimalEntity getEntity(BlockPos mobPos, InterChunkPos interChunkPos, IWorld world) {
        HorseEntity result = new HorseEntity(EntityType.HORSE, world.getWorld());

        result.setHorseVariant(
            getHorseBase(interChunkPos) + getHorseMarking(mobPos)*256
        );

        return result;
    }

    private int getHorseBase(InterChunkPos interChunkPos) {
        Random random = randomGen.getRandom(interChunkPos.getX(), interChunkPos.getZ());

        return random.nextInt(7);
    }

    private int getHorseMarking(BlockPos horsePos) {
        Random random = randomGen.getRandom(horsePos.getX(), horsePos.getZ());

        return random.nextInt(6);
    }
}

