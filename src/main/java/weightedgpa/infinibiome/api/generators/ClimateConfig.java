package weightedgpa.infinibiome.api.generators;

import weightedgpa.infinibiome.api.generators.nonworldgen.ConfigIO;
import weightedgpa.infinibiome.api.dependency.DependencyInjector;
import weightedgpa.infinibiome.api.generators.nonworldgen.DefaultConfig;

public class ClimateConfig implements DefaultConfig {
    public final double scale;
    public final double frozenTransitionBorder;
    public final double desertTransitionBorder;

    public ClimateConfig(DependencyInjector di){
        ConfigIO config = di.get(ConfigIO.class).subConfig("CLIMATE");

        this.scale = config.getRelativeFloat(
            "climate_scale",
            3000,
            500,
            Integer.MAX_VALUE,
            "Doubling/Halving this value will double/halve the climate scale."
        );

        this.frozenTransitionBorder = config.getRelativeFloat(
            "frozen_transition_border",
            50,
            0,
            Integer.MAX_VALUE,
            "Doubling/Halving this value will double/halve the transition length between deserts and non-desert areas"
        );

        this.desertTransitionBorder = config.getRelativeFloat(
            "desert_transition_border",
            50,
            0,
            Integer.MAX_VALUE,
            "Doubling/Halving this value will double/halve the transition length between frozen and non-frozen areas"
        );
    }
}
