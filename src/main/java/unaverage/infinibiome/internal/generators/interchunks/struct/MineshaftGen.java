package weightedgpa.infinibiome.internal.generators.interchunks.struct;

import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.structure.MineshaftConfig;
import net.minecraft.world.gen.feature.structure.MineshaftStructure;
import weightedgpa.infinibiome.api.Infinibiome;
import weightedgpa.infinibiome.api.dependency.DependencyInjector;
import weightedgpa.infinibiome.api.posdata.LandmassInfo;

import static weightedgpa.infinibiome.internal.generators.utils.condition.ConditionHelper.*;

public final class MineshaftGen extends StructGenBase {


    public MineshaftGen(DependencyInjector di){
        super(di, Infinibiome.MOD_ID + ":mineshaft");

        baseConfig = initConfig()
            .withStruct(
                Feature.MINESHAFT,
                new MineshaftConfig(1, MineshaftStructure.Type.NORMAL)
            )
            .withChance(
                Config.class, 8
            )
            .addExtraConditions(
                onlyInLandMass(di, 0, LandmassInfo::isLand)
            );
    }

    public static final class Config extends StructConfigBase{
        public Config(DependencyInjector di){
            super(di);
        }

        @Override
        String name() {
            return "mineshaft_rate";
        }

        @Override
        double defaultRate() {
            return 0.1;
        }

    }
}
