package weightedgpa.infinibiome.internal.generators.interchunks;


import net.minecraft.block.BlockState;
import weightedgpa.infinibiome.internal.misc.MCHelper;
import weightedgpa.infinibiome.internal.misc.MathHelper;
import net.minecraft.block.Blocks;
import weightedgpa.infinibiome.internal.floatfunc.FloatFunc;
import weightedgpa.infinibiome.api.pos.BlockPos2D;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.IWorld;
import weightedgpa.infinibiome.api.posdata.PosDataKeys;
import weightedgpa.infinibiome.api.posdata.PosDataProvider;


import java.util.*;


final class SurfacePool {
    private static final int MIN_CLEAR_HEIGHT = 3;
    private static final int MAX_CLEAR_HEIGHT = 4;

    final BlockPos2D center;
    final int liquidY;
    final int clearHeight;
    final Config config;

    private final Map<BlockPos2D, BlockState> blockLayout = new HashMap<>();
    private final PosDataProvider data;

    private SurfacePool(
        BlockPos2D center,
        IWorldReader world,
        PosDataProvider data,
        Random random,
        Config config
    ) {
        this.config = config;
        this.center = center;

        this.data = data;

        initBlockLayout(world, random);

        this.liquidY = getLiquidY();
        this.clearHeight = MathHelper.randomInt(MIN_CLEAR_HEIGHT, MAX_CLEAR_HEIGHT, random);
    }

    static boolean tryGeneratePoolAt(
        BlockPos2D center,
        Config poolConfig,
        IWorld world,
        PosDataProvider data,
        Random random
    ){
        return new SurfacePool(center, world, data, random, poolConfig).generate(world, random);
    }

    Collection<BlockPos2D> iteratePoolAndEdge(int edgeDistance){
        if (edgeDistance == 0) return blockLayout.keySet();

        Collection<BlockPos2D> out = new HashSet<>(blockLayout.keySet());

        for (BlockPos2D pos: blockLayout.keySet()) {
            for (int x = -edgeDistance; x <= edgeDistance; x++) {
                for (int z = -edgeDistance; z <= edgeDistance; z++) {
                    BlockPos2D scanPos = pos.offset(x, z);

                    if (!out.contains(scanPos)){
                        out.add(scanPos);
                    }
                }
            }
        }

        return out;
    }

    private void initBlockLayout(IWorldReader world, Random random){
        int maxRadius = (int)Math.ceil(
            config.innerRadiusFunc.getOutputInterval().getMax() +
            config.outerRadiusFunc.getOutputInterval().getMax()
        );

        for (int x = -maxRadius; x <= maxRadius; x++){
            for (int z = -maxRadius; z <= maxRadius; z++){
                BlockPos2D currentPos = center.offset(x, z);

                double innerRadius = config.innerRadiusFunc.getOutput(currentPos);
                double outerRadius = config.outerRadiusFunc.getOutput(currentPos);

                double distanceFromCenterSq = MathHelper.getDistanceSq(BlockPos2D.INFO, center, currentPos);

                if (distanceFromCenterSq < Math.pow(innerRadius, 2)){
                    blockLayout.put(currentPos, config.innerBlocksFunc.getBlock(currentPos.to3D(liquidY), world, random));
                }
                else if (distanceFromCenterSq < Math.pow(innerRadius + outerRadius, 2)){
                    blockLayout.put(currentPos, config.outerBlocksFunc.getBlock(currentPos.to3D(liquidY), world, random));
                }
            }
        }
    }

    private int getLiquidY(){
        return iteratePoolAndEdge(0)
            .stream()
            .mapToInt(
                v -> (int)data.get(PosDataKeys.MAPPED_HEIGHT, v)
            )
            .min()
            .getAsInt();
    }

    private boolean generate(IWorld world, Random random){
        if (!config.extraConditions.passes(this, world, random)) return false;

        if (!floorIsValid(world)) return false;

        if (!ceilIsValid(world)) return false;

        if (liquidNearby(world)) return false;

        clearPlantsAbovePool(world);

        placePool(world);

        config.extraSteps.run(this, world, random);

        return true;
    }

