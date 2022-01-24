package weightedgpa.infinibiome.internal.generators.chunks;

import net.minecraft.block.Blocks;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.chunk.IChunk;
import weightedgpa.infinibiome.api.Infinibiome;
import weightedgpa.infinibiome.api.dependency.DependencyInjector;
import weightedgpa.infinibiome.internal.floatfunc.generators.RandomGen;
import weightedgpa.infinibiome.internal.misc.*;
import weightedgpa.infinibiome.api.generators.*;

import java.util.Random;

public final class BedrockGen implements ChunkGen {
    private static final int HEIGHT = 6;

    private final RandomGen randomProducer;

    public BedrockGen(DependencyInjector di) {
        Seed seed = di.get(Seed.class).newSeed(Infinibiome.MOD_ID + ":bedrock");

        this.randomProducer = new RandomGen(seed);
    }

    @Override
    public Timing getChunkGenTiming() {
        return ChunkGenTimings.BEDROCK;
    }

    @Override
    public void buildChunk(ChunkPos chunkPos, IChunk chunk) {
        Random random = randomProducer.getRandom(chunkPos.x, chunkPos.z);

        MCHelper.forEachPos(
            chunkPos,
            pos -> {
                chunk.setBlockState(pos.to3D(0), Blocks.BEDROCK.getDefaultState(), MCHelper.DEFAULT_IS_MOVING);

                for (int y = 1; y <= HEIGHT; y++){
                    if (random.nextInt(HEIGHT) >= y){
                        chunk.setBlockState(pos.to3D(y), Blocks.BEDROCK.getDefaultState(), MCHelper.DEFAULT_IS_MOVING);
                    }
                }
            }
        );
    }
}
