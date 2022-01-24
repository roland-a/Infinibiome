package weightedgpa.infinibiome.internal.generators.nonworldgen.spawners;

import net.minecraft.world.server.ServerWorld;
import weightedgpa.infinibiome.api.dependency.DependencyInjector;
import weightedgpa.infinibiome.api.generators.nonworldgen.MobTicker;

public final class PatrolSpawner implements MobTicker {
    private final net.minecraft.world.spawner.PatrolSpawner patrolSpawner;

    public PatrolSpawner(DependencyInjector di){
        patrolSpawner = new net.minecraft.world.spawner.PatrolSpawner();
    }

    @Override
    public boolean spawnsInPeaceful() {
        return false;
    }

    @Override
    public void atTick(ServerWorld world) {
        patrolSpawner.tick(world, true, true);
    }
}
