package weightedgpa.infinibiome.internal.generators.interchunks.plant;

import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.ProbabilityConfig;
import weightedgpa.infinibiome.api.Infinibiome;
import weightedgpa.infinibiome.api.dependency.DependencyInjector;
import weightedgpa.infinibiome.api.generators.InterChunkGenTimings;
import weightedgpa.infinibiome.api.generators.Timing;
import weightedgpa.infinibiome.api.posdata.PosDataHelper;
import weightedgpa.infinibiome.internal.floatfunc.util.Interval;
import weightedgpa.infinibiome.internal.generators.interchunks.SmallObjectGenBase;
import weightedgpa.infinibiome.internal.generators.utils.condition.ConditionList;
import weightedgpa.infinibiome.internal.misc.Helper;

import static weightedgpa.infinibiome.internal.generators.utils.condition.ConditionHelper.onlyInHumidity;
import static weightedgpa.infinibiome.internal.generators.utils.condition.ConditionHelper.onlyInTemperature;


public final class BambooGen extends SmallObjectGenBase {
    public BambooGen(DependencyInjector di) {
        super(di, Infinibiome.MOD_ID + ":bamboo");

        config = initConfig()
            .setWithFeature(
                Feature.BAMBOO,
                new ProbabilityConfig(0.0f)
            )
            .setCount(
                Helper.initUniformNoise(seed, Helper.COMMON_SCALE)
                    .mapInterval(
                        new Interval(0, 64)
                    )
            )
            .setAttemptsPerCount(4)
            .aboveHighestTerrainBlock()
            .onlyInRegion(
                0.2f
            )
            .addExtraConditions(
                onlyInTemperature(
                    di,
                    PosDataHelper.HOT_INTERVAL
                ),
                onlyInHumidity(
                    di,
                    PosDataHelper.WET_INTERVAL
                )
            );
    }

    public ConditionList getConditions(){
        return config.conditions;
    }

    @Override
    public Timing getInterChunkTiming() {
        return InterChunkGenTimings.PLANTS;
    }
}
