package weightedgpa.infinibiome.internal.generators.nonworldgen.spawners;

import net.minecraft.world.server.ServerWorld;
import weightedgpa.infinibiome.api.dependency.DependencyInjector;
import weightedgpa.infinibiome.api.generators.nonworldgen.MobTicker;

public final class PhantomSpawner implements MobTicker {
    private final net.minecraft.world.spawner.PhantomSpawner phantomSpawner;

    public PhantomSpawner(DependencyInjector di){
        phantomSpawner = new net.minecraft.world.spawner.PhantomSpawner();
    }

    @Override
    public boolean spawnsInPeaceful() {
        return false;
    }

    @Override
    public void atTick(ServerWorld world) {
        phantomSpawner.tick(world, true, true);
    }
}
