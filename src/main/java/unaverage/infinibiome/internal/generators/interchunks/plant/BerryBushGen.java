package weightedgpa.infinibiome.internal.generators.interchunks.plant;

import net.minecraft.block.Blocks;
import net.minecraft.block.SweetBerryBushBlock;
import weightedgpa.infinibiome.api.Infinibiome;
import weightedgpa.infinibiome.api.dependency.DependencyInjector;
import weightedgpa.infinibiome.api.posdata.PosDataHelper;
import weightedgpa.infinibiome.internal.floatfunc.util.Interval;
import weightedgpa.infinibiome.internal.generators.utils.GenHelper;

import static weightedgpa.infinibiome.internal.generators.utils.condition.ConditionHelper.*;

//spawns only in cold climate
public final class BerryBushGen extends PlantGenBase {
    public BerryBushGen(DependencyInjector di) {
        super(
            di,
            Infinibiome.MOD_ID + ":berryBush"
        );

        config = initConfig()
            .setPlant(Blocks.SWEET_BERRY_BUSH.getDefaultState().with(SweetBerryBushBlock.AGE, 1))
            .setAboveWater()
            .setRate(
                new Interval(0, 0.5)
            )
            .setRadius(
                new Interval(1, 2)
            )
            .setDensity(
                new Interval(0.5, 0.8)
            )
            .neverInMushroomIsland()
            .setTemperature(
                PosDataHelper.COLD_INTERVAL
            )
            .anyHumidity()
            .setNoSpawnRegion()
            .setExtraConditions(
                onlyInTreeDensity(
                    di,
                    GenHelper.FORESTED_TREE_INTERVAL
                )
            )
            .setNoGroundBoneMeal();
    }
}
