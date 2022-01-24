package weightedgpa.infinibiome.internal.minecraftImpl;

import net.minecraft.entity.EntityClassification;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.MutableBoundingBox;
import net.minecraft.world.*;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeContainer;
import net.minecraft.world.biome.BiomeManager;
import net.minecraft.world.biome.provider.BiomeProvider;
import net.minecraft.world.chunk.ChunkPrimer;
import net.minecraft.world.chunk.IChunk;
import net.minecraft.world.gen.*;
import net.minecraft.world.gen.feature.IFeatureConfig;
import net.minecraft.world.gen.feature.structure.Structure;
import net.minecraft.world.gen.feature.structure.StructureStart;
import net.minecraft.world.gen.feature.template.TemplateManager;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.util.Lazy;
import org.apache.commons.lang3.mutable.Mutable;
import org.apache.commons.lang3.mutable.MutableObject;
import weightedgpa.infinibiome.api.generators.StructGen;
import weightedgpa.infinibiome.api.dependency.DependencyInjector;
import weightedgpa.infinibiome.internal.misc.MCHelper;
import weightedgpa.infinibiome.api.posdata.PosDataKeys;
import weightedgpa.infinibiome.api.posdata.PosDataProvider;
import weightedgpa.infinibiome.api.pos.InterChunkPos;
import weightedgpa.infinibiome.api.pos.BlockPos2D;
import weightedgpa.infinibiome.internal.generators.chunks.ChunkGens;
import weightedgpa.infinibiome.internal.generators.interchunks.InterChunkGens;
import weightedgpa.infinibiome.internal.generators.nonworldgen.spawnList.MobSpawnListModifiers;
import weightedgpa.infinibiome.internal.generators.interchunks.struct.StructGens;
import weightedgpa.infinibiome.internal.generators.nonworldgen.spawners.MobSpawners;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;

public final class IBChunkGenerator extends ChunkGenerator<GenerationSettings> {
    private final PosDataProvider posData;

    private final Lazy<ChunkGens> chunkGens;
    private final Lazy<InterChunkGens> interChunkGens;
    private final Lazy<MobSpawners> tickers;
    private final Lazy<MobSpawnListModifiers> mobSpawnListModifiers;
    private final Lazy<StructGens> structs;

    public IBChunkGenerator(DependencyInjector di){
        super(
            di.get(ServerWorld.class),
            di.get(BiomeProvider.class),
            new GenerationSettings()
        );

        this.posData = di.get(PosDataProvider.class);
        this.chunkGens = Lazy.of(() -> di.get(ChunkGens.class));
        this.interChunkGens = Lazy.of(() -> di.get(InterChunkGens.class));
        this.structs = Lazy.of(() -> di.get(StructGens.class));
        this.tickers = Lazy.of(() -> di.get(MobSpawners.class));
        this.mobSpawnListModifiers = Lazy.of(() -> di.get(MobSpawnListModifiers.class));
    }

    @Override
    public void generateBiomes(IChunk chunkIn) {
        ChunkPos chunkpos = chunkIn.getPos();
        ((ChunkPrimer)chunkIn).setBiomes(new BiomeContainer(chunkpos, biomeProvider));
    }

    @Override
    public Biome getBiome(BiomeManager biomeManagerIn, BlockPos posIn) {
        return super.getBiome(biomeManagerIn, posIn);
    }

    @Override
    public void makeBase(IWorld worldIn, IChunk chunkIn) {
        chunkGens.get().buildChunk(
            chunkIn.getPos(),
            chunkIn
        );
    }

    @Override
    public void decorate(WorldGenRegion region) {
        InterChunkPos interChunkPos = new InterChunkPos(new ChunkPos(
            region.getMainChunkX(),
            region.getMainChunkZ()
        ));

        interChunkGens.get().generateAll(
            interChunkPos,
            region
        );
    }

    @Override
    public List<Biome.SpawnListEntry> getPossibleCreatures(EntityClassification creatureType, BlockPos pos) {
        if (creatureType == EntityClassification.CREATURE){
            return Collections.emptyList();
        }

        //todo figure out why making a defensive copy doesnt work
        // might blow up
        List<Biome.SpawnListEntry> result = IBBiomes.getBiome(MCHelper.to2D(pos), posData).getSpawns(creatureType);

        mobSpawnListModifiers.get().modifyList(pos, creatureType, result, world);

        return result;
    }

