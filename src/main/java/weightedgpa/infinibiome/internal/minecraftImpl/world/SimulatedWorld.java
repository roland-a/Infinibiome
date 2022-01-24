package weightedgpa.infinibiome.internal.minecraftImpl.world;

import it.unimi.dsi.fastutil.longs.LongSet;
import it.unimi.dsi.fastutil.shorts.ShortList;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.IFluidState;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.particles.IParticleData;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.palette.UpgradeData;
import net.minecraft.world.*;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeContainer;
import net.minecraft.world.biome.BiomeManager;
import net.minecraft.world.border.WorldBorder;
import net.minecraft.world.chunk.*;
import net.minecraft.world.dimension.Dimension;
import net.minecraft.world.gen.ChunkGenerator;
import net.minecraft.world.gen.GenerationStage;
import net.minecraft.world.gen.Heightmap;
import net.minecraft.world.gen.feature.structure.StructureStart;
import net.minecraft.world.lighting.WorldLightManager;
import net.minecraft.world.server.ServerWorld;
import net.minecraft.world.storage.WorldInfo;
import weightedgpa.infinibiome.api.dependency.DependencyInjector;
import weightedgpa.infinibiome.api.generators.InterChunkGen;
import weightedgpa.infinibiome.api.generators.StructGen;
import weightedgpa.infinibiome.internal.generators.interchunks.mob.MobGenBase;
import weightedgpa.infinibiome.internal.misc.MCHelper;
import weightedgpa.infinibiome.api.pos.InterChunkPos;
import weightedgpa.infinibiome.internal.generators.chunks.ChunkGens;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Stream;


//todo replace this with a way of loading any chunks fully without deadlocks
public final class SimulatedWorld {
    private final ChunkGens chunkGens;
    private final List<InterChunkGen> interChunks;
    private final ChunkGenerator chunkGenerator;
    private final ServerWorld world;

    public SimulatedWorld(DependencyInjector di) {
        this.chunkGens = di.get(ChunkGens.class);
        this.interChunks = di.getAll(InterChunkGen.class);
        this.chunkGenerator = di.get(ChunkGenerator.class);
        this.world = di.get(ServerWorld.class);

        interChunks.removeIf(
            g ->
                g instanceof StructGen ||
                g instanceof MobGenBase
        );
    }

    public IWorld simulateInterchunks(InterChunkPos interChunkPos){
        IWorld interChunkWorld = new InterChunks(interChunkPos);

        interChunks.forEach(
            g -> g.generate(interChunkPos, interChunkWorld)
        );

        return interChunkWorld;
    }

    private class InterChunks implements IWorld {
        private final BlockState[][][] blocks = new BlockState[32][256][32];
        private final InterChunkPos interChunkPos;

        InterChunks(InterChunkPos interChunkPos) {
            this.interChunkPos = interChunkPos;
            for (int x = 0; x <= 1; x++) {
                for (int z = 0; z <= 1; z++) {
                    ChunkPos chunkPos = new ChunkPos(
                        interChunkPos.getLowestChunkPos().x + x,
                        interChunkPos.getLowestChunkPos().z + z
                    );

                    IChunk chunk = new ChunkWrapper(chunkPos);

                    chunkGens.buildChunk(chunkPos, chunk);
                }
            }
        }

        @Nullable
        @Override
        public TileEntity getTileEntity(BlockPos pos) {
            return null;
        }

        @Override
        public BlockState getBlockState(BlockPos pos) {
            int x = pos.getX() - interChunkPos.getLowestChunkPos().getXStart();
            int y = pos.getY();
            int z = pos.getZ() - interChunkPos.getLowestChunkPos().getZStart();

            if (y < 0) y = 0;
            if (y > 255) y = 255;

            BlockState block = blocks[x][y][z];

            if (block == null) return Blocks.AIR.getDefaultState();

            return block;
        }

        @Override
        public IFluidState getFluidState(BlockPos pos) {
            return getBlockState(pos).getFluidState();
        }

        @Override
        public boolean setBlockState(BlockPos pos, BlockState newState, int flags) {
            int x = pos.getX() - interChunkPos.getLowestChunkPos().getXStart();
            int y = pos.getY();
            int z = pos.getZ() - interChunkPos.getLowestChunkPos().getZStart();

            if (y < 0) y = 0;
            if (y > 255) y = 255;

            blocks[x][y][z] = newState;

            return true;
        }

        @Override
        public boolean removeBlock(BlockPos pos, boolean isMoving) {
            return setBlockState(pos, Blocks.AIR.getDefaultState(), MCHelper.DEFAULT_FLAG);
        }

