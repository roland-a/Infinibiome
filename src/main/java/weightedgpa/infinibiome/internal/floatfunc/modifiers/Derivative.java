package weightedgpa.infinibiome.internal.floatfunc.modifiers;

import weightedgpa.infinibiome.api.pos.FloatPosInfo;
import weightedgpa.infinibiome.api.pos.PosInfo;
import weightedgpa.infinibiome.internal.floatfunc.FloatFunc;
import weightedgpa.infinibiome.internal.floatfunc.util.Interval;


public class Derivative<T> implements FloatFunc<T> {
    private static final double CHANGE = .1f;

    private final FloatFunc<T> base;
    private final FloatPosInfo<T> posInfo;

    public Derivative(FloatFunc<T> base, FloatPosInfo<T> posInfo) {
        this.base = base;
        this.posInfo = posInfo;
    }

    @Override
    public double getOutput(T input) {
        double original = base.getOutput(input);

        double incrementX = base.getOutput(
            posInfo.build(
                posInfo.getX(input) + CHANGE,
                posInfo.getZ(input)
            )
        );

        double incrementZ = base.getOutput(
            posInfo.build(
                posInfo.getX(input),
                posInfo.getZ(input) + CHANGE
            )
        );

        double incrementXZ = base.getOutput(
            posInfo.build(
                posInfo.getX(input) + CHANGE,
                posInfo.getZ(input) + CHANGE
            )
        );

        return (
            ((original - incrementX) / CHANGE) +
            ((original - incrementZ) / CHANGE)// +
            //((original - incrementXZ) / CHANGE)
        ) / 2;
    }

    @Override
    public Interval getOutputInterval() {
        return new Interval(
            (-base.getOutputInterval().getSize() / CHANGE) / 300,
            (base.getOutputInterval().getSize() / CHANGE) / 300
        );
    }
}
