package weightedgpa.infinibiome.internal.generators.interchunks.struct;

import net.minecraft.world.gen.feature.Feature;
import weightedgpa.infinibiome.api.Infinibiome;
import weightedgpa.infinibiome.internal.floatfunc.util.Interval;
import weightedgpa.infinibiome.api.dependency.DependencyInjector;
import weightedgpa.infinibiome.api.posdata.LandmassInfo;
import weightedgpa.infinibiome.internal.misc.MCHelper;

import static weightedgpa.infinibiome.internal.generators.utils.condition.ConditionHelper.*;

public final class IllagerMansionGen extends StructGenBase {


    public IllagerMansionGen(DependencyInjector di) {
        super(di, Infinibiome.MOD_ID + ":mansion");

        baseConfig = initConfig()
            .withStruct(
                Feature.WOODLAND_MANSION
            )
            .withChance(
                Config.class,
                16
            )
            .addExtraConditions(
                onlyInHeight(
                    di,
                    new Interval(MCHelper.WATER_HEIGHT + 1, Double.POSITIVE_INFINITY)
                ),
                onlyInLandMass(
                    di,
                    50,
                    LandmassInfo::isLand
                ),
                onlyInSlope(
                    di,
                    50,
                    new Interval(0, 1/2d)
                )
            );
    }

    public static final class Config extends StructConfigBase{
        public Config(DependencyInjector di){
            super(di);
        }

        @Override
        String name() {
            return "illager_mansion_rate";
        }

        @Override
        double defaultRate() {
            return 0.002;
        }

    }
}
