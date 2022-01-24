package weightedgpa.infinibiome.internal.generators.interchunks.struct;

import net.minecraft.entity.EntityClassification;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorld;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.feature.Feature;
import weightedgpa.infinibiome.api.Infinibiome;
import weightedgpa.infinibiome.api.dependency.DependencyInjector;
import weightedgpa.infinibiome.api.generators.Timing;
import weightedgpa.infinibiome.api.generators.nonworldgen.MobSpawnListModifier;
import weightedgpa.infinibiome.api.generators.nonworldgen.MobSpawnListModifierTiming;
import weightedgpa.infinibiome.api.posdata.LandmassInfo;
import weightedgpa.infinibiome.internal.floatfunc.util.Interval;
import weightedgpa.infinibiome.internal.misc.MCHelper;

import java.util.List;

import static weightedgpa.infinibiome.internal.generators.utils.condition.ConditionHelper.onlyInHeight;
import static weightedgpa.infinibiome.internal.generators.utils.condition.ConditionHelper.onlyInLandMass;

public final class OceanMonumentGen extends StructGenBase implements MobSpawnListModifier {
    public OceanMonumentGen(DependencyInjector di) {
        super(di, Infinibiome.MOD_ID + ":oceanMonument");

        baseConfig = initConfig()
            .withStruct(
                Feature.OCEAN_MONUMENT
            )
            .withChance(
                Config.class, 16
            )
            .addExtraConditions(
                onlyInLandMass(
                    di,
                    LandmassInfo::isOcean
                ),
                onlyInHeight(
                    di,
                    32,
                    new Interval(0, MCHelper.WATER_HEIGHT - 25)
                )
            );
    }

    @Override
    public Timing getMobSpawnModifierTiming() {
        return MobSpawnListModifierTiming.STRUCT;
    }

    @Override
    public void modifyList(BlockPos pos, EntityClassification creatureType, List<Biome.SpawnListEntry> spawnListEntries, IWorld world) {
        if (!Feature.OCEAN_MONUMENT.isPositionInsideStructure(world, pos)) return;

        //no squids should spawn near monuments
        if (creatureType == EntityClassification.WATER_CREATURE){
            spawnListEntries.clear();
            return;
        }

        if (creatureType != EntityClassification.MONSTER) return;

        spawnListEntries.clear();

        spawnListEntries.addAll(
            Feature.OCEAN_MONUMENT.getSpawnList()
        );
    }

    public static final class Config extends StructConfigBase{
        public Config(DependencyInjector di){
            super(di);
        }

        @Override
        String name() {
            return "monument_rate";
        }

        @Override
        double defaultRate() {
            return 0.01;
        }

    }
}
