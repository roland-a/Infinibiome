package weightedgpa.infinibiome.internal.display;

import com.google.common.collect.Lists;
import net.minecraft.util.registry.Bootstrap;
import net.minecraft.world.IWorld;
import net.minecraft.world.chunk.IChunk;
import net.minecraft.world.gen.ChunkGenerator;
import net.minecraft.world.gen.GenerationSettings;
import net.minecraft.world.gen.Heightmap;
import net.minecraft.world.gen.WorldGenRegion;
import weightedgpa.infinibiome.api.posdata.PosDataKeys;
import weightedgpa.infinibiome.internal.dependency.DependencyInjectorImpl;
import weightedgpa.infinibiome.internal.generators.nonworldgen.ConfigIOImpl;
import weightedgpa.infinibiome.api.generators.nonworldgen.ConfigIO;
import weightedgpa.infinibiome.api.dependency.DependencyInjector;
import weightedgpa.infinibiome.api.dependency.DependencyModule;
import weightedgpa.infinibiome.api.generators.Seed;
import weightedgpa.infinibiome.api.posdata.PosDataProvider;
import weightedgpa.infinibiome.internal.generators.DefaultModule;
import weightedgpa.infinibiome.internal.generators.posdata.LakeGen;
import weightedgpa.infinibiome.internal.generators.posdata.MushroomIslandGen;
import weightedgpa.infinibiome.internal.minecraftImpl.world.NullWorld;

import java.util.ArrayList;
import java.util.List;

abstract class DataMapBase extends DisplayMapBase {
    protected final PosDataProvider posData;
    protected final DependencyInjector di;

    static {
        Bootstrap.register();
    }

    DataMapBase(int scale, String seedBranch) {
        super(scale);

        Seed seed = Seed.ROOT.newSeed(seedBranch);

        List<DependencyModule> depManager = new ArrayList<>();

        depManager.add(
            DefaultModule.INSTANCE
        );

        depManager.add(
            t -> {
                t.addItem(
                    Seed.class,
                    __ -> seed
                );

                t.addItem(
                    ConfigIO.class,
                    __ -> new ConfigIOImpl(
                        Lists.newArrayList(DefaultModule.INSTANCE)
                    )
                );

                t.addItem(
                    ChunkGenerator.class,
                    __ -> new NullChunkGenerator()
                );

                //t.blacklist(o -> o instanceof RiverGen);
                t.blacklist(o -> o instanceof LakeGen);
                t.blacklist(o -> o instanceof MushroomIslandGen);
            }

        );

        this.di = new DependencyInjectorImpl(depManager).initInjectorIgnoreErrors();

        PosDataKeys.init();

        this.posData = di.get(PosDataProvider.class);
    }

    private class NullChunkGenerator extends ChunkGenerator<GenerationSettings> {
        private NullChunkGenerator() {
            super(new NullWorld(), null, null);
        }

        @Override
        public int getGroundHeight() {
            return 0;
        }

        @Override
        public void makeBase(IWorld worldIn, IChunk chunkIn) {
        }

        @Override
        public void generateSurface(WorldGenRegion worldGenRegion, IChunk iChunk) {
        }

        @Override
        public int getHeight(int p_222529_1_, int p_222529_2_, Heightmap.Type heightmapType) {
            return 0;
        }
    }
}
