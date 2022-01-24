package weightedgpa.infinibiome.api.posdata;

import weightedgpa.infinibiome.api.pos.BlockPos2D;

//todo maybe return an immutable version of posDataTable
public interface PosDataProvider {
    <T> T get(PosDataKey<T> key, BlockPos2D pos);

    double get(PosDataKeyFloat key, BlockPos2D pos);

    <T> T get(PosDataKeyDefered<T> key, BlockPos2D pos);
}
