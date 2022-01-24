package weightedgpa.infinibiome.internal.generators.interchunks.ore;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
//import net.minecraftforge.event.terraingen.OreGenEvent;
import weightedgpa.infinibiome.api.Infinibiome;
import weightedgpa.infinibiome.api.dependency.DependencyInjector;

public final class GoldOreGen extends OreGenBase {
    public GoldOreGen(DependencyInjector di) {
        super(di, Infinibiome.MOD_ID + ":goldOre", Config.class);
    }

    @Override
    BlockState getBlock() {
        return Blocks.GOLD_ORE.getDefaultState();
    }



    public static class Config extends OreConfig {
        public Config(DependencyInjector di){
            super(di);
        }

        @Override
        String getName() {
            return "gold";
        }

        @Override
        double getDefaultRatePerChunk() {
            return 2;
        }

        @Override
        int getDefaultMaxHeight() {
            return 32;
        }

        @Override
        int getDefaultOreCount() {
            return 8;
        }
    }
}
