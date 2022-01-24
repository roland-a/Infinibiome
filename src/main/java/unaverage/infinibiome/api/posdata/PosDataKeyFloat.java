package weightedgpa.infinibiome.api.posdata;

import weightedgpa.infinibiome.api.pos.BlockPos2D;
import weightedgpa.infinibiome.internal.misc.IndexedKeysFloat;

import java.util.function.ToDoubleFunction;

/**
 * Same as {@link PosDataKey}, but with float values
 */
public final class PosDataKeyFloat {
    public final IndexedKeysFloat.Key<BlockPos2D> internalKey;

    public PosDataKeyFloat(ToDoubleFunction<BlockPos2D> toDefault) {
        internalKey = PosDataTable.FLOAT_DATA_MAP.new Key<>(toDefault);
    }

    public PosDataKeyFloat() {
        this(p -> Double.NaN);
    }
}
