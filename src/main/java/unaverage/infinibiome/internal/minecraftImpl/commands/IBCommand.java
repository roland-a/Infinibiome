package weightedgpa.infinibiome.internal.minecraftImpl.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.command.CommandSource;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.event.server.FMLServerStartingEvent;

public final class IBCommand {
    private IBCommand(){}

    @SubscribeEvent
    public static void onServerStarting(FMLServerStartingEvent e){
        //System.out.println("registering commands");

        e.getCommandDispatcher().register(initCommand());
    }

    private static LiteralArgumentBuilder<CommandSource> initCommand(){
        return LiteralArgumentBuilder.<CommandSource>literal("ib")
            .requires(r -> r.hasPermissionLevel(2))
            .then(
                DebugCommand.init()
            )
            .then(
                LocateObjectCommand.init()
            )
            .then(
                LocateClimateCommand.init()
            );
    }
}
