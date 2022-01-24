package weightedgpa.infinibiome.internal.generators.interchunks.ore;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
//import net.minecraftforge.event.terraingen.OreGenEvent;
import weightedgpa.infinibiome.api.Infinibiome;
import weightedgpa.infinibiome.api.dependency.DependencyInjector;

public final class DioriteOreGen extends OreGenBase {
    public DioriteOreGen(DependencyInjector di) {
        super(di, Infinibiome.MOD_ID + ":dioriteOre", Config.class);
    }

    @Override
    BlockState getBlock() {
        return Blocks.DIORITE.getDefaultState();
    }

    public static class Config extends OreConfig {


        public Config(DependencyInjector di){
            super(di);
        }

        @Override
        String getName() {
            return "diorite";
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
