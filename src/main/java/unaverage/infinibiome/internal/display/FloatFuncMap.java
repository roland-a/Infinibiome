package weightedgpa.infinibiome.internal.display;

import weightedgpa.infinibiome.api.pos.FloatPosInfo;
import weightedgpa.infinibiome.internal.floatfunc.generators.RandomGen;
import weightedgpa.infinibiome.internal.floatfunc.generators.SimplexNoise;
import weightedgpa.infinibiome.internal.floatfunc.modifiers.Derivative;
import weightedgpa.infinibiome.internal.floatfunc.modifiers.Interpolation;
import weightedgpa.infinibiome.internal.floatfunc.util.Interval;
import weightedgpa.infinibiome.api.generators.Seed;
import weightedgpa.infinibiome.api.pos.BlockPos2D;
import weightedgpa.infinibiome.internal.floatfunc.FloatFunc;

import java.awt.Color;

public final class FloatFuncMap extends DisplayMapBase {
    private final FloatFunc<BlockPos2D> noise;

    public FloatFuncMap(){
        this(
            1,
            getNoise()
        );
    }

    private static FloatFunc<BlockPos2D> getNoise() {
        return new Derivative<>(
            new SimplexNoise<>(Seed.ROOT, 100, Pos.INFO),
            Pos.INFO
        )
        .mapInput(
            p -> new Pos(p.getBlockX(), p.getBlockZ())
        );
    }

    private FloatFuncMap(int scale, FloatFunc<BlockPos2D> noise) {
        super(scale);

        this.noise = noise;
    }

    @Override
    protected Color getColor(int posX, int posZ, int screenPixelX, int screenPixelZ) {
        float color = (float)noise.getOutputInterval().mapInterval(
            noise.getOutput(new BlockPos2D(posX, posZ)),
            Interval.PERCENT
        );

        return new Color(color, color, color);
    }

    static class Pos{
        static FloatPosInfo<Pos> INFO = new FloatPosInfo<Pos>() {
            @Override
            public Pos build(double x, double z) {
                return new Pos(x, z);
            }

            @Override
            public double getX(Pos pos) {
                return pos.x;
            }

            @Override
            public double getZ(Pos pos) {
                return pos.z;
            }
        };

        final double x;
        final double z;

        Pos(double x, double z) {
            this.x = x;
            this.z = z;
        }
    }
}