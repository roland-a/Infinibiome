package weightedgpa.infinibiome.internal.generators.interchunks.tree;

import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorld;
import weightedgpa.infinibiome.internal.misc.Pair;
import weightedgpa.infinibiome.internal.floatfunc.FloatFunc;
import weightedgpa.infinibiome.internal.floatfunc.modifiers.IntervalMapperWrapper;
import weightedgpa.infinibiome.internal.floatfunc.util.Interval;
import weightedgpa.infinibiome.internal.misc.MCHelper;
import weightedgpa.infinibiome.api.generators.Seed;
import weightedgpa.infinibiome.api.pos.BlockPos2D;
import weightedgpa.infinibiome.internal.misc.Helper;


final class TreeHelper {
    private TreeHelper() {}

    static final double COMMON_REGION_RATE = 0.5f;

    static final int COMMON_ISOLATION_RADIUS = 3;

    static final Pair<Integer, Integer> SMALL_TREE_HEIGHT = new Pair<>(5, 8);

    static FloatFunc<BlockPos2D> initCommonDensity(Seed seed){
        seed = seed.newSeed("density");

        return Helper.initUniformNoise(seed, Helper.COMMON_SCALE)
            .skew(
                FloatFunc.constFunc(-5)
            );
    }

    static FloatFunc<BlockPos2D> initSpruceRatioNoise(Seed seed){
        seed = seed.newSeed("ratio");

        FloatFunc<BlockPos2D> base = Helper.initUniformNoise(seed, Helper.COMMON_SCALE);

        return new IntervalMapperWrapper<>(base)
            .addBranch(
                new Interval(0/4d, 1/4d),
                0, 0
            )
            .addBranch(
                new Interval(1/4d, 3/4d),
                0, 1
            )
            .addBranch(
                new Interval(3/4d, 4/4d),
                1, 1
            );
    }

    static void fixTwoByTwoTrees(BlockPos pos, IWorld world) {
        for (int y = 1; y <= 3; y++) {
            BlockPos currPos = pos.down(y);

            if (canPlaceDirt(currPos, world)) {
                placeDirt(currPos, world);
                break;
            }
        }
    }

    private static boolean canPlaceDirt(BlockPos pos, IWorld world){
        for (int x = -1; x <= 1; x++) {
            for (int z = -1; z <= 1; z++) {
                BlockPos currPos = pos.add(x, 0, z);

                Block block = world.getBlockState(currPos).getBlock();

                if (block.equals(Blocks.GRASS_BLOCK) || block.equals(Blocks.DIRT)){
                    return true;
                }
            }
        }
        return false;
    }

    private static void placeDirt(BlockPos pos, IWorld world){
        for (int x = -1; x <= 1; x++) {
            for (int z = -1; z <= 1; z++) {
                BlockPos currPos = pos.add(x, 0, z);

                Block trunkBlock = world.getBlockState(currPos.up()).getBlock();

                if (!BlockTags.LOGS.contains(trunkBlock)) continue;

                world.setBlockState(currPos, Blocks.DIRT.getDefaultState(), MCHelper.DEFAULT_FLAG);

                fixGrass(currPos.down(), world);
            }
        }
    }

    private static void fixGrass(BlockPos pos, IWorld world){
        if (!world.getBlockState(pos).getBlock().equals(Blocks.GRASS_BLOCK)) return;

        world.setBlockState(pos, Blocks.DIRT.getDefaultState(), MCHelper.DEFAULT_FLAG);
    }
}
