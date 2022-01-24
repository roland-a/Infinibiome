package weightedgpa.infinibiome.internal.misc;

import weightedgpa.infinibiome.api.pos.IntPosInfo;

import java.util.concurrent.atomic.AtomicReferenceArray;
import java.util.function.Function;

public final class PosModCache<I, O> {
    private final int gridLength;
    private final int gridLengthLog2;
    private final Function<I, O> base;
    private final AtomicReferenceArray<Entry<O>> keyArray;
    private final IntPosInfo<I> posInfo;

    public PosModCache(int gridLength, Function<I, O> base, IntPosInfo<I> posInfo){
        this.gridLengthLog2 = Log2helper.asLog2(gridLength);
        this.gridLength = gridLength;
        this.base = base;
        this.keyArray = new AtomicReferenceArray<>(gridLength * gridLength);
        this.posInfo = posInfo;
    }

    public O get(I pos) {
        int posX = posInfo.getIntX(pos);
        int posZ = posInfo.getIntZ(pos);

        int modX = Log2helper.mod(posX, gridLengthLog2);
        int modZ = Log2helper.mod(posZ, gridLengthLog2);

        int index = Log2helper.mult(modX, gridLengthLog2) + modZ;

        Entry<O> entryAtIndex = keyArray.get(index);

        if (entryAtIndex != null && posX == entryAtIndex.x && posZ == entryAtIndex.z){
            return entryAtIndex.value;
        }

        O newValue = base.apply(pos);

        keyArray.set(index, new Entry<>(posX, posZ, newValue));

        return newValue;
    }

    private static class Entry<O> {
        private final int x;
        private final int z;
        private final O value;

        private Entry(int x, int z, O value) {
            this.x = x;
            this.z = z;
            this.value = value;
        }
    }
}
