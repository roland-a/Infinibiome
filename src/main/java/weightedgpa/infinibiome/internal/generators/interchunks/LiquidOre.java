package weightedgpa.infinibiome.internal.generators.interchunks;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorld;
import net.minecraft.world.IWorldReader;
import weightedgpa.infinibiome.api.Infinibiome;
import weightedgpa.infinibiome.api.dependency.DependencyInjector;
import weightedgpa.infinibiome.internal.misc.MCHelper;

import java.util.Random;

public final class LiquidOre extends SmallObjectGenBase {
    private final Type type;

    public LiquidOre(Type type, DependencyInjector di) {
        super(di, Infinibiome.MOD_ID + ":liquidOre" + type.name());

        this.type = type;

        config = initConfig()
            .getGenerateFunc(
                this::generate
            )
            .setCount(
                type.frequency
            )
            .setAttemptsPerCount(1)
            .randomBetweenHeight(
                0,
                type.maxHeight
            )
            .noChancePerChunk()
            .noExtraConditions();
    }

    private void generate(BlockPos pos, IWorld world, Random random){
        if (!world.getBlockState(pos).getBlock().equals(Blocks.STONE)) return;

        if (!nextToOneAir(pos, world)) return;

        if (nextToLiquid(pos, world)) return;

        if (!world.getBlockState(pos.up()).getBlock().equals(Blocks.STONE)) return;

        if (!world.getBlockState(pos.down()).getBlock().equals(Blocks.STONE)) return;

        world.setBlockState(pos, type.block, MCHelper.DEFAULT_FLAG);

        world.getPendingFluidTicks().scheduleTick(
            pos,
            type.block.getFluidState().getFluid(),
            0
        );
    }

    private boolean nextToOneAir(BlockPos pos, IWorldReader world){
        int count = 0;

        for (Direction d: MCHelper.NSWE){
            if (world.getBlockState(pos.offset(d)).isAir()) {
                count += 1;
            }
        }
        return count == 1;
    }

    private boolean nextToLiquid(BlockPos pos, IWorldReader world){
        for (Direction d: Direction.values()){
            if (world.getBlockState(pos.offset(d)).getMaterial().isLiquid()) return true;
        }
        return false;
    }


    public enum Type{
        WATER(Blocks.WATER.getDefaultState(), 32, 128),
        LAVA(Blocks.LAVA.getDefaultState(), 4, MCHelper.WATER_HEIGHT);

        private final BlockState block;
        private final double frequency;
        private final int maxHeight;

        Type(BlockState block, double frequency, int maxHeight) {
            this.block = block;
            this.frequency = frequency;
            this.maxHeight = maxHeight;
        }
    }
}
