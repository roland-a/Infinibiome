package weightedgpa.infinibiome.internal.generators.interchunks.struct;

import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.structure.OceanRuinConfig;
import net.minecraft.world.gen.feature.structure.OceanRuinStructure;
import weightedgpa.infinibiome.api.Infinibiome;
import weightedgpa.infinibiome.internal.floatfunc.util.Interval;
import weightedgpa.infinibiome.api.dependency.DependencyInjector;
import weightedgpa.infinibiome.api.pos.BlockPos2D;
import weightedgpa.infinibiome.api.posdata.LandmassInfo;
import weightedgpa.infinibiome.internal.misc.MCHelper;

import static weightedgpa.infinibiome.api.posdata.PosDataHelper.*;
import static weightedgpa.infinibiome.internal.generators.utils.condition.ConditionHelper.*;

public final class OceanRuinGen extends StructGenBase {
    public OceanRuinGen(DependencyInjector di) {
        super(di, Infinibiome.MOD_ID + ":oceanRuins");

        baseConfig = initConfig()
            .withStructAndFunc(
                Feature.OCEAN_RUIN,
                this::getConfig
            )
            .withChance(
                Config.class, 1
            )
            .addExtraConditions(
                onlyInHeight(di, new Interval(0, MCHelper.WATER_HEIGHT - 16)),
                onlyInLandMass(di, LandmassInfo::isOcean)
            );
    }

    private OceanRuinConfig getConfig(ChunkPos chunkPos) {
        BlockPos2D pos = MCHelper.lowestPos(chunkPos);

        double temperature = getTemperature(pos, posData);

        if (FREEZE_INTERVAL.contains(temperature) || COLD_INTERVAL.contains(temperature)) {
            return new OceanRuinConfig(OceanRuinStructure.Type.COLD, .3f, .9f);
        }
        assert WARM_INTERVAL.contains(temperature) || HOT_INTERVAL.contains(temperature);

        return new OceanRuinConfig(OceanRuinStructure.Type.COLD, .3f, .9f);
    }

    public static final class Config extends StructConfigBase{
        public Config(DependencyInjector di){
            super(di);
        }

        @Override
        String name() {
            return "ocean_ruins_rate";
        }

        @Override
        double defaultRate() {
            return 0.005;
        }

    }
}
