package weightedgpa.infinibiome.internal.generators.interchunks;

import net.minecraft.world.biome.DefaultBiomeFeatures;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.LiquidsConfig;
import weightedgpa.infinibiome.api.Infinibiome;
import weightedgpa.infinibiome.api.dependency.DependencyInjector;
import weightedgpa.infinibiome.internal.misc.MCHelper;
import weightedgpa.infinibiome.internal.misc.MathHelper;

public final class UndergroundPoolGen extends SmallObjectGenBase {
    public UndergroundPoolGen(Type type, DependencyInjector di) {
        super(di, Infinibiome.MOD_ID + ":undergroundPool" + type.name());

        config = initConfig()
            .setWithFeature(
                Feature.SPRING_FEATURE,
                type.config
            )
            .setCount(1)
            .setAttemptsPerCount(1)
            .customHeightFunc(
                (pos, world, random) -> {
                    int maxHeight = MCHelper.getHighestTerrainHeight(
                        pos,
                        world
                    ) - 10;

                    int minHeight = 10;

                    if (minHeight >= maxHeight) return minHeight;

                    return MathHelper.randomInt(
                        minHeight,
                        maxHeight,
                        random
                    );
                }
            )
            .noChancePerChunk()
            .noExtraConditions();
    }

    public enum Type{
        WATER(DefaultBiomeFeatures.WATER_SPRING_CONFIG),
        LAVA(DefaultBiomeFeatures.LAVA_SPRING_CONFIG);

        private final LiquidsConfig config;

        Type(LiquidsConfig config) {
            this.config = config;
        }
    }
}
