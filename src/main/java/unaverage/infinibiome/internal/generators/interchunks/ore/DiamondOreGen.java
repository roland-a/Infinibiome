package weightedgpa.infinibiome.internal.generators.interchunks.ore;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
//import net.minecraftforge.event.terraingen.OreGenEvent;
import weightedgpa.infinibiome.api.Infinibiome;
import weightedgpa.infinibiome.api.dependency.DependencyInjector;

public final class DiamondOreGen extends OreGenBase {
    public DiamondOreGen(DependencyInjector di) {
        super(di, Infinibiome.MOD_ID + ":diamondOre", Config.class);
    }

    @Override
    BlockState getBlock() {
        return Blocks.DIAMOND_ORE.getDefaultState();
    }

    public static class Config extends OreConfig {


        public Config(DependencyInjector di){
            super(di);
        }

        @Override
        String getName() {
            return "diamond";
        }

        @Override
        double getDefaultRatePerChunk() {
            return 1;
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
