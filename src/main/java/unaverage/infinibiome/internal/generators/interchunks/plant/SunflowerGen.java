package weightedgpa.infinibiome.internal.generators.interchunks.plant;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorldReader;
import weightedgpa.infinibiome.api.Infinibiome;
import weightedgpa.infinibiome.api.dependency.DependencyInjector;
import weightedgpa.infinibiome.internal.floatfunc.util.Interval;
import weightedgpa.infinibiome.internal.generators.utils.GenHelper;

import java.util.Collections;
import java.util.List;
import java.util.Random;

import static weightedgpa.infinibiome.internal.generators.utils.condition.ConditionHelper.*;

//spawns scattered
//spawns only in dryish climate in sparse tree density
public final class SunflowerGen extends PlantGenBase {
    public SunflowerGen(DependencyInjector di) {
        super(
            di,
            Infinibiome.MOD_ID + ":sunflower"
        );

        config = initConfig()
            .setPlantBlockFunc(this::getPlantBlocks)
            .setSampleBlock(Blocks.SUNFLOWER)
            .setAboveWater()
            .setScatteredRate(
                new Interval(0, 128)
            )
            .neverInMushroomIsland()
            .anyNonFreezingTemperature()
            .setHumdity(
                GenHelper.DRYISH
            )
            .setSpawnRegion(
                PlantHelper.COMMON_REGION_RATE
            )
            .setExtraConditions(
                onlyInTreeDensity(
                    di,
                    GenHelper.NON_FORESTED_TREE_INTERVAL
                )
            );
    }

    private List<BlockState> getPlantBlocks(BlockPos pos, IWorldReader world, Random random){
        if (!validSpace(pos, world)) return Collections.emptyList();

        return PlantHelper.initDouble(Blocks.SUNFLOWER);
    }

    private boolean validSpace(BlockPos plantPos, IWorldReader world) {
        BlockPos startingPos = plantPos.up();

        for (int i = 1; i <= 6; i++) {
            BlockPos currPos = startingPos.offset(Direction.EAST, i).offset(Direction.UP, i);

            if (!world.getBlockState(currPos).isAir()) {
                return false;
            }
        }
        return true;
    }
}
