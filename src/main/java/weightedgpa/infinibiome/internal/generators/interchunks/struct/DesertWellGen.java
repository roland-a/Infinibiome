package weightedgpa.infinibiome.internal.generators.interchunks.struct;

import net.minecraft.world.gen.feature.Feature;
import weightedgpa.infinibiome.api.Infinibiome;
import weightedgpa.infinibiome.api.dependency.DependencyInjector;
import weightedgpa.infinibiome.api.generators.nonworldgen.Locatable;
import weightedgpa.infinibiome.api.posdata.PosDataHelper;
import weightedgpa.infinibiome.internal.generators.interchunks.SmallObjectGenBase;

import static weightedgpa.infinibiome.internal.generators.utils.condition.ConditionHelper.*;

public final class DesertWellGen extends SmallObjectGenBase implements Locatable.HasPointsProvider {
    public DesertWellGen(DependencyInjector di){
        super(di, Infinibiome.MOD_ID + ":desertWell");

        config = initConfig()
            .setWithFeature(
                Feature.DESERT_WELL
            )
            .setCount(1)
            .setAttemptsPerCount(4)
            .aboveHighestTerrainBlock()
            .setChancePerChunk(
                di.getAll(Config.class).get(0).rate
            )
            .addExtraConditions(
                onlyInHumidity(
                    di,
                    PosDataHelper.DRY_INTERVAL
                ),
                //makes sure there isnt a river nearby
                StructHelper.alwaysAboveWater(
                    di,
                    50
                )
            );
    }

    public static final class Config extends StructConfigBase{
        public Config(DependencyInjector di){
            super(di);
        }

        @Override
        String name() {
            return "desert_well_rate";
        }

        @Override
        double defaultRate() {
            return 1/500d;
        }

    }
}
