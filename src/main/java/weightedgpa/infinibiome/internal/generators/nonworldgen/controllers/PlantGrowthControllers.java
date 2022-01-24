package weightedgpa.infinibiome.internal.generators.nonworldgen.controllers;

import net.minecraft.block.*;
import net.minecraftforge.event.entity.player.BonemealEvent;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import weightedgpa.infinibiome.api.dependency.DependencyInjector;
import weightedgpa.infinibiome.api.generators.nonworldgen.PlantGrowthController;
import weightedgpa.infinibiome.internal.minecraftImpl.IBWorldType;

import java.util.*;

import static weightedgpa.infinibiome.api.generators.nonworldgen.PlantGrowthController.*;

public final class PlantGrowthControllers {
    private static List<PlantGrowthController> plantGrowthControllers = null;

    public static void refresh(DependencyInjector di){
        plantGrowthControllers = di.getAll(PlantGrowthController.class);
    }

    @SubscribeEvent
    public static void onTickGrowth(BlockEvent.CropGrowEvent.Pre e){
        if (!(e.getWorld().getWorld().getWorldType() instanceof IBWorldType)) return;

        Random random = new Random();

        Result<PlantGrowthController> result = getPlant(e.getPos(), e.getWorld(), random, plantGrowthControllers);

        if (result instanceof Result.NOT_CONTROLLED) {
            //System.out.println(e.getState() + " " + result);
            return;
        }

        if (result instanceof Result.INVALID_GROWTH){
            //System.out.println(e.getState() + " " + result);
            e.setResult(Event.Result.DENY);
            return;
        }

        PlantGrowthController controller = ((Result.OK<PlantGrowthController>) result).controller;

        if (!controller.canGrowWithTick(e.getPos(), e.getWorld(), random)){
            //System.out.println(e.getState() + " " + result + " dont grow");
            e.setResult(Event.Result.DENY);
            return;
        }

        //System.out.println(e.getState() + " " + result + " grow");
        e.setResult(Event.Result.DEFAULT);
    }

    @SubscribeEvent
    public static void onBoneMeal(BonemealEvent e){
        if (!(e.getWorld().getWorldType() instanceof IBWorldType)) return;

        if (!(e.getBlock().getBlock() instanceof IGrowable)) return;

        if (e.getWorld().isRemote()){
            e.setResult(Event.Result.ALLOW);
            return;
        }

        Random random = new Random();

        Result<PlantGrowthController> result = getPlant(e.getPos(), e.getWorld(), random, plantGrowthControllers);

        //System.out.println(result);

        if (result instanceof Result.NOT_CONTROLLED) {
            return;
        }

        if (result instanceof Result.INVALID_GROWTH){
            e.setCanceled(true);
            return;
        }

        PlantGrowthController controller = ((Result.OK<PlantGrowthController>) result).controller;

        //System.out.println(controller.getPlantControllerWeight(MCHelper.to2D(e.getPos())));

        if (!controller.canGrowWithBonemeal(e.getPos(), e.getWorld(), random)){
            //System.out.println("dont grow");
            e.setResult(Event.Result.ALLOW);
            return;
        }
        //System.out.println("grow");
        e.setResult(Event.Result.DEFAULT);
    }

}
