package weightedgpa.infinibiome.internal.generators.interchunks.ore;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
//import net.minecraftforge.event.terraingen.OreGenEvent;
import weightedgpa.infinibiome.api.Infinibiome;
import weightedgpa.infinibiome.api.dependency.DependencyInjector;

public final class GraniteOreGen extends OreGenBase {
    public GraniteOreGen(DependencyInjector di) {
        super(di, Infinibiome.MOD_ID + ":graniteOre", Config.class);
    }

    @Override
    BlockState getBlock() {
        return Blocks.GRANITE.getDefaultState();
    }

    public static class Config extends OreConfig {
        public Config(DependencyInjector di){
            super(di);
        }

        @Override
        String getName() {
            return "granite";
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
