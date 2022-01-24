package weightedgpa.infinibiome.api.generators;

public final class SurfaceTimings {
    private SurfaceTimings() {}

    private static final Timing.Builder timingBuilder = Timing.initBuilder(ChunkGenTimings.SURFACE);

    public static final Timing
        PATCHY = timingBuilder.getNextTiming(),
        FULL = timingBuilder.getNextTiming(),
        GRASS = timingBuilder.getNextTiming(),
        MYCELIUM = timingBuilder.getNextTiming();
}
