package weightedgpa.infinibiome.internal.generators.chunks.surface;

import weightedgpa.infinibiome.internal.floatfunc.FloatFunc;
import weightedgpa.infinibiome.internal.floatfunc.generators.SimplexNoise;
import weightedgpa.infinibiome.internal.floatfunc.util.Interval;
import weightedgpa.infinibiome.api.generators.Seed;
import weightedgpa.infinibiome.api.pos.BlockPos2D;
import weightedgpa.infinibiome.internal.misc.Helper;

import java.util.function.Predicate;

final class Patches {
    private final FloatFunc<BlockPos2D> layerCountFunc;
    private final FloatFunc<BlockPos2D> patchSizeFunc;
    private final PatchLayer[] layers;

    @SuppressWarnings("ObjectAllocationInLoop")
    Patches(Seed seed) {
        seed = seed.newSeed("patches");

        this.layerCountFunc = initBaseLayerCount(seed);
        this.patchSizeFunc = initClusterSize(seed);
        this.layers = new PatchLayer[(int) layerCountFunc.getOutputInterval().getMax()];

        for (int i = 0; i < layers.length; i++){
            layers[i] = new PatchLayer(
                seed.newSeed("patch " + i)
            );
        }
    }

    //region patch noise
    private static FloatFunc<BlockPos2D> initBaseLayerCount(Seed seed){
        seed = seed.newSeed("baseLayerCount");

        return Helper.initUniformNoise(seed, Helper.COMMON_SCALE)
            .mapInterval(
                new Interval(1, 20)
            );
    }

    private static FloatFunc<BlockPos2D> initClusterSize(Seed seed){
        seed = seed.newSeed("clusterSize");

        return Helper.<BlockPos2D>initUniformNoise(seed, Helper.COMMON_SCALE)
            .mapInterval(
                new Interval(0.01f, 0.10f)
            );
    }

    boolean canPlaceAt(BlockPos2D input, double extraProbability) {
        int layerCount = (int)(layerCountFunc.getOutput(input) * extraProbability);

        for (int i = 0; i < layerCount; i++){
            if (layers[i].test(input)){
                return true;
            }
        }
        return false;
    }

    private class PatchLayer implements Predicate<BlockPos2D> {
        private static final double SCALE = 10;

        private final FloatFunc<BlockPos2D> base;

        PatchLayer(Seed seed) {
            seed = seed.newSeed("patchLayer");

            this.base = new SimplexNoise<>(seed, SCALE, BlockPos2D.INFO)
                .toUniform(
                    SimplexNoise.PERCENTILE_TABLE
                );
        }

        @Override
        public boolean test(BlockPos2D input) {
            return base.getOutput(input) < patchSizeFunc.getOutput(input);
        }
    }
}
