package weightedgpa.infinibiome.internal.generators.interchunks.ore;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
//import net.minecraftforge.event.terraingen.OreGenEvent;
import weightedgpa.infinibiome.api.Infinibiome;
import weightedgpa.infinibiome.api.dependency.DependencyInjector;

public final class IronOreGen extends OreGenBase {
    public IronOreGen(DependencyInjector di) {
        super(di, Infinibiome.MOD_ID + ":ironOre", Config.class);
    }

    @Override
    BlockState getBlock() {
        return Blocks.IRON_ORE.getDefaultState();
    }



    public static class Config extends OreConfig {
        public Config(DependencyInjector di){
            super(di);
        }

        @Override
        String getName() {
            return "iron";
        }

        @Override
        double getDefaultRatePerChunk() {
            return 16;
        }

        @Override
        int getDefaultMaxHeight() {
            return 128;
        }

        @Override
        int getDefaultOreCount() {
            return 8;
        }
    }
}
