package weightedgpa.infinibiome.internal.misc;

import net.minecraft.world.gen.ChunkGenerator;
import net.minecraft.world.server.ServerWorld;
import weightedgpa.infinibiome.api.Infinibiome;
import weightedgpa.infinibiome.api.dependency.DependencyInjector;
import weightedgpa.infinibiome.api.generators.Seed;
import weightedgpa.infinibiome.api.generators.nonworldgen.ConfigIO;
import weightedgpa.infinibiome.internal.dependency.DependencyInjectorImpl;
import weightedgpa.infinibiome.internal.generators.nonworldgen.ConfigIOImpl;
import weightedgpa.infinibiome.internal.minecraftImpl.IBChunkGenerator;

public final class DIRootGen {
    private static volatile boolean printed = false;

    public static DependencyInjector createDiWhenReady(ServerWorld world){
        while (world.getSaveHandler() == null){
            if (!printed){
                System.out.println("Waiting for world to fully initialize");
                printed = true;
            }
        }
        if (printed) {
            System.out.println("world fully initialized");
            printed = false;
        }

        //System.out.println(world.getSeed());

        DependencyInjector result = new DependencyInjectorImpl(Infinibiome.depModules)
            .addItem(
                Seed.class,
                __ -> Seed.ROOT.copyWithoutWarnings().newSeed(
                    String.valueOf(world.getSeed())
                )
            )
            .addItem(
                ServerWorld.class,
                __ -> world
            )
            .addItem(
                ConfigIO.class,
                ConfigIOImpl::new
            )
            .addItem(
                ChunkGenerator.class,
                IBChunkGenerator::new
            )
            .initInjector();

        result.refreshStaticItems();

        return result;
    }
}
