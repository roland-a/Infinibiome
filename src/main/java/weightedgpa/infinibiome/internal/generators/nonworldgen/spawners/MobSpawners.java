package weightedgpa.infinibiome.internal.generators.nonworldgen.spawners;

import net.minecraft.world.Difficulty;
import net.minecraft.world.GameRules;
import net.minecraft.world.server.ServerWorld;
import weightedgpa.infinibiome.api.dependency.DependencyInjector;
import weightedgpa.infinibiome.api.dependency.SingleDep;
import weightedgpa.infinibiome.api.generators.nonworldgen.MobTicker;

import java.util.List;

public final class MobSpawners implements SingleDep {
    private final List<MobTicker> mobTickers;

    public MobSpawners(DependencyInjector di){
        this.mobTickers = di.getAll(MobTicker.class);
    }

    public void run(ServerWorld world){
        if (!world.getGameRules().getBoolean(GameRules.DO_MOB_SPAWNING)) return;

        for (MobTicker m : mobTickers) {
            boolean isPeaceful = world.getDifficulty() == Difficulty.PEACEFUL;

            if (isPeaceful && !m.spawnsInPeaceful()) continue;

            m.atTick(world);
        }
    }
}
