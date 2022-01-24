package weightedgpa.infinibiome.internal.generators.interchunks.plant;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.SixWayBlock;
import net.minecraft.state.BooleanProperty;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorldReader;
import org.jetbrains.annotations.Nullable;
import weightedgpa.infinibiome.api.Infinibiome;
import weightedgpa.infinibiome.api.dependency.DependencyInjector;
import weightedgpa.infinibiome.api.pos.BlockPos2D;
import weightedgpa.infinibiome.internal.floatfunc.util.Interval;
import weightedgpa.infinibiome.internal.generators.utils.GenHelper;
import weightedgpa.infinibiome.internal.misc.MCHelper;


import java.util.*;

import static weightedgpa.infinibiome.internal.generators.utils.condition.ConditionHelper.onlyInRegion;

public final class VineGen extends PlantGenBase {
    private final Type type;

    public VineGen(Type type, DependencyInjector di) {
        super(di, Infinibiome.MOD_ID + ":vine" + type.name());

        this.type = type;

        config = initConfig()
            .setPlantBlockFunc(
                this::getPlant
            )
            .setSampleBlock(
                Blocks.VINE
            )
            .setAboveWater()
            .setScatteredRate(
                new Interval(32, 255)
            )
            .alsoInMushroomIsland()
            .anyNonFreezingTemperature()
            .setHumdity(
                type.humidity
            )
            .setSpawnRegion(type.regionRate)
            .setNoExtraConditions()
            .setNoGroundCheck();
    }

    @Override
    public String toString() {
        return "VineGen{" +
            "type=" + type +
            '}';
    }

    public enum Type{
        WET(PlantHelper.COMMON_REGION_RATE * 2, GenHelper.WETISH),
        DRY(PlantHelper.COMMON_REGION_RATE, GenHelper.DRYISH);

        private final double regionRate;
        private final Interval humidity;

        Type(double regionRate, Interval humidity) {
            this.regionRate = regionRate;
            this.humidity = humidity;
        }

    }

    private List<@Nullable BlockState> getPlant(BlockPos plantPos3D, IWorldReader world, Random random){
        BlockPos2D plantPos = MCHelper.to2D(plantPos3D);

        Integer maxHeight = getHighestHeight(plantPos, world, random);

        if (maxHeight == null) return Collections.emptyList();

        BlockState vineBlockState = getValidVineState(
            plantPos.to3D(maxHeight),
            world
        );

        assert vineBlockState != null;

        List<BlockState> result = new ArrayList<>();

        boolean obstructed = false;

        for (int y = maxHeight; y >= plantPos3D.getY(); y--){
            if (!MCHelper.isMostlyAir(world.getBlockState(plantPos.to3D(y)))){
                obstructed = true;
            }

            if (obstructed) {
                result.add(0, null);
            } else {
                result.add(0, vineBlockState);
            }
        }
        return result;
    }

    @Nullable
    private Integer getHighestHeight(BlockPos2D plantPos, IWorldReader world, Random random){
        int startingHeight = MCHelper.getHighestTerrainHeight(plantPos, world);
        int maxHeight = MCHelper.getHighestNonAirY(plantPos, world);

        BlockPos.Mutable checkPos = new BlockPos.Mutable(plantPos.getBlockX(), startingHeight, plantPos.getBlockZ());

        IntList validHeights = new IntArrayList();

        for (; checkPos.getY() <= maxHeight; checkPos.move(Direction.UP)){
            if (getValidVineState(checkPos, world) != null){
                validHeights.add(checkPos.getY());
            }
        }

        if (validHeights.isEmpty()){
            return null;
        }

        int randomIndex = random.nextInt(validHeights.size());

        return validHeights.getInt(randomIndex);
    }

    @Nullable
    private BlockState getValidVineState(BlockPos pos, IWorldReader world){
        BlockState block = Blocks.VINE.getDefaultState();

        boolean set = false;

        for (Direction d: MCHelper.NSWE){
            if (canSustainVine(pos, world, d)){
                block = setState(block, d);

                set = true;
            }
        }

        if (!set) return null;

        return block;
    }

    private boolean canSustainVine(BlockPos pos, IWorldReader world, Direction direction){
        return setState(
            Blocks.VINE.getDefaultState(),
            direction
        )
        .isValidPosition(
            world,
            pos
        );
    }

    private BlockState setState(BlockState block, Direction direction){
        BooleanProperty property = SixWayBlock.FACING_TO_PROPERTY_MAP.get(direction);

        return block.with(property, true);
    }
}