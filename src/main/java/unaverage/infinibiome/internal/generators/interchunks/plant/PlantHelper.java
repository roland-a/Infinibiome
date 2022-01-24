package weightedgpa.infinibiome.internal.generators.interchunks.plant;

import com.google.common.collect.ImmutableList;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.DoublePlantBlock;
import net.minecraft.state.properties.DoubleBlockHalf;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorldReader;
import weightedgpa.infinibiome.api.generators.Seed;
import weightedgpa.infinibiome.api.pos.BlockPos2D;
import weightedgpa.infinibiome.api.posdata.PosDataProvider;
import weightedgpa.infinibiome.internal.floatfunc.FloatFunc;
import weightedgpa.infinibiome.internal.floatfunc.generators.RandomGen;
import weightedgpa.infinibiome.internal.floatfunc.util.Interval;
import weightedgpa.infinibiome.internal.minecraftImpl.IBBiomes;
import weightedgpa.infinibiome.internal.misc.Helper;
import weightedgpa.infinibiome.internal.misc.MCHelper;

import java.util.List;

final class PlantHelper {
    private PlantHelper(){}

    static final double COMMON_REGION_RATE = 0.3f;

    static List<BlockState> initDouble(Block block){
        return ImmutableList.of(
            block.getDefaultState().with(DoublePlantBlock.HALF, DoubleBlockHalf.LOWER),
            block.getDefaultState().with(DoublePlantBlock.HALF, DoubleBlockHalf.UPPER)
        );
    }

    static final double COMMON_CLUSTERED_SKEW = 0;
    static final double COMMON_SCATTERED_SKEW = -5;

    static FloatFunc<BlockPos2D> getRateFunc(Seed seed, Interval count, double skew){
        seed = seed.newSeed("rate");

        return Helper
            .initUniformNoise(seed, Helper.COMMON_SCALE)
            .skew(
                FloatFunc.constFunc(skew)
            )
            .mapInterval(count);
    }

    static FloatFunc<BlockPos2D> getCommonClusterRateFunc(Seed seed){
        return getRateFunc(seed, new Interval(0.1, 0.3), COMMON_CLUSTERED_SKEW);
    }

    static FloatFunc<BlockPos2D> getRadiusFunc(Seed seed, Interval radius){
        seed = seed.newSeed("radius");

        assert radius.getMin() >= 0;
        assert radius.getMax() <= 8;

        return new RandomGen(seed)
            .asPercentFloatFunc(BlockPos2D.INFO)
            .mapInterval(radius);
    }

    static final Interval COMMON_RADIUS = new Interval(3, 5);

    static final Interval COMMON_DENSITY = new Interval(0.2, 0.4);

    static final Interval SCATTERED_RADIUS = new Interval(0, 0);

    static final Interval SCATTERED_DENSITY = new Interval(1, 1);

    static boolean iceAtPos(BlockPos pos, IWorldReader world, PosDataProvider posData){
        if (MCHelper.isMostlyWater(world.getBlockState(pos.up()))) return false;
        
        return IBBiomes.getBiome(MCHelper.to2D(pos), posData).doesWaterFreeze(world, pos, false);
    }
}
