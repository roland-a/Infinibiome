package weightedgpa.infinibiome.internal.generators.interchunks.struct;

import net.minecraft.world.biome.DefaultBiomeFeatures;
import net.minecraft.world.gen.feature.ConfiguredFeature;
import net.minecraft.world.gen.feature.Feature;
import weightedgpa.infinibiome.api.Infinibiome;
import weightedgpa.infinibiome.api.dependency.DependencyInjector;
import weightedgpa.infinibiome.internal.generators.interchunks.SmallObjectGenBase;
import weightedgpa.infinibiome.internal.generators.utils.condition.ConditionHelper;

public final class MushroomBigGen extends SmallObjectGenBase {
    private final Type type;

    public MushroomBigGen(Type type, DependencyInjector di) {
        super(di, Infinibiome.MOD_ID + ":bigMushroom" + type.name());

        this.type = type;

        config = initConfig()
            .setWithFeature(
                type.feature
            )
            .setCount(1)
            .setAttemptsPerCount(1)
            .aboveHighestTerrainBlock()
            .noChancePerChunk()
            .addExtraConditions(
                ConditionHelper.onlyInMushroomIsland(di)
            );
    }

    @Override
    public String toString() {
        return "MushroomBig{" +
            "type=" + type +
            '}';
    }

    public enum Type{
        RED(Feature.HUGE_RED_MUSHROOM.withConfiguration(DefaultBiomeFeatures.BIG_BROWN_MUSHROOM)),
        BROWN(Feature.HUGE_BROWN_MUSHROOM.withConfiguration(DefaultBiomeFeatures.BIG_RED_MUSHROOM)),;
        
        private final ConfiguredFeature<?, ?> feature;

        Type(ConfiguredFeature<?, ?> feature) {
            this.feature = feature;
        }
    }
}
