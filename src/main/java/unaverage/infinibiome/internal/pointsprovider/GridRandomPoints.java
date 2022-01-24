package weightedgpa.infinibiome.internal.pointsprovider;

import weightedgpa.infinibiome.api.pos.PosInfo;
import weightedgpa.infinibiome.internal.floatfunc.generators.RandomGen;
import weightedgpa.infinibiome.internal.floatfunc.IntFunc;
import weightedgpa.infinibiome.api.generators.Seed;
import weightedgpa.infinibiome.internal.misc.Log2helper;
import weightedgpa.infinibiome.internal.pos.Grid;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Random;

/**
 * Every grid has points randomly placed inside
 */
public final class GridRandomPoints<T> extends GriddedPointsProvider<T> {
    private final RandomGen randomGen;
    private final IntFunc<T> pointsPerGridFunc;
    private final int gridLengthLog2;
    private final PosInfo<T> posInfo;

    public GridRandomPoints(Seed seed, IntFunc<T> pointsPerGridFunc, int gridLength, PosInfo<T> posInfo) {
        seed = seed.newSeed("griddedRandom");

        assert pointsPerGridFunc.getOutputInterval().getMax() > 0;

        this.randomGen = new RandomGen(seed);
        this.pointsPerGridFunc = pointsPerGridFunc;
        this.gridLengthLog2 = Log2helper.asLog2(gridLength);
        this.posInfo = posInfo;
    }


    @Override
    public PosInfo<T> getPosInfo() {
        return posInfo;
    }

    @Override
    public Iterable<T> getOutputFromGrid(Grid<T> grid) {
        Random random = randomGen.getRandom(grid.getGridX(), grid.getGridZ());

        int pointCount = pointsPerGridFunc.getIntOutput(grid.getLowest());

        Collection<T> out = new ArrayList<>(pointCount);

        for (int i = 0; i < pointCount; i++) {
            out.add(
                grid.randomPos(random)
            );
        }
        return out;
    }

    @Override
    public int getGridLengthLog2() {
        return gridLengthLog2;
    }
}


