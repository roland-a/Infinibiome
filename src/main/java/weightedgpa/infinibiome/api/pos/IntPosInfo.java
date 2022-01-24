package weightedgpa.infinibiome.api.pos;

import weightedgpa.infinibiome.internal.misc.Log2helper;


/**
 * Gives information about a positional data type
 */
public interface IntPosInfo<T> extends PosInfo<T> {
    int getIntX(T pos);
    int getIntZ(T pos);

    int getLog2Scale();

    default int getOffset(){
        return 0;
    }

    @Override
    default <O> O convert(T pos, IntPosInfo<O> otherPosInfo){
        int resultX;

        resultX = getIntX(pos);
        resultX = Log2helper.mult(resultX, getLog2Scale());
        resultX += getOffset();
        resultX -= otherPosInfo.getOffset();
        resultX = Log2helper.floorDiv(resultX, otherPosInfo.getLog2Scale());

        int resultZ;

        resultZ = getIntZ(pos);
        resultZ = Log2helper.mult(resultZ, getLog2Scale());
        resultZ += getOffset();
        resultZ -= otherPosInfo.getOffset();
        resultZ = Log2helper.floorDiv(resultZ, otherPosInfo.getLog2Scale());

        return otherPosInfo.build(
            resultX,
            resultZ
        );
    }

    @Override
    default IntPosInfo<T> floor() {
        return this;
    }

    @Override
    T build(int x, int z);

    @Override
    default double getX(T pos){
        return getIntX(pos);
    }

    @Override
    default double getZ(T pos){
        return getIntZ(pos);
    }

    default T offset(T center, int xOffset, int zOffset){
        return build(
            getIntX(center) + xOffset,
            getIntZ(center) + zOffset
        );
    }
}
