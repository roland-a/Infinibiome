package weightedgpa.infinibiome.internal.generators.interchunks.plant;

import net.minecraft.block.Blocks;
import weightedgpa.infinibiome.api.Infinibiome;
import weightedgpa.infinibiome.api.dependency.DependencyInjector;
import weightedgpa.infinibiome.api.posdata.PosDataHelper;
import weightedgpa.infinibiome.internal.floatfunc.util.Interval;
import weightedgpa.infinibiome.api.pos.BlockPos2D;
import weightedgpa.infinibiome.api.posdata.PosDataKeys;
import weightedgpa.infinibiome.internal.generators.utils.condition.Condition;

import static weightedgpa.infinibiome.internal.generators.interchunks.plant.PlantHelper.*;

public final class SeaGrassGen extends PlantGenBase {
    private final GrassType type;

    public SeaGrassGen(GrassType type, DependencyInjector di) {
        super(
            di,
            Infinibiome.MOD_ID + ":seaGrass" + type.name()
        );

        config = initConfig()
            .setPlant(
                type.getShortOrTall(
                    () -> Blocks.SEAGRASS,
                    () -> Blocks.TALL_SEAGRASS
                )
            )
            .setUnderwater()
            .setRate(
                type.clusteredOrScattered(
                    () -> getCommonClusterRateFunc(seed),
                    () -> getRateFunc(seed, new Interval(0, 128), COMMON_SCATTERED_SKEW)
                )
            )
            .setRadius(
                type.clusteredOrScattered(
                    () -> COMMON_RADIUS,
                    () -> SCATTERED_RADIUS
                )
            )
            .setDensity(
                type.clusteredOrScattered(
                    () -> COMMON_DENSITY,
                    () -> SCATTERED_DENSITY
                )
            )
            .alsoInMushroomIsland()
            .anyTemperatureIncludingFreezing()
            .anyHumidity()
            .setNoSpawnRegion()
            .setExtraConditions(
                //spawns only in rivers, lakes, and oceans
                //should not spawn in ponds
                new Condition.BoolInterpolated() {
                    @Override
                    public boolean passes(BlockPos2D pos) {
                        return
                            posData.get(PosDataKeys.LANDMASS_TYPE, pos).isOcean() ||
                            PosDataHelper.isUnderwaterPortionOfLakeOrRiver(pos, posData);
                    }
                }
            );

        this.type = type;
    }

    @Override
    public String toString() {
        return "SeaGrass{" +
            "type=" + type +
            '}';
    }
}
