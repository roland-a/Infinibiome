package weightedgpa.infinibiome.internal.generators.interchunks.struct;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorld;
import net.minecraftforge.common.Tags;
import org.apache.commons.lang3.mutable.MutableInt;
import weightedgpa.infinibiome.api.dependency.DependencyInjector;
import weightedgpa.infinibiome.api.pos.BlockPos2D;
import weightedgpa.infinibiome.api.pos.InterChunkPos;
import weightedgpa.infinibiome.api.posdata.PosDataKeys;
import weightedgpa.infinibiome.api.posdata.PosDataProvider;
import weightedgpa.infinibiome.internal.floatfunc.util.Interval;
import weightedgpa.infinibiome.internal.generators.utils.condition.Condition;
import weightedgpa.infinibiome.internal.generators.utils.condition.ConditionHelper;
import weightedgpa.infinibiome.internal.misc.MCHelper;

import javax.annotation.Nullable;
import java.util.List;
import java.util.function.Predicate;

final class StructHelper {
    private StructHelper(){}

    static void placeDirtUnderStruct(InterChunkPos interChunkPos, IWorld world, PosDataProvider data){
        interChunkPos.forEachCenterPos(p -> {
            Integer fillDirtHeight = getLowestLocalBuildingBlock(p, world, data);

            if (fillDirtHeight == null) return;

            int terrainHeight = MCHelper.getHighestTerrainHeight(p, world);

            /*
            world.setBlockState(
                p.to3D(terrainHeight),
                Blocks.PODZOL.getDefaultState(),
                MCHelper.DEFAULT_TAG
            );

            world.setBlockState(
                p.to3D(fillDirtHeight),
                Blocks.OAK_PLANKS.getDefaultState(),
                MCHelper.DEFAULT_TAG
            );

             */

            for (int y = terrainHeight; y < fillDirtHeight; y++){
                BlockPos currPos = p.to3D(y);

                world.setBlockState(
                    currPos,
                    getFillerBlock(p, data),
                    MCHelper.DEFAULT_FLAG
                );
            }

            if (getFillerBlock(p, data).getBlock().equals(Blocks.DIRT)) {
                BlockPos grassPos = p.to3D(fillDirtHeight - 1);

                world.setBlockState(
                    grassPos,
                    Blocks.GRASS_BLOCK.getDefaultState(),
                    MCHelper.DEFAULT_FLAG
                );
            }
        });
    }

    private static BlockState getFillerBlock(BlockPos2D pos, PosDataProvider data){
        List<BlockState> groundBlocks = data.get(PosDataKeys.GROUND_BLOCKS, pos);

        if (groundBlocks.isEmpty()) return Blocks.COBBLESTONE.getDefaultState();

        return groundBlocks.get(0);
    }

    @Nullable
    private static Integer getLowestLocalBuildingBlock(BlockPos2D pos, IWorld world, PosDataProvider data){
        MutableInt overallLowestHeight = new MutableInt(Integer.MAX_VALUE);

        scan(
            pos,
            p -> {
                Integer currLowestHeight = getLowestBuildingBlock(p, world);

                //prevents from looking into other buildings
                if (currLowestHeight == null) return false;

                if (currLowestHeight < overallLowestHeight.getValue()) {
                    overallLowestHeight.setValue(currLowestHeight);
                }
                return true;
            }
        );

        if (overallLowestHeight.getValue() == Integer.MAX_VALUE) return null;

        return overallLowestHeight.getValue();
    }

    @Nullable
    private static Integer getLowestBuildingBlock(BlockPos2D pos, IWorld world){
        int terrainHeight = MCHelper.getHighestTerrainHeight(pos, world);

        for (int y = terrainHeight + 1; y < 255; y++){
            BlockPos currPos = pos.to3D(y);

            if (isValidBuildingBlock(currPos, world)) {
                /*
                world.setBlockState(
                    pos.to3D(terrainHeight),
                    Blocks.PODZOL.getDefaultState(),
                    MCHelper.DEFAULT_TAG
                );

                world.setBlockState(
                    pos.to3D(y),
                    Blocks.OAK_PLANKS.getDefaultState(),
                    MCHelper.DEFAULT_TAG
                );

                 */


                return y;
            }
        }
        return null;
    }

    private static void scan(BlockPos2D center, Predicate<BlockPos2D> func){
        {
            boolean flag = func.test(center);

            if (!flag) return;
        }

        for (Direction d: MCHelper.NSWE){
            for (int i = 1; i <= 7; i++){
                BlockPos2D currPos = center.offset(d, i);

                boolean flag = func.test(currPos);

                if (!flag) break;
            }
        }
        for (Direction d: MCHelper.NSWE){
            for (int i = 1; i <= 7; i++){
                BlockPos2D currPos = center.offset(d, i).offset(d.rotateY(), i);

                boolean flag = func.test(currPos);

                if (!flag) break;
            }
        }
    }

    private static boolean isValidBuildingBlock(BlockPos pos, IWorld world){
        BlockState block = world.getBlockState(pos);

        for (int x = -1; x <= 1; x++){
            for (int y = -1; y <= 1; y++) {
                for (int z = -1; z <= 1; z++) {
                    if (BlockTags.LEAVES.contains(world.getBlockState(pos.add(x,y,z)).getBlock())) {
                        return false;
                    }
                }
            }
        }

        if (Blocks.GRASS_PATH.equals(block.getBlock())) return false;

        if (BlockTags.LEAVES.contains(block.getBlock())) return false;

        if (Blocks.FARMLAND.equals(block.getBlock())) return true;

        if (Tags.Blocks.DIRT.contains(block.getBlock())) return false;

        if (Tags.Blocks.SAND.contains(block.getBlock())) return false;

        if (Blocks.MUSHROOM_STEM.equals(block.getBlock())) return false;

        if (Blocks.BROWN_MUSHROOM_BLOCK.equals(block.getBlock())) return false;

        if (Blocks.RED_MUSHROOM_BLOCK.equals(block.getBlock())) return false;

        if (MCHelper.isSolid(block)) return true;

        if (Tags.Blocks.GLASS.contains(block.getBlock())) return true;

        if (Tags.Blocks.GLASS_PANES.contains(block.getBlock())) return true;

        if (BlockTags.SLABS.contains(block.getBlock())) return true;

        if (BlockTags.STAIRS.contains(block.getBlock())) return true;

        if (BlockTags.DOORS.contains(block.getBlock())) return true;

        if (BlockTags.FENCES.contains(block.getBlock())) return true;

        if (BlockTags.WALLS.contains(block.getBlock())) return true;

        return false;
    }

    public static Condition alwaysAboveWater(DependencyInjector di, int radius) {
        return ConditionHelper.onlyInHeight(
            di,
            radius,
            new Interval(
                MCHelper.WATER_HEIGHT + 1,
                Double.POSITIVE_INFINITY
            )
        );
    }
    /*
    public static void replaceWithSuitableCrop(InterChunkPos interChunkPos, IWorld world, DependencyInjector di){
        interChunkPos.forEachCenterPos( p -> {
            for (int y = 0; y < 255; y++){
                BlockPos pos = p.to3D(y);

                BlockState block = world.getBlockState(pos);

                if ()

            }
        });
    }

     */
}
