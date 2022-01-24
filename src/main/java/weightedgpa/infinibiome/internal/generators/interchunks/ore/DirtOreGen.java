package weightedgpa.infinibiome.internal.generators.interchunks.ore;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
//import net.minecraftforge.event.terraingen.OreGenEvent;
import weightedgpa.infinibiome.api.Infinibiome;
import weightedgpa.infinibiome.api.dependency.DependencyInjector;

public final class DirtOreGen extends OreGenBase {
    public DirtOreGen(DependencyInjector di) {
        super(di, Infinibiome.MOD_ID + ":dirtOre", Config.class);
    }

    @Override
    BlockState getBlock() {
        return Blocks.DIRT.getDefaultState();
    }



    public static class Config extends OreConfig {
        public Config(DependencyInjector di){
            super(di);
        }

        @Override
        String getName() {
            return "dirt";
        }

        @Override
        double getDefaultRatePerChunk() {
            return 3;
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
