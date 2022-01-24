package weightedgpa.infinibiome.internal.floatfunc.generators;

import weightedgpa.infinibiome.api.generators.Seed;
import weightedgpa.infinibiome.api.pos.PosInfo;
import weightedgpa.infinibiome.internal.floatfunc.FloatFunc;
import weightedgpa.infinibiome.internal.floatfunc.util.Interval;


public final class CubicNoise<I> implements FloatFunc<I> {
    public static final Interval OUTPUT_INTERVAL = new Interval(-1, 1);
    private final RawNoise noise;
    private final double inverseScale;
    private final PosInfo<I> posInfo;
    
    public CubicNoise(Seed seed, double scale, PosInfo<I> posInfo){
        seed = seed.newSeed("cubicNoise");

        this.posInfo = posInfo;
        this.noise = new RawNoise(seed.getAsInt());
        this.inverseScale = 1/scale;
    }

    @Override
    public double getOutput(I input) {
        return noise.sample(
            posInfo.getX(input) * inverseScale,
            posInfo.getZ(input) * inverseScale
        );
    }

    public double getOutput(I input, double scale) {
        return noise.sample(
            posInfo.getX(input) * inverseScale * (1/scale),
            posInfo.getZ(input) * inverseScale * (1/scale)
        );
    }

    @Override
    public Interval getOutputInterval() {
        return OUTPUT_INTERVAL;
    }

    //from https://github.com/jobtalle/CubicNoise
    private static class RawNoise {
        private static final int RND_A = 134775813;
        private static final int RND_B = 1103515245;

        private final int seed;

        RawNoise(int seed)
        {
            this.seed = seed;
        }

        double sample(double x, double y)
        {
            int xi = (int) Math.floor(x);
            double lerpx = x - xi;
            int yi = (int) Math.floor(y);
            double lerpy = y - yi;

            double xSamples[] = new double[4];

            for(int i = 0; i < 4; ++i)
                xSamples[i] = interpolate(
                    randomize(seed, xi - 1, yi - 1 + i),
                    randomize(seed, xi, yi - 1 + i),
                    randomize(seed, xi + 1, yi - 1 + i),
                    randomize(seed, xi + 2, yi - 1 + i),
                    lerpx
                );

            return interpolate(xSamples[0], xSamples[1], xSamples[2], xSamples[3], lerpy);
        }

        private static double randomize(int seed, int x, int y)
        {
            return (double) ((((x ^ y) * RND_A) ^ (seed + x)) * (((RND_B * x) << 16) ^ (RND_B * y) - RND_A)) / Integer.MAX_VALUE;
        }

        private static double interpolate(double a, double b, double c, double d, double x)
        {
            double p = (d - c) - (a - b);

            return x * (x * (x * p + ((a - b) - p)) + (c - a)) + b;
        }
    }

}
