package weightedgpa.infinibiome.internal.generators.interchunks.ore;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
//import net.minecraftforge.event.terraingen.OreGenEvent;
import weightedgpa.infinibiome.api.Infinibiome;
import weightedgpa.infinibiome.api.dependency.DependencyInjector;

public final class LapisOreGen extends OreGenBase {
    public LapisOreGen(DependencyInjector di) {
        super(di, Infinibiome.MOD_ID + ":lapisOre", Config.class);

    }

    @Override
    BlockState getBlock() {
        return Blocks.LAPIS_ORE.getDefaultState();
    }

    public static class Config extends OreConfig {
        public Config(DependencyInjector di){
            super(di);
        }

        @Override
        String getName() {
            return "lapis";
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
            return 8;
        }
    }
}