        @Override
        public boolean destroyBlock(BlockPos p_175655_1_, boolean p_175655_2_) {
            return removeBlock(p_175655_1_, p_175655_2_);
        }

        @Override
        public boolean destroyBlock(BlockPos blockPos, boolean b, @Nullable Entity entity) {
            return removeBlock(blockPos, b);
        }

        @Override
        public boolean hasBlockState(BlockPos pos, Predicate<BlockState> predicate) {
            return predicate.test(getBlockState(pos));
        }

        @Override
        public long getSeed() {
            return chunkGenerator.getSeed();
        }

        @Override
        public ITickList<Block> getPendingBlockTicks() {
            return EmptyTickList.get();
        }

        @Override
        public ITickList<Fluid> getPendingFluidTicks() {
            return EmptyTickList.get();
        }

        @Override
        public int getSeaLevel() {
            return MCHelper.WATER_HEIGHT;
        }

        @Override
        public int getMaxHeight() {
            return 256;
        }

        @Override
        public Random getRandom() {
            return new Random();
        }

        @Override
        public int getHeight(Heightmap.Type heightmapType, int x, int z) {
            BlockPos.Mutable mutPos = new BlockPos.Mutable(x, 0, z);

            for (int y = 255; y > 1; y--){
                mutPos.setY(y);

                if (heightmapType.getHeightLimitPredicate().test(getBlockState(mutPos))){
                    return y + 1;
                }
            }
            return 0;
        }

        @Nullable
        @Override
        public IChunk getChunk(int x, int z, ChunkStatus requiredStatus, boolean nonnull) {
            return new ChunkWrapper(
                new ChunkPos(x, z)
            );
        }

        @Override
        public boolean isRemote() {
            return false;
        }

        @Override
        public WorldBorder getWorldBorder() {
            return world.getWorldBorder();
        }

        @Override
        public WorldInfo getWorldInfo() {
            return world.getWorldInfo();
        }

        @Override
        public int getSkylightSubtracted() {
            return world.getSkylightSubtracted();
        }

        @Override
        public Dimension getDimension() {
            return world.getDimension();
        }

        @Override
        public WorldLightManager getLightManager() {
            return world.getLightManager();
        }

        @Override
        public BiomeManager getBiomeManager() {
            return world.getBiomeManager();
        }

        @Override
        public DifficultyInstance getDifficultyForLocation(BlockPos pos) {
            return world.getDifficultyForLocation(pos);
        }

        @Override
        public BlockPos getSpawnPoint() {
            return world.getSpawnPoint();
        }

        @Override
        public void notifyNeighbors(BlockPos pos, Block blockIn) {
        }

        @Override
        public void playSound(@Nullable PlayerEntity player, BlockPos pos, SoundEvent soundIn, SoundCategory category, float volume, float pitch) {
        }

        @Override
        public void addParticle(IParticleData particleData, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed) {
        }

        @Override
        public void playEvent(@Nullable PlayerEntity player, int type, BlockPos pos, int data) {
        }

        @Override
        public World getWorld() {
            throw new UnsupportedOperationException();
        }

        @Override
        public AbstractChunkProvider getChunkProvider() {
            throw new UnsupportedOperationException();
        }

        @Override
        public List<Entity> getEntitiesInAABBexcluding(@Nullable Entity entityIn, AxisAlignedBB boundingBox, @Nullable Predicate<? super Entity> predicate) {
            throw new UnsupportedOperationException();
        }

        @Override
        public <T extends Entity> List<T> getEntitiesWithinAABB(Class<? extends T> clazz, AxisAlignedBB aabb, @Nullable Predicate<? super T> filter) {
            throw new UnsupportedOperationException();
        }

        @Override
        public List<? extends PlayerEntity> getPlayers() {
            throw new UnsupportedOperationException();
        }

        @Override
        public Biome getNoiseBiomeRaw(int x, int y, int z) {
            throw new UnsupportedOperationException();
        }

        private class ChunkWrapper implements IChunk {
            private final ChunkPos chunkPos;

            ChunkWrapper(ChunkPos chunkPos) {
                this.chunkPos = chunkPos;
            }

            @Override
            public ChunkPos getPos() {
                return chunkPos;
            }

            @Override
            public BlockState getBlockState(BlockPos pos) {
                return InterChunks.this.getBlockState(pos);
            }

            @Override
            public IFluidState getFluidState(BlockPos pos) {
                return InterChunks.this.getFluidState(pos);
            }

