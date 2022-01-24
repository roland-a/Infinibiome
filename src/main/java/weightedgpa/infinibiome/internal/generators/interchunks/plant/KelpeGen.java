package weightedgpa.infinibiome.internal.generators.interchunks.plant;

import com.google.common.collect.Lists;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.KelpTopBlock;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorldReader;
import weightedgpa.infinibiome.api.Infinibiome;
import weightedgpa.infinibiome.internal.floatfunc.util.Interval;
import weightedgpa.infinibiome.api.dependency.DependencyInjector;
import weightedgpa.infinibiome.internal.misc.MathHelper;
import weightedgpa.infinibiome.api.posdata.LandmassInfo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import static weightedgpa.infinibiome.internal.generators.utils.condition.ConditionHelper.*;

public final class KelpeGen extends PlantGenBase {
    public KelpeGen(DependencyInjector di) {
        super(
            di,
            Infinibiome.MOD_ID + ":kelpe"
        );

        config = initConfig()
            .setPlantBlockFunc(this::getPlantBlocks)
            .setSampleBlock(Blocks.KELP_PLANT)
            .setUnderwater()
            .setScatteredRate(
                new Interval(0.1, 0.5)
            )
            .alsoInMushroomIsland()
            .anyNonFreezingTemperature()
            .anyHumidity()
            .setNoSpawnRegion()
            .setExtraConditions(
                onlyInLandMass(
                    di,
                    LandmassInfo::isOcean
                )
            )
            .setBoneMealPlantFunc(this::getPlantBlocksOnBoneMeal);

    }

    private List<BlockState> getPlantBlocks(BlockPos plantPos, IWorldReader world, Random random) {
        List<BlockState> result = new ArrayList<>();

        int height = MathHelper.randomInt(2, 25, random);

        for (int y = 0; y < height; y++) {
            if (!world.getBlockState(plantPos.up(y)).getBlock().equals(Blocks.WATER)) {
                break;
            }

            result.add(Blocks.KELP_PLANT.getDefaultState());
        }

        if (result.size() < 2) {
            return Collections.emptyList();
        }

        result.set(
            result.size() - 1,
            Blocks.KELP.getDefaultState()
                .with(KelpTopBlock.AGE, 25)
        );

        return result;
    }

    List<BlockState> getPlantBlocksOnBoneMeal(BlockPos plantPos, IWorldReader world, Random random) {
        return Lists.newArrayList(
            Blocks.KELP.getDefaultState()
        );
    }
}
