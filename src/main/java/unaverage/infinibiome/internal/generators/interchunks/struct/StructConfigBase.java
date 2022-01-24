package weightedgpa.infinibiome.internal.generators.interchunks.struct;

import weightedgpa.infinibiome.api.dependency.DependencyInjector;
import weightedgpa.infinibiome.api.generators.nonworldgen.DefaultConfig;
import weightedgpa.infinibiome.api.generators.nonworldgen.ConfigIO;

abstract class StructConfigBase implements DefaultConfig {
    private static final String DESCRIPTION = "Doubling/Halving this value will double/halve the rate of this structure.";

    abstract String name();

    abstract double defaultRate();

    final double rate;

    StructConfigBase(DependencyInjector di){
        this.rate = di.get(ConfigIO.class).subConfig("STRUCT").getRelativeFloat(
            name(),
            defaultRate(),
            0,
            1,
            DESCRIPTION
        );
    }
}
