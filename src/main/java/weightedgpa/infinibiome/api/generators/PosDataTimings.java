package weightedgpa.infinibiome.api.generators;

public final class PosDataTimings {
    private PosDataTimings() {}

    private static final Timing.Builder timingBuilder = Timing.initBuilder();

    public static final Timing
        CLIMATE = timingBuilder.getNextTiming(),
        LANDMASS = timingBuilder.getNextTiming(),
        HEIGHT = timingBuilder.getNextTiming(),
        LAKE = timingBuilder.getNextTiming(),
        RIVER = timingBuilder.getNextTiming(),
        MUSHROOM_ISLAND = timingBuilder.getNextTiming(),
        TERRAIN_3D = timingBuilder.getNextTiming();
}
