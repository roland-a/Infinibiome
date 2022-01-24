package weightedgpa.infinibiome.internal.generators.nonworldgen.controllers;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.player.BonemealEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import weightedgpa.infinibiome.api.dependency.DependencyInjector;
import weightedgpa.infinibiome.api.generators.nonworldgen.GroundBoneMealController;
import weightedgpa.infinibiome.internal.minecraftImpl.IBWorldType;
import weightedgpa.infinibiome.internal.minecraftImpl.world.ChangeDetectingWorld;
import weightedgpa.infinibiome.internal.minecraftImpl.world.ChangeHoldingWorld;
import weightedgpa.infinibiome.internal.minecraftImpl.world.ClientUpdatingWorld;
import weightedgpa.infinibiome.internal.misc.Helper;
import weightedgpa.infinibiome.internal.misc.MCHelper;
import weightedgpa.infinibiome.internal.misc.MathHelper;

import java.util.*;

public final class GroundBoneMealControllers {
    private GroundBoneMealControllers(){}

    private static List<GroundBoneMealController> groundBoneMealControllers = null;

    public static void refresh(DependencyInjector di){
        groundBoneMealControllers = di.getAll(GroundBoneMealController.class);
    }

    @SuppressWarnings("unused")
    @SubscribeEvent
    public static void onGroundBoneMeal(BonemealEvent e){
        if (!(e.getWorld().getWorldType() instanceof IBWorldType)) return;

        if (!isValidGroundToBonemeal(e.getPos(), e.getWorld())) return;

        if (e.getWorld().isRemote()){
            e.setResult(Event.Result.ALLOW);
            return;
        }

        Random random = new Random();

        if (!somethingGeneratesHere(e.getPos().up(), e.getWorld())){
            e.setCanceled(true);
            return;
        }

        if (!MathHelper.randomBool(1/3d, random)){
            e.setResult(Event.Result.ALLOW);
            return;
        }

        for (GroundBoneMealController g: Helper.shuffle(groundBoneMealControllers, random)){
            double chance = g.getGroundBonemealChance(MCHelper.to2D(e.getPos()));

            if (chance > 1) chance = 1;

            if (MathHelper.randomBool(chance, random)){
                continue;
            }

            ChangeDetectingWorld wrapper = new ChangeDetectingWorld(e.getWorld());

            g.spawnFromGroundBoneMeal(e.getPos().up(), wrapper, random);

            if (wrapper.anyChange()) {
                break;
            }
        }

        e.setResult(Event.Result.ALLOW);
    }

    private static boolean somethingGeneratesHere(BlockPos pos, World world){
        ChangeHoldingWorld wrapper = new ChangeHoldingWorld(world);

        Random random = new Random();

        for (GroundBoneMealController g: groundBoneMealControllers){
            g.spawnFromGroundBoneMeal(pos, wrapper, random);

            if (wrapper.anyChange()) return true;
        }
        return false;
    }
    
    private static boolean isValidGroundToBonemeal(BlockPos pos, World world){
        if (!MCHelper.isSolid(world.getBlockState(pos))) return false;

        if (MCHelper.isSolid(world.getBlockState(pos.up()))) return false;
        
        return true;
    }
}
