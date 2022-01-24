package weightedgpa.infinibiome.internal.generators.nonworldgen.spawners;

import net.minecraft.world.server.ServerWorld;
import net.minecraft.world.spawner.CatSpawner;
import weightedgpa.infinibiome.api.dependency.DependencyInjector;
import weightedgpa.infinibiome.api.generators.nonworldgen.MobTicker;

public final class CatSpawners implements MobTicker {
    private final CatSpawner catSpawner;

    public CatSpawners(DependencyInjector di) {
        this.catSpawner = new CatSpawner();
    }

    @Override
    public boolean spawnsInPeaceful() {
        return true;
    }

    @Override
    public void atTick(ServerWorld world) {
        catSpawner.tick(world, true, true);
    }
}
