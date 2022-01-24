package weightedgpa.infinibiome.internal.minecraftImpl.commands;

import com.mojang.brigadier.ImmutableStringReader;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import weightedgpa.infinibiome.internal.floatfunc.util.Interval;
import weightedgpa.infinibiome.api.dependency.DependencyInjector;
import weightedgpa.infinibiome.api.pos.BlockPos2D;
import weightedgpa.infinibiome.api.pos.InterChunkPos;
import weightedgpa.infinibiome.api.posdata.PosDataHelper;
import weightedgpa.infinibiome.api.posdata.PosDataKeys;
import weightedgpa.infinibiome.api.posdata.PosDataProvider;
import weightedgpa.infinibiome.internal.generators.interchunks.tree.TreeGens;
import weightedgpa.infinibiome.internal.generators.utils.PredicateSearcher;
import weightedgpa.infinibiome.internal.misc.Helper;

import java.util.function.Supplier;

public final class LocateClimateCommand {
    private LocateClimateCommand() {}

    private static PosDataProvider data = null;
    private static TreeGens treeGens = null;

    public static void refresh(DependencyInjector di){
        data = di.get(PosDataProvider.class);
        treeGens = di.get(TreeGens.class);
    }

    static LiteralArgumentBuilder<CommandSource> init(){
        return Commands.literal("locateClimate").then(
            Commands.argument("temperature", new FloatOrAnyArguments(Interval.PERCENT)).then(
                Commands.argument("humidity", new FloatOrAnyArguments(Interval.PERCENT)).then(
                    Commands.argument("amplitude", new FloatOrAnyArguments(new Interval(0, 200))).then(
                        Commands.argument("treeDensity", new FloatOrAnyArguments(Interval.PERCENT)).executes(
                            CommandHelper.wrap(LocateClimateCommand::runCommand)
                        )
                    )
                )
            )
        );
    }

    private static void runCommand(CommandContext<CommandSource> csc){
        new Thread(
            () -> {
                BlockPos2D result = Helper.timed(
                    30,
                    () -> locateClimate(
                        new BlockPos2D((int)csc.getSource().getPos().x, (int)csc.getSource().getPos().z),
                        csc.getArgument("temperature", Interval.class),
                        csc.getArgument("humidity", Interval.class),
                        csc.getArgument("amplitude", Interval.class),
                        csc.getArgument("treeDensity", Interval.class)
                    )
                );

                CommandHelper.printPos(result, "climate", csc.getSource());
            }
        )
        .start();
    }

    private static BlockPos2D locateClimate(
        BlockPos2D centerPos,
        Interval temperature,
        Interval humidity,
        Interval amplitude,
        Interval treeDensity
    ){
        return new PredicateSearcher<>(
            32,
            pos -> {
                if (!data.get(PosDataKeys.LANDMASS_TYPE, pos).isLand()) return false;

                if (data.get(PosDataKeys.IS_MUSHROOM_ISLAND, pos)) return false;

                if (!isCloseEnough(temperature, () -> PosDataHelper.getTemperature(pos, data))) return false;

                if (!isCloseEnough(humidity, () -> PosDataHelper.getHumidity(pos, data))) return false;

                if (!isCloseEnough(amplitude, () -> data.get(PosDataKeys.AMP, pos))) return false;

                return isCloseEnough(treeDensity, () -> getTreeDensity(pos));
            },
            BlockPos2D.INFO
        )
        .getClosestPoint(
            centerPos
        );
    }

    private static double getTreeDensity(BlockPos2D pos){
        double humidity = PosDataHelper.getHumidity(pos, data);

        if (PosDataHelper.DRY_INTERVAL.contains(humidity)) return 0;

        return treeGens.getApproxDensity(new InterChunkPos(pos));
    }

    private static boolean isCloseEnough(Interval wanted, Supplier<Double> actualFunc){
        if (wanted.equals(Interval.ALL_VALUES)) return true;

        return wanted.contains(actualFunc.get());
    }

    private static final class FloatOrAnyArguments implements ArgumentType<Interval>{
        private final Interval acceptable;

        FloatOrAnyArguments(Interval acceptable) {
            this.acceptable = acceptable;
        }

        @Override
        public Interval parse(StringReader reader) throws CommandSyntaxException {
            String str = reader.readUnquotedString();

            if (!str.contains("_")){
                throw CommandSyntaxException.BUILT_IN_EXCEPTIONS.readerInvalidFloat().createWithContext(reader, str);
            }
            
            String[] args = str.split("_", -1);

            return new Interval(parse(args[0], Double.NEGATIVE_INFINITY, reader), parse(args[1], Double.POSITIVE_INFINITY, reader));
        }

        private double parse(String str, double defaultValue, ImmutableStringReader reader) throws CommandSyntaxException{
            if (str.isEmpty()) return defaultValue;

            return parse(str, reader);
        }

        private double parse(String str, ImmutableStringReader reader) throws CommandSyntaxException{
            double result;

            try {
                result = Double.parseDouble(str);
            }
            catch (NumberFormatException e){
                throw CommandSyntaxException.BUILT_IN_EXCEPTIONS.readerInvalidFloat().createWithContext(reader, str);
            }

            if (result < acceptable.getMin()){
                throw CommandSyntaxException.BUILT_IN_EXCEPTIONS.floatTooLow().createWithContext(reader, result, acceptable.getMin());
            }
            if (result > acceptable.getMax()){
                throw CommandSyntaxException.BUILT_IN_EXCEPTIONS.floatTooHigh().createWithContext(reader, result, acceptable.getMax());
            }
            return result;
        }
    }
    

}
