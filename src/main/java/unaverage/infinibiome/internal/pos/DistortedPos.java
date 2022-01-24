package weightedgpa.infinibiome.internal.pos;

import weightedgpa.infinibiome.api.pos.FloatPosInfo;
import weightedgpa.infinibiome.api.pos.PosInfo;

public final class DistortedPos {
    private final double x;
    private final double z;

    DistortedPos(double x, double z) {
        this.x = x;
        this.z = z;
    }

    public static final PosInfo<DistortedPos> INFO = new FloatPosInfo<DistortedPos>() {
        @Override
        public double getX(DistortedPos pos) {
            return pos.x;
        }

        @Override
        public double getZ(DistortedPos pos) {
            return pos.z;
        }

        @Override
        public DistortedPos build(double x, double z) {
            return new DistortedPos(x, z);
        }
    };

}
