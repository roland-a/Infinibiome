package weightedgpa.infinibiome.internal.generators.interchunks.struct;

import net.minecraft.world.gen.feature.Feature;
import weightedgpa.infinibiome.api.Infinibiome;
import weightedgpa.infinibiome.api.dependency.DependencyInjector;
import weightedgpa.infinibiome.api.posdata.LandmassInfo;
import weightedgpa.infinibiome.api.posdata.PosDataHelper;
import weightedgpa.infinibiome.internal.generators.utils.condition.ConditionHelper;

public final class WitchHutGen extends StructGenBase {


    public WitchHutGen(DependencyInjector di) {
        super(di, Infinibiome.MOD_ID + ":witchHut");

        baseConfig = initConfig()
            .withStruct(
                Feature.SWAMP_HUT
            )
            .withChance(
                Config.class,
                2
            )
            .addExtraConditions(
                ConditionHelper.onlyInLandMass(
                    di,
                    LandmassInfo::isLand
                ),
                ConditionHelper.onlyInHumidity(
                    di,
                    PosDataHelper.WET_INTERVAL
                ),
                ConditionHelper.onlyInTemperature(
                    di,
                    PosDataHelper.WARM_INTERVAL
                )
            );
    }

    public static final class Config extends StructConfigBase{
        public Config(DependencyInjector di){
            super(di);
        }

        @Override
        String name() {
            return "witch_hut_rate";
        }

        @Override
        double defaultRate() {
            return 0.005;
        }

    }
}