    private boolean floorIsValid(IWorldReader world){
        for (BlockPos2D floorPos2D : iteratePoolAndEdge(0)) {
            BlockPos floorPos = floorPos2D.to3D(liquidY - 1);

            if (!MCHelper.isSolid(world.getBlockState(floorPos))) {
                return false;
            }
        }
        return true;
    }

    private boolean ceilIsValid(IWorldReader world){
        for (BlockPos2D currPos2D: iteratePoolAndEdge(0)) {
            int mappedHeight = MCHelper.getHighestTerrainHeight(currPos2D, world);

            if (mappedHeight > liquidY + clearHeight){
                continue;
            }

            BlockState blockAtSurface = world.getBlockState(currPos2D.to3D(mappedHeight));
            BlockState blockAboveSurface = world.getBlockState(currPos2D.to3D(mappedHeight + 1));

            if (blockAtSurface.getBlock().equals(Blocks.GRASS_PATH)){
                return false;
            }
            if (!blockAboveSurface.isAir() && !MCHelper.isPlant(blockAboveSurface.getBlock()) && !blockAboveSurface.getBlock().equals(Blocks.SNOW)) {
                return false;
            }
        }
        return true;
    }

    private boolean liquidNearby(IWorldReader world){
        for (int y = liquidY - 1; y <= liquidY + clearHeight + 1; y++){
            for (BlockPos2D currPos2D: iteratePoolAndEdge(1)){
                BlockPos currPos = currPos2D.to3D(y);

                if (world.getBlockState(currPos).getMaterial().isLiquid()){
                    return true;
                }
            }
        }
        return false;
    }

    private void placePool(IWorld world){
        for (Map.Entry<BlockPos2D, BlockState> entry : blockLayout.entrySet()){
            BlockPos2D pos2D = entry.getKey();
            BlockState block = entry.getValue();

            world.setBlockState(pos2D.to3D(liquidY), block, MCHelper.DEFAULT_FLAG);

            for (int y = liquidY + 1; y <= liquidY + clearHeight; y++){
                world.setBlockState(pos2D.to3D(y), Blocks.AIR.getDefaultState(), MCHelper.DEFAULT_FLAG);
            }
        }
    }

    private void clearPlantsAbovePool(IWorld world){
        for (BlockPos2D pos: iteratePoolAndEdge(0)){
            MCHelper.clearVertically(
                pos.to3D(liquidY),
                world,
                b -> MCHelper.isPlant(b.getBlock()) || b.getBlock().equals(Blocks.SNOW)
            );
        }
    }

    static class Config{
        private final FloatFunc<BlockPos2D> innerRadiusFunc;
        private final FloatFunc<BlockPos2D> outerRadiusFunc;
        private final GetBlockFunc innerBlocksFunc;
        private final GetBlockFunc outerBlocksFunc;
        private final ExtraCondition extraConditions;
        private final ExtraStep extraSteps;

        Config(
            FloatFunc<BlockPos2D> innerRadiusFunc,
            FloatFunc<BlockPos2D> outerRadiusFunc,
            GetBlockFunc innerBlocksFunc,
            GetBlockFunc outerBlocksFunc,
            ExtraCondition extraConditions,
            ExtraStep extraSteps
        ) {
            this.innerRadiusFunc = innerRadiusFunc;
            this.outerRadiusFunc = outerRadiusFunc;
            this.innerBlocksFunc = innerBlocksFunc;
            this.outerBlocksFunc = outerBlocksFunc;
            this.extraConditions = extraConditions;
            this.extraSteps = extraSteps;
        }

        @FunctionalInterface
        interface GetBlockFunc{
            BlockState getBlock(BlockPos pos, IWorldReader world, Random random);
        }

        @FunctionalInterface
        interface ExtraCondition{
            boolean passes(SurfacePool poolLayout, IWorldReader world, Random random);
        }

        @FunctionalInterface
        interface ExtraStep{
            void run(SurfacePool poolLayout, IWorld world, Random random);
        }
    }
}

