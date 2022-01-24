package weightedgpa.infinibiome.internal.generators.interchunks.plant;

import net.minecraft.block.Blocks;
import weightedgpa.infinibiome.api.Infinibiome;
import weightedgpa.infinibiome.api.dependency.DependencyInjector;
import weightedgpa.infinibiome.internal.floatfunc.util.Interval;
import weightedgpa.infinibiome.internal.generators.utils.GenHelper;

import static weightedgpa.infinibiome.internal.generators.interchunks.plant.PlantHelper.*;

public final class FernGen extends PlantGenBase {
    private final GrassType type;

    public FernGen(GrassType type, DependencyInjector di){
        super(di, Infinibiome.MOD_ID + ":fern" + type.name());

        this.type = type;

        config = initConfig()
            .setPlant(
                type.getShortOrTall(
                    () -> Blocks.FERN,
                    () -> Blocks.LARGE_FERN
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
            .setHumdity(
                GenHelper.WETISH
            )
            .setSpawnRegion(
                COMMON_REGION_RATE
            )
            .setNoExtraConditions()
            .setCantOverridePlants();
    }

    @Override
    public String toString() {
        return "fern{" + "type=" + type + '}';
    }
}
