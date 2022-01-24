package weightedgpa.infinibiome.internal.generators.nonworldgen.spawners;

import net.minecraft.world.server.ServerWorld;
import net.minecraft.world.spawner.WanderingTraderSpawner;
import weightedgpa.infinibiome.api.dependency.DependencyInjector;
import weightedgpa.infinibiome.api.generators.nonworldgen.MobTicker;

public final class WandererSpawner implements MobTicker {
    private WanderingTraderSpawner wandererSpawner = null;

    public WandererSpawner(DependencyInjector di) {}

    @Override
    public boolean spawnsInPeaceful() {
        return true;
    }

    @Override
    public void atTick(ServerWorld world) {
        if (wandererSpawner == null){
            wandererSpawner = new WanderingTraderSpawner(world);
        }
        wandererSpawner.tick();
    }
}
