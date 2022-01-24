package weightedgpa.infinibiome.internal.generators.nonworldgen.spawners;

import net.minecraft.village.VillageSiege;
import net.minecraft.world.server.ServerWorld;
import weightedgpa.infinibiome.api.dependency.DependencyInjector;
import weightedgpa.infinibiome.api.generators.nonworldgen.MobTicker;

public final class RaidSpawner implements MobTicker {
    private final VillageSiege raidSpawner;

    public RaidSpawner(DependencyInjector di){
        this.raidSpawner = new VillageSiege();
    }

    @Override
    public boolean spawnsInPeaceful() {
        return false;
    }

    @Override
    public void atTick(ServerWorld world) {
        raidSpawner.tick(world, true, true);
    }
}
