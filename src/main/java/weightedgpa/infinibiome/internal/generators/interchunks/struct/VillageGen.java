package weightedgpa.infinibiome.internal.generators.interchunks.struct;

import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.IWorld;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.structure.VillageConfig;
import weightedgpa.infinibiome.api.Infinibiome;
import weightedgpa.infinibiome.internal.floatfunc.util.Interval;
import weightedgpa.infinibiome.api.dependency.DependencyInjector;
import weightedgpa.infinibiome.api.pos.BlockPos2D;
import weightedgpa.infinibiome.api.pos.InterChunkPos;
import weightedgpa.infinibiome.api.posdata.LandmassInfo;
import weightedgpa.infinibiome.api.posdata.PosDataHelper;
import weightedgpa.infinibiome.internal.misc.MCHelper;

import static weightedgpa.infinibiome.internal.generators.utils.condition.ConditionHelper.*;

public final class VillageGen extends StructGenBase {


    public VillageGen(DependencyInjector di) {
        super(di, Infinibiome.MOD_ID + ":village");

        baseConfig = initConfig()
            .withStructAndFunc(
                Feature.VILLAGE,
                this::getVillageConfig
            )
            .withChance(
                Config.class,
                16
            )
            .addExtraConditions(
                StructHelper.alwaysAboveWater(
                    di,
                    20
                ),
                onlyInLandMass(
                    di,
                    50,
                    LandmassInfo::isLand
                ),
                onlyInSlope(
                    di,
                    50,
                    new Interval(0, 1/4f)
                )
            );
    }

    @Override
    public void postGenerate(InterChunkPos pos, IWorld world) {
        StructHelper.placeDirtUnderStruct(pos, world, posData);
    }

    private VillageConfig getVillageConfig(ChunkPos chunkPos){
        BlockPos2D pos = MCHelper.lowestPos(chunkPos);

        double temperature = PosDataHelper.getTemperature(pos, posData);

        double humidity = PosDataHelper.getHumidity(pos, posData);

        if (PosDataHelper.DRY_INTERVAL.contains(humidity)){
            return new VillageConfig("village/desert/town_centers", 6);
        }
        if (PosDataHelper.FREEZE_INTERVAL.contains(temperature)){
            return new VillageConfig("village/snowy/town_centers", 6);
        }
        if (PosDataHelper.COLD_INTERVAL.contains(temperature)){
            return new VillageConfig("village/taiga/town_centers", 6);
        }
        if (PosDataHelper.HOT_INTERVAL.contains(temperature) && PosDataHelper.SEMI_DRY_INTERVAL.contains(humidity)){
            return new VillageConfig("village/savanna/town_centers", 6);
        }
        return new VillageConfig("village/plains/town_centers", 6);
    }

    public static final class Config extends StructConfigBase{
        public Config(DependencyInjector di){
            super(di);
        }

        @Override
        String name() {
            return "village_rate";
        }

        @Override
        double defaultRate() {
            return 0.1;
        }

    }
}
