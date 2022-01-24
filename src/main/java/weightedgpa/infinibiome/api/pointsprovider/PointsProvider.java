package weightedgpa.infinibiome.api.pointsprovider;

import com.google.common.collect.Iterables;
import org.apache.commons.lang3.mutable.MutableInt;
import weightedgpa.infinibiome.api.pos.IntPosInfo;
import weightedgpa.infinibiome.api.pos.PosInfo;
import weightedgpa.infinibiome.internal.misc.MathHelper;

import java.util.Iterator;
import java.util.function.Predicate;

public interface PointsProvider<T> {
    /**
     * Gets all the closest points from the search center.
     * The points returned are sorted by distance from the search center in ascending order.
     *
     * @param center
     * The search center.
     *
     * @param maxCount
     * The maximum number of points it will return
     */
    Iterable<T> getClosestPoints(T center, int maxCount);

    /**
     * Gets all the points within a distance to the search center.
     * The points returned are given in an unsorted, arbitrary order.
     *
     * @param center
     * The search center.
     *
     * @param maxRadius
     * The radius from the search center that all points returned must be within.
     */
    Iterable<T> getBoundedPoints(T center, double maxRadius);

    PosInfo<T> getPosInfo();

    default boolean hasPoint(T pos){
        for (T point: getBoundedPoints(pos, 0)){
            assert point.equals(pos): pos + " " + point;

            return true;
        }
        return false;
    }

    default T getClosestPoint(T center){
        Iterable<T> result = getClosestPoints(center, 1);

        if (Iterables.isEmpty(result)){
            return null;
        }
        return Iterables.get(result, 0);
    }

    default PointsProvider<T> filterOutput(Predicate<T> condition){
        return new PointsProvider<T>() {
            @Override
            public Iterable<T> getClosestPoints(T center, int maxCount) {
                MutableInt counter = new MutableInt(0);

                Iterator<T> points = PointsProvider.this.getClosestPoints(center, Integer.MAX_VALUE).iterator();
                
                return () -> new Iterator<T>(){
                    @Override
                    public boolean hasNext() {
                        return points.hasNext() && counter.getValue() < maxCount;
                    }

                    @Override
                    public T next() {
                        while (true){
                            T point = points.next();

                            if (condition.test(point)){
                                counter.increment();
                                return point;
                            }
                        }
                    }
                };
            }

            @Override
            public Iterable<T> getBoundedPoints(T center, double maxRadius) {
                return Iterables.filter(
                    PointsProvider.this.getBoundedPoints(center, maxRadius),
                    condition::test
                );
            }

            @Override
            public PosInfo<T> getPosInfo() {
                return PointsProvider.this.getPosInfo();
            }
        };
    }

    default <NT> PointsProvider<NT> mapPoints(IntPosInfo<NT> newPosInfo){
        return new PointsProvider<NT>() {
            @Override
            public Iterable<NT> getClosestPoints(NT center, int maxCount) {
                T centerMapped = newPosInfo.convert(
                    center,
                    PointsProvider.this.getPosInfo().floor()
                );

                return Iterables.transform(
                    PointsProvider.this.getClosestPoints(centerMapped, maxCount),
                    p -> PointsProvider.this.getPosInfo().convert(
                        p,
                        newPosInfo
                    )
                );
            }

            @Override
            public Iterable<NT> getBoundedPoints(NT center, double maxRadius) {
                T centerMapped = newPosInfo.convert(
                    center,
                    PointsProvider.this.getPosInfo().floor()
                );

                return Iterables.transform(
                    PointsProvider.this.getBoundedPoints(centerMapped, maxRadius),
                    p -> PointsProvider.this.getPosInfo().convert(
                        p,
                        newPosInfo
                    )
                );
            }

            @Override
            public IntPosInfo<NT> getPosInfo() {
                return newPosInfo;
            }
        };
    }

    default double getDistanceToVoronoiBorder(T center){
        return MathHelper.getDistanceToVoronoiBorder(
            center, this, 5
        );
    }
}
