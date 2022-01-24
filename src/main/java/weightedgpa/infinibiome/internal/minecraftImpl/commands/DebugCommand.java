package weightedgpa.infinibiome.internal.minecraftImpl.commands;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import it.unimi.dsi.fastutil.Hash;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.command.ISuggestionProvider;
import net.minecraft.util.text.StringTextComponent;
import org.jetbrains.annotations.Nullable;
import weightedgpa.infinibiome.api.pos.BlockPos2D;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

public final class DebugCommand {
    private DebugCommand(){}

    private static final Map<String, Map<String, Function<BlockPos2D, Object>>> debugs = new HashMap<>();

    public static void registerDebugFunc(String group, String name, Function<BlockPos2D, Object> display){
        debugs.computeIfAbsent(
            CommandHelper.fix(group),
            __ -> new LinkedHashMap<>()
        )
        .put(
            CommandHelper.fix(name),
            display
        );
    }

    static LiteralArgumentBuilder<CommandSource> init(){
        return Commands.literal("debug").then(
            Commands.argument("group", new GroupArg()).executes(
                CommandHelper.wrap(DebugCommand::runCommand)
            )
        );
    }

    private static void runCommand(CommandContext<CommandSource> csc) {
        Map<String, Function<BlockPos2D, Object>> map = csc.getArgument("group", Map.class);

        BlockPos2D pos = new BlockPos2D(
            (int)csc.getSource().getPos().x,
            (int)csc.getSource().getPos().z
        );

        String group = "";

        for (String currGroup: debugs.keySet()){
            if (debugs.get(currGroup).equals(map)){
                group = currGroup;
            }
        }

        csc.getSource()
            .sendFeedback(
                new StringTextComponent(
                    String.format(
                        "[%s]",
                        group
                    )
                ),
                true
            );

        for (Map.Entry<String, Function<BlockPos2D, Object>> entry : map.entrySet()) {
            csc.getSource()
                .sendFeedback(
                    new StringTextComponent(
                        String.format(
                            "%s: %s",
                            entry.getKey(),
                            entry.getValue().apply(pos)
                        )
                    ),
                    true
                );
        }
    }

    static class GroupArg implements ArgumentType<Map<String, Function<BlockPos2D,Object>>>{

        @Override
        public Map<String, Function<BlockPos2D, Object>> parse(StringReader reader) throws CommandSyntaxException {
            return debugs.getOrDefault(
                reader.readUnquotedString(),
                new HashMap<>()
            );
        }

        @Override
        public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
            return ISuggestionProvider.suggest(debugs.keySet(), builder);
        }
    }
}
