package weightedgpa.infinibiome.api.posdata;

import weightedgpa.infinibiome.api.pos.BlockPos2D;
import weightedgpa.infinibiome.internal.misc.IndexedKeys;

import java.util.function.Function;
import java.util.function.Supplier;

//todo probably merge with PosDataKey
public final class PosDataKeyDefered<T> {
    public final IndexedKeys.Key<BlockPos2D, Supplier<T>> internalKey;

    public PosDataKeyDefered(Function<BlockPos2D, Supplier<T>> getDefault) {
        this.internalKey = PosDataTable.DEFERRED_KEY_MAP.new Key<>(getDefault);
    }

    public PosDataKeyDefered() {
        this(p -> null);
    }
}

