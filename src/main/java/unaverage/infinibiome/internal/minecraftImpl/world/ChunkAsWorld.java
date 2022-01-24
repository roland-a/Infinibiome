package weightedgpa.infinibiome.internal.minecraftImpl.world;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.IFluidState;
import net.minecraft.particles.IParticleData;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.*;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeManager;
import net.minecraft.world.border.WorldBorder;
import net.minecraft.world.chunk.AbstractChunkProvider;
import net.minecraft.world.chunk.ChunkStatus;
import net.minecraft.world.chunk.IChunk;
import net.minecraft.world.dimension.Dimension;
import net.minecraft.world.gen.Heightmap;
import net.minecraft.world.lighting.WorldLightManager;
import net.minecraft.world.storage.WorldInfo;
import weightedgpa.infinibiome.internal.misc.MCHelper;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Predicate;

public class ChunkAsWorld implements IWorld {
    private final IChunk chunk;

    public ChunkAsWorld(IChunk chunk) {
        this.chunk = chunk;
    }

    @Nullable
    public BlockState setBlockState(BlockPos pos, BlockState state, boolean isMoving) {
        return chunk.setBlockState(pos, state, isMoving);
    }

    @Override
    public BlockState getBlockState(BlockPos pos) {
        return chunk.getBlockState(pos);
    }

    @Nullable
    @Override
    public TileEntity getTileEntity(BlockPos pos) {
        return chunk.getTileEntity(pos);
    }

    @Override
    public IFluidState getFluidState(BlockPos pos) {
        return chunk.getFluidState(pos);
    }

    @Override
    public boolean hasBlockState(BlockPos p_217375_1_, Predicate<BlockState> p_217375_2_) {
        return p_217375_2_.test(getBlockState(p_217375_1_));
    }

    @Override
    public boolean setBlockState(BlockPos pos, BlockState newState, int flags) {
        chunk.setBlockState(pos, newState, MCHelper.DEFAULT_IS_MOVING);

        return true;
    }

    @Override
    public boolean removeBlock(BlockPos pos, boolean isMoving) {
        chunk.setBlockState(
            pos,
            chunk.getBlockState(pos).getFluidState().getBlockState(),
            isMoving
        );

        return true;
    }

    @Override
    public boolean destroyBlock(BlockPos p_225521_1_, boolean p_225521_2_, @Nullable Entity p_225521_3_) {
        return removeBlock(p_225521_1_, p_225521_2_);
    }

    @Override
    public int getHeight(Heightmap.Type heightmapType, int x, int z) {
        return chunk.getTopBlockY(heightmapType, x, z) + 1;
    }

    @Override
    public boolean isRemote() {
        return false;
    }

    @Override
    public int getSeaLevel() {
        return MCHelper.WATER_HEIGHT;
    }

    @Override
    public long getSeed() {
        throw new UnsupportedOperationException();
    }

    @Override
    public ITickList<Block> getPendingBlockTicks() {
        throw new UnsupportedOperationException();
    }

    @Override
    public ITickList<Fluid> getPendingFluidTicks() {
        throw new UnsupportedOperationException();
    }

    @Override
    public World getWorld() {
        throw new UnsupportedOperationException();
    }

    @Override
    public WorldInfo getWorldInfo() {
        throw new UnsupportedOperationException();
    }

    @Override
    public DifficultyInstance getDifficultyForLocation(BlockPos pos) {
        throw new UnsupportedOperationException();
    }

    @Override
    public AbstractChunkProvider getChunkProvider() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Random getRandom() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void notifyNeighbors(BlockPos pos, Block blockIn) {
        throw new UnsupportedOperationException();
    }

    @Override
    public BlockPos getSpawnPoint() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void playSound(@Nullable PlayerEntity player, BlockPos pos, SoundEvent soundIn, SoundCategory category, float volume, float pitch) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void addParticle(IParticleData particleData, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void playEvent(@Nullable PlayerEntity player, int type, BlockPos pos, int data) {
        throw new UnsupportedOperationException();
    }

    @Override
    public WorldBorder getWorldBorder() {
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

    @Nullable
    @Override
    public IChunk getChunk(int x, int z, ChunkStatus requiredStatus, boolean nonnull) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int getSkylightSubtracted() {
        throw new UnsupportedOperationException();
    }

    @Override
    public BiomeManager getBiomeManager() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Biome getNoiseBiomeRaw(int x, int y, int z) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Dimension getDimension() {
        throw new UnsupportedOperationException();
    }

    @Override
    public WorldLightManager getLightManager() {
        throw new UnsupportedOperationException();
    }
}
