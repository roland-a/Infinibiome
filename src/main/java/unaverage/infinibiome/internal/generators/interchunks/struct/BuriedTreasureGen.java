package weightedgpa.infinibiome.internal.generators.interchunks.struct;

import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.structure.BuriedTreasureConfig;
import weightedgpa.infinibiome.api.Infinibiome;
import weightedgpa.infinibiome.api.dependency.DependencyInjector;

import static weightedgpa.infinibiome.internal.generators.utils.condition.ConditionHelper.*;

public final class BuriedTreasureGen extends StructGenBase {
    public BuriedTreasureGen(DependencyInjector di) {
        super(di, Infinibiome.MOD_ID + ":buriedTreasure");

        baseConfig = initConfig()
            .withStruct(
                Feature.BURIED_TREASURE,
                new BuriedTreasureConfig(1)
            )
            .withChance(
                Config.class, 1
            )
            .addExtraConditions(
                onlyInLandMass(
                    di,
                    l -> l.getTransitionToBeach() > 0.8f
                )
            );
    }

    public static final class Config extends StructConfigBase{
        public Config(DependencyInjector di){
            super(di);
        }

        @Override
        String name() {
            return "buried_treasure_rate";
        }

        @Override
        double defaultRate() {
            return 0.001;
        }

    }
}
