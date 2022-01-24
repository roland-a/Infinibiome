package weightedgpa.infinibiome.api.generators.nonworldgen;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorld;
import weightedgpa.infinibiome.api.dependency.MultiDep;
import weightedgpa.infinibiome.api.pos.BlockPos2D;
import weightedgpa.infinibiome.internal.misc.Helper;
import weightedgpa.infinibiome.internal.misc.MCHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Used for controlling plant growth
 */
public interface PlantGrowthController extends MultiDep {
    boolean controlsPlant(BlockState block, boolean is2x2);

    default boolean controlsPlant(BlockPos pos, IWorld world){
        return controlsPlant(
            world.getBlockState(pos),
            is2x2(pos, world)
        );
    }
    
    boolean isValidGrowth(BlockPos pos, IWorld world);
    
    boolean canGrowWithBonemeal(BlockPos pos, IWorld world, Random random);

    default boolean canGrowWithTick(BlockPos pos, IWorld world, Random random){
        return canGrowWithBonemeal(pos, world, random);
    }

    default double getPlantControllerWeight(BlockPos2D pos){
        return 1;
    }

    static boolean is2x2(BlockPos pos, IWorld world){
        Block saplingBlock = world.getBlockState(pos).getBlock();

        for (Direction d: MCHelper.NSWE){
            //if any of these fail, try looking in the other direction
            if (!world.getBlockState(pos.offset(d)).getBlock().equals(saplingBlock)) continue;
            if (!world.getBlockState(pos.offset(d.rotateY())).getBlock().equals(saplingBlock)) continue;
            if (!world.getBlockState(pos.offset(d).offset(d.rotateY())).getBlock().equals(saplingBlock)) continue;

            return true;
        }
        return false;
    }

    static <T extends PlantGrowthController> Result<T> getPlant(BlockPos pos, IWorld world, Random random, List<T> plantGrowthControllers){
        plantGrowthControllers = new ArrayList<>(plantGrowthControllers);

        plantGrowthControllers.removeIf(
            p -> !p.controlsPlant(pos, world)
        );

        if (plantGrowthControllers.isEmpty()) return new Result.NOT_CONTROLLED<>();

        plantGrowthControllers.removeIf(
            p -> !p.isValidGrowth(pos, world)
        );

        if (plantGrowthControllers.isEmpty()) return new Result.INVALID_GROWTH<>();

        return new Result.OK<>(
            Helper.pickWeighted(
                pl -> pl.getPlantControllerWeight(MCHelper.to2D(pos)),
                random,
                plantGrowthControllers
            )
        );
    }

    abstract class Result<T>{
        public static class OK<T> extends Result<T>{
            public final T controller;

            OK(T controller) {
                this.controller = controller;
            }

            @Override
            public String toString() {
                return "OK{" +
                    "controller=" + controller +
                    '}';
            }
        }

        public static class NOT_CONTROLLED<T> extends Result<T>{}

        public static class INVALID_GROWTH<T> extends Result<T>{}
    }

}
