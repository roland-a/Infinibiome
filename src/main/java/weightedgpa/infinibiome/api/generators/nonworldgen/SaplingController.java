package weightedgpa.infinibiome.api.generators.nonworldgen;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorld;

import java.util.Random;

public interface SaplingController extends PlantGrowthController {
    void growFromSapling(BlockPos pos, IWorld world, Random random);
}
