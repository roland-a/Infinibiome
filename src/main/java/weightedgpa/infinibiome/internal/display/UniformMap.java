package weightedgpa.infinibiome.internal.display;

import weightedgpa.infinibiome.api.posdata.PosDataHelper;
import weightedgpa.infinibiome.internal.floatfunc.FloatFunc;
import weightedgpa.infinibiome.internal.floatfunc.generators.CubicNoise;
import weightedgpa.infinibiome.internal.floatfunc.util.Interval;
import weightedgpa.infinibiome.internal.floatfunc.util.PercentileTable;
import weightedgpa.infinibiome.api.generators.Seed;
import weightedgpa.infinibiome.api.pos.BlockPos2D;
import weightedgpa.infinibiome.internal.misc.Helper;
import weightedgpa.infinibiome.internal.misc.MathHelper;

import java.applet.Applet;
import java.awt.*;
import java.util.function.Function;

public final class UniformMap extends Applet {
    private static final int LENGTH = 800;

    private final Function<Double, Color> func;
    private final float[] values;
    private final PercentileTable percentileTable;
    private final int scale;

    public UniformMap(){
        this(
            initNoise(),
            UniformMap::getColor,
            1,
            1024
        );
    }

    private UniformMap(FloatFunc<BlockPos2D> noise, Function<Double, Color> func, int scale, int fineness) {
        this.func = func;
        this.scale = scale;
        this.values = new float[LENGTH * LENGTH];

        Helper.iterXZParallel(
            LENGTH,
            LENGTH,
            (x, z) -> values[x * LENGTH + z] = (float)noise.getOutput(
                new BlockPos2D(x * scale, z * scale)
            )
        );

        this.percentileTable = PercentileTable.generate(values, fineness);
    }

    private static FloatFunc<BlockPos2D> initNoise(){
        CubicNoise<BlockPos2D> n1 = new CubicNoise<>(Seed.ROOT, 50, BlockPos2D.INFO);

        FloatFunc<BlockPos2D> n2 = new CubicNoise<>(Seed.ROOT.newSeed("2"), 50, BlockPos2D.INFO)
            .mapInterval(new Interval(0, 1));

        FloatFunc<BlockPos2D> n3 = new CubicNoise<>(Seed.ROOT.newSeed("3"), 50, BlockPos2D.INFO)
            .mapInterval(new Interval(0, 1));

        return input -> {
            return MathHelper.fractal(
                i -> n1.getOutput(input, i),
                n1.getOutputInterval(),
                5,
                .5,
                .5
            );
        };
    }

    private static Color getColor(double percent){
        if (PosDataHelper.FREEZE_INTERVAL.contains(percent)){
            return new Color(255,255,255);
        }
        if (PosDataHelper.COLD_INTERVAL.contains(percent)){
            return new Color(70, 100, 100);
        }
        if (PosDataHelper.WARM_INTERVAL.contains(percent)){
            return new Color(0, 150, 0);
        }
        return new Color(0, 70, 0);
    }

    @Override
    public void paint(Graphics graphics) {
        for (int x = 0; x < LENGTH; x++){
            for (int z = 0; z < LENGTH; z++){
                double value;

                value = values[x * LENGTH + z];

                value = percentileTable.rawValueToPercentile(value);

                graphics.setColor(func.apply(value));

                graphics.fillRect(
                    x, z, 1, 1
                );
            }
        }
    }
}
