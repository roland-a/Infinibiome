package weightedgpa.infinibiome.internal.minecraftImpl.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.command.CommandSource;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentUtils;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.util.text.event.ClickEvent;
import net.minecraft.util.text.event.HoverEvent;
import weightedgpa.infinibiome.internal.misc.MathHelper;
import weightedgpa.infinibiome.api.pos.BlockPos2D;

import javax.annotation.Nullable;
import java.util.function.Consumer;
import java.util.regex.Pattern;

final class CommandHelper {
    private CommandHelper() {}

    static String fix(String name){
        name = replaceSpecialCharacters(name);

        name = removeImport(name);

        name = removeColon(name);

        name = removeHex(name);

        name = removeGen(name);

        return name;
    }

    private static final Pattern SPECIAL_CHAR = Pattern.compile("[{}=,]");

    private static String replaceSpecialCharacters(String str){
        return SPECIAL_CHAR.matcher(str).replaceAll("_");
    }

    private static final Pattern DOT = Pattern.compile("\\.");

    private static String removeImport(String str){
        String[] split = DOT.split(str, 0);

        return split[split.length-1];
    }

    private static final Pattern COLON = Pattern.compile(":");

    private static String removeColon(String str){
        String[] split = COLON.split(str, 0);

        return split[split.length-1];
    }

    private static final Pattern AT = Pattern.compile("@");

    private static String removeHex(String str){
        String[] split = AT.split(str, 0);

        return split[0];
    }

    //needs to run after removeHex
    private static final Pattern GEN = Pattern.compile("Gen$");

    private static String removeGen(String str){
        return GEN.matcher(str).replaceAll("");
    }

    static void printPos(@Nullable BlockPos2D result, String name, CommandSource source){
        if (result == null){
            source.sendFeedback(
                new TranslationTextComponent("commands.locate.failed"),
                true
            );
            return;
        }

        BlockPos2D playerPos = new BlockPos2D((int) source.getPos().x, (int) source.getPos().z);

        int dist = (int)MathHelper.getDistance(
            BlockPos2D.INFO,
            result,
            playerPos
        );

        ITextComponent itextcomponent = TextComponentUtils.wrapInSquareBrackets(new TranslationTextComponent("chat.coordinates", result.getBlockX(), "~", result.getBlockZ())).applyTextStyle((p_211746_1_) -> {
            p_211746_1_.setColor(TextFormatting.GREEN).setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/tp @s " + result.getBlockX() + " ~ " + result.getBlockZ())).setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new TranslationTextComponent("chat.coordinates.tooltip")));
        });

        source.sendFeedback(new TranslationTextComponent("commands.locate.success", name, itextcomponent, dist), true);
    }

    static <T> Command<T> wrap(Consumer<CommandContext<T>> command){
        return csc -> {
            try {
                command.accept(csc);
                return 0;
            }
            catch (Exception e){
                e.printStackTrace();
                throw e;
            }
        };
    }
}
