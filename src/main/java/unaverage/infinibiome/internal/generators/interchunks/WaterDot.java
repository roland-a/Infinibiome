package weightedgpa.infinibiome.internal.generators.interchunks;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorld;
import net.minecraftforge.common.Tags;
import weightedgpa.infinibiome.api.Infinibiome;
import weightedgpa.infinibiome.api.dependency.DependencyInjector;
import weightedgpa.infinibiome.api.generators.InterChunkGenTimings;
import weightedgpa.infinibiome.api.generators.Timing;
import weightedgpa.infinibiome.api.generators.nonworldgen.Locatable;
import weightedgpa.infinibiome.api.posdata.PosDataHelper;
import weightedgpa.infinibiome.internal.generators.utils.condition.ConditionHelper;
import weightedgpa.infinibiome.internal.misc.MCHelper;

import java.util.Random;

public final class WaterDot extends SmallObjectGenBase implements Locatable.HasPointsProvider {
    public WaterDot(DependencyInjector di) {
        super(di, Infinibiome.MOD_ID + ":waterDot");

        config = initConfig()
            .getGenerateFunc(
                this::generate
            )
            .setCount(64)
            //good chance one will overlap and not generate
            .setAttemptsPerCount(4)
            .atHighestTerrainBlock()
            .onlyInRegion(
                0.5f
            )
            .addExtraConditions(
                ConditionHelper.onlyInHumidity(
                    di,
                    PosDataHelper.WET_INTERVAL
                )
            );
    }

    private void generate(BlockPos pos, IWorld world, Random random){
        {
            BlockState block = world.getBlockState(pos);

            if (Tags.Blocks.DIRT.contains(block.getBlock())) return;
        }
        {
            BlockState block = world.getBlockState(pos.up());

            if (!MCHelper.isMostlyAir(block)) return;
        }
        for (Direction d: MCHelper.NSWE){
            BlockState block = world.getBlockState(pos.offset(d));

            if (!MCHelper.isSolid(block)) return;
        }
        {
            BlockState block = world.getBlockState(pos.down());

            if (MCHelper.isSolid(block)) return;
        }

        world.setBlockState(pos, Blocks.WATER.getDefaultState(), MCHelper.DEFAULT_FLAG);
    }

    @Override
    public Timing getInterChunkTiming() {
        return InterChunkGenTimings.WATER_POOL;
    }
}
