package weightedgpa.infinibiome.internal.pointsprovider;

import weightedgpa.infinibiome.api.pointsprovider.PointsProvider;
import weightedgpa.infinibiome.api.pos.IntPosInfo;
import weightedgpa.infinibiome.api.pos.PosInfo;

import java.util.Collections;

/**
 * Contains no points at all.
 */
public final class EmptyPointsProvider<T> implements PointsProvider<T> {
    private final PosInfo<T> posInfo;

    public EmptyPointsProvider(PosInfo<T> posInfo){
        this.posInfo = posInfo;

    }

    @Override
    public Iterable<T> getClosestPoints(Object center, int maxCount) {
        return Collections.emptyList();
    }

    @Override
    public Iterable<T> getBoundedPoints(Object center, double maxRadius) {
        return Collections.emptyList();
    }

    @Override
    public PosInfo<T> getPosInfo() {
        return posInfo;
    }
}
