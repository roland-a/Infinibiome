package weightedgpa.infinibiome.internal.generators.interchunks.tree;

import com.google.common.collect.Lists;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.world.gen.blockstateprovider.SimpleBlockStateProvider;
import net.minecraft.world.gen.feature.*;
import net.minecraft.world.gen.foliageplacer.PineFoliagePlacer;
import net.minecraft.world.gen.foliageplacer.SpruceFoliagePlacer;
import net.minecraftforge.common.IPlantable;
import weightedgpa.infinibiome.api.Infinibiome;
import weightedgpa.infinibiome.api.dependency.DependencyInjector;
import weightedgpa.infinibiome.internal.generators.utils.condition.Condition;
import weightedgpa.infinibiome.internal.generators.utils.GenHelper;
import weightedgpa.infinibiome.internal.misc.MathHelper;
import weightedgpa.infinibiome.api.posdata.PosDataHelper;
import weightedgpa.infinibiome.api.pos.BlockPos2D;
import weightedgpa.infinibiome.internal.floatfunc.FloatFunc;
import weightedgpa.infinibiome.internal.generators.utils.condition.ConditionHelper;

import java.util.List;
import java.util.Random;
import java.util.function.Function;

public final class SpruceSmallGen extends TreeGenBase {
    private static final BlockState LOG = Blocks.SPRUCE_LOG.getDefaultState();
    private static final BlockState LEAF = Blocks.SPRUCE_LEAVES.getDefaultState();
    private static final IPlantable SAPLING = (IPlantable) Blocks.SPRUCE_SAPLING;

    private final FloatFunc<BlockPos2D> ratioFunc;
    private final Type type;

    public SpruceSmallGen(Type type, DependencyInjector di){
        super(di, Infinibiome.MOD_ID + ":spruceSmall" + type.name());

        this.type = type;

        config = this.<TreeFeatureConfig>initConfig()
            .setFeature(
                Feature.NORMAL_TREE
            )
            .setConfigFunc(
                this::getConfig
            )
            .setHeightFunc(6, 12)
            .setIsolationRadius(TreeHelper.COMMON_ISOLATION_RADIUS)
            .onlyGrowIn1x1Config()
            .setWithCommonDensity()
            .setRegionRate(
                type.regionRate
            )
            .addExtraConditions(
                type.conditions.apply(di)
            );

        this.ratioFunc = TreeHelper.initSpruceRatioNoise(seed);
    }

    TreeFeatureConfig getConfig(BlockPos2D pos, int height, Random random) {
        double ratio = this.ratioFunc.getOutput(pos);

        boolean isPine = MathHelper.randomBool(ratio, random);

        if (isPine){
            return new TreeFeatureConfig.Builder(
                new SimpleBlockStateProvider(LOG),
                new SimpleBlockStateProvider(LEAF),
                new PineFoliagePlacer(1, 0)
            )
            .baseHeight(height+1)
            .trunkTopOffset(1)
            .foliageHeight(3)
            .ignoreVines()
            .setSapling(
                (IPlantable)Blocks.SPRUCE_SAPLING
            )
            .build();
        }

        return new TreeFeatureConfig.Builder(
            new SimpleBlockStateProvider(LOG),
            new SimpleBlockStateProvider(LEAF),
            new SpruceFoliagePlacer(2, 1)
        )
        .baseHeight(height)
        .trunkHeight(1)
        .ignoreVines()
        .setSapling(
            SAPLING
        )
        .build();
    }

    @Override
    public String toString() {
        return "SpruceSmallGen{" +
            "type=" + type +
            '}';
    }

    public enum Type {
        NORMAL(
            TreeHelper.COMMON_REGION_RATE,
            di -> Lists.newArrayList(
                ConditionHelper.onlyInTemperature(
                    di,
                    GenHelper.COLDISH
                )
            )
        ),
        HOT(
            TreeHelper.COMMON_REGION_RATE / 3,
            di -> Lists.newArrayList(
                ConditionHelper.onlyInTemperature(
                    di,
                    PosDataHelper.HOT_INTERVAL
                ),
                ConditionHelper.onlyInHumidity(
                    di,
                    GenHelper.DRYISH
                )
            )
        );

        final double regionRate;
        final Function<DependencyInjector, List<Condition>> conditions;

        Type(double regionRate, Function<DependencyInjector, List<Condition>> conditions) {
            this.regionRate = regionRate;
            this.conditions = conditions;
        }
    }
}




