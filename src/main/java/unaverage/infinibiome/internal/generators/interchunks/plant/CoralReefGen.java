package weightedgpa.infinibiome.internal.generators.interchunks.struct;

import com.google.common.collect.ImmutableList;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.IFeatureConfig;
import net.minecraft.world.gen.feature.SingleRandomFeature;
import weightedgpa.infinibiome.api.Infinibiome;
import weightedgpa.infinibiome.api.dependency.DependencyInjector;
import weightedgpa.infinibiome.api.generators.nonworldgen.Locatable;
import weightedgpa.infinibiome.api.pos.BlockPos2D;
import weightedgpa.infinibiome.api.posdata.LandmassInfo;
import weightedgpa.infinibiome.api.posdata.PosDataHelper;
import weightedgpa.infinibiome.internal.floatfunc.util.Interval;
import weightedgpa.infinibiome.internal.generators.interchunks.SmallObjectGenBase;
import weightedgpa.infinibiome.internal.misc.Helper;

import static weightedgpa.infinibiome.internal.generators.utils.condition.ConditionHelper.*;

public final class CoralReefGen extends SmallObjectGenBase implements Locatable.HasPointsProvider {
    public CoralReefGen(DependencyInjector di){
        super(di, Infinibiome.MOD_ID + ":coralReef");

        config = initConfig()
            .setWithFeature(
                Feature.SIMPLE_RANDOM_SELECTOR,
                new SingleRandomFeature(
                    ImmutableList.of(
                        Feature.CORAL_TREE.withConfiguration(IFeatureConfig.NO_FEATURE_CONFIG),
                        Feature.CORAL_CLAW.withConfiguration(IFeatureConfig.NO_FEATURE_CONFIG),
                        Feature.CORAL_MUSHROOM.withConfiguration(IFeatureConfig.NO_FEATURE_CONFIG)
                    )
                )
            )
            .setCount(
                Helper.<BlockPos2D>initUniformNoise(seed.newSeed("count"), Helper.COMMON_SCALE)
                    .mapInterval(
                        new Interval(3, 8)
                    )
            )
            .setAttemptsPerCount(4)
            .aboveHighestTerrainBlock()
            .onlyInRegion(
                0.2f
            )
            .addExtraConditions(
                onlyInLandMass(
                    di,
                    LandmassInfo::isOcean
                ),
                onlyInTemperature(
                    di,
                    PosDataHelper.HOT_INTERVAL
                )
            );
    }
}
