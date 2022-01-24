package weightedgpa.infinibiome.internal.generators.interchunks.ore;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import weightedgpa.infinibiome.api.Infinibiome;
import weightedgpa.infinibiome.api.dependency.DependencyInjector;

public final class AndesiteOreGen extends OreGenBase {
    public AndesiteOreGen(DependencyInjector di) {
        super(di, Infinibiome.MOD_ID + ":andesiteOre", Config.class);
    }

    @Override
    BlockState getBlock() {
        return Blocks.ANDESITE.getDefaultState();
    }

    public static class Config extends OreConfig {
        public Config(DependencyInjector di){
            super(di);
        }

        @Override
        String getName() {
            return "andesite";
        }

        @Override
        double getDefaultRatePerChunk() {
            return 2;
        }

        @Override
        int getDefaultMaxHeight() {
            return 128;
        }

        @Override
        int getDefaultOreCount() {
            return 40;
        }
    }
}
