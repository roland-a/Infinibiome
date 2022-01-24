package weightedgpa.infinibiome.internal.generators.interchunks.ore;

import com.mojang.datafixers.Dynamic;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorld;
import net.minecraft.world.gen.ChunkGenerator;
import net.minecraft.world.gen.GenerationSettings;
import net.minecraft.world.gen.Heightmap;
import net.minecraft.world.gen.feature.ConfiguredFeature;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.OreFeatureConfig;
import net.minecraftforge.common.util.Lazy;
import weightedgpa.infinibiome.api.dependency.DependencyInjector;
import weightedgpa.infinibiome.api.generators.InterChunkGen;
import weightedgpa.infinibiome.api.generators.InterChunkGenTimings;
import weightedgpa.infinibiome.api.generators.Timing;
import weightedgpa.infinibiome.api.pos.BlockPos2D;
import weightedgpa.infinibiome.api.pos.InterChunkPos;
import weightedgpa.infinibiome.internal.generators.utils.GeneratorBase;
import weightedgpa.infinibiome.internal.misc.MCHelper;
import weightedgpa.infinibiome.internal.misc.MathHelper;

import java.util.BitSet;
import java.util.Random;
import java.util.function.Function;

abstract class OreGenBase extends GeneratorBase implements InterChunkGen {
    //final boolean canSpawnEveryWhere;

    private final OreConfig config;
    private final Lazy<ConfiguredFeature<OreFeatureConfig, ?>> feature;

    OreGenBase(DependencyInjector di, String seedBranch, Class<? extends OreConfig> configClass) {
        super(di, seedBranch);

        config = di.getAll(configClass).get(0);

        feature = Lazy.of(() -> new FixedOreFeature(OreFeatureConfig::deserialize).withConfiguration(
            new OreFeatureConfig(
                OreFeatureConfig.FillerBlockType.NATURAL_STONE,
                getBlock(),
                config.oreCount
            )
        ));
    }

    abstract BlockState getBlock();

    boolean canSpawnOnSurface(){
        return false;
    }

    @Override
    public final Timing getInterChunkTiming() {
        return InterChunkGenTimings.ORES;
    }

    @Override
    public void generate(InterChunkPos interChunkPos, IWorld interChunks) {
        Random random = randomGen.getRandom(interChunkPos.getX(), interChunkPos.getZ());

        double avgCount = config.ratePerChunk;

        int count = MathHelper.randomRound(avgCount, random);

        for (int i = 0; i < count; i++){
            BlockPos2D pos2D = interChunkPos.getRandomCenterPos(random);

            int height = MathHelper.randomInt(
                config.minHeight,
                config.maxHeight,
                random
            );

            feature.get().place(
                interChunks,
                chunkGenerator,
                random,
                pos2D.to3D(height)
            );
        }
    }

    private boolean isOnSurface(BlockPos pos, IWorld world){
        if (pos.getY() < 0){
            return false;
        }
        if (pos.getY() > 255){
            return false;
        }

        int surfaceHeight = MCHelper.getHighestTerrainHeight(
            MCHelper.to2D(pos),
            world
        );

        return pos.getY() == surfaceHeight;
    }

   
    @SuppressWarnings({"LocalVariableNamingConvention", "PointlessArithmeticExpression", "MethodWithTooManyParameters", "MethodParameterNamingConvention", "OverlyComplexMethod", "OverlyLongMethod", "OverlyNestedMethod"})
    private class FixedOreFeature extends Feature<OreFeatureConfig> {
        FixedOreFeature(Function<Dynamic<?>, ? extends OreFeatureConfig> p_i51472_1_) {
            super(p_i51472_1_);
        }

        @Override
        public boolean place(IWorld world, ChunkGenerator<? extends GenerationSettings> chunkGenerator, Random rand, BlockPos pos, OreFeatureConfig config) {
            double lvt_6_1_ = rand.nextDouble() * 3.1415927;
            double lvt_7_1_ = config.size / 8.0;
            int lvt_8_1_ = MathHelper.ceil((config.size / 16.0 * 2.0 + 1.0) / 2.0);
            double lvt_9_1_ = (pos.getX() + Math.sin(lvt_6_1_) * lvt_7_1_);
            double lvt_11_1_ = (pos.getX() - Math.sin(lvt_6_1_) * lvt_7_1_);
            double lvt_13_1_ = (pos.getZ() + Math.cos(lvt_6_1_) * lvt_7_1_);
            double lvt_15_1_ = (pos.getZ() - Math.cos(lvt_6_1_) * lvt_7_1_);
            double lvt_18_1_ = (pos.getY() + rand.nextInt(3) - 2);
            double lvt_20_1_ = (pos.getY() + rand.nextInt(3) - 2);
            int lvt_22_1_ = pos.getX() - MathHelper.ceil(lvt_7_1_) - lvt_8_1_;
            int lvt_23_1_ = pos.getY() - 2 - lvt_8_1_;
            int lvt_24_1_ = pos.getZ() - MathHelper.ceil(lvt_7_1_) - lvt_8_1_;
            int lvt_25_1_ = 2 * (MathHelper.ceil(lvt_7_1_) + lvt_8_1_);
            int lvt_26_1_ = 2 * (2 + lvt_8_1_);

            for(int lvt_27_1_ = lvt_22_1_; lvt_27_1_ <= lvt_22_1_ + lvt_25_1_; ++lvt_27_1_) {
                for(int lvt_28_1_ = lvt_24_1_; lvt_28_1_ <= lvt_24_1_ + lvt_25_1_; ++lvt_28_1_) {
                    if (lvt_23_1_ <= world.getHeight(Heightmap.Type.OCEAN_FLOOR_WG, lvt_27_1_, lvt_28_1_)) {
                        return this.func_207803_a(world, rand, config, lvt_9_1_, lvt_11_1_, lvt_13_1_, lvt_15_1_, lvt_18_1_, lvt_20_1_, lvt_22_1_, lvt_23_1_, lvt_24_1_, lvt_25_1_, lvt_26_1_);
                    }
                }
            }

            return false;
        }

        boolean func_207803_a(IWorld p_207803_1_, Random p_207803_2_, OreFeatureConfig p_207803_3_, double p_207803_4_, double p_207803_6_, double p_207803_8_, double p_207803_10_, double p_207803_12_, double p_207803_14_, int p_207803_16_, int p_207803_17_, int p_207803_18_, int p_207803_19_, int p_207803_20_) {
            int lvt_21_1_ = 0;
            BitSet lvt_22_1_ = new BitSet(p_207803_19_ * p_207803_20_ * p_207803_19_);
            BlockPos.Mutable lvt_23_1_ = new BlockPos.Mutable();
            double[] lvt_24_1_ = new double[p_207803_3_.size * 4];

            int lvt_25_2_;
            double lvt_27_2_;
            double lvt_29_2_;
            double lvt_31_2_;
            double lvt_33_2_;
            for(lvt_25_2_ = 0; lvt_25_2_ < p_207803_3_.size; ++lvt_25_2_) {
                double lvt_26_1_ = lvt_25_2_ / (double)p_207803_3_.size;
                lvt_27_2_ = net.minecraft.util.math.MathHelper.lerp(lvt_26_1_, p_207803_4_, p_207803_6_);
                lvt_29_2_ = net.minecraft.util.math.MathHelper.lerp(lvt_26_1_, p_207803_12_, p_207803_14_);
                lvt_31_2_ = net.minecraft.util.math.MathHelper.lerp(lvt_26_1_, p_207803_8_, p_207803_10_);
                lvt_33_2_ = p_207803_2_.nextDouble() * p_207803_3_.size / 16.0D;
                double lvt_35_1_ = ((Math.sin(3.1415927 * lvt_26_1_) + 1.0) * lvt_33_2_ + 1.0D) / 2.0D;
                lvt_24_1_[lvt_25_2_ * 4 + 0] = lvt_27_2_;
                lvt_24_1_[lvt_25_2_ * 4 + 1] = lvt_29_2_;
                lvt_24_1_[lvt_25_2_ * 4 + 2] = lvt_31_2_;
                lvt_24_1_[lvt_25_2_ * 4 + 3] = lvt_35_1_;
            }

            for(lvt_25_2_ = 0; lvt_25_2_ < p_207803_3_.size - 1; ++lvt_25_2_) {
                if (lvt_24_1_[lvt_25_2_ * 4 + 3] > 0.0D) {
                    for(int lvt_26_2_ = lvt_25_2_ + 1; lvt_26_2_ < p_207803_3_.size; ++lvt_26_2_) {
                        if (lvt_24_1_[lvt_26_2_ * 4 + 3] > 0.0D) {
                            lvt_27_2_ = lvt_24_1_[lvt_25_2_ * 4 + 0] - lvt_24_1_[lvt_26_2_ * 4 + 0];
                            lvt_29_2_ = lvt_24_1_[lvt_25_2_ * 4 + 1] - lvt_24_1_[lvt_26_2_ * 4 + 1];
                            lvt_31_2_ = lvt_24_1_[lvt_25_2_ * 4 + 2] - lvt_24_1_[lvt_26_2_ * 4 + 2];
                            lvt_33_2_ = lvt_24_1_[lvt_25_2_ * 4 + 3] - lvt_24_1_[lvt_26_2_ * 4 + 3];
                            if (lvt_33_2_ * lvt_33_2_ > lvt_27_2_ * lvt_27_2_ + lvt_29_2_ * lvt_29_2_ + lvt_31_2_ * lvt_31_2_) {
                                if (lvt_33_2_ > 0.0D) {
                                    lvt_24_1_[lvt_26_2_ * 4 + 3] = -1.0D;
                                } else {
                                    lvt_24_1_[lvt_25_2_ * 4 + 3] = -1.0D;
                                }
                            }
                        }
                    }
                }
            }

            for(lvt_25_2_ = 0; lvt_25_2_ < p_207803_3_.size; ++lvt_25_2_) {
                double lvt_26_3_ = lvt_24_1_[lvt_25_2_ * 4 + 3];
                if (lvt_26_3_ >= 0.0D) {
                    double lvt_28_1_ = lvt_24_1_[lvt_25_2_ * 4 + 0];
                    double lvt_30_1_ = lvt_24_1_[lvt_25_2_ * 4 + 1];
                    double lvt_32_1_ = lvt_24_1_[lvt_25_2_ * 4 + 2];
                    int lvt_34_1_ = Math.max(MathHelper.floor(lvt_28_1_ - lvt_26_3_), p_207803_16_);
                    int lvt_35_2_ = Math.max(MathHelper.floor(lvt_30_1_ - lvt_26_3_), p_207803_17_);
                    int lvt_36_1_ = Math.max(MathHelper.floor(lvt_32_1_ - lvt_26_3_), p_207803_18_);
                    int lvt_37_1_ = Math.max(MathHelper.floor(lvt_28_1_ + lvt_26_3_), lvt_34_1_);
                    int lvt_38_1_ = Math.max(MathHelper.floor(lvt_30_1_ + lvt_26_3_), lvt_35_2_);
                    int lvt_39_1_ = Math.max(MathHelper.floor(lvt_32_1_ + lvt_26_3_), lvt_36_1_);

                    for(int lvt_40_1_ = lvt_34_1_; lvt_40_1_ <= lvt_37_1_; ++lvt_40_1_) {
                        double lvt_41_1_ = (lvt_40_1_ + 0.5D - lvt_28_1_) / lvt_26_3_;
                        if (lvt_41_1_ * lvt_41_1_ < 1.0D) {
                            for(int lvt_43_1_ = lvt_35_2_; lvt_43_1_ <= lvt_38_1_; ++lvt_43_1_) {
                                double lvt_44_1_ = (lvt_43_1_ + 0.5D - lvt_30_1_) / lvt_26_3_;
                                if (lvt_41_1_ * lvt_41_1_ + lvt_44_1_ * lvt_44_1_ < 1.0D) {
                                    for(int lvt_46_1_ = lvt_36_1_; lvt_46_1_ <= lvt_39_1_; ++lvt_46_1_) {
                                        double lvt_47_1_ = (lvt_46_1_ + 0.5D - lvt_32_1_) / lvt_26_3_;
                                        if (lvt_41_1_ * lvt_41_1_ + lvt_44_1_ * lvt_44_1_ + lvt_47_1_ * lvt_47_1_ < 1.0D) {
                                            int lvt_49_1_ = lvt_40_1_ - p_207803_16_ + (lvt_43_1_ - p_207803_17_) * p_207803_19_ + (lvt_46_1_ - p_207803_18_) * p_207803_19_ * p_207803_20_;
                                            if (!lvt_22_1_.get(lvt_49_1_)) {
                                                lvt_22_1_.set(lvt_49_1_);
                                                lvt_23_1_.setPos(lvt_40_1_, lvt_43_1_, lvt_46_1_);
                                                if (p_207803_3_.target.getTargetBlockPredicate().test(p_207803_1_.getBlockState(lvt_23_1_))) {

                                                    //only modifiication
                                                    if (canSpawnOnSurface() || !isOnSurface(lvt_23_1_, p_207803_1_)){
                                                        p_207803_1_.setBlockState(lvt_23_1_, p_207803_3_.state, MCHelper.DEFAULT_FLAG);
                                                        ++lvt_21_1_;
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            return lvt_21_1_ > 0;
        }
    }
}
