package weightedgpa.infinibiome.internal.floatfunc.generators;

import net.openhft.hashing.LongHashFunction;
import weightedgpa.infinibiome.api.pos.IntPosInfo;
import weightedgpa.infinibiome.internal.floatfunc.FloatFunc;
import weightedgpa.infinibiome.internal.floatfunc.util.Interval;
import weightedgpa.infinibiome.api.generators.Seed;

import java.util.Random;


public final class RandomGen {
    private final LongHashFunction hasher;

    public RandomGen(Seed seed){
        seed = seed.newSeed("randomGen");

        this.hasher = LongHashFunction.xx(seed.getAsInt());
    }

    public <I> FloatFunc<I> asPercentFloatFunc(IntPosInfo<I> posInfo){
        return new FloatFunc<I>() {
            @Override
            public double getOutput(I input) {
                return getPercent(
                    posInfo.getIntX(input),
                    posInfo.getIntZ(input)
                );
            }

            @Override
            public Interval getOutputInterval() {
                return Interval.PERCENT;
            }
        };
    }

    public int getRandomInt(int x, int z) {
        int merged = (x << 16) | (z & 0xffff);

        return (int)hasher.hashInt(merged);
    }

    public Random getRandom(int x, int z){
        return new Random(getRandomInt(x, z));
    }

    public double getPercent(int x, int z) {
        return (getRandomInt(x, z) & 0xffffff) / (double)(1 << 24);
    }

}
