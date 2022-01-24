package weightedgpa.infinibiome.internal.pointsprovider;

import weightedgpa.infinibiome.api.pointsprovider.PointsProvider;
import weightedgpa.infinibiome.internal.misc.MathHelper;
import weightedgpa.infinibiome.internal.misc.Log2helper;
import weightedgpa.infinibiome.internal.pos.Grid;

import java.util.*;

public abstract class GriddedPointsProvider<T> implements PointsProvider<T> {
    public abstract Iterable<T> getOutputFromGrid(Grid<T> grid);
    public abstract int getGridLengthLog2();

    @Override
    public Iterable<T> getClosestPoints(T center, int maxCount) {
        return () -> new ClosestIterable(center, maxCount);
    }

    @Override
    public Iterable<T> getBoundedPoints(T center, double maxRadius) {
        return () -> new BoundedIterable(center, maxRadius);
    }

    private final class ClosestIterable implements Iterator<T>{
        int currentRing = 0;
        int count = 0;

        final List<Pair<T>> postponedPoints = new ArrayList<>();
        final Queue<Pair<T>> buffer = new PriorityQueue<>(16, Comparator.comparing(p -> p.distSq));

        final T center;
        final Grid<T> currentGrid;
        final int maxCount;

        private ClosestIterable(T center, int maxCount) {
            this.center = center;
            this.currentGrid = new Grid<>(center, getGridLengthLog2(), getPosInfo());
            this.maxCount = maxCount;
        }

        @Override
        public boolean hasNext() {
            return count < maxCount;
        }

        @Override
        public T next() {
            if (!hasNext()){
                throw new NoSuchElementException();
            }

            for (; buffer.isEmpty(); currentRing++){
                //points that are outside the current ring will always be outside the maxDistance
                //points inside the current ring but outside the maxDistance have no guarantee of being closer to the center than the points outside the ring
                //those points must be postponed and checked if its within the next maxDistance
                double currentMaxDistance = currentRing * Log2helper.toNormal(getGridLengthLog2());

                double currentMaxDistanceSq = Math.pow(currentMaxDistance, 2);

                loadPostponedPoints(currentMaxDistanceSq);

                loadPointsFromCurrentRing(currentMaxDistanceSq);
            }

            count++;

            return buffer.poll().point;
        }

        //checks if previously postponed points are within the current max distance
        private void loadPostponedPoints(double currentMaxRadiusSq){
            for (int i = 0; i < postponedPoints.size(); i++){
                Pair<T> output = postponedPoints.get(i);

                if (output.distSq <= currentMaxRadiusSq){
                    buffer.add(output);
                    postponedPoints.remove(i);
                    i--;
                }
            }
        }

        //gets all points from the current ring, and postpone points that are outside the max distance
        private void loadPointsFromCurrentRing(double currentMaxRadiusSq){
            for (int x = -currentRing; x <= currentRing; x++){
                for (int z = -currentRing; z <= currentRing; z++){
                    if (Math.abs(x) < currentRing && Math.abs(z) < currentRing){
                        continue;
                    }

                    currentGrid.setOffset(x, z);

                    for (T point: getOutputFromGrid(currentGrid)){
                        double distSq = MathHelper.getDistanceSq(getPosInfo(), center, point);

                        if (distSq <= currentMaxRadiusSq) {
                            buffer.add(new Pair<>(point, distSq));
                        }
                        else {
                            postponedPoints.add(new Pair<>(point, distSq));
                        }
                    }
                }
            }
        }
    }

    static class Pair<T>{
        final T point;
        final double distSq;

        Pair(T point, double distSq) {
            this.point = point;
            this.distSq = distSq;
        }
    }

    private final class BoundedIterable implements Iterator<T>{
        private int x;
        private int z;
        private final Queue<T> buffer = new ArrayDeque<>();

        private final T center;
        private final double maxRadiusSq;

        private final Grid<T> currentGrid;
        private final int maxRadiusGrid;

        private BoundedIterable(T center, double maxRadius) {
            this.center = center;
            this.maxRadiusSq = maxRadius * maxRadius;

            this.currentGrid = new Grid<>(center, getGridLengthLog2(), getPosInfo());
            this.maxRadiusGrid = MathHelper.ceil(maxRadius / Log2helper.toNormal(getGridLengthLog2()));

            this.x = -maxRadiusGrid;
            this.z = -maxRadiusGrid;
        }

        @Override
        public boolean hasNext() {
            tryFillBuffer();

            return !buffer.isEmpty();
        }

        @SuppressWarnings("ConstantConditions")
        @Override
        public T next() {
            if (!hasNext()){
                throw new NoSuchElementException();
            }

            return buffer.poll();
        }

        private void tryFillBuffer(){
            for (; buffer.isEmpty() && canNextGrid(); nextGrid()){
                currentGrid.setOffset(x, z);

                for (T point: getOutputFromGrid(currentGrid)){
                    double distanceSq = MathHelper.getDistanceSq(getPosInfo(), point, center);

                    assert Double.isFinite(distanceSq);

                    if (distanceSq > maxRadiusSq){
                        continue;
                    }

                    buffer.add(point);
                }
            }
        }

        private void nextGrid(){
            if (x == maxRadiusGrid){
                x = -maxRadiusGrid;
                z++;
            }
            else {
                x++;
            }
        }

        private boolean canNextGrid(){
            return x <= maxRadiusGrid && z <= maxRadiusGrid;
        }
    }
}