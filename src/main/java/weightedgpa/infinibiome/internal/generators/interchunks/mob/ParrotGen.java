package weightedgpa.infinibiome.internal.generators.interchunks.mob;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.passive.ParrotEntity;
import net.minecraft.world.IWorld;
import weightedgpa.infinibiome.api.Infinibiome;
import weightedgpa.infinibiome.internal.floatfunc.util.Interval;
import weightedgpa.infinibiome.api.dependency.DependencyInjector;
import weightedgpa.infinibiome.internal.generators.utils.GenHelper;
import weightedgpa.infinibiome.internal.misc.MCHelper;
import weightedgpa.infinibiome.api.pos.BlockPos2D;
import weightedgpa.infinibiome.api.pos.InterChunkPos;
import net.minecraft.util.math.BlockPos;


import java.util.concurrent.ThreadLocalRandom;

import static weightedgpa.infinibiome.internal.generators.utils.condition.ConditionHelper.*;

//spawns in mountainous areas
public final class ParrotGen extends MobGenBase {
    public ParrotGen(DependencyInjector di) {
        super(di, Infinibiome.MOD_ID + ":parrot");

        config = initConfig()
            .getEntity(
                this::getEntity
            )
            .setGroupCount(1)
            .setBabyChance(MobHelper.COMMON_BABY_RATE)
            .alwaysAboveWater()
            .neverInMushroomIsland()
            .anySlopeIncludingHigh()
            .onlyOnNonBeachLand()
            .anyNonFreezingTemp()
            .anyNonDesertHumidity()
            .setChancePerChunk(1/20d)
            .addExtraConditions(
                onlyInTreeDensity(
                    di,
                    GenHelper.FORESTED_TREE_INTERVAL
                ),
                onlyInHeight(
                    di,
                    new Interval(MCHelper.WATER_HEIGHT + 30, Double.POSITIVE_INFINITY)
                ),
                onlyInAmp(
                    di,
                    new Interval(60, Double.POSITIVE_INFINITY)
                )
            );
    }

    @Override
    int getHeight(BlockPos2D mobPos2D, IWorld world) {
        return MCHelper.getHighestNonAirY(mobPos2D, world) + 1;
    }

    private AnimalEntity getEntity(BlockPos mobPos, InterChunkPos interChunkPos, IWorld world) {
        ParrotEntity result = new ParrotEntity(EntityType.PARROT, world.getWorld());

        int variant = ThreadLocalRandom.current()
            .nextInt(5);

        result.setVariant(variant);

        return result;
    }
}
