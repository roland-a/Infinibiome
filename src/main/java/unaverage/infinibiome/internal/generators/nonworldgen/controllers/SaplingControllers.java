package weightedgpa.infinibiome.internal.generators.nonworldgen.controllers;

import net.minecraft.block.Block;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorldReader;
import net.minecraftforge.event.world.SaplingGrowTreeEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import weightedgpa.infinibiome.api.dependency.DependencyInjector;
import weightedgpa.infinibiome.api.generators.nonworldgen.PlantGrowthController;
import weightedgpa.infinibiome.api.generators.nonworldgen.SaplingController;
import weightedgpa.infinibiome.internal.generators.interchunks.tree.NoPlantWrapper;
import weightedgpa.infinibiome.internal.minecraftImpl.IBWorldType;
import weightedgpa.infinibiome.internal.minecraftImpl.world.ClientUpdatingWorld;

import java.util.List;
import java.util.Random;

import static weightedgpa.infinibiome.api.generators.nonworldgen.PlantGrowthController.*;
import static weightedgpa.infinibiome.api.generators.nonworldgen.PlantGrowthController.getPlant;

public final class SaplingControllers {
    private SaplingControllers(){}

    private static List<SaplingController> saplingControllers = null;

    public static void refresh(DependencyInjector di){
        saplingControllers = di.getAll(SaplingController.class);
    }

    @SuppressWarnings("unused")
    @SubscribeEvent
    //always run on serverside
    public static void onSaplingGrowEvent(SaplingGrowTreeEvent e){
        if (!(e.getWorld().getWorld().getWorldType() instanceof IBWorldType)) return;

        Random random = new Random();

        PlantGrowthController.Result<SaplingController> result = getPlant(e.getPos(), e.getWorld(), random, saplingControllers);

        //System.out.println(result);

        if (result instanceof Result.NOT_CONTROLLED) {
            return;
        }

        if (result instanceof Result.INVALID_GROWTH){
            e.setResult(Event.Result.DENY);
            return;
        }

        SaplingController controller = ((Result.OK<SaplingController>)result).controller;

        //System.out.println(controller + " " + controller.getPlantControllerWeight(MCHelper.to2D(e.getPos())));

        if (!controller.canGrowWithTick(e.getPos(), e.getWorld(), random)){
            //System.out.println("no growth");
            e.setResult(Event.Result.DENY);
            return;
        }

        //BlockEvent.CropGrowEvent isnt called on saplings for some reason
        controller.growFromSapling(
            fix2x2Pos(e.getPos(), e.getWorld()),
            e.getWorld(),
            random
        );

        //System.out.println("growth");
        e.setResult(Event.Result.DENY);
    }

    private static BlockPos fix2x2Pos(BlockPos pos, IWorldReader world){
        Block originalSapling = world.getBlockState(pos).getBlock();

        BlockPos[] posList = {
            pos.offset(Direction.NORTH).offset(Direction.WEST),
            pos.offset(Direction.NORTH),
            pos.offset(Direction.WEST)
        };

        for (BlockPos checkPos: posList){
            if (world.getBlockState(checkPos).getBlock().equals(originalSapling)){
                return checkPos;
            }
        }
        return pos;
    }

}
