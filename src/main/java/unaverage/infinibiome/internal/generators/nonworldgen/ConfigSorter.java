package weightedgpa.infinibiome.internal.generators.nonworldgen;

import weightedgpa.infinibiome.api.dependency.DependencyInjector;
import weightedgpa.infinibiome.api.generators.nonworldgen.ConfigIO;
import weightedgpa.infinibiome.api.generators.nonworldgen.DefaultConfig;

public final class ConfigSorter implements DefaultConfig {
    public ConfigSorter(DependencyInjector di){
        ConfigIO config = di.get(ConfigIO.class);

        config.subConfig("LAND");
        config.subConfig("CLIMATE");
        config.subConfig("STRUCT");
        config.subConfig("ORE");
        config.subConfig("NON_WORLD_GEN");
    }
}
