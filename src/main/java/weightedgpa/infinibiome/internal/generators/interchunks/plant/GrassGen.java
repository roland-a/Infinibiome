package weightedgpa.infinibiome.internal.generators.interchunks.plant;

import net.minecraft.block.Blocks;
import weightedgpa.infinibiome.api.Infinibiome;
import weightedgpa.infinibiome.api.dependency.DependencyInjector;
import weightedgpa.infinibiome.internal.floatfunc.util.Interval;

import static weightedgpa.infinibiome.internal.generators.interchunks.plant.PlantHelper.*;

public final class GrassGen extends PlantGenBase {
    private final GrassType type;

    public GrassGen(GrassType type, DependencyInjector di) {
        super(di, Infinibiome.MOD_ID + ":grass" + type.name());

        this.type = type;

        config = initConfig()
            .setPlant(
                type.getShortOrTall(
                    () -> Blocks.GRASS,
                    () -> Blocks.TALL_GRASS
                )
            )
            .setAboveWater()
            .setRate(
                type.clusteredOrScattered(
                    () -> getRateFunc(seed, new Interval(0, 0.5), COMMON_CLUSTERED_SKEW),
                    () -> getRateFunc(seed, new Interval(0, 128), COMMON_SCATTERED_SKEW)
                )
            )
            .setRadius(
                type.clusteredOrScattered(
                    () -> new Interval(4, 7),
                    () -> SCATTERED_RADIUS
                )
            )
            .setDensity(
                type.clusteredOrScattered(
                    () -> new Interval(0.2, 0.5),
                    () -> SCATTERED_DENSITY
                )
            )
            .neverInMushroomIsland()
            .anyTemperatureIncludingFreezing()
            .anyHumidity()
            .setSpawnRegion(
                type.clusteredOrScattered(
                    () -> COMMON_REGION_RATE*4,
                    () -> COMMON_REGION_RATE*2
                )
            )
            .setNoExtraConditions()
            .setCantOverridePlants();
    }

    @Override
    public String toString() {
        return "Grass{" +
            "type=" + type +
            '}';
    }
}
