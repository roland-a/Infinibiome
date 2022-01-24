package weightedgpa.infinibiome.internal.minecraftImpl.commands;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.mojang.brigadier.tree.LiteralCommandNode;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.command.ISuggestionProvider;
import net.minecraft.util.math.Vec3d;
import weightedgpa.infinibiome.api.dependency.DependencyInjector;
import weightedgpa.infinibiome.api.generators.nonworldgen.Locatable;
import weightedgpa.infinibiome.api.pos.BlockPos2D;
import weightedgpa.infinibiome.internal.misc.Helper;

import java.util.ConcurrentModificationException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;


public final class LocateObjectCommand {
    private LocateObjectCommand() {}

    private static final Map<String, Locatable> locatables = new HashMap<>();

    public static void refresh(DependencyInjector di){
        locatables.clear();

        for (Locatable l: di.getAll(Locatable.class)){

            locatables.put(
                CommandHelper.fix(l.toString()),
                l
            );
        }
    }



    static LiteralCommandNode<CommandSource> init(){
        return Commands.literal("locate").then(
            Commands.argument("name", new LocatableArguments()).executes(
                CommandHelper.wrap(LocateObjectCommand::runCommand)
            )
        )
        .build();
    }

    private static void runCommand(CommandContext<CommandSource> csc){
        new Thread(
            () -> {
                Vec3d pos = csc.getSource().getPos();

                Locatable locatable = csc.getArgument("name", Locatable.class);

                BlockPos2D result = Helper.timed(
                    60,
                    () -> locatable.getClosestInstance(new BlockPos2D((int)pos.x, (int) pos.z))
                );

                CommandHelper.printPos(
                    result,
                    CommandHelper.fix(locatable.toString()),
                    csc.getSource()
                );
            }
        )
        .start();
    }

    private static class LocatableArguments implements ArgumentType<Locatable> {
        @Override
        public Locatable parse(StringReader reader) throws CommandSyntaxException {
            return locatables.get(reader.readUnquotedString());
        }

        @Override
        public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
            try {
                return ISuggestionProvider.suggest(locatables.keySet(), builder);
            }
            catch (ConcurrentModificationException e){
                return new CompletableFuture<>();
            }
        }
    }
}

