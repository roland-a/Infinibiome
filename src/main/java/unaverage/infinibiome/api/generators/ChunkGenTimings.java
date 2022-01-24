package weightedgpa.infinibiome.api.generators;

public final class ChunkGenTimings {
    private ChunkGenTimings(){}

    private static final Timing.Builder timingBuilder = Timing.initBuilder();

    public static final Timing
        BASE_TERRAIN = timingBuilder.getNextTiming(),
        BEDROCK = timingBuilder.getNextTiming(),
        CAVE = timingBuilder.getNextTiming(),
        CAVE_UNDERWATER = timingBuilder.getNextTiming(),
        RAVINE = timingBuilder.getNextTiming(),
        RAVINE_UNDERWATER = timingBuilder.getNextTiming(),
        SURFACE = timingBuilder.getNextTiming();

}
