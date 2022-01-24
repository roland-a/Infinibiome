package weightedgpa.infinibiome.api.pos;

import weightedgpa.infinibiome.internal.misc.MathHelper;
import weightedgpa.infinibiome.internal.misc.Log2helper;

/**
 * Visitor object that can obtain the x and y value of a position type
 * Also allows the construction of a position type
 */
public interface PosInfo<T> {
    double getX(T pos);
    double getZ(T pos);

    T build(int x, int z);

    default <O> O convert(T pos, IntPosInfo<O> otherPosInfo){
        int resultX;

        resultX = MathHelper.floor(getX(pos));
        resultX -= otherPosInfo.getOffset();
        resultX = Log2helper.floorDiv(resultX, otherPosInfo.getLog2Scale());

        int resultZ;

        resultZ = MathHelper.floor(getZ(pos));
        resultZ -= otherPosInfo.getOffset();
        resultZ = Log2helper.floorDiv(resultZ, otherPosInfo.getLog2Scale());

        return otherPosInfo.build(
            resultX,
            resultZ
        );
    }

    default IntPosInfo<T> floor(){
        return new IntPosInfo<T>() {
            @Override
            public int getIntX(T pos) {
                return MathHelper.floor(PosInfo.this.getX(pos));
            }

            @Override
            public int getIntZ(T pos) {
                return MathHelper.floor((int)PosInfo.this.getZ(pos));
            }

            @Override
            public int getLog2Scale() {
                return 0;
            }

            @Override
            public T build(int x, int z) {
                return PosInfo.this.build(x, z);
            }
        };
    }
}
