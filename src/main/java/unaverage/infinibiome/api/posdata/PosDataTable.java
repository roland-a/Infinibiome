package weightedgpa.infinibiome.api.posdata;

import weightedgpa.infinibiome.api.pos.BlockPos2D;
import weightedgpa.infinibiome.internal.misc.IndexedKeys;
import weightedgpa.infinibiome.internal.misc.IndexedKeysFloat;

import java.util.function.Supplier;

public interface PosDataTable {
    IndexedKeys INDEXED_KEYS = new IndexedKeys();
    IndexedKeys DEFERRED_KEY_MAP = new IndexedKeys();
    IndexedKeysFloat FLOAT_DATA_MAP = new IndexedKeysFloat();

    BlockPos2D getPos();

    <T> T get(PosDataKey<T> key);

    <T> void set(PosDataKey<T> key, T value);

    double get(PosDataKeyFloat key);

    void set(PosDataKeyFloat key, double value);

    <T> T get(PosDataKeyDefered<T> key);

    <T> void set(PosDataKeyDefered<T> key, Supplier<T> value);
}
