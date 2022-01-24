package weightedgpa.infinibiome.internal.generators.interchunks.plant;


import com.google.common.collect.Lists;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.util.Direction;
import weightedgpa.infinibiome.api.Infinibiome;
import weightedgpa.infinibiome.api.dependency.DependencyInjector;
import weightedgpa.infinibiome.api.posdata.LandmassInfo;
import weightedgpa.infinibiome.internal.generators.utils.GenHelper;
import weightedgpa.infinibiome.internal.misc.MCHelper;
import weightedgpa.infinibiome.internal.floatfunc.util.Interval;
import weightedgpa.infinibiome.internal.misc.Helper;
import weightedgpa.infinibiome.api.pos.BlockPos2D;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorldReader;

import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.function.Predicate;

import static weightedgpa.infinibiome.internal.generators.utils.condition.ConditionHelper.*;

//spawns in wet climate
public final class SugarcaneGen extends PlantGenBase {
    private final Predicate<BlockPos2D> extraTallChance;

    public SugarcaneGen(DependencyInjector di) {
        super(
            di,
            Infinibiome.MOD_ID + ":sugarcane"
        );

        config = initConfig()
            .setPlantBlockFunc(
                this::getPlantBlocks
            )
            .setSampleBlock(Blocks.SUGAR_CANE)
            .setAboveWater()
            .setRate(
                new Interval(0.1, 0.1)
            )
            .setRadius(
                new Interval(7, 7)
            )
            .setDensity(
                new Interval(0.4, 0.4)
            )
            .neverInMushroomIsland()
            //hasWater makes sure this wont spawn with only frozen water
            .anyTemperatureIncludingFreezing()
            .setHumdity(
                GenHelper.WETISH
            )
            .setNoSpawnRegion()
            .setExtraConditions(
                onlyInLandMass(
                    di,
                    LandmassInfo::isLand
                )
            )
            .setBoneMealPlantFunc(
                this::getPlantBlocksOnBoneMeal
            );

        this.extraTallChance =
            Helper.initUniformNoise(seed.newSeed("extraTallChance"), Helper.COMMON_SCALE)
                .mapInterval(
                    new Interval(0.0, 0.5)
                )
                .randomBool(BlockPos2D.INFO, seed);
    }

    private List<BlockState> getPlantBlocks(BlockPos plantPos, IWorldReader world, Random random) {
        if (!hasWater(plantPos.down(), world)) return Collections.emptyList();

        if (extraTallChance.test(MCHelper.to2D(plantPos))) {
            return Collections.nCopies(
                4,
                Blocks.SUGAR_CANE.getDefaultState()
            );
        }
        return Collections.nCopies(
            3,
            Blocks.SUGAR_CANE.getDefaultState()
        );
    }

    private boolean hasWater(BlockPos pos, IWorldReader world){
        for (Direction d: MCHelper.NSWE){
            if (PlantHelper.iceAtPos(pos.offset(d), world, posData)) continue;

            if (MCHelper.isMostlyWater(world.getBlockState(pos.offset(d)))) return true;
        }
        return false;
    }

    private List<BlockState> getPlantBlocksOnBoneMeal(BlockPos plantPos, IWorldReader world, Random random) {
        return Lists.newArrayList(Blocks.SUGAR_CANE.getDefaultState());
    }
}