package weightedgpa.infinibiome.internal.generators.interchunks.ore;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import weightedgpa.infinibiome.api.Infinibiome;
import weightedgpa.infinibiome.api.dependency.DependencyInjector;

public final class ClayOreGen extends OreGenBase {
    public ClayOreGen(DependencyInjector di) {
        super(di, Infinibiome.MOD_ID + ":clayOre", Config.class);
    }

    @Override
    BlockState getBlock() {
        return Blocks.CLAY.getDefaultState();
    }

    public static class Config extends OreConfig {
        public Config(DependencyInjector di){
            super(di);
        }

        @Override
        String getName() {
            return "clay";
        }

        @Override
        double getDefaultRatePerChunk() {
            return 4;
        }

        @Override
        int getDefaultMaxHeight() {
            return 64+16;
        }

        @Override
        int getDefaultMinHeight() {
            return 64-16;
        }

        @Override
        int getDefaultOreCount() {
            return 16;
        }
    }
}
