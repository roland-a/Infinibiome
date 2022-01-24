package weightedgpa.infinibiome.api.generators;

import weightedgpa.infinibiome.api.generators.nonworldgen.ConfigIO;
import weightedgpa.infinibiome.api.dependency.DependencyInjector;
import weightedgpa.infinibiome.api.generators.nonworldgen.DefaultConfig;

public final class PlantGrowthConfig implements DefaultConfig {
    public final boolean allowPlantUniformGrowth;
    public final boolean allowSaplingUniformGrowth;
    public final boolean allowFlowerDuplication;

    public PlantGrowthConfig(DependencyInjector di) {
        ConfigIO config = di.get(ConfigIO.class).subConfig("NON_WORLD_GEN");

        allowPlantUniformGrowth = config.getBool(
            "allow_plant_uniform_growth",
            false,
            "Enable this to allow plants to grow regardless of locations like in vanilla\n" +
            "Otherwise plants will grow slowly outside where they're naturally found.\n" +
            "Disable this to encourage creating multiple bases to grow different crops.\n" +
            "Plants that don't naturally generate outside structures are unaffected by this configuration for now."
        );

        allowSaplingUniformGrowth = config.getBool(
            "allow_sapling_uniform_growth",
            false,
            "Enable this to allow saplings to grow regardless of locations like in vanilla.\n" +
            "Otherwise saplings to grow slowly outside where they're naturally found.\n" +
            "Disable this to encourage making creating multiple bases to grow different trees."
        );

        allowFlowerDuplication = config.getBool(
            "allow_flower_duplication",
            false,
            "Enable this to allow flowers to duplicate by bonemealing it, like in vanilla.\n" +
            "The only other way to obtain more flowers is by bonemealing the ground where they are naturally found.\n" +
            "Disable this to encourage making multiple bases to grow different flowers."
        );
    }
}
