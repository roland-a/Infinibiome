package weightedgpa.infinibiome.internal.generators.interchunks;


import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorldReader;
import weightedgpa.infinibiome.api.Infinibiome;
import weightedgpa.infinibiome.api.dependency.DependencyInjector;
import weightedgpa.infinibiome.internal.misc.*;
import weightedgpa.infinibiome.api.posdata.PosDataHelper;
import weightedgpa.infinibiome.internal.floatfunc.util.Interval;
import weightedgpa.infinibiome.api.generators.*;
import weightedgpa.infinibiome.api.pos.InterChunkPos;
import weightedgpa.infinibiome.api.pos.BlockPos2D;
import net.minecraft.world.IWorld;
import weightedgpa.infinibiome.internal.floatfunc.FloatFunc;
import weightedgpa.infinibiome.internal.floatfunc.modifiers.IntervalMapperWrapper;
import weightedgpa.infinibiome.internal.generators.utils.GeneratorBase;
import weightedgpa.infinibiome.internal.misc.Helper;


import java.util.Random;

public final class SurfacePoolWaterGen extends GeneratorBase implements InterChunkGen {
    private final FloatFunc<BlockPos2D> chancePerChunkFunc;
    private final FloatFunc<BlockPos2D> waterRadiusFunc;
    private final FloatFunc<BlockPos2D> sideRadiusFunc;

    public SurfacePoolWaterGen(DependencyInjector di) {
        super(di, Infinibiome.MOD_ID + ":surfaceWaterPool");

        this.chancePerChunkFunc =
            new IntervalMapperWrapper<>(
                PosDataHelper.initHumidityNoise2D(di)
            )
            .addBranch(
                PosDataHelper.SEMI_WET_INTERVAL,
                0.1f, 0.2f
            ).addBranch(
                PosDataHelper.WET_INTERVAL,
                0.2f, 0.5f
            );

        this.waterRadiusFunc = Helper.<BlockPos2D>initUniformNoise(seed, 10)
            .mapInterval(
                new Interval(3f, 5f)
            );

        this.sideRadiusFunc = FloatFunc.constFunc(2f);
    }

    @Override
    public Timing getInterChunkTiming() {
        return InterChunkGenTimings.WATER_POOL;
    }

    private SurfacePool.Config getConfig(){
        return new SurfacePool.Config(
            waterRadiusFunc,
            sideRadiusFunc,
            (pos, world, rand) -> Blocks.WATER.getDefaultState(),
            (pos, world, rand) -> getOuterBlock(pos, world),
            (pool, world, rand) -> true,
            (pool, world, rand) -> {}
        );
    }

    private BlockState getOuterBlock(BlockPos pos, IWorldReader world) {
        if (world.getBlockState(pos).getBlock().equals(Blocks.STONE)){
            return Blocks.STONE.getDefaultState();
        }

        return world.getBlockState(
            MCHelper.to2D(pos).to3D(
                p -> MCHelper.getHighestTerrainHeight(p, world)
            )
        );
    }

    @Override
    public void generate(InterChunkPos interChunkPos, IWorld interChunks) {
        Random random = randomGen.getRandom(interChunkPos.getX(), interChunkPos.getZ());

        double chancePerChunk = chancePerChunkFunc.getOutput(interChunkPos.getLowestCenterBlockPos());

        if (!MathHelper.randomBool(chancePerChunk, random)){
            return;
        }

        for (int i = 0; i < 8; i++){
            BlockPos2D waterLakeCenter = interChunkPos.getRandomCenterPos(random);

            boolean flag = SurfacePool.tryGeneratePoolAt(waterLakeCenter, getConfig(), interChunks, posData, random);

            if (flag) break;
        }
    }

}
