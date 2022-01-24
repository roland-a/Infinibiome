package weightedgpa.infinibiome.internal.generators.interchunks.ore;

import weightedgpa.infinibiome.api.generators.nonworldgen.ConfigIO;
import weightedgpa.infinibiome.api.dependency.DependencyInjector;
import weightedgpa.infinibiome.api.generators.nonworldgen.DefaultConfig;

public abstract class OreConfig implements DefaultConfig {
    abstract String getName();
    abstract double getDefaultRatePerChunk();
    abstract int getDefaultOreCount();
    abstract int getDefaultMaxHeight();
    int getDefaultMinHeight() {
        return 0;
    }

    final double ratePerChunk;
    final int oreCount;
    final int maxHeight;
    final int minHeight;

    public OreConfig(DependencyInjector di){
        ConfigIO config = di.get(ConfigIO.class).subConfig("ORE").subConfig(getName());

        ratePerChunk = config.getFloat(
            "rate_per_chunk",
            getDefaultRatePerChunk(),
            0,
            128,
            ""
        );

        oreCount = config.getInt(
            "ore_count",
            getDefaultOreCount(),
            0,
            128,
            ""
        );

        minHeight = config.getInt(
            "min_height",
            getDefaultMinHeight(),
            0,
            256,
            ""
        );

        maxHeight = config.getInt(
            "max_height",
            getDefaultMaxHeight(),
            0,
            256,
            ""
        );
    }
}
