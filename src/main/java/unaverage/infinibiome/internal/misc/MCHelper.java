package weightedgpa.infinibiome.internal.misc;

import com.google.common.collect.ImmutableList;
import net.minecraft.block.*;
import net.minecraft.entity.Entity;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.IWorld;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.gen.Heightmap;
import net.minecraftforge.common.IPlantable;
import org.apache.commons.lang3.mutable.MutableInt;
import weightedgpa.infinibiome.api.pos.BlockPos2D;
import weightedgpa.infinibiome.api.pos.IntPosInfo;
import weightedgpa.infinibiome.api.pos.InterChunkPos;
import weightedgpa.infinibiome.api.posdata.PosDataProvider;
import weightedgpa.infinibiome.internal.floatfunc.util.Interval;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;


@SuppressWarnings("Unused")
public final class MCHelper {
    public static final int DEFAULT_FLAG = 4 | 16;
    public static final boolean DEFAULT_IS_MOVING = false;

    public static final int WATER_HEIGHT = 63;

    public static final Interval VALID_WORLD_HEIGHT = new Interval(1, 255);

    public static final IntPosInfo<BlockPos> MC_POS_INFO = new IntPosInfo<BlockPos>() {
        @Override
        public int getIntX(BlockPos pos) {
            return pos.getX();
        }

        @Override
        public int getIntZ(BlockPos pos) {
            return pos.getZ();
        }

        @Override
        public int getLog2Scale() {
            return 0;
        }

        @Override
        public BlockPos build(int x, int z) {
            return new BlockPos(x, 0, z);
        }
    };

    public static final IntPosInfo<ChunkPos> CHUNK_POS_INFO = new IntPosInfo<ChunkPos>() {
        @Override
        public int getIntX(ChunkPos pos) {
            return pos.x;
        }

        @Override
        public int getIntZ(ChunkPos pos) {
            return pos.z;
        }

        @Override
        public int getLog2Scale() {
            return 4;
        }

        @Override
        public ChunkPos build(int x, int z) {
            return new ChunkPos(x, z);
        }
    };

    private MCHelper(){}

    public static void forEachPos(ChunkPos chunkPos, Consumer<BlockPos2D> consumer){
        BlockPos2D.Mutable lowestPos = new BlockPos2D.Mutable();

        for (int x = 0; x < 16; x++){
            for (int z = 0; z < 16; z++){
                lowestPos.setPos(
                    chunkPos.getXStart() + x,
                    chunkPos.getZStart() + z
                );

                consumer.accept(lowestPos);
            }
        }
    }

    public static boolean isMostlyAir(BlockState block) {
        if (block.getBlock().equals(Blocks.SNOW)) return true;

        return !block.getMaterial().isSolid() && block.getFluidState().isEmpty();
    }

    public static boolean isMostlyWater(BlockState block) {
        return block.getBlock().equals(Blocks.WATER) || (!block.getMaterial().isSolid() && !block.getFluidState().isEmpty());
    }

    public static void clearVertically(BlockPos pos, IWorld world, Predicate<BlockState> clearable){
        BlockPos.Mutable mutPos = new BlockPos.Mutable(pos);

        int maxY = getHighestNonAirY(to2D(pos), world);
        int minY = pos.getY();

        for (int y = minY; y <= maxY; y++){
            mutPos.setY(y);

            BlockState block = world.getBlockState(mutPos);

            if (clearable.test(block)) {
                world.setBlockState(mutPos, block.getFluidState().getBlockState(), DEFAULT_FLAG);
            }
        }
    }

    public static boolean isPlant(Block block){
        return block instanceof IPlantable;
    }

    public static boolean isSolid(BlockState block){
        if (block.getBlock().equals(Blocks.SNOW)) return false;

        return block.isSolid();
    }

    public static int getHighestSurfaceHeight(BlockPos2D pos, IWorldReader world){
        return world.getHeight(
            Heightmap.Type.MOTION_BLOCKING_NO_LEAVES,
            pos.getBlockX(),
            pos.getBlockZ()
        ) - 1;
    }

    public static int getHighestTerrainHeight(BlockPos2D pos, IWorldReader world){
        int maxY = world.getHeight(
            Heightmap.Type.OCEAN_FLOOR_WG,
            pos.getBlockX(),
            pos.getBlockZ()
        ) - 1;

        BlockPos.Mutable mutPos = new BlockPos.Mutable(pos.getBlockX(), 0, pos.getBlockZ());

        for (int y = maxY; y > 1; y--){
            BlockState block = world.getBlockState(mutPos);

            if (isSolid(block)) {
                return y;
            }
        }
        return -1;
    }

