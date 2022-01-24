package weightedgpa.infinibiome.internal.generators.posdata;

import weightedgpa.infinibiome.api.generators.PosDataGen;
import weightedgpa.infinibiome.api.dependency.DependencyInjector;
import weightedgpa.infinibiome.api.generators.Timing;
import weightedgpa.infinibiome.api.pos.BlockPos2D;
import weightedgpa.infinibiome.api.posdata.*;
import weightedgpa.infinibiome.internal.misc.IndexedKeysFloat;
import weightedgpa.infinibiome.internal.misc.IndexedKeys;
import weightedgpa.infinibiome.internal.misc.PosModCache;
import weightedgpa.infinibiome.internal.misc.Helper;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.Supplier;

public final class PosDataProviderBase implements PosDataProvider {
    private final List<PosDataGen> dataGens;
    private final PosModCache<BlockPos2D, Table> dataCache;

    public PosDataProviderBase(DependencyInjector di){
        this(
            di.getAll(PosDataGen.class),
            512
        );
    }

    public PosDataProviderBase(DependencyInjector di, Timing timing){
        this(
            di.getAll(PosDataGen.class),
            16
        );

        dataGens.removeIf(p -> p.getTiming().compareTo(timing) >= 0);
    }

    public PosDataProviderBase(List<PosDataGen> dataGens, int cacheSize){
        this.dataGens = new ArrayList<>(dataGens);

        Helper.strictSort(this.dataGens, Comparator.comparing(PosDataGen::getTiming));

        dataCache = new PosModCache<>(
            cacheSize,
            this::posToDataUncached,
            BlockPos2D.INFO
        );
    }

    @Override
    public <T> T get(PosDataKey<T> key, BlockPos2D pos){
        return getTable(pos).get(key);
    }

    @Override
    public double get(PosDataKeyFloat key, BlockPos2D pos){
        return getTable(pos).get(key);
    }

    @Override
    public <T> T get(PosDataKeyDefered<T> key, BlockPos2D pos) {
        return getTable(pos).get(key);
    }

    private Table getTable(BlockPos2D pos){
        return dataCache.get(pos);
    }

    private Table posToDataUncached(BlockPos2D pos){
        Table dataOutput = new Table(pos);

        for (PosDataGen dataGen: dataGens){
            perDataGen(dataGen, dataOutput);
        }

        return dataOutput;
    }

    private void perDataGen(PosDataGen dataGen, Table dataOutput){
        try {
            dataGen.generateData(dataOutput);
        } catch (Throwable e){
            throw new RuntimeException(dataGen.toString(), e);
        }
    }

    private static class Table implements PosDataTable {
        private final BlockPos2D pos;
        private final IndexedKeys.Table table = INDEXED_KEYS.new Table();
        private final IndexedKeys.Table tableDeferred = DEFERRED_KEY_MAP.new Table();
        private final IndexedKeysFloat.Table tableFloat = FLOAT_DATA_MAP.new Table();

        private Table(BlockPos2D pos) {
            this.pos = pos.toImmutable();
        }

        /**
         * @return The position this is in
         */
        @Override
        public BlockPos2D getPos(){
            return pos;
        }

        /**
         * Returns a value associated with a key
         */
        @Override
        public <T> T get(PosDataKey<T> key){
            T result = table.getValue(key.internalKey, pos);

            assert result != null: "no value set yet";

            return result;
        }

        /**
         * Same as {@link #get(PosDataKey)}, but with float data
         */
        @Override
        public double get(PosDataKeyFloat key){
            double result = tableFloat.getValue(key.internalKey, pos);

            assert !Double.isNaN(result): "no value set yet";

            return result;
        }

        /**
         * Returns a value associated with a key
         */
        @Override
        public <T> T get(PosDataKeyDefered<T> key){
            Supplier<T> result = tableDeferred.getValue(key.internalKey, pos);

            assert result != null;

            return result.get();
        }

        /**
         * Replaces a value associated with a key with a new one
         */
        @Override
        public <T> void set(PosDataKey<T> key, T value){
            table.setValue(key.internalKey, value);
        }

        /**
         * Same as {@link #set(PosDataKey, Object)}, but with float data
         */
        @Override
        public void set(PosDataKeyFloat key, double item){
            assert !Double.isNaN(item);

            tableFloat.setValue(key.internalKey, item);
        }

        /**
         * Same as {@link #set(PosDataKey, Object)}, but the final data isnt set until later
         */
        @Override
        public <T> void set(PosDataKeyDefered<T> key, Supplier<T> valueProducer){
            tableDeferred.setValue(key.internalKey, valueProducer);
        }
    }
}
