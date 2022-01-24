package weightedgpa.infinibiome.internal.generators.interchunks.plant;

import com.google.common.collect.Lists;
import net.minecraft.block.Blocks;
import net.minecraft.util.Direction;
import weightedgpa.infinibiome.api.Infinibiome;
import weightedgpa.infinibiome.api.dependency.DependencyInjector;
import weightedgpa.infinibiome.api.generators.Seed;
import weightedgpa.infinibiome.api.posdata.LandmassInfo;
import weightedgpa.infinibiome.internal.generators.utils.GenHelper;
import weightedgpa.infinibiome.internal.misc.MCHelper;
import net.minecraft.block.BlockState;
import weightedgpa.infinibiome.internal.floatfunc.util.Interval;
import weightedgpa.infinibiome.api.pos.BlockPos2D;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorldReader;
import weightedgpa.infinibiome.internal.misc.Helper;

import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.function.Predicate;

import static weightedgpa.infinibiome.internal.generators.utils.condition.ConditionHelper.*;

//spawns in dry climate
public final class CactusGen extends PlantGenBase {
    private final Predicate<BlockPos2D> extraCactusHeight;

    public CactusGen(DependencyInjector di) {
        super(
            di,
            Infinibiome.MOD_ID + ":cactus"
        );

        config = initConfig()
            .setPlantBlockFunc(
                this::getPlantBlocks
            )
            .setSampleBlock(Blocks.CACTUS)
            .setAboveWater()
            .setScatteredRate(
                new Interval(0, 8)
            )
            .neverInMushroomIsland()
            .anyTemperatureIncludingFreezing()
            .setHumdity(
                GenHelper.DRYISH
            )
            .setSpawnRegion(
                PlantHelper.COMMON_REGION_RATE*3
            )
            .setExtraConditions(
                onlyInLandMass(
                    di,
                    LandmassInfo::isLand
                )
            )
            .setBoneMealPlantFunc(this::getPlantBlocksOnBoneMeal);

        this.extraCactusHeight = initCactusExtraHeightChance(seed);
    }

    private Predicate<BlockPos2D> initCactusExtraHeightChance(Seed seed) {
        seed = seed.newSeed("extraCactusHeight");

        return Helper.initUniformNoise(seed, Helper.COMMON_SCALE)
            .randomBool(
                BlockPos2D.INFO, seed
            );
    }

    private List<BlockState> getPlantBlocks(BlockPos plantPos, IWorldReader world, Random random) {
        int height;

        if (extraCactusHeight.test(MCHelper.to2D(plantPos))) {
            height = 4;
        } else {
            height = 3;
        }

        if (!surroundedByAir(plantPos, height, world)) {
            return Collections.emptyList();
        }

        return Collections.nCopies(
            height,
            Blocks.CACTUS.getDefaultState()
        );
    }

    private List<BlockState> getPlantBlocksOnBoneMeal(BlockPos plantPos, IWorldReader world, Random random) {
        if (!surroundedByAir(plantPos, 1, world)) {
            return Collections.emptyList();
        }

        if (world.getBlockState(plantPos.down()).getBlock().equals(Blocks.CACTUS)) {
            return Collections.emptyList();
        }

        return Lists.newArrayList(Blocks.CACTUS.getDefaultState());
    }

    private static boolean surroundedByAir(BlockPos pos, int height, IWorldReader world) {
        for (int y = 0; y <= height; y++) {
            for (Direction d : MCHelper.NSWE) {
                BlockPos currPos = pos.up(y).offset(d);
                BlockState currBlock = world.getBlockState(currPos);

                if (!MCHelper.isMostlyAir(currBlock)) return false;
            }
        }
        return true;
    }
}