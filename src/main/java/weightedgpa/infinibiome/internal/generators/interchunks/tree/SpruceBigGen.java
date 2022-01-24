package weightedgpa.infinibiome.internal.generators.interchunks.tree;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MutableBoundingBox;
import net.minecraft.world.gen.IWorldGenerationReader;
import net.minecraft.world.gen.blockstateprovider.SimpleBlockStateProvider;
import net.minecraft.world.gen.feature.*;
import net.minecraftforge.common.IPlantable;
import weightedgpa.infinibiome.api.Infinibiome;
import weightedgpa.infinibiome.internal.floatfunc.FloatFunc;
import weightedgpa.infinibiome.api.dependency.DependencyInjector;
import weightedgpa.infinibiome.internal.generators.utils.GenHelper;
import weightedgpa.infinibiome.internal.misc.MathHelper;
import weightedgpa.infinibiome.api.pos.BlockPos2D;
import weightedgpa.infinibiome.internal.generators.utils.condition.ConditionHelper;

import java.util.Random;
import java.util.Set;

public final class SpruceBigGen extends TreeGenBase {
    private static final BlockState LOG = Blocks.SPRUCE_LOG.getDefaultState();
    private static final BlockState LEAF = Blocks.SPRUCE_LEAVES.getDefaultState();
    private static final IPlantable SAPLING = (IPlantable) Blocks.SPRUCE_SAPLING;
    //private static final BlockState PODZOL = Blocks.DIRT.getDefaultState().withProperty(BlockDirt.VARIANT, BlockDirt.DirtType.PODZOL);

    private final FloatFunc<BlockPos2D> ratioFunc;

    public SpruceBigGen(DependencyInjector di) {
        super(di, Infinibiome.MOD_ID + ":spruceBig");

        config = this.<HugeTreeFeatureConfig>initConfig()
            .setFeature(
                new MegaPineTreeFixed()
            )
            .setConfigFunc(
                this::getConfig
            )
            .setHeightFunc(15, 45)
            .setIsolationRadius(TreeHelper.COMMON_ISOLATION_RADIUS)
            .onlyGrowIn2x2Config()
            .setWithCommonDensity()
            .setRegionRate(
                TreeHelper.COMMON_REGION_RATE / 2f
            )
            .addExtraConditions(
                ConditionHelper.onlyInTemperature(
                    di,
                    GenHelper.COLDISH
                )
            );

        this.ratioFunc = TreeHelper.initSpruceRatioNoise(seed);
    }

    private HugeTreeFeatureConfig getConfig(BlockPos2D pos, int height, Random random) {
        double ratio = ratioFunc.getOutput(pos);

        if (MathHelper.randomBool(ratio, random)){
            return new HugeTreeFeatureConfig.Builder(
                new SimpleBlockStateProvider(LOG),
                new SimpleBlockStateProvider(LEAF)
            )
            .baseHeight(height+1)
            //.func_227283_b_(15)
            .crownHeight(3)
            .setSapling(
                (IPlantable) Blocks.SPRUCE_SAPLING
            )
            .build();
        }

        return new HugeTreeFeatureConfig.Builder(
                new SimpleBlockStateProvider(LOG),
                new SimpleBlockStateProvider(LEAF)
            )
            .baseHeight(height+1)
            .crownHeight(12)
            .setSapling(
                SAPLING
            )
            .build();

    }

    //fixed for better trunk height control
    @SuppressWarnings("all")
    public class MegaPineTreeFixed extends HugeTreesFeature<HugeTreeFeatureConfig> {
        public MegaPineTreeFixed() {
            super(HugeTreeFeatureConfig::deserializeSpruce);
        }

        /**
         * Called when placing the tree feature.
         */
        @Override
        public boolean place(IWorldGenerationReader generationReader, Random rand, BlockPos positionIn, Set<BlockPos> p_225557_4_, Set<BlockPos> p_225557_5_, MutableBoundingBox boundingBoxIn, HugeTreeFeatureConfig configIn) {
            int i = this.func_227256_a_(rand, configIn);
            if (!this.hasRoom(generationReader, positionIn, i, configIn)) {
                return false;
            } else {
                this.func_227253_a_(generationReader, rand, positionIn.getX(), positionIn.getZ(), positionIn.getY() + i, 0, p_225557_5_, boundingBoxIn, configIn);
                this.func_227254_a_(generationReader, rand, positionIn, i, p_225557_4_, boundingBoxIn, configIn);
                return true;
            }
        }

        private void func_227253_a_(IWorldGenerationReader p_227253_1_, Random p_227253_2_, int p_227253_3_, int p_227253_4_, int p_227253_5_, int p_227253_6_, Set<BlockPos> p_227253_7_, MutableBoundingBox p_227253_8_, HugeTreeFeatureConfig p_227253_9_) {
            int i = p_227253_2_.nextInt(5) + p_227253_9_.crownHeight;
            int j = 0;

            for(int k = p_227253_5_ - i; k <= p_227253_5_; ++k) {
                int l = p_227253_5_ - k;
                int i1 = p_227253_6_ + net.minecraft.util.math.MathHelper.floor((float)l / (float)i * 3.5F);
                this.func_227255_a_(p_227253_1_, p_227253_2_, new BlockPos(p_227253_3_, k, p_227253_4_), i1 + (l > 0 && i1 == j && (k & 1) == 0 ? 1 : 0), p_227253_7_, p_227253_8_, p_227253_9_);
                j = i1;
            }

        }

        @Override
        protected int func_227256_a_(Random p_227256_1_, HugeTreeFeatureConfig p_227256_2_) {
            int i = p_227256_2_.baseHeight;
            if (p_227256_2_.heightInterval > 1) {
                i += p_227256_1_.nextInt(p_227256_2_.heightInterval);
            }

            return i;
        }
    }
}
