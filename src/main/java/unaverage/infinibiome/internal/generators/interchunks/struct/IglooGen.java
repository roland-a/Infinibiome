package weightedgpa.infinibiome.internal.generators.interchunks.struct;

import net.minecraft.world.gen.feature.Feature;
import weightedgpa.infinibiome.api.Infinibiome;
import weightedgpa.infinibiome.internal.floatfunc.util.Interval;
import weightedgpa.infinibiome.api.dependency.DependencyInjector;
import weightedgpa.infinibiome.api.posdata.LandmassInfo;
import weightedgpa.infinibiome.api.posdata.PosDataHelper;
import weightedgpa.infinibiome.internal.misc.MCHelper;

import static weightedgpa.infinibiome.internal.generators.utils.condition.ConditionHelper.*;

public final class IglooGen extends StructGenBase {


    public IglooGen(DependencyInjector di) {
        super(di, Infinibiome.MOD_ID + ":igloo");

        baseConfig = initConfig()
            .withStruct(
                Feature.IGLOO
            )
            .withChance(
                Config.class, 1
            )
            .addExtraConditions(
                onlyInTemperature(di, PosDataHelper.FREEZE_INTERVAL),
                onlyInHeight(di, new Interval(MCHelper.WATER_HEIGHT + 1, Double.POSITIVE_INFINITY)),
                onlyInLandMass(di, LandmassInfo::isLand)
            );
    }


    public static final class Config extends StructConfigBase{
        public Config(DependencyInjector di){
            super(di);
        }

        @Override
        String name() {
            return "igloo_rate";
        }

        @Override
        double defaultRate() {
            return 0.0005;
        }

    }
}
