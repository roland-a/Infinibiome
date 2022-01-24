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
import net.minecraft.item.crafting.RecipeManager;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.particles.IParticleData;
import net.minecraft.profiler.IProfiler;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.tags.NetworkTagManager;
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
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.gen.Heightmap;
import net.minecraft.world.gen.feature.structure.StructureStart;
import net.minecraft.world.lighting.WorldLightManager;
import net.minecraft.world.storage.MapData;
import net.minecraft.world.storage.WorldInfo;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.BooleanSupplier;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Stream;

public class NullWorld extends World{
    public NullWorld() {
        super(
            new WorldInfo() {
            },
            DimensionType.OVERWORLD,
            (a, b) -> new AbstractChunkProvider() {
                @Nullable
                @Override
                public IChunk getChunk(int chunkX, int chunkZ, ChunkStatus requiredStatus, boolean load) {
                    return null;
                }

                @Override
                public void tick(BooleanSupplier hasTimeLeft) {

                }

                @Override
                public String makeString() {
                    return null;
                }

                @Override
                public WorldLightManager getLightManager() {
                    return null;
                }

                @Override
                public IBlockReader getWorld() {
                    return null;
                }
            },
            new IProfiler() {
                @Override
                public void startTick() {

                }

                @Override
                public void endTick() {

                }

                @Override
                public void startSection(String name) {

                }

                @Override
                public void startSection(Supplier<String> nameSupplier) {

                }

                @Override
                public void endSection() {

                }

                @Override
                public void endStartSection(String name) {

                }

                @Override
                public void endStartSection(Supplier<String> nameSupplier) {

                }

                @Override
                public void func_230035_c_(String p_230035_1_) {

                }

                @Override
                public void func_230036_c_(Supplier<String> p_230036_1_) {

                }
            },
            false
        );
    }

    @Override
    public IChunk getChunk(int chunkX, int chunkZ, ChunkStatus requiredStatus) {
        return new net.minecraft.world.chunk.EmptyChunk(
            this,
            new ChunkPos(chunkX, chunkZ)
        );
    }

    @Override
    public void notifyBlockUpdate(BlockPos pos, BlockState oldState, BlockState newState, int flags) {

    }

    @Override
    public void playSound(@Nullable PlayerEntity player, double x, double y, double z, SoundEvent soundIn, SoundCategory category, float volume, float pitch) {

    }

    @Override
    public void playMovingSound(@Nullable PlayerEntity playerIn, Entity entityIn, SoundEvent eventIn, SoundCategory categoryIn, float volume, float pitch) {

    }

    @Nullable
    @Override
    public Entity getEntityByID(int id) {
        return null;
    }

    @Nullable
    @Override
    public MapData getMapData(String mapName) {
        return null;
    }

    @Override
    public void registerMapData(MapData mapDataIn) {

    }

    @Override
    public int getNextMapId() {
        return 0;
    }

    @Override
    public void sendBlockBreakProgress(int breakerId, BlockPos pos, int progress) {

    }

    @Override
    public Scoreboard getScoreboard() {
        return null;
    }

    @Override
    public RecipeManager getRecipeManager() {
        return null;
    }

    @Override
    public NetworkTagManager getTags() {
        return null;
    }

    @Override
    public ITickList<Block> getPendingBlockTicks() {
        return null;
    }

    @Override
    public ITickList<Fluid> getPendingFluidTicks() {
        return null;
    }

    @Override
    public void playEvent(@Nullable PlayerEntity player, int type, BlockPos pos, int data) {

    }

    @Override
    public List<? extends PlayerEntity> getPlayers() {
        return null;
    }

    @Override
    public Biome getNoiseBiomeRaw(int x, int y, int z) {
        return null;
    }


    /*
    class EmptyChunk implements IChunk{
        @Nullable
        @Override
        public BlockState setBlockState(BlockPos pos, BlockState state, boolean isMoving) {
            return Blocks.AIR.getDefaultState();
        }

        @Override
        public void addTileEntity(BlockPos pos, TileEntity tileEntityIn) {

        }

        @Override
        public void addEntity(Entity entityIn) {

        }

        @Override
        public Set<BlockPos> getTileEntitiesPos() {
            return null;
        }

        @Override
        public ChunkSection[] getSections() {
            return new ChunkSection[0];
        }

        @Override
        public Collection<Map.Entry<Heightmap.Type, Heightmap>> getHeightmaps() {
            return null;
        }

        @Override
        public void setHeightmap(Heightmap.Type type, long[] data) {

        }

        @Override
        public Heightmap getHeightmap(Heightmap.Type typeIn) {
            return null;
        }

        @Override
        public int getTopBlockY(Heightmap.Type heightmapType, int x, int z) {
            return 0;
        }

        @Override
        public ChunkPos getPos() {
            return null;
        }

        @Override
        public void setLastSaveTime(long saveTime) {

        }

        @Override
        public Map<String, StructureStart> getStructureStarts() {
            return null;
        }

        @Override
        public void setStructureStarts(Map<String, StructureStart> structureStartsIn) {

        }

        @Nullable
        @Override
        public BiomeContainer getBiomes() {
            return null;
        }

        @Override
        public void setModified(boolean modified) {

        }

        @Override
        public boolean isModified() {
            return false;
        }

        @Override
        public ChunkStatus getStatus() {
            return null;
        }

        @Override
        public void removeTileEntity(BlockPos pos) {

        }

        @Override
        public ShortList[] getPackedPositions() {
            return new ShortList[0];
        }

        @Nullable
        @Override
        public CompoundNBT getDeferredTileEntity(BlockPos pos) {
            return null;
        }

        @Nullable
        @Override
        public CompoundNBT getTileEntityNBT(BlockPos pos) {
            return null;
        }

        @Override
        public Stream<BlockPos> getLightSources() {
            return null;
        }

        @Override
        public ITickList<Block> getBlocksToBeTicked() {
            return null;
        }

        @Override
        public ITickList<Fluid> getFluidsToBeTicked() {
            return null;
        }

        @Override
        public UpgradeData getUpgradeData() {
            return null;
        }

        @Override
        public void setInhabitedTime(long newInhabitedTime) {

        }

        @Override
        public long getInhabitedTime() {
            return 0;
        }

        @Override
        public boolean hasLight() {
            return false;
        }

        @Override
        public void setLight(boolean lightCorrectIn) {

        }

        @Nullable
        @Override
        public TileEntity getTileEntity(BlockPos pos) {
            return null;
        }

        @Override
        public BlockState getBlockState(BlockPos pos) {
            return null;
        }

        @Override
        public IFluidState getFluidState(BlockPos pos) {
            return null;
        }

        @Nullable
        @Override
        public StructureStart getStructureStart(String stucture) {
            return null;
        }

        @Override
        public void putStructureStart(String structureIn, StructureStart structureStartIn) {

        }

        @Override
        public LongSet getStructureReferences(String structureIn) {
            return null;
        }

        @Override
        public void addStructureReference(String strucutre, long reference) {

        }

        @Override
        public Map<String, LongSet> getStructureReferences() {
            return null;
        }

        @Override
        public void setStructureReferences(Map<String, LongSet> p_201606_1_) {

        }
    }

     */
}
