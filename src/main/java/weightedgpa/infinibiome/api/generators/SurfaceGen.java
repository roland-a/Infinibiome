package weightedgpa.infinibiome.api.generators;

import net.minecraft.world.IWorld;
import weightedgpa.infinibiome.api.dependency.MultiDep;
import weightedgpa.infinibiome.api.pos.BlockPos2D;

import java.util.Random;

/**
 * Objects that generate surface blocks, like grass or podzol
 */
public interface SurfaceGen extends MultiDep {
    Timing getTiming();

    void generate(BlockPos2D pos, IWorld world, Random random);
}
