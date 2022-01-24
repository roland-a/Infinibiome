package weightedgpa.infinibiome.internal.generators.interchunks.ore;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
//import net.minecraftforge.event.terraingen.OreGenEvent;
import weightedgpa.infinibiome.api.Infinibiome;
import weightedgpa.infinibiome.api.dependency.DependencyInjector;

public final class GravelOreGen extends OreGenBase {
    public GravelOreGen(DependencyInjector di) {
        super(di, Infinibiome.MOD_ID + ":gravelOre", Config.class);
    }

    @Override
    BlockState getBlock() {
        return Blocks.GRAVEL.getDefaultState();
    }

    public static class Config extends OreConfig {
        public Config(DependencyInjector di){
            super(di);
        }

        @Override
        String getName() {
            return "gravel";
        }

        @Override
        double getDefaultRatePerChunk() {
            return 4;
        }

        @Override
        int getDefaultMaxHeight() {
            return 128;
        }

        @Override
        int getDefaultOreCount() {
            return 30;
        }
    }
}
