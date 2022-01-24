package weightedgpa.infinibiome.internal.generators.interchunks.ore;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
//import net.minecraftforge.event.terraingen.OreGenEvent;
import weightedgpa.infinibiome.api.Infinibiome;
import weightedgpa.infinibiome.api.dependency.DependencyInjector;

public final class RedstoneOreGen extends OreGenBase {
    public RedstoneOreGen(DependencyInjector di) {
        super(di, Infinibiome.MOD_ID + ":redstoneOre", Config.class);
    }

    @Override
    BlockState getBlock() {
        return Blocks.REDSTONE_ORE.getDefaultState();
    }


    public static class Config extends OreConfig {
        public Config(DependencyInjector di){
            super(di);
        }

        @Override
        String getName() {
            return "redstone";
        }

        @Override
        double getDefaultRatePerChunk() {
            return 8;
        }

        @Override
        int getDefaultMaxHeight() {
            return 16;
        }

        @Override
        int getDefaultOreCount() {
            return 8;
        }
    }
}