            @Nullable
            @Override
            public BlockState setBlockState(BlockPos pos, BlockState state, boolean isMoving) {
                InterChunks.this.setBlockState(pos, state, MCHelper.DEFAULT_FLAG);

                return state;
            }

            @Override
            public BitSet getCarvingMask(GenerationStage.Carving type) {
                return new BitSet(65536);
            }

            @Override
            public int getTopBlockY(Heightmap.Type heightmapType, int x, int z) {
                return InterChunks.this.getHeight(heightmapType, x, z) - 1;
            }

            @Override
            public ITickList<Block> getBlocksToBeTicked() {
                return new EmptyTickList<>();
            }

            @Override
            public ITickList<Fluid> getFluidsToBeTicked() {
                return new EmptyTickList<>();
            }

            @Nullable
            @Override
            public TileEntity getTileEntity(BlockPos pos) {
                throw new UnsupportedOperationException();
            }

            @Override
            public void addTileEntity(BlockPos pos, TileEntity tileEntityIn) {
                throw new UnsupportedOperationException();
            }

            @Override
            public void addEntity(Entity entityIn) {
                throw new UnsupportedOperationException();
            }

            @Override
            public Set<BlockPos> getTileEntitiesPos() {
                throw new UnsupportedOperationException();
            }

            @Override
            public ChunkSection[] getSections() {
                throw new UnsupportedOperationException();
            }

            @Override
            public Collection<Map.Entry<Heightmap.Type, Heightmap>> getHeightmaps() {
                throw new UnsupportedOperationException();
            }

            @Override
            public void setHeightmap(Heightmap.Type type, long[] data) {
                throw new UnsupportedOperationException();
            }

            @Override
            public Heightmap getHeightmap(Heightmap.Type typeIn) {
                throw new UnsupportedOperationException();
            }

            @Override
            public void setLastSaveTime(long saveTime) {
                throw new UnsupportedOperationException();
            }

            @Override
            public Map<String, StructureStart> getStructureStarts() {
                throw new UnsupportedOperationException();
            }

            @Override
            public void setStructureStarts(Map<String, StructureStart> structureStartsIn) {
                throw new UnsupportedOperationException();
            }

            @Nullable
            @Override
            public BiomeContainer getBiomes() {
                throw new UnsupportedOperationException();
            }

            @Override
            public void setModified(boolean modified) {
                throw new UnsupportedOperationException();
            }

            @Override
            public boolean isModified() {
                throw new UnsupportedOperationException();
            }

            @Override
            public ChunkStatus getStatus() {
                throw new UnsupportedOperationException();
            }

            @Override
            public void removeTileEntity(BlockPos pos) {
                throw new UnsupportedOperationException();
            }

            @Override
            public ShortList[] getPackedPositions() {
                throw new UnsupportedOperationException();
            }

            @Nullable
            @Override
            public CompoundNBT getDeferredTileEntity(BlockPos pos) {
                throw new UnsupportedOperationException();
            }

            @Nullable
            @Override
            public CompoundNBT getTileEntityNBT(BlockPos pos) {
                throw new UnsupportedOperationException();
            }

            @Override
            public Stream<BlockPos> getLightSources() {
                throw new UnsupportedOperationException();
            }

            @Override
            public UpgradeData getUpgradeData() {
                throw new UnsupportedOperationException();
            }

            @Override
            public void setInhabitedTime(long newInhabitedTime) {
                throw new UnsupportedOperationException();
            }

            @Override
            public long getInhabitedTime() {
                throw new UnsupportedOperationException();
            }

            @Override
            public boolean hasLight() {
                throw new UnsupportedOperationException();
            }

            @Override
            public void setLight(boolean lightCorrectIn) {
                throw new UnsupportedOperationException();
            }

            @Nullable
            @Override
            public StructureStart getStructureStart(String stucture) {
                throw new UnsupportedOperationException();
            }

            @Override
            public void putStructureStart(String structureIn, StructureStart structureStartIn) {
                throw new UnsupportedOperationException();
            }

            @Override
            public LongSet getStructureReferences(String structureIn) {
                throw new UnsupportedOperationException();
            }

            @Override
            public void addStructureReference(String strucutre, long reference) {
                throw new UnsupportedOperationException();
            }

            @Override
            public Map<String, LongSet> getStructureReferences() {
                throw new UnsupportedOperationException();
            }

            @Override
            public void setStructureReferences(Map<String, LongSet> p_201606_1_) {
                throw new UnsupportedOperationException();
            }
        }
    }
}
