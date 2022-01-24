package weightedgpa.infinibiome.internal.generators.interchunks.struct;

import com.google.common.collect.Lists;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorld;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.gen.feature.Feature;
import weightedgpa.infinibiome.api.Infinibiome;
import weightedgpa.infinibiome.internal.floatfunc.util.Interval;
import weightedgpa.infinibiome.api.dependency.DependencyInjector;
import weightedgpa.infinibiome.internal.misc.MCHelper;
import weightedgpa.infinibiome.internal.misc.MathHelper;
import weightedgpa.infinibiome.api.pos.InterChunkPos;
import weightedgpa.infinibiome.api.posdata.LandmassInfo;
import weightedgpa.infinibiome.api.posdata.PosDataHelper;

import javax.annotation.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import static weightedgpa.infinibiome.internal.generators.utils.condition.ConditionHelper.*;

public final class JunglePyramidGen extends StructGenBase {


    private static final Map<Block, BlockState> INFESTED_BLOCKS = new HashMap<>();

    private static final List<Block> PUZZLE_COMPONENTS = Lists.newArrayList(
        Blocks.CHISELED_STONE_BRICKS,
        Blocks.PISTON,
        Blocks.STICKY_PISTON,
        Blocks.REPEATER
    );

    private static final double INFESTED_RATE = 1/20d;

    static {
        INFESTED_BLOCKS.put(
            Blocks.COBBLESTONE,
            Blocks.INFESTED_COBBLESTONE.getDefaultState()
        );
        INFESTED_BLOCKS.put(
            Blocks.CHISELED_STONE_BRICKS,
            Blocks.INFESTED_CHISELED_STONE_BRICKS.getDefaultState()
        );
    }

    public JunglePyramidGen(DependencyInjector di) {
        super(di, Infinibiome.MOD_ID + ":junglePyramid");

        baseConfig = initConfig()
            .withStruct(
                Feature.JUNGLE_TEMPLE
            )
            .withChance(
                Config.class, 8
            )
            .addExtraConditions(
                onlyInTemperature(
                    di,
                    PosDataHelper.HOT_INTERVAL
                ),
                onlyInHumidity(
                    di,
                    PosDataHelper.WET_INTERVAL
                ),
                onlyInHeight(
                    di,
                    new Interval(MCHelper.WATER_HEIGHT + 1, Double.POSITIVE_INFINITY)
                ),
                onlyInLandMass(
                    di,
                    LandmassInfo::isLand
                ),
                onlyInTreeDensity(
                    di,
                    new Interval(0.5f, Double.POSITIVE_INFINITY)
                )
            );
    }

    @Override
    public void postGenerate(InterChunkPos interChunkPos, IWorld world) {
        Random random = randomGen.getRandom(interChunkPos.getX(), interChunkPos.getZ());

        interChunkPos.forEachCenterPos(pos2D -> {
            int minHeight = MCHelper.getHighestTerrainHeight(pos2D, world) - 10;

            for (int y = 255; y > minHeight; y--){
                BlockPos pos = pos2D.to3D(y);

                BlockState replacementBlock = getReplacementBlock(pos, world, random);

                if (replacementBlock == null) continue;

                world.setBlockState(pos, replacementBlock, MCHelper.DEFAULT_FLAG);
            }
        });
    }

    @Nullable
    private BlockState getReplacementBlock(BlockPos pos, IWorldReader world, Random random){
        if (belowRedstone(pos, world)) return null;

        Block block = world.getBlockState(pos).getBlock();

        if (isNearPuzzleComponents(pos, world)){
            if (block.equals(Blocks.MOSSY_COBBLESTONE)){
                return Blocks.INFESTED_COBBLESTONE.getDefaultState();
            }
            return INFESTED_BLOCKS.get(block);
        }

        if (!MathHelper.randomBool(INFESTED_RATE, random)) return null;

        return INFESTED_BLOCKS.get(block);
    }

    private boolean isNearPuzzleComponents(BlockPos pos, IWorldReader world){
        for (int x = -2; x <= 2; x++){
            for (int y = -2; y <= 2; y++){
                for (int z = -2; z <= 2; z++){
                    BlockPos currPos = pos.add(x, y, z);

                    Block currBlock = world.getBlockState(currPos).getBlock();

                    if (PUZZLE_COMPONENTS.contains(currBlock)) return true;
                }
            }
        }
        return false;
    }

    private boolean belowRedstone(BlockPos pos, IWorldReader world){
        return world.getBlockState(pos.up()).getBlock().equals(Blocks.REDSTONE_WIRE);
    }

    public static final class Config extends StructConfigBase{
        public Config(DependencyInjector di){
            super(di);
        }

        @Override
        String name() {
            return "jungle_pyramid_rate";
        }

        @Override
        double defaultRate() {
            return 0.1;
        }

    }
}
