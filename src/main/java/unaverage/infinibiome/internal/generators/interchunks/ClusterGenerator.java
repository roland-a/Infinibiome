package weightedgpa.infinibiome.internal.generators.interchunks;

/*
import net.minecraft.util.math.BlockPos;
import weightedgpa.infinibiome.api.generators.InterChunkGen;
import weightedgpa.infinibiome.api.pos.BlockPos2D;
import weightedgpa.infinibiome.api.pos.InterChunkPos;
import weightedgpa.infinibiome.internal.floatfunc.FloatFunc;
import weightedgpa.infinibiome.internal.floatfunc.IntFunc;


public abstract class ClusterGenerator {
    public void generateAtInterChunk(
        InterChunkPos interChunkPos,
        int count,
        FloatFunc<BlockPos2D> radiusFunc,
        FloatFunc<BlockPos2D> densityFunc,
        IntFunc<BlockPos2D> heightFunc,
        GenerateAtFunc generateAtFunc
    ){

    }

    public void generateCluster(
        BlockPos2D centerPos,
        float radius,
        float density,
        IntFunc<BlockPos2D> getHeightFunc,
        GenerateAtFunc generateAtFunc
    ){


    }

    interface GenerateAtFunc {
        void generateAt(BlockPos pos);
    }

    /*
    protected ClusterGenerator(DependencyInjector di, String seedBranch) {
        super(di, seedBranch);
    }

    @Override
    public void generate(InterChunkPos interChunkPos, IWorld interChunks) {
        Random random = randomGen.getRandom(interChunkPos);

        int count = getCount(interChunkPos, interChunks, random);

        for (int i = 0; i < count; i++){
            BlockPos2D centerPos2D = interChunkPos.getRandomCenterPos(random);

            generateClusterAt(centerPos2D, interChunks, random);
        }
    }

    protected abstract float getRadius(BlockPos2D pos, IWorld world, Random random);

    protected abstract float getDensity(BlockPos2D pos, IWorld world, Random random);

    protected abstract int getCount(InterChunkPos pos, IWorld world, Random random);

    protected void generateClusterAt(BlockPos2D centerPos, IWorld world, Random random){
        float radius = getRadius(centerPos, world, random);

        int radiusCeil = (int)Math.ceil(radius);

        float density = getDensity(centerPos, world, random);

        for (int x = -radiusCeil; x <= radiusCeil; x++){
            for (int z = -radiusCeil; z <= radiusCeil; z++){
                if (!MathHelper.randomBool(density, random)) continue;

                BlockPos2D currPos2D = centerPos.offset(x, z);

                if (MathHelper.getDistance(currPos2D, centerPos) > radius) continue;

                int currHeight = getHeight(currPos2D, centerPos, world, random);

                BlockPos currPos = currPos2D.to3D(currHeight);

                generateAt(currPos, centerPos, world, random);
            }
        }
    }

    protected abstract int getHeight(BlockPos2D thisPos, BlockPos2D centerPos, IWorld world, Random random);

    protected abstract void generateAt(BlockPos thisPos, BlockPos2D centerPos, IWorld world, Random random);

}
*/
