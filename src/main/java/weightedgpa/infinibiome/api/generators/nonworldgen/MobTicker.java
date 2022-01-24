package weightedgpa.infinibiome.api.generators.nonworldgen;

import net.minecraft.world.server.ServerWorld;
import weightedgpa.infinibiome.api.dependency.MultiDep;

public interface MobTicker extends MultiDep {
    boolean spawnsInPeaceful();

    void atTick(ServerWorld world);
}
