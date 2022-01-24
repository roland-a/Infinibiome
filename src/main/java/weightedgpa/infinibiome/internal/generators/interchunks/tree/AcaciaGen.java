package weightedgpa.infinibiome.internal.generators.interchunks.tree;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.world.gen.blockstateprovider.SimpleBlockStateProvider;
import net.minecraft.world.gen.feature.*;
import net.minecraft.world.gen.foliageplacer.AcaciaFoliagePlacer;
import net.minecraftforge.common.IPlantable;
import weightedgpa.infinibiome.api.Infinibiome;
import weightedgpa.infinibiome.api.dependency.DependencyInjector;
import weightedgpa.infinibiome.api.posdata.PosDataHelper;
import weightedgpa.infinibiome.internal.generators.utils.GenHelper;

import static weightedgpa.infinibiome.internal.generators.utils.condition.ConditionHelper.*;

public final class AcaciaGen extends TreeGenBase {
    private static final BlockState LOG = Blocks.ACACIA_LOG.getDefaultState();
    private static final BlockState LEAF = Blocks.ACACIA_LEAVES.getDefaultState();
    private static final IPlantable SAPLING = (IPlantable) Blocks.ACACIA_SAPLING;

    public AcaciaGen(DependencyInjector di) {
        super(di, Infinibiome.MOD_ID + ":acacia");

        config = this.<TreeFeatureConfig>initConfig()
            .setFeature(
                Feature.ACACIA_TREE
            )
            .setConfigFunc(
                (pos, __, random) -> new TreeFeatureConfig.Builder(
                    new SimpleBlockStateProvider(LOG),
                    new SimpleBlockStateProvider(LEAF),
                    new AcaciaFoliagePlacer(2, 0)
                )
                .baseHeight(5)
                .heightRandA(2)
                .heightRandB(2)
                .trunkHeight(0)
                .ignoreVines()
                .setSapling(
                    SAPLING
                )
                .build()
            )
            .setHeightFunc(0, 0)
            .setIsolationRadius(TreeHelper.COMMON_ISOLATION_RADIUS)
            .growInAnySaplingConfig()
            .setWithCommonDensity()
            .setRegionRate(
                TreeHelper.COMMON_REGION_RATE * 3
            )
            .addExtraConditions(
                onlyInTemperature(
                    di,
                    PosDataHelper.HOT_INTERVAL
                ),
                onlyInHumidity(
                    di,
                    GenHelper.DRYISH
                )
            );
    }
}
