package weightedgpa.infinibiome.internal.pointsprovider;

import weightedgpa.infinibiome.api.pos.PosInfo;
import weightedgpa.infinibiome.internal.pos.Grid;
import weightedgpa.infinibiome.internal.misc.Log2helper;

import java.util.Arrays;

/**
 * Contains a point at the lowest point of every grid
 *
 * @param <T>
 */
public final class GridLowestPoint<T> extends GriddedPointsProvider<T> {
    private final int gridLengthLog2;
    private final PosInfo<T> posInfo;

    public GridLowestPoint(int gridLength, PosInfo<T> posInfo) {
        this.gridLengthLog2 = Log2helper.asLog2(gridLength);
        this.posInfo = posInfo;
    }

    @Override
    public PosInfo<T> getPosInfo() {
        return posInfo;
    }

    @Override
    public Iterable<T> getOutputFromGrid(Grid<T> grid) {
        T lowest = grid.getLowest();

        return Arrays.asList(lowest);
    }

    @Override
    public int getGridLengthLog2() {
        return gridLengthLog2;
    }
}
