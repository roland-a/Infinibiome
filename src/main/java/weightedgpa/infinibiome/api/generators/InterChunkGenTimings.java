package weightedgpa.infinibiome.api.generators;

public final class InterChunkGenTimings {
    private InterChunkGenTimings(){}

    private static final Timing.Builder timingBuilder = Timing.initBuilder();

    public static final Timing
        ORES = timingBuilder.getNextTiming(),
        STRUCTS = timingBuilder.getNextTiming(),
        WATER_POOL = timingBuilder.getNextTiming(),
        TREES = timingBuilder.getNextTiming(),
        PLANTS = timingBuilder.getNextTiming(),
        BEEHIVE = timingBuilder.getNextTiming(),
        LAVA_POOL = timingBuilder.getNextTiming(),
        SNOW = timingBuilder.getNextTiming(),
        MOBS = timingBuilder.getNextTiming();
}
