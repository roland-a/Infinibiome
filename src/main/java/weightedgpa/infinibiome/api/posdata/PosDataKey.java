package weightedgpa.infinibiome.api.posdata;

import weightedgpa.infinibiome.api.pos.BlockPos2D;
import weightedgpa.infinibiome.internal.misc.IndexedKeys;

import java.util.function.Function;

/**
 * Used to retrieving specific data at specific locations stored in {@link PosDataProvider} or {@link PosDataTable}
 */
public final class PosDataKey<T> {
    public final IndexedKeys.Key<BlockPos2D, T> internalKey;

    public PosDataKey(Function<BlockPos2D, T> getDefault) {
        this.internalKey = PosDataTable.INDEXED_KEYS.new Key<>(getDefault);
    }

    public PosDataKey() {
        this(p -> null);
    }
}
