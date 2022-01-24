package weightedgpa.infinibiome.internal.generators.interchunks.struct;

import net.minecraft.world.gen.feature.Feature;
import weightedgpa.infinibiome.api.Infinibiome;
import weightedgpa.infinibiome.internal.floatfunc.util.Interval;
import weightedgpa.infinibiome.api.dependency.DependencyInjector;
import weightedgpa.infinibiome.api.posdata.LandmassInfo;
import weightedgpa.infinibiome.api.posdata.PosDataHelper;
import weightedgpa.infinibiome.internal.misc.MCHelper;

import static weightedgpa.infinibiome.internal.generators.utils.condition.ConditionHelper.*;

public final class DesertPyramidGen extends StructGenBase {


    public DesertPyramidGen(DependencyInjector di) {
        super(di, Infinibiome.MOD_ID + ":desertPyramid");

        baseConfig = initConfig()
            .withStruct(
                Feature.DESERT_PYRAMID
            )
            .withChance(
                Config.class, 8
            )
            .addExtraConditions(
                onlyInHumidity(di, PosDataHelper.DRY_INTERVAL),
                onlyInLandMass(di, LandmassInfo::isLand),
                onlyInHeight(di, new Interval(MCHelper.WATER_HEIGHT + 1, Double.POSITIVE_INFINITY))
            );
    }

    public static final class Config extends StructConfigBase{
        public Config(DependencyInjector di){
            super(di);
        }

        @Override
        String name() {
            return "desert_pyramid_rate";
        }

        @Override
        double defaultRate() {
            return 0.1;
        }

    }
}
