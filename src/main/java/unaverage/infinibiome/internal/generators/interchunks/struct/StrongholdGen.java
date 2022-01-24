package weightedgpa.infinibiome.internal.generators.interchunks.struct;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.gen.feature.Feature;
import weightedgpa.infinibiome.api.Infinibiome;
import weightedgpa.infinibiome.api.generators.nonworldgen.DefaultConfig;
import weightedgpa.infinibiome.api.generators.nonworldgen.ConfigIO;
import weightedgpa.infinibiome.api.dependency.DependencyInjector;
import weightedgpa.infinibiome.api.pos.BlockPos2D;
import weightedgpa.infinibiome.api.posdata.PosDataKeys;
import weightedgpa.infinibiome.internal.misc.Helper;
import weightedgpa.infinibiome.internal.misc.MCHelper;
import weightedgpa.infinibiome.internal.pointsprovider.FinitePoints;

import java.util.Arrays;
import java.util.Random;

public final class StrongholdGen extends StructGenBase {
    private final Config config;

    public StrongholdGen(DependencyInjector di){
        super(di, Infinibiome.MOD_ID + ":stronghold");

        this.config = di.get(Config.class);

        this.baseConfig = initConfig()
            .withStruct(
                Feature.STRONGHOLD
            )
            .withPoints(
                new FinitePoints<>(
                    Arrays.asList(initLocations()),
                    MCHelper.CHUNK_POS_INFO
                )
            )
            .noExtraCondition();
    }

    //mostly copy and paste of vanilla code
    private ChunkPos[] initLocations(){
        int i2 = config.distance;
        int j2 = config.count;
        int i = config.spread;

        ChunkPos[] result = new ChunkPos[j2];

        int j = 0;

        Random random = new Random();

        random.setSeed(chunkGenerator.getSeed());

        double d1 = random.nextDouble() * Math.PI * 2.0D;
        int k = j;
        if (j < result.length) {
            int l = 0;
            int i1 = 0;

            for(int j1 = 0; j1 < result.length; ++j1) {
                double d0 = (double)(4 * i2 + i2 * i1 * 6) + (random.nextDouble() - 0.5D) * (double)i2 * 2.5D;
                int k1 = (int)Math.round(Math.cos(d1) * d0);
                int l1 = (int)Math.round(Math.sin(d1) * d0);
                BlockPos blockpos = Helper.findSuitableSpot(new BlockPos((k1 << 4) + 8, 0, (l1 << 4) + 8), 112, this::isValidPos, MCHelper.MC_POS_INFO);

                if (blockpos != null) {
                    k1 = blockpos.getX() >> 4;
                    l1 = blockpos.getZ() >> 4;
                }

                if (j1 >= k) {
                    result[j1] = new ChunkPos(k1, l1);
                }

                d1 += (Math.PI * 2D) / (double)i;
                ++l;
                if (l == i) {
                    ++i1;
                    l = 0;
                    i = i + 2 * i / (i1 + 1);
                    i = Math.min(i, result.length - j1);
                    d1 += random.nextDouble() * Math.PI * 2.0D;
                }
            }
        }

        return result;
    }

    private boolean isValidPos(BlockPos pos){
        if (
            !Helper.passesSurroundingTest(
                MCHelper.to2D(pos),
                100,
                p -> posData.get(PosDataKeys.MAPPED_HEIGHT, p) > 30,
                BlockPos2D.INFO
            )
        ) {
            return false;
        }
        return true;
    }

    public static class Config implements DefaultConfig {
        private final int count;
        private final int distance;
        private final int spread;

        public Config(DependencyInjector di){
            ConfigIO config = di.get(ConfigIO.class).subConfig("STRUCT").subConfig("stronghold");

            this.count = config.getInt(
                "count",
                128,
                0,
                1024,
                ""
            );

            this.distance = config.getInt(
                "distance",
                32,
                1,
                1024,
                ""
            );

            this.spread = config.getInt(
                "spread",
                3,
                1,
                1024,
                ""
            );
        }
    }
}
