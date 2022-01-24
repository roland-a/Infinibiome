package weightedgpa.infinibiome.internal.generators.interchunks;

import net.minecraft.block.BeehiveBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.passive.BeeEntity;
import net.minecraft.tags.BlockTags;
import net.minecraft.tileentity.BeehiveTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorld;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.IPlantable;
import net.minecraftforge.common.Tags;
import weightedgpa.infinibiome.api.Infinibiome;
import weightedgpa.infinibiome.api.dependency.DependencyInjector;
import weightedgpa.infinibiome.api.generators.InterChunkGen;
import weightedgpa.infinibiome.api.generators.InterChunkGenTimings;
import weightedgpa.infinibiome.api.generators.Timing;
import weightedgpa.infinibiome.api.posdata.PosDataHelper;
import weightedgpa.infinibiome.internal.generators.utils.GeneratorBase;
import weightedgpa.infinibiome.internal.misc.*;
import weightedgpa.infinibiome.api.pos.BlockPos2D;
import weightedgpa.infinibiome.api.pos.InterChunkPos;
import weightedgpa.infinibiome.internal.minecraftImpl.commands.DebugCommand;
import weightedgpa.infinibiome.internal.minecraftImpl.world.SimulatedWorld;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public final class BeehiveGen extends GeneratorBase implements InterChunkGen {
    public BeehiveGen(DependencyInjector di) {
        super(di, Infinibiome.MOD_ID + ":beeHive");

        DebugCommand.registerDebugFunc(
            "flower",
            "density",
            p -> {
                double result = 0;

                for (Block flower: BlockTags.FLOWERS.getAllElements()){
                    result += MCHelper.getPlantDensity(
                        new InterChunkPos(p),
                        di.get(ServerWorld.class),
                        (IPlantable)flower,
                        () -> 0d
                    );
                }

                return String.valueOf(result);
            }
        );
    }



    @Override
    public Timing getInterChunkTiming() {
        return InterChunkGenTimings.BEEHIVE;
    }

    @Override
    public void generate(InterChunkPos interChunkPos, IWorld interChunks) {
        Random random = randomGen.getRandom(interChunkPos.getX(), interChunkPos.getZ());

        List<HiveInfo> validPos = getValidHivePos(interChunkPos, interChunks, random);

        int count = Math.min(
            validPos.size(),
            getCount(interChunkPos, interChunks, random)
        );

        for (int i = 0; i < count; i++){
            generateHive(validPos.get(i), interChunks, random);
        }
    }

    private int getCount(InterChunkPos interChunkPos, IWorld world, Random random) {
        double avgCount = 0;

        for (Block flower: BlockTags.FLOWERS.getAllElements()){
            avgCount += MCHelper.getPlantDensity(
                interChunkPos,
                world,
                (IPlantable) flower,
                () -> 0d
            );
        }

        avgCount *= 1/8d;

        return MathHelper.randomRound(avgCount, random);
    }

    private boolean isTooColdOrDry(BlockPos2D pos){
        double temp = PosDataHelper.getTemperature(pos, posData);

        if (PosDataHelper.FREEZE_INTERVAL.contains(temp)) return true;

        double humd = PosDataHelper.getHumidity(pos, posData);

        if (PosDataHelper.DRY_INTERVAL.contains(humd)) return true;

        return false;
    }

    private List<HiveInfo> getValidHivePos(InterChunkPos interChunkPos, IWorld world, Random random) {
        List<BeehiveGen.HiveInfo> result = new ArrayList<>();

        result.addAll(
            new TreeType().getAllValidHivePos(interChunkPos, world, random)
        );

        result.addAll(
            new GroundType().getAllValidHivePos(interChunkPos, world, random)
        );

        result.removeIf(
            p -> PosDataHelper.FREEZE_INTERVAL.contains(PosDataHelper.getTemperature(p.pos, posData))
        );

        return result;
    }

    private void generateHive(HiveInfo hiveInfo, IWorld world, Random random) {
        BlockPos hivePos = hiveInfo.pos;
        Direction hiveDirection = hiveInfo.direction;

        placeHiveBlock(hivePos, hiveDirection, world);
        placeBeeEntity(hivePos, world, random);

        hiveInfo.extraStepFunc.run(hivePos, hiveDirection, world, random);
    }

    private void placeHiveBlock(BlockPos hivePos, Direction hiveDirection, IWorld world){
        world.setBlockState(
            hivePos,
            getHiveBlock(hiveDirection),
            MCHelper.DEFAULT_FLAG
        );
    }

    private void placeBeeEntity(BlockPos hivePos, IWorld world, Random random){
        TileEntity tileEntity = world.getTileEntity(hivePos);

        if (tileEntity instanceof BeehiveTileEntity) {
            BeehiveTileEntity beehivetileentity = (BeehiveTileEntity)tileEntity;
            int j = 2 + random.nextInt(2);

            for(int k = 0; k < j; ++k) {
                BeeEntity beeentity = new BeeEntity(EntityType.BEE, world.getWorld());
                beehivetileentity.tryEnterHive(beeentity, false, random.nextInt(599));
            }
        }
    }

    private BlockState getHiveBlock(Direction direction){
        return Blocks.BEE_NEST.getDefaultState()
            .with(
                BeehiveBlock.FACING,
                direction
            )
            .with(
                BeehiveBlock.HONEY_LEVEL,
                5
            );
    }

    private class TreeType {
        private List<BeehiveGen.HiveInfo> getAllValidHivePos(InterChunkPos interChunkPos, IWorld world, Random random){
            List<BeehiveGen.HiveInfo> result = new ArrayList<>();

            interChunkPos.forEachCenterPos(p -> {
                if (isTooColdOrDry(p)) return;

                if (!insideTree(p, world)) return;

                Direction hiveDirection = getBeeHiveDirection(random);

                BlockPos2D hivePos2D = p.offset(hiveDirection);

                Integer hiveHeight = getHiveHeight(hivePos2D, world);

                if (hiveHeight == null) return;

                BlockPos hivePos = hivePos2D.to3D(hiveHeight);

                result.add(
                    new HiveInfo(
                        hivePos,
                        hiveDirection,
                        HiveInfo.ExtraStepFunc.DO_NOTHING
                    )
                );
            });

            Collections.shuffle(result);

            return result;
        }

        boolean insideTree(BlockPos2D pos, IWorldReader world){
            int trunkHeight = MCHelper.getHighestTerrainHeight(pos, world) + 1;

            BlockPos trunkPos = pos.to3D(trunkHeight);

            BlockState trunkBlock = world.getBlockState(trunkPos);

            return trunkBlock.isIn(BlockTags.LOGS);
        }

        Direction getBeeHiveDirection(Random random){
            int randomIndex = random.nextInt(MCHelper.NSWE.size());

            return MCHelper.NSWE.get(randomIndex);
        }

        @Nullable
        Integer getHiveHeight(BlockPos2D hivePos, IWorld world){
            int groundHeight = MCHelper.getHighestTerrainHeight(hivePos, world);

            for (int y = groundHeight; y < groundHeight + 10; y++){
                BlockState scannedBlock = world.getBlockState(hivePos.to3D(y));

                if (scannedBlock.isIn(BlockTags.LEAVES) || scannedBlock.isIn(BlockTags.LOGS)){
                    int hiveHeight = y - 1;

                    if (hiveHeight - groundHeight < 2) return null;

                    return hiveHeight;
                }
            }
            return null;
        }
    }
    
    private class GroundType {
        private List<BeehiveGen.HiveInfo> getAllValidHivePos(InterChunkPos interChunkPos, IWorld world, Random random){
            List<BeehiveGen.HiveInfo> result = new ArrayList<>();

            interChunkPos.forEachCenterPos(p -> {
                if (isTooColdOrDry(p)) return;

                int centerHeight = MCHelper.getHighestTerrainHeight(p, world);

                //not valid if its underwater
                if (centerHeight < MCHelper.WATER_HEIGHT) return;

                Direction tunnelDirection = getCornerDirection(p, world, random);

                if (tunnelDirection == null) return;

                if (!isValidTunnel(p.to3D(centerHeight), tunnelDirection, world)) return;

                result.add(
                    new HiveInfo(
                        p.to3D(centerHeight).offset(tunnelDirection, 2),
                        tunnelDirection.getOpposite(),
                        this::extraStep
                    )
                );
            });

            Collections.shuffle(result);

            return result;
        }

        void extraStep(BlockPos hivePos, Direction hiveDirection, IWorld world, Random random) {
            //System.out.println(hivePos);

            world.removeBlock(hivePos.offset(hiveDirection), false);
            world.removeBlock(hivePos.offset(hiveDirection, 2), false);
            MCHelper.clearVertically(hivePos, world, b -> MCHelper.isSolid(b) || b.getBlock().equals(Blocks.SNOW));
        }

        @Nullable
        Direction getCornerDirection(BlockPos2D centerPos, IWorld world, Random random){
            List<Direction> shuffledDirection = new ArrayList<>(MCHelper.NSWE);

            //prevents beehives from always picking one direction over the other
            Collections.shuffle(shuffledDirection, random);

            for (Direction d: shuffledDirection){
                if (isCorner(centerPos, d, world)){
                    return d;
                }
            }
            return null;
        }

        boolean isCorner(BlockPos2D centerPos, Direction direction, IWorld world){
            int center = MCHelper.getHighestTerrainHeight(centerPos, world);
            int corner1 = MCHelper.getHighestTerrainHeight(centerPos.offset(direction), world);
            int corner2 = MCHelper.getHighestTerrainHeight(centerPos.offset(direction.rotateY()), world);

            int oppositeCorner1 = MCHelper.getHighestTerrainHeight(centerPos.offset(direction.getOpposite()), world);
            int oppositeCorner2 = MCHelper.getHighestTerrainHeight(centerPos.offset(direction.rotateY().getOpposite()), world);

            if (corner1 != center + 1) return false;

            if (corner2 != center + 1) return false;

            if (oppositeCorner1 != center) return false;

            if (oppositeCorner2 != center) return false;

            return true;
        }

        boolean isValidTunnel(BlockPos centerPos, Direction tunnelDirection, IWorld world){
            for (int i = 0; i <= 2; i++){
                BlockPos tunnelPos = centerPos.offset(tunnelDirection, i);

                if (!isValidSolid(tunnelPos, tunnelDirection.rotateY(), world)) return false;

                if (!isValidSolid(tunnelPos, tunnelDirection.rotateY().getOpposite(), world)) return false;

                if (i == 0){
                    if (!MCHelper.isMostlyAir(getBlock(centerPos, Direction.UP, world))) return false;
                }
                else {
                    if (!isValidSolid(tunnelPos, Direction.UP, world)) return false;
                }
            }
            return true;
        }


        boolean isValidSolid(BlockPos centerPos, Direction direction, IWorld world){
            return isValidSolid(getBlock(centerPos, direction, world));
        }

        boolean isValidSolid(BlockState block){
            if (!MCHelper.isSolid(block)) return false;

            if (Tags.Blocks.SAND.contains(block.getBlock())) return false;

            if (Tags.Blocks.GRAVEL.contains(block.getBlock())) return false;

            if (Tags.Blocks.STONE.contains(block.getBlock())) return false;

            if (Tags.Blocks.SANDSTONE.contains(block.getBlock())) return false;

            return true;
        }

        BlockState getBlock(BlockPos pos, Direction d, IWorld world){
            return world.getBlockState(pos.offset(d));
        }
    }

    private static class HiveInfo{
        final BlockPos pos;
        final Direction direction;
        final ExtraStepFunc extraStepFunc;

        HiveInfo(BlockPos pos, Direction direction, ExtraStepFunc extraStepFunc) {
            this.pos = pos;
            this.direction = direction;
            this.extraStepFunc = extraStepFunc;
        }

        @FunctionalInterface
        interface ExtraStepFunc {
            ExtraStepFunc DO_NOTHING = (pos, direction, world, random) -> {};

            void run(BlockPos pos, Direction direction, IWorld world, Random random);
        }

        @Override
        public String toString() {
            return "HiveInfo{" +
                "pos=" + pos +
                ", direction=" + direction +
                '}';
        }
    }
}
