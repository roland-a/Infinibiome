package weightedgpa.infinibiome.internal.generators.interchunks.ore;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
//import net.minecraftforge.event.terraingen.OreGenEvent;
import weightedgpa.infinibiome.api.Infinibiome;
import weightedgpa.infinibiome.api.dependency.DependencyInjector;

public final class EmeraldOreGen extends OreGenBase {
    public EmeraldOreGen(DependencyInjector di) {
        super(di, Infinibiome.MOD_ID + ":emeraldOre", Config.class);
    }

    @Override
    BlockState getBlock() {
        return Blocks.EMERALD_ORE.getDefaultState();
    }



    public static class Config extends OreConfig {
        public Config(DependencyInjector di){
            super(di);
        }

        @Override
        String getName() {
            return "emerald";
        }

        @Override
        double getDefaultRatePerChunk() {
            return 1;
        }

        @Override
        int getDefaultMaxHeight() {
            return 32;
        }

        @Override
        int getDefaultOreCount() {
            return 1;
        }
    }
}
