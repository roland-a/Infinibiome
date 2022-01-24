package weightedgpa.infinibiome.internal.generators.interchunks.mob;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.passive.horse.LlamaEntity;
import net.minecraft.world.IWorld;
import weightedgpa.infinibiome.api.Infinibiome;
import weightedgpa.infinibiome.api.dependency.DependencyInjector;
import weightedgpa.infinibiome.api.pos.InterChunkPos;
import net.minecraft.util.math.BlockPos;
import weightedgpa.infinibiome.internal.floatfunc.util.Interval;
import weightedgpa.infinibiome.internal.generators.utils.GenHelper;
import weightedgpa.infinibiome.internal.misc.MCHelper;

import java.util.Random;

import static weightedgpa.infinibiome.api.posdata.PosDataHelper.*;
import static weightedgpa.infinibiome.internal.generators.utils.condition.ConditionHelper.*;

//spawns in dryish climate in high altitudes with low tree density
public final class LlamaGen extends MobGenBase {
    private static final int[] hotVariants = {0, 2};
    private static final int[] otherVariants = {1, 3};

    public LlamaGen(DependencyInjector di) {
        super(di, Infinibiome.MOD_ID + ":llama");

        config = initConfig()
            .getEntity(
                this::getEntity
            )
            .setGroupCount(4, 5)
            .setBabyChance(1/5d)
            .alwaysAboveWater()
            .neverInMushroomIsland()
            .anySlopeIncludingHigh()
            .onlyOnNonBeachLand()
            .anyTemperatureIncludingFreezing()
            .setHumidity(
                GenHelper.DRYISH
            )
            .setChancePerChunk(1/20d)
            .addExtraConditions(
                onlyInTreeDensity(
                    di,
                    GenHelper.NON_FORESTED_TREE_INTERVAL
                ),
                onlyInAmp(
                    di,
                    new Interval(60, Double.POSITIVE_INFINITY)
                ),
		        onlyInHeight(
                    di,
                    new Interval(MCHelper.WATER_HEIGHT + 30, Double.POSITIVE_INFINITY)
                )
            );
    }

    private AnimalEntity getEntity(BlockPos mobPos, InterChunkPos interChunkPos, IWorld world) {
        LlamaEntity result = new LlamaEntity(EntityType.LLAMA, world.getWorld());

        int variant = getVariants(mobPos);

        result.setVariant(variant);

        return result;
    }

    private int getVariants(BlockPos mobPos) {
        Random random = new Random();

        double temperature = getTemperature(mobPos, posData);

        if (HOT_INTERVAL.contains(temperature)) {
            int randomIndex = random.nextInt(hotVariants.length);

            return hotVariants[randomIndex];
        }

        int randomIndex = random.nextInt(otherVariants.length);

        return otherVariants[randomIndex];
    }
}
