package weightedgpa.infinibiome.api.generators.nonworldgen;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorld;
import weightedgpa.infinibiome.api.dependency.MultiDep;
import weightedgpa.infinibiome.api.pos.BlockPos2D;

import java.util.Random;

/**
 * Used for controlling what plants generates after bonemealing the ground
 */
public interface GroundBoneMealController extends MultiDep {
    double getGroundBonemealChance(BlockPos2D pos);

    void spawnFromGroundBoneMeal(BlockPos plantPos, IWorld world, Random random);
}
