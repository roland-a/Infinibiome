package weightedgpa.infinibiome.internal.minecraftImpl;

import net.minecraft.block.BlockState;
import net.minecraft.entity.EntityClassification;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeManager;
import net.minecraft.world.biome.provider.BiomeProvider;
import net.minecraft.world.chunk.IChunk;
import net.minecraft.world.gen.*;
import net.minecraft.world.gen.feature.IFeatureConfig;
import net.minecraft.world.gen.feature.structure.Structure;
import net.minecraft.world.gen.feature.template.TemplateManager;
import net.minecraft.world.server.ServerWorld;
import weightedgpa.infinibiome.api.dependency.DependencyInjector;
import weightedgpa.infinibiome.internal.misc.DIRootGen;

import javax.annotation.Nullable;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

public final class WrappedChunkGenerator extends ChunkGenerator<GenerationSettings> {
    private volatile IBChunkGenerator actualChunkGenerator = null;
    //private transient IBBiomeProvider actualBiomeProvider = null;

    public WrappedChunkGenerator(ServerWorld world) {
        super(world, null, null);

        new Thread(
            () -> {
                try {
                    actualChunkGenerator = (IBChunkGenerator) DIRootGen.createDiWhenReady(world).get(ChunkGenerator.class);
                }
                catch (Exception e){
                    e.printStackTrace();

                    throw new RuntimeException(e);
                }
            }
        )
        .start();
    }

    @Override
    public BiomeProvider getBiomeProvider() {
        while (actualChunkGenerator == null){}

        return actualChunkGenerator.getBiomeProvider();
    }

    @Override
    public void generateBiomes(IChunk chunkIn) {
        actualChunkGenerator.generateBiomes(chunkIn);
    }

    @Override
    public void makeBase(IWorld worldIn, IChunk chunkIn) {
        actualChunkGenerator.makeBase(worldIn, chunkIn);
    }

    @Override
    public void decorate(WorldGenRegion region) {
        actualChunkGenerator.decorate(region);
    }

    @Override
    public List<Biome.SpawnListEntry> getPossibleCreatures(EntityClassification creatureType, BlockPos pos) {
        return actualChunkGenerator.getPossibleCreatures(creatureType, pos);
    }

    @Override
    public int getGroundHeight() {
        return actualChunkGenerator.getGroundHeight();
    }

    @Override
    public int getSeaLevel() {
        return actualChunkGenerator.getSeaLevel();
    }

    @Override
    public int getHeight(int x, int z, Heightmap.Type type) {
        return actualChunkGenerator.getHeight(x, z, type);
    }

    @Override
    public boolean hasStructure(Biome biomeIn, Structure<? extends IFeatureConfig> structureIn) {
        return actualChunkGenerator.hasStructure(biomeIn, structureIn);
    }

    @Override
    @Nullable
    public <C extends IFeatureConfig> C getStructureConfig(Biome biomeIn, Structure<C> structureIn) {
        return actualChunkGenerator.getStructureConfig(biomeIn, structureIn);
    }

    @Override
    public void generateStructures(BiomeManager __, IChunk chunk, ChunkGenerator<?> ___, TemplateManager templateManager) {
        actualChunkGenerator.generateStructures(__, chunk, ___, templateManager);
    }

    @Override
    @Nullable
    public BlockPos findNearestStructure(World worldIn, String name, BlockPos pos, int radius, boolean p_211403_5_) {
        return actualChunkGenerator.findNearestStructure(worldIn, name, pos, radius, p_211403_5_);
    }

    @Override
    public void spawnMobs(ServerWorld worldIn, boolean spawnHostileMobs, boolean spawnPeacefulMobs) {
        actualChunkGenerator.spawnMobs(worldIn, spawnHostileMobs, spawnPeacefulMobs);
    }

    @Override
    public void spawnMobs(WorldGenRegion worldGenRegion) {
        actualChunkGenerator.spawnMobs(worldGenRegion);
    }

    @Override
    public void generateSurface(WorldGenRegion worldGenRegion, IChunk iChunk) {
        actualChunkGenerator.generateSurface(worldGenRegion, iChunk);
    }

    @Override
    public void generateCarvers(BiomeManager p_225550_1_, IChunk p_225550_2_, GenerationStage.Carving p_225550_3_) {
        actualChunkGenerator.generateCarvers(p_225550_1_, p_225550_2_, p_225550_3_);
    }

    @Override
    public Biome getBiome(BiomeManager biomeManagerIn, BlockPos posIn) {
        return actualChunkGenerator.getBiome(biomeManagerIn, posIn);
    }

    @Override
    public GenerationSettings getSettings() {
        return actualChunkGenerator.getSettings();
    }

    @Override
    public long getSeed() {
        return actualChunkGenerator.getSeed();
    }

    @Override
    public int getMaxHeight() {
        return actualChunkGenerator.getMaxHeight();
    }

    @Override
    public void generateStructureStarts(IWorld worldIn, IChunk chunkIn) {
        actualChunkGenerator.generateStructureStarts(worldIn, chunkIn);
    }

    @Override
    public int getNoiseHeight(int x, int z, Heightmap.Type heightmapType) {
        return actualChunkGenerator.getNoiseHeight(x, z, heightmapType);
    }

    @Override
    public int getNoiseHeightMinusOne(int x, int z, Heightmap.Type heightmapType) {
        return actualChunkGenerator.getNoiseHeightMinusOne(x, z, heightmapType);
    }

    /*
    private class WrappedBiomeProvider extends BiomeProvider {
        protected WrappedBiomeProvider() {
            super(new HashSet<>());
        }

        @Override
        public List<Biome> getBiomesToSpawnIn() {
            return actualBiomeProvider.getBiomesToSpawnIn();
        }

        @Override
        public Biome getNoiseBiome(int x, int y, int z) {
            return actualBiomeProvider.getNoiseBiome(x, y, z);
        }

        @Override
        public boolean hasStructure(Structure<?> structure) {
            return actualBiomeProvider.hasStructure(structure);
        }

        @Override
        public BlockPos func_225531_a_(int x, int y, int z, int dist, List<Biome> list, Random rand) {
            return actualBiomeProvider.func_225531_a_(x, y, z, dist, list, rand);
        }

        @Override
        public Set<Biome> getBiomes(int xIn, int yIn, int zIn, int radius) {
            return actualBiomeProvider.getBiomes(xIn, yIn, zIn, radius);
        }

        @Override
        public float func_222365_c(int p_222365_1_, int p_222365_2_) {
            return actualBiomeProvider.func_222365_c(p_222365_1_, p_222365_2_);
        }

        @Override
        public Set<BlockState> getSurfaceBlocks() {
            return actualBiomeProvider.getSurfaceBlocks();
        }
    }

     */
}
