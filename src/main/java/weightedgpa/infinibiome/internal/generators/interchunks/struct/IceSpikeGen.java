package weightedgpa.infinibiome.internal.generators.interchunks.struct;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.IWorld;
import net.minecraft.world.gen.ChunkGenerator;
import net.minecraft.world.gen.GenerationSettings;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.NoFeatureConfig;
import net.minecraftforge.common.Tags;
import weightedgpa.infinibiome.api.Infinibiome;
import weightedgpa.infinibiome.api.dependency.DependencyInjector;
import weightedgpa.infinibiome.api.generators.nonworldgen.Locatable;
import weightedgpa.infinibiome.api.pos.BlockPos2D;
import weightedgpa.infinibiome.api.posdata.PosDataHelper;
import weightedgpa.infinibiome.internal.floatfunc.util.Interval;
import weightedgpa.infinibiome.internal.generators.interchunks.SmallObjectGenBase;
import weightedgpa.infinibiome.internal.misc.Helper;
import weightedgpa.infinibiome.internal.misc.MCHelper;

import java.util.Random;

import static weightedgpa.infinibiome.internal.generators.utils.condition.ConditionHelper.onlyInRegion;
import static weightedgpa.infinibiome.internal.generators.utils.condition.ConditionHelper.onlyInTemperature;

public final class IceSpikeGen extends SmallObjectGenBase implements Locatable.HasPointsProvider {
    public IceSpikeGen(DependencyInjector di) {
        super(di, Infinibiome.MOD_ID + ":iceSpikes");

        config = initConfig()
            .setWithFeature(
                new FixedIceSpikeFeature()
            )
            .setCount(
                Helper.<BlockPos2D>initUniformNoise(seed.newSeed("count"), Helper.COMMON_SCALE)
                    .mapInterval(
                        new Interval(0, 4)
                    )
            )
            .setAttemptsPerCount(4)
            .aboveHighestTerrainBlock()
            .onlyInRegion(0.1f)
            .addExtraConditions(
                onlyInTemperature(
                    di,
                    PosDataHelper.FREEZE_INTERVAL
                )
            );
    }

    //modified so it can generate properly on normal land and underwater
    @SuppressWarnings("all")
    private static class FixedIceSpikeFeature extends Feature<NoFeatureConfig> {
        FixedIceSpikeFeature() {
            super(NoFeatureConfig::deserialize);
        }

        public boolean place(IWorld p_212245_1_, ChunkGenerator<? extends GenerationSettings> p_212245_2_, Random p_212245_3_, BlockPos p_212245_4_, NoFeatureConfig p_212245_5_) {
            while(p_212245_1_.isAirBlock(p_212245_4_) && p_212245_4_.getY() > 2) {
                p_212245_4_ = p_212245_4_.down();
            }

            p_212245_4_ = p_212245_4_.up(p_212245_3_.nextInt(4));
            int i = p_212245_3_.nextInt(4) + 7;
            int j = i / 4 + p_212245_3_.nextInt(2);
            if (j > 1 && p_212245_3_.nextInt(60) == 0) {
                p_212245_4_ = p_212245_4_.up(10 + p_212245_3_.nextInt(30));
            }

            int k;
            int l;
            for(k = 0; k < i; ++k) {
                double f = (1.0F - (double)k / (double)i) * (double)j;
                l = MathHelper.ceil(f);

                for(int i1 = -l; i1 <= l; ++i1) {
                    double f1 = (double)MathHelper.abs(i1) - 0.25F;

                    for(int j1 = -l; j1 <= l; ++j1) {
                        double f2 = (double)MathHelper.abs(j1) - 0.25F;
                        if ((i1 == 0 && j1 == 0 || f1 * f1 + f2 * f2 <= f * f) && (i1 != -l && i1 != l && j1 != -l && j1 != l || p_212245_3_.nextFloat() <= 0.75F)) {
                            BlockState blockstate = p_212245_1_.getBlockState(p_212245_4_.add(i1, k, j1));
                            //Block block = blockstate.getBlock();
                            if (isValid(blockstate)) {
                                this.setBlockState(p_212245_1_, p_212245_4_.add(i1, k, j1), Blocks.PACKED_ICE.getDefaultState());
                            }

                            if (k != 0 && l > 1) {
                                blockstate = p_212245_1_.getBlockState(p_212245_4_.add(i1, -k, j1));
                                //block = blockstate.getBlock();
                                if (isValid(blockstate)) {
                                    this.setBlockState(p_212245_1_, p_212245_4_.add(i1, -k, j1), Blocks.PACKED_ICE.getDefaultState());
                                }
                            }
                        }
                    }
                }
            }

            k = j - 1;
            if (k < 0) {
                k = 0;
            } else if (k > 1) {
                k = 1;
            }

            for(int l1 = -k; l1 <= k; ++l1) {
                for(l = -k; l <= k; ++l) {
                    BlockPos blockpos = p_212245_4_.add(l1, -1, l);
                    int j2 = 50;
                    if (Math.abs(l1) == 1 && Math.abs(l) == 1) {
                        j2 = p_212245_3_.nextInt(5);
                    }

                    while(blockpos.getY() > 50) {
                        BlockState blockstate1 = p_212245_1_.getBlockState(blockpos);
                        //Block block1 = blockstate1.getBlock();
                        if (!isValid(blockstate1)) {
                            break;
                        }

                        this.setBlockState(p_212245_1_, blockpos, Blocks.PACKED_ICE.getDefaultState());
                        blockpos = blockpos.down();
                        --j2;
                        if (j2 <= 0) {
                            blockpos = blockpos.down(p_212245_3_.nextInt(5) + 1);
                            j2 = p_212245_3_.nextInt(5);
                        }
                    }
                }
            }

            return true;
        }

        private boolean isValid(BlockState block){
            if (MCHelper.isMostlyAir(block)) return true;

            if (MCHelper.isMostlyWater(block)) return true;

            if (Tags.Blocks.DIRT.contains(block.getBlock())) return true;

            if (Tags.Blocks.SAND.contains(block.getBlock())) return true;

            if (Tags.Blocks.GRAVEL.contains(block.getBlock())) return true;

            if (BlockTags.ICE.contains(block.getBlock())) return true;

            if (block.getBlock().equals(Blocks.SNOW_BLOCK)) return true;

            return false;
        }
    }
}
