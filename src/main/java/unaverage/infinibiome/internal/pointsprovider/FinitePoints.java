package weightedgpa.infinibiome.internal.pointsprovider;

import weightedgpa.infinibiome.api.pointsprovider.PointsProvider;
import weightedgpa.infinibiome.api.pos.IntPosInfo;
import weightedgpa.infinibiome.api.pos.PosInfo;
import weightedgpa.infinibiome.internal.misc.MathHelper;
import weightedgpa.infinibiome.internal.misc.Pair;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Contains a finite list of points
 */
public final class FinitePoints<T> implements PointsProvider<T> {
    private final List<T> points;
    private final PosInfo<T> posInfo;

    public FinitePoints(List<T> points, PosInfo<T> posInfo) {
        this.points = new ArrayList<>(points);
        this.posInfo = posInfo;
    }

    //can be optimized if needed
    @Override
    public Iterable<T> getClosestPoints(T center, int maxCount) {
        List<Pair<T, Double>> result = points.stream().map(
            p -> new Pair<>(
                p,
                MathHelper.getDistanceSq(
                    posInfo,
                    p,
                    center
                )
            )
        )
        .collect(Collectors.toList());

        result.sort(
            Comparator.comparing(a -> a.second)
        );

        return result
            .stream()
            .map(p -> p.first)
            .collect(Collectors.toList())
            .subList(0, maxCount);
    }

    @Override
    public Iterable<T> getBoundedPoints(T center, double maxRadius) {
        double maxRadiusSq = maxRadius * maxRadius;

        return points.stream().filter(
            p -> MathHelper.getDistanceSq(
                posInfo,
                p,
                center
            ) <= maxRadiusSq
        )
        .collect(Collectors.toList());
    }

    @Override
    public PointsProvider<T> filterOutput(Predicate<T> condition) {
        return new FinitePoints<>(
            points.stream().filter(condition).collect(Collectors.toList()),
            posInfo
        );
    }

    @Override
    public <NT> PointsProvider<NT> mapPoints(IntPosInfo<NT> newPosInfo) {
        return new FinitePoints<>(
            points.stream().map(p -> posInfo.convert(p, newPosInfo)).collect(Collectors.toList()),
            newPosInfo
        );
    }

    @Override
    public PosInfo<T> getPosInfo() {
        return posInfo;
    }
}
