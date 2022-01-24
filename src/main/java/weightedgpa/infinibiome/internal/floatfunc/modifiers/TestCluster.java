package weightedgpa.infinibiome.internal.floatfunc.modifiers;

/*
public final class TestCluster<T extends IntPos2D> implements Noise<T> {
    private final IntNoise<T> pointsPerGrid;
    private final Noise<ClusterRadiusInput<T>> nonFadeRadius;
    private final Noise<ClusterRadiusInput<T>> fadingRadius;
    private final int gridLength;
    private final BiFunction<Integer, Integer, T> posBuilder;
    private final GridWithPoints primaryGrid;
    private final GridWithPoints secondaryGrid;
    private final GridWithPoints ternaryGrid;

    private TestCluster(IntNoise<T> pointsPerGrid, Noise<ClusterRadiusInput<T>> nonFadeRadius, Noise<ClusterRadiusInput<T>> fadingRadius, int gridLength, BiFunction<Integer, Integer, T> posBuilder, GridWithPoints primaryGrid, GridWithPoints secondaryGrid, GridWithPoints ternaryGrid) {
        this.pointsPerGrid = pointsPerGrid;
        this.nonFadeRadius = nonFadeRadius;
        this.fadingRadius = fadingRadius;
        this.gridLength = gridLength;
        this.posBuilder = posBuilder;
        this.primaryGrid = new GridWithPoints(primaryGrid);
        this.secondaryGrid = new GridWithPoints(secondaryGrid);
        this.ternaryGrid = new GridWithPoints(ternaryGrid);
    }

    private TestCluster(Seed seed, IntNoise<T> pointsPerGrid, Noise<ClusterRadiusInput<T>> nonFadeRadius, Noise<ClusterRadiusInput<T>> fadingRadius, int gridLength, BiFunction<Integer, Integer, T> posBuilder) {
        assert nonFadeRadius.getOutputRange().getMax() + fadingRadius.getOutputRange().getMax() < gridLength / 2;
        assert pointsPerGrid.getOutputRange().getMin() > 0;


        seed = seed.newBranch("cluster");

        this.pointsPerGrid = pointsPerGrid;
        this.nonFadeRadius = nonFadeRadius;
        this.fadingRadius = fadingRadius;
        this.gridLength = gridLength;
        this.posBuilder = posBuilder;

        this.primaryGrid = new GridWithPoints(seed.newBranch("primary"), 0);
        this.secondaryGrid = new GridWithPoints(seed.newBranch("secondary"), gridLength / 3 * 1);
        this.ternaryGrid = new GridWithPoints(seed.newBranch("ternary"), gridLength / 3 * 2);
    }

    public static <T extends IntPos2D> TestCluster<T> init(Seed seed, IntNoise<T> pointsPerGrid, Noise<ClusterRadiusInput<T>> nonFadeRadius, Noise<ClusterRadiusInput<T>> fadingRadius, int gridLength, BiFunction<Integer, Integer, T> posBuilder) {
        return new TestCluster<>(
            seed,
            pointsPerGrid,
            nonFadeRadius,
            fadingRadius,
            gridLength,
            posBuilder
        );
    }

    @Override
    public float getOutput(T input) {
        float result = primaryGrid.getValue(input);

        if (result == 1){
            return result;
        }

        result = Math.max(result, secondaryGrid.getValue(input));

        if (result == 1){
            return result;
        }

        return Math.max(result, ternaryGrid.getValue(input));
    }

    @Override
    public Range getOutputRange() {
        return Range.PERCENT;
    }

    private class GridWithPoints {
        private final IntNoise<T> randomSeedProducer;
        private final int offset;

        private GridWithPoints(GridWithPoints original) {
            this.randomSeedProducer = original.randomSeedProducer;
            this.offset = original.offset;
        }

        private GridWithPoints(Seed seed, int offset) {
            this.randomSeedProducer = RandomNoise2D.init(seed);
            this.offset = offset;
        }

        private float getValue(T input){
            final int minGridX = input.getX() - Math.floorMod(input.getX() + offset, gridLength);
            final int minGridZ = input.getZ() - Math.floorMod(input.getZ() + offset, gridLength);

            float result = 0;

            for (T point: getPointsInGrid(minGridX, minGridZ)){
                final ClusterRadiusInput<T> clusterInput = ClusterRadiusInput.init(input, point);

                result = ClusterHelper.newResult(
                    Distance2D.fromPos(input, point),
                    nonFadeRadius.getOutput(clusterInput),
                    fadingRadius.getOutput(clusterInput),
                    result
                );

                if (result == 1){
                    return 1;
                }
            }
            return result;
        }

        private List<T> getPointsInGrid(int gridMinX, int gridMinZ){
            final T gridMinPos = posBuilder.apply(gridMinX, gridMinZ);
            final int pointCount = pointsPerGrid.getIntOutput(gridMinPos);

            if (pointCount == 0){
                return Collections.emptyList();
            }

            final int pointMinX = (int)Math.ceil(gridMinX + nonFadeRadius.getOutputRange().getMax() + fadingRadius.getOutputRange().getMax());
            final int pointMinZ = (int)Math.ceil(gridMinZ + nonFadeRadius.getOutputRange().getMax() + fadingRadius.getOutputRange().getMax());

            //max are end exclusive
            final int gridMaxX = gridMinX + gridLength;
            final int gridMaxZ = gridMinZ + gridLength;

            final int pointMaxX = (int)Math.floor(gridMaxX - nonFadeRadius.getOutputRange().getMin() - fadingRadius.getOutputRange().getMin());
            final int pointMaxZ = (int)Math.floor(gridMaxZ - nonFadeRadius.getOutputRange().getMin() - fadingRadius.getOutputRange().getMin());

            List<T> result = new ArrayList<>(pointCount);

            Random random = new Random(randomSeedProducer.getIntOutput(gridMinPos));

            for (int i = 0; i < pointCount; i++){
                final T newPoint = posBuilder.apply(
                    Helper.randomInt(random, pointMinX, pointMaxX),
                    Helper.randomInt(random, pointMinZ, pointMaxZ)
                );
                result.add(newPoint);
            }

            return result;
        }
    }
}

 */
