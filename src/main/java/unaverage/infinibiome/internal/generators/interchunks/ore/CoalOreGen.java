package weightedgpa.infinibiome.internal.generators.interchunks.ore;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
//import net.minecraftforge.event.terraingen.OreGenEvent;
import weightedgpa.infinibiome.api.Infinibiome;
import weightedgpa.infinibiome.api.dependency.DependencyInjector;

public final class CoalOreGen extends OreGenBase {
    public CoalOreGen(DependencyInjector di) {
        super(di, Infinibiome.MOD_ID + ":coalOre", Config.class);
    }

    @Override
    BlockState getBlock() {
        return Blocks.COAL_ORE.getDefaultState();
    }



    @Override
    boolean canSpawnOnSurface() {
        return true;
    }

    public static class Config extends OreConfig {
        public Config(DependencyInjector di){
            super(di);
        }

        @Override
        String getName() {
            return "coal";
        }

        @Override
        double getDefaultRatePerChunk() {
            return 12;
        }

        @Override
        int getDefaultMaxHeight() {
            return 128;
        }

        @Override
        int getDefaultOreCount() {
            return 16;
        }
    }
}
