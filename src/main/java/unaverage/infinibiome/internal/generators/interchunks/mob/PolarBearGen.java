package weightedgpa.infinibiome.internal.generators.interchunks.mob;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.passive.PolarBearEntity;
import net.minecraft.world.IWorld;
import weightedgpa.infinibiome.api.Infinibiome;
import weightedgpa.infinibiome.api.dependency.DependencyInjector;
import weightedgpa.infinibiome.api.pos.BlockPos2D;
import weightedgpa.infinibiome.internal.generators.utils.GenHelper;
import weightedgpa.infinibiome.internal.misc.MCHelper;

import static weightedgpa.infinibiome.api.posdata.PosDataHelper.FREEZE_INTERVAL;
import static weightedgpa.infinibiome.internal.generators.utils.condition.ConditionHelper.*;

//spawns in any freezing climate with low tree density
public final class PolarBearGen extends MobGenBase {
    public PolarBearGen(DependencyInjector di) {
        super(di, Infinibiome.MOD_ID + ":polarBear");

        config = initConfig()
            .getEntity(
                w -> new PolarBearEntity(EntityType.POLAR_BEAR, w.getWorld())
            )
            .setGroupCount(1)
            .setBabyChance(0.5)
            .alwaysAboveWater()
            .neverInMushroomIsland()
            .anyNonHighSlope()
            .setLandMass(l -> true)
            .setTemperature(
                FREEZE_INTERVAL
            )
            .anyHumidityIncludingDesert()
            .setChancePerChunk(
                1/100d
            )
            .addExtraConditions(
                onlyInTreeDensity(
                    di,
                    GenHelper.NON_FORESTED_TREE_INTERVAL
                )
            );
    }

    @Override
    int getHeight(BlockPos2D mobPos2D, IWorld world) {
        return MCHelper.getHighestSurfaceHeight(mobPos2D, world) + 1;
    }
}
