package weightedgpa.infinibiome.internal.pos;

import weightedgpa.infinibiome.api.pos.PosInfo;
import weightedgpa.infinibiome.internal.floatfunc.FloatFunc;
import weightedgpa.infinibiome.api.generators.Seed;

import java.util.function.Function;

public final class PosDistorter<I> {
    private final FloatFunc<? super I> xOffsetFunc;
    private final FloatFunc<? super I> zOffsetFunc;
    private final PosInfo<I> posInfo;

    public PosDistorter(Seed seed, Function<Seed, FloatFunc<I>> axisOffsetTemplate, PosInfo<I> posInfo) {
        this.posInfo = posInfo;
        this.xOffsetFunc = axisOffsetTemplate.apply(seed.newSeed("x"));
        this.zOffsetFunc = axisOffsetTemplate.apply(seed.newSeed("z"));
    }

    public DistortedPos distortPos(I pos){
        return new DistortedPos(
            posInfo.getX(pos) + xOffsetFunc.getOutput(pos),
            posInfo.getZ(pos) + zOffsetFunc.getOutput(pos)
        );
    }
}