    @Override
    public int getGroundHeight() {
        return MCHelper.WATER_HEIGHT;
    }

    @Override
    public int getSeaLevel() {
        return MCHelper.WATER_HEIGHT;
    }

    @Override
    public int getHeight(int x, int z, Heightmap.Type type) {
        int level = (int) posData.get(
            PosDataKeys.MAPPED_HEIGHT,
            new BlockPos2D(x, z)
        ) + 1;

        if (type == Heightmap.Type.OCEAN_FLOOR || type == Heightmap.Type.OCEAN_FLOOR_WG) {
            return level;
        }

        return Math.max(getSeaLevel(), level);
    }


    /*
    @Override
    public int func_222529_a(int x, int z, Heightmap.Type type) {
        BlockPos.Mutable pos = new BlockPos.Mutable(x, 255, z);

        for (; pos.getY()  0; pos.move(Direction.DOWN)){
            BlockState block = world.getBlockState(pos);

            if (type.getHeightLimitPredicate().test(block)){
                return pos.getY() + 1;
            }
        }
        return 0;
    }
     */

    @Override
    public boolean hasStructure(Biome biomeIn, Structure<? extends IFeatureConfig> structureIn) {
        return getStructureConfig(biomeIn, structureIn) != null;
    }

    @Nullable
    @Override
    public <C extends IFeatureConfig> C getStructureConfig(Biome biomeIn, Structure<C> structureIn) {
        if (!(biomeIn instanceof PosEmbeddedBiomes.Biome)) return null;

        return structs.get().hasStructureStartHere(
            structureIn,
            ((PosEmbeddedBiomes.Biome)biomeIn).getPos()
        );
    }

    @Override
    public void generateStructures(BiomeManager __, IChunk chunk, ChunkGenerator<?> ___, TemplateManager templateManager) {
        BlockPos pos = new BlockPos(chunk.getPos().getXStart() + 9, 0,chunk.getPos().getZStart() + 9);

        Biome biome = PosEmbeddedBiomes.MANAGER.getBiome(pos);

        structs.get().forEachStructs(
            structGen -> {
                Structure<?> structure = structGen.getStruct();

                int refCount = getRefCount(structure, chunk);

                StructureStart structurestart = null;

                if (canSpawn(structGen, chunk)) {
                    structurestart = structure.getStartFactory().create(
                        structure,
                        chunk.getPos().x,
                        chunk.getPos().z,
                        MutableBoundingBox.getNewBoundingBox(),
                        refCount,
                        getSeed()
                    );

                    structurestart.init(
                        this,
                        templateManager,
                        chunk.getPos().x,
                        chunk.getPos().z,
                        biome
                    );
                }

                if (structurestart == null || !structurestart.isValid()){
                    structurestart = StructureStart.DUMMY;
                }

                chunk.putStructureStart(structure.getStructureName(), structurestart);
            }
        );
    }

    private int getRefCount(Structure<?> structure, IStructureReader chunk){
        StructureStart structurestart = chunk.getStructureStart(structure.getStructureName());

        if (structurestart != null) {
            return structurestart.getRefCount();
        }
        return 0;
    }

    private boolean canSpawn(StructGen<?> structGen, IChunk chunk){
        return structGen.hasStructureStartHere(chunk.getPos()) != null;
    }

    @Nullable
    @Override
    public BlockPos findNearestStructure(World worldIn, String name, BlockPos pos, int radius, boolean p_211403_5_) {
        Mutable<BlockPos2D> result = new MutableObject<>(null);

        structs.get().forEachStructs(
            structGen -> {
                if (result.getValue() != null) return;

                if (!structGen.getStruct().getStructureName().equals(name)) return;

                result.setValue(
                    structGen.getClosestInstance(MCHelper.to2D(pos))
                );
            }
        );

        if(result.getValue() == null) return null;

        return result.getValue().to3D(0);
    }

    @Override
    public void spawnMobs(ServerWorld worldIn, boolean spawnHostileMobs, boolean spawnPeacefulMobs) {
        tickers.get().run(worldIn);
    }

    @Override
    public void spawnMobs(WorldGenRegion worldGenRegion) {
    }

    @Override
    public void generateSurface(WorldGenRegion worldGenRegion, IChunk iChunk) {
    }

    @Override
    public void generateCarvers(BiomeManager p_225550_1_, IChunk p_225550_2_, GenerationStage.Carving p_225550_3_) {
    }
}
