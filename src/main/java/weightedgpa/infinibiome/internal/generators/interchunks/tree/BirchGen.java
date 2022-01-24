package weightedgpa.infinibiome.internal.generators.interchunks.tree;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.world.gen.blockstateprovider.SimpleBlockStateProvider;
import net.minecraft.world.gen.feature.*;
import net.minecraft.world.gen.foliageplacer.BlobFoliagePlacer;
import net.minecraftforge.common.IPlantable;
import weightedgpa.infinibiome.api.Infinibiome;
import weightedgpa.infinibiome.api.dependency.DependencyInjector;
import weightedgpa.infinibiome.internal.floatfunc.FloatFunc;

public final class BirchGen extends TreeGenBase {
    private static final BlockState LOG = Blocks.BIRCH_LOG.getDefaultState();
    private static final BlockState LEAF = Blocks.BIRCH_LEAVES.getDefaultState();
    private static final IPlantable SAPLING = (IPlantable)Blocks.BIRCH_SAPLING;

    public BirchGen(DependencyInjector di) {
        super(di, Infinibiome.MOD_ID + ":birch");

        config = this.<TreeFeatureConfig>initConfig()
            .setFeature(
                Feature.NORMAL_TREE
            )
            .setConfigFunc(
                (pos, height, random) -> new TreeFeatureConfig.Builder(
                    new SimpleBlockStateProvider(LOG),
                    new SimpleBlockStateProvider(LEAF),
                    new BlobFoliagePlacer(2, 0)
                )
                .baseHeight(height)
                .trunkHeight(height-3)
                .foliageHeight(3)
                .ignoreVines()
                .setSapling(
                    SAPLING
                )
                .build()
            )
            .setHeightFunc(TreeHelper.SMALL_TREE_HEIGHT)
            .setIsolationRadius(TreeHelper.COMMON_ISOLATION_RADIUS)
            .growInAnySaplingConfig()
            .setDensity(
                TreeHelper.initCommonDensity(seed).skew(
                    FloatFunc.constFunc(-2)
                )
            )
            .setRegionRate(
                TreeHelper.COMMON_REGION_RATE / 2
            )
            .noExtraConditions();
    }
}
