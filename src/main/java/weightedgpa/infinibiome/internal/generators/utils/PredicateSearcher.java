package weightedgpa.infinibiome.internal.generators.utils;

import com.google.common.collect.Lists;
import weightedgpa.infinibiome.api.pos.PosInfo;
import weightedgpa.infinibiome.internal.pointsprovider.GriddedPointsProvider;
import weightedgpa.infinibiome.internal.pos.Grid;
import weightedgpa.infinibiome.internal.misc.Log2helper;

import java.util.Collections;
import java.util.function.Predicate;

public final class PredicateSearcher<T> extends GriddedPointsProvider<T> {
    private final Predicate<T> predicate;
    private final int gridLengthLog2;

    private final PosInfo<T> posInfo;

    public PredicateSearcher(int gridSize, Predicate<T> predicate, PosInfo<T> posInfo) {
        this.predicate = predicate;
        this.gridLengthLog2 = Log2helper.asLog2(gridSize);
        this.posInfo = posInfo;
    }

    @Override
    public int getGridLengthLog2() {
        return gridLengthLog2;
    }

    @Override
    public PosInfo<T> getPosInfo() {
        return posInfo;
    }

    @Override
    public Iterable<T> getOutputFromGrid(Grid<T> grid) {
        T pos = grid.getLowest();

        if (predicate.test(pos)){
            return Lists.newArrayList(pos);
        }
        return Collections.emptyList();
    }
}
