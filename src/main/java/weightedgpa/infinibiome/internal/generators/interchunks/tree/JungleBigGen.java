package weightedgpa.infinibiome.internal.generators.interchunks.tree;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.MutableBoundingBox;
import net.minecraft.world.gen.IWorldGenerationReader;
import net.minecraft.world.gen.blockstateprovider.SimpleBlockStateProvider;
import net.minecraft.world.gen.feature.BaseTreeFeatureConfig;
import net.minecraft.world.gen.feature.HugeTreeFeatureConfig;
import net.minecraft.world.gen.feature.HugeTreesFeature;
import net.minecraftforge.common.IPlantable;
import weightedgpa.infinibiome.api.Infinibiome;
import weightedgpa.infinibiome.api.dependency.DependencyInjector;
import weightedgpa.infinibiome.api.posdata.PosDataHelper;

import java.util.Random;
import java.util.Set;

import static weightedgpa.infinibiome.internal.generators.utils.condition.ConditionHelper.onlyInHumidity;
import static weightedgpa.infinibiome.internal.generators.utils.condition.ConditionHelper.onlyInTemperature;

public final class JungleBigGen extends TreeGenBase {
    private static final BlockState LOG = Blocks.JUNGLE_LOG.getDefaultState();

    private static final BlockState LEAF = Blocks.JUNGLE_LEAVES.getDefaultState();
    private static final IPlantable SAPLING = (IPlantable) Blocks.JUNGLE_SAPLING;

    public JungleBigGen(DependencyInjector di) {
        super(di, Infinibiome.MOD_ID + ":jungleBig");

        //.func_227283_b_(20)
        config = this.<HugeTreeFeatureConfig>initConfig()
            .setFeature(
                new MegaJungleFeatureFixed()
            )
            .setConfigFunc(
                (pos, height, random) -> new HugeTreeFeatureConfig.Builder(
                    new SimpleBlockStateProvider(LOG),
                    new SimpleBlockStateProvider(LEAF)
                )
                .baseHeight(height+1)
                //.func_227283_b_(20)
                .setSapling(
                    SAPLING
                )
                .build()
            )
            .setHeightFunc(15, 45)
            .setIsolationRadius(TreeHelper.COMMON_ISOLATION_RADIUS)
            .onlyGrowIn2x2Config()
            .setWithCommonDensity()
            .setRegionRate(
                TreeHelper.COMMON_REGION_RATE * 2
            )
            .addExtraConditions(
                onlyInTemperature(
                    di,
                    PosDataHelper.HOT_INTERVAL
                ),
                onlyInHumidity(
                    di,
                    PosDataHelper.WET_INTERVAL
                )
            );
    }

    @SuppressWarnings("all")
    //fixed for better height control
    private static class MegaJungleFeatureFixed extends HugeTreesFeature<HugeTreeFeatureConfig> {
        MegaJungleFeatureFixed() {
            super(HugeTreeFeatureConfig::deserializeJungle);
        }

        /**
         * Called when placing the tree feature.
         */
        public boolean place(IWorldGenerationReader generationReader, Random rand, BlockPos positionIn, Set<BlockPos> p_225557_4_, Set<BlockPos> p_225557_5_, MutableBoundingBox boundingBoxIn, HugeTreeFeatureConfig configIn) {
            int i = this.func_227256_a_(rand, configIn);
            if (!this.hasRoom(generationReader, positionIn, i, configIn)) {
                return false;
            } else {
                this.func_227252_c_(generationReader, rand, positionIn.up(i), 2, p_225557_5_, boundingBoxIn, configIn);

                for(int j = positionIn.getY() + i - 2 - rand.nextInt(4); j > positionIn.getY() + i / 2; j -= 2 + rand.nextInt(4)) {
                    float f = rand.nextFloat() * ((float)Math.PI * 2F);
                    int k = positionIn.getX() + (int)(0.5F + MathHelper.cos(f) * 4.0F);
                    int l = positionIn.getZ() + (int)(0.5F + MathHelper.sin(f) * 4.0F);

                    for(int i1 = 0; i1 < 5; ++i1) {
                        k = positionIn.getX() + (int)(1.5F + MathHelper.cos(f) * (float)i1);
                        l = positionIn.getZ() + (int)(1.5F + MathHelper.sin(f) * (float)i1);
                        BlockPos blockpos = new BlockPos(k, j - 3 + i1 / 2, l);
                        this.setLog(generationReader, rand, blockpos, p_225557_4_, boundingBoxIn, configIn);
                    }

                    int l1 = 1 + rand.nextInt(2);
                    int i2 = j;

                    for(int j1 = j - l1; j1 <= i2; ++j1) {
                        int k1 = j1 - i2;
                        this.func_227257_b_(generationReader, rand, new BlockPos(k, j1, l), 1 - k1, p_225557_5_, boundingBoxIn, configIn);
                    }
                }

                this.func_227254_a_(generationReader, rand, positionIn, i, p_225557_4_, boundingBoxIn, configIn);
                return true;
            }
        }

        private void func_227252_c_(IWorldGenerationReader p_227252_1_, Random p_227252_2_, BlockPos p_227252_3_, int p_227252_4_, Set<BlockPos> p_227252_5_, MutableBoundingBox p_227252_6_, BaseTreeFeatureConfig p_227252_7_) {
            int i = 2;

            for(int j = -2; j <= 0; ++j) {
                this.func_227255_a_(p_227252_1_, p_227252_2_, p_227252_3_.up(j), p_227252_4_ + 1 - j, p_227252_5_, p_227252_6_, p_227252_7_);
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