    public static int getHighestNonAirY(BlockPos2D pos, IWorldReader world) {
        return world.getHeight(
            Heightmap.Type.WORLD_SURFACE,
            pos.getBlockX(),
            pos.getBlockZ()
        ) - 1;
    }

    public static int getHighestY(BlockPos2D pos, IWorldReader world, Predicate<BlockState> predicate) {
        BlockPos.Mutable mutPos = new BlockPos.Mutable(pos.getBlockX(), 0, pos.getBlockZ());

        int maxHeight = getHighestNonAirY(pos, world);

        for (int y = maxHeight; y > 1; y--) {
            mutPos.setY(y);

            BlockState block = world.getBlockState(mutPos);

            if (block.isAir()) continue;

            if (predicate.test(block)) {
                return y;
            }
        }
        return -1;
    }

    public static BlockPos2D to2D(BlockPos pos) {
        return new BlockPos2D(pos.getX(), pos.getZ());
    }

    public static BlockPos2D lowestPos(ChunkPos chunkPos) {
        return new BlockPos2D(chunkPos.getXStart(), chunkPos.getZStart());
    }

    public static final List<Direction> NSWE = ImmutableList.of(
        Direction.NORTH,
        Direction.SOUTH,
        Direction.WEST,
        Direction.EAST
    );

    public static final List<Direction> NSWED = ImmutableList.of(
        Direction.NORTH,
        Direction.SOUTH,
        Direction.WEST,
        Direction.EAST,
        Direction.DOWN
    );

    public static double getTreeDensity(
        InterChunkPos interChunkPos,
        IWorldReader world,
        Supplier<Double> onIndeterminate
    ) {
        MutableInt leafCounter = new MutableInt(0);
        MutableInt groundCounter = new MutableInt(0);

        interChunkPos.forEachCenterPos(
            pos2D -> {
                int leafHeight = getHighestNonAirY(pos2D, world);

                BlockPos leafPos = pos2D.to3D(leafHeight);

                Block leafBlock = world.getBlockState(leafPos).getBlock();

                if (BlockTags.LEAVES.contains(leafBlock)){
                    leafCounter.increment();
                    groundCounter.increment();
                    return;
                }

                int groundHeight = getHighestSurfaceHeight(pos2D, world);

                BlockPos groundPos = pos2D.to3D(groundHeight);

                BlockState groundBlock = world.getBlockState(groundPos);

                if (groundBlock.canSustainPlant(world, leafPos, Direction.DOWN, (IPlantable)Blocks.OAK_SAPLING)){
                    groundCounter.increment();
                }
            }
        );

        if (groundCounter.getValue() <= 50) return onIndeterminate.get();

        return leafCounter.getValue() / (double)groundCounter.getValue();
    }

    public static double getPlantDensity(
        InterChunkPos interChunkPos,
        IWorldReader world,
        IPlantable requestedPlantBlock,
        Supplier<Double> onIndeterminate
    ) {
        MutableInt plantCounter = new MutableInt(0);
        MutableInt groundCounter = new MutableInt(0);

        interChunkPos.forEachCenterPos(p -> {
            int plantHeight = getHighestSurfaceHeight(p, world) + 1;

            BlockPos plantPos = p.to3D(plantHeight);
            Block plantBlock = world.getBlockState(plantPos).getBlock();

            if (plantBlock.equals(requestedPlantBlock)){
                plantCounter.increment();
                groundCounter.increment();
                return;
            }

            BlockPos groundPos = plantPos.down();
            BlockState groundBlock = world.getBlockState(groundPos);

            if (groundBlock.canSustainPlant(world, plantPos, Direction.DOWN, requestedPlantBlock)){
                groundCounter.increment();
            }
        });

        if (groundCounter.getValue() <= 50) return onIndeterminate.get();

        return plantCounter.getValue() / (double)groundCounter.getValue();
    }

    public static boolean spawnEntity(Entity entity, BlockPos pos, IWorld world) {
        if (!world.getWorldBorder().contains(pos)) {
            return false;
        }

        Random random = new Random();

        entity.setLocationAndAngles(
            pos.getX() + 0.5f,
            pos.getY(),
            pos.getZ() + 0.5f,
            random.nextFloat()*360,
            0
        );

        world.addEntity(entity);

        //return MCHelper.toMCWorld(world).getWorld().spawnEntity(entity);
        return true;
    }
}
