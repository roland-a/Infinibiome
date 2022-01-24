package weightedgpa.infinibiome.internal.generators.interchunks.tree;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.world.gen.blockstateprovider.SimpleBlockStateProvider;
import net.minecraft.world.gen.feature.*;
import net.minecraft.world.gen.foliageplacer.BlobFoliagePlacer;
import net.minecraftforge.common.IPlantable;
import weightedgpa.infinibiome.api.Infinibiome;
import weightedgpa.infinibiome.api.dependency.DependencyInjector;
import weightedgpa.infinibiome.api.pos.BlockPos2D;
import weightedgpa.infinibiome.api.pos.InterChunkPos;

import static weightedgpa.infinibiome.internal.generators.utils.condition.ConditionHelper.onlyInRegion;

public final class OakSmallGen extends TreeGenBase {
    private static final BlockState LOG = Blocks.OAK_LOG.getDefaultState();
    private static final BlockState LEAF = Blocks.OAK_LEAVES.getDefaultState();
    private static final IPlantable SAPLING = (IPlantable) Blocks.OAK_SAPLING;

    public OakSmallGen(DependencyInjector di){
        super(di, Infinibiome.MOD_ID + ":oakSmall");

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
                .trunkHeight(height - 3)
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
            .setWithCommonDensity()
            .setRegionRate(
                TreeHelper.COMMON_REGION_RATE
            )
            .noExtraConditions();
    }

    @Override
    public double getPlantControllerWeight(BlockPos2D pos) {
        if (growthConfig.allowSaplingUniformGrowth){
            return 9/10d;
        }

        return getDensity(new InterChunkPos(pos));
    }
}
