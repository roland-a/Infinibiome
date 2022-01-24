package weightedgpa.infinibiome.internal.generators.interchunks.tree;

import net.minecraft.block.*;
import net.minecraft.world.gen.blockstateprovider.SimpleBlockStateProvider;
import net.minecraft.world.gen.feature.*;
import net.minecraft.world.gen.foliageplacer.BlobFoliagePlacer;
import net.minecraftforge.common.IPlantable;
import weightedgpa.infinibiome.api.Infinibiome;
import weightedgpa.infinibiome.api.dependency.DependencyInjector;
import weightedgpa.infinibiome.api.pos.BlockPos2D;
import weightedgpa.infinibiome.api.pos.InterChunkPos;

public final class OakBigGen extends TreeGenBase {
    private static final BlockState LOG = Blocks.OAK_LOG.getDefaultState();
    private static final BlockState LEAF = Blocks.OAK_LEAVES.getDefaultState();
    private static final IPlantable SAPLING = (IPlantable) Blocks.OAK_SAPLING;

    public OakBigGen(DependencyInjector di) {
        super(di, Infinibiome.MOD_ID + ":oakBig");

        config = this.<TreeFeatureConfig>initConfig()
            .setFeature(
                Feature.FANCY_TREE
            )
            .setConfigFunc(
                (pos, __, random) -> new TreeFeatureConfig.Builder(
                    new SimpleBlockStateProvider(LOG),
                    new SimpleBlockStateProvider(LEAF),
                    new BlobFoliagePlacer(0, 0)
                )
                .ignoreVines()
                .setSapling(SAPLING)
                .build()
        )
        .setHeightFunc(0, 0)
        .setIsolationRadius(TreeHelper.COMMON_ISOLATION_RADIUS)
        .growInAnySaplingConfig()
        .setWithCommonDensity()
        .setRegionRate(
            TreeHelper.COMMON_REGION_RATE / 2
        )
        .noExtraConditions();
    }

    @Override
    public double getPlantControllerWeight(BlockPos2D pos) {
        if (growthConfig.allowSaplingUniformGrowth){
            return 1/10d;
        }
        return getDensity(new InterChunkPos(pos));
    }
}
