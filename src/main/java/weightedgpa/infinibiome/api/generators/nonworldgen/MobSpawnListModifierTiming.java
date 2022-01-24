package weightedgpa.infinibiome.api.generators.nonworldgen;

import weightedgpa.infinibiome.api.generators.Timing;

public final class MobSpawnListModifierTiming {
    private MobSpawnListModifierTiming(){}

    private static final Timing.Builder builder = Timing.initBuilder();

    public static final Timing
        NORMAL = builder.getNextTiming(),
        STRUCT = builder.getNextTiming();
}

