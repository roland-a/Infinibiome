package weightedgpa.infinibiome.internal.minecraftImpl.world;

import it.unimi.dsi.fastutil.longs.LongSet;
import it.unimi.dsi.fastutil.shorts.ShortList;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityPredicate;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.IFluidState;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.particles.IParticleData;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.*;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.palette.UpgradeData;
import net.minecraft.world.*;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeContainer;
import net.minecraft.world.biome.BiomeManager;
import net.minecraft.world.border.WorldBorder;
import net.minecraft.world.chunk.AbstractChunkProvider;
import net.minecraft.world.chunk.ChunkSection;
import net.minecraft.world.chunk.ChunkStatus;
import net.minecraft.world.chunk.IChunk;
import net.minecraft.world.dimension.Dimension;
import net.minecraft.world.gen.GenerationStage;
import net.minecraft.world.gen.Heightmap;
import net.minecraft.world.gen.feature.structure.StructureStart;
import net.minecraft.world.level.ColorResolver;
import net.minecraft.world.lighting.WorldLightManager;
import net.minecraft.world.storage.WorldInfo;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Stream;

@SuppressWarnings({"MethodParameterNamingConvention", "deprecation", "OverlyComplexClass"})
public abstract class WorldWrapper implements IWorld {
    protected final IWorld inner;

    protected WorldWrapper(IWorld inner) {
        this.inner = inner;
    }


    protected abstract BlockState getBlock(BlockPos pos);

    protected abstract void setBlock(BlockPos pos, BlockState block, int tag);

    @Override
    public BlockState getBlockState(BlockPos blockPos) {
        return getBlock(blockPos);
    }

    @Override
    public IFluidState getFluidState(BlockPos blockPos) {
        return getBlock(blockPos).getFluidState();
    }

    @Override
    public boolean hasBlockState(BlockPos blockPos, Predicate<BlockState> predicate) {
        return predicate.test(getBlock(blockPos));
    }

    @Override
    public boolean setBlockState(BlockPos blockPos, BlockState blockState, int tag) {
        setBlock(blockPos, blockState, tag);

        return true;
    }

    @Override
    public boolean removeBlock(BlockPos blockPos, boolean isMoving) {
        IFluidState ifluidstate = getFluidState(blockPos);

        setBlock(blockPos, ifluidstate.getBlockState(), 3 | (isMoving ? 64 : 0));

        return true;
    }

    @Override
    @Nullable
    public IBlockReader getBlockReader(int p_225522_1_, int p_225522_2_) {
        return this;
    }

    @Override
    public IChunk getChunk(BlockPos p_217349_1_) {
        return new ChunkWrapper(inner.getChunk(p_217349_1_));
    }

    @Override
    public IChunk getChunk(int p_212866_1_, int p_212866_2_) {
        return new ChunkWrapper(inner.getChunk(p_212866_1_, p_212866_2_));
    }

    @Override
    public IChunk getChunk(int p_217348_1_, int p_217348_2_, ChunkStatus p_217348_3_) {
        return new ChunkWrapper(inner.getChunk(p_217348_1_, p_217348_2_, p_217348_3_));
    }

    @Override
    @Nullable
    public IChunk getChunk(int i, int i1, ChunkStatus chunkStatus, boolean b) {
        return new ChunkWrapper(inner.getChunk(i, i1, chunkStatus, b));
    }

    private class ChunkWrapper implements IChunk {
        private final IChunk innerChunk;

        ChunkWrapper(IChunk innerChunk) {
            this.innerChunk = innerChunk;
        }

        @Override
        public BlockState getBlockState(BlockPos p_180495_1_) {
            return WorldWrapper.this.getBlock(p_180495_1_);
        }

        @Override
        public IFluidState getFluidState(BlockPos p_204610_1_) {
            return WorldWrapper.this.getFluidState(p_204610_1_);
        }

        @Override
        @Nullable
        public BlockState setBlockState(BlockPos p_177436_1_, BlockState p_177436_2_, boolean p_177436_3_) {
            WorldWrapper.this.setBlock(p_177436_1_, p_177436_2_, 0);

            return p_177436_2_;
        }


        //region delagate
        @Override
        public Heightmap getHeightmap(Heightmap.Type p_217303_1_) {
            return innerChunk.getHeightmap(p_217303_1_);
        }

        @Override
        public Set<BlockPos> getTileEntitiesPos() {
            return innerChunk.getTileEntitiesPos();
        }

        @Override
        public ChunkSection[] getSections() {
            return innerChunk.getSections();
        }

        @Override
        public void addEntity(Entity p_76612_1_) {
            innerChunk.addEntity(p_76612_1_);
        }

        @Override
        public void setHeightmap(Heightmap.Type p_201607_1_, long[] p_201607_2_) {
            innerChunk.setHeightmap(p_201607_1_, p_201607_2_);
        }

        @Override
        public int getTopBlockY(Heightmap.Type p_201576_1_, int p_201576_2_, int p_201576_3_) {
            return innerChunk.getTopBlockY(p_201576_1_, p_201576_2_, p_201576_3_);
        }

        @Override
        @Nullable
        public TileEntity getTileEntity(BlockPos p_175625_1_) {
            return innerChunk.getTileEntity(p_175625_1_);
        }

        @Override
        public void addTileEntity(BlockPos p_177426_1_, TileEntity p_177426_2_) {
            innerChunk.addTileEntity(p_177426_1_, p_177426_2_);
        }

        @Override
        public void addTileEntity(CompoundNBT p_201591_1_) {
            innerChunk.addTileEntity(p_201591_1_);
        }

        @Override
        @Nullable
        public CompoundNBT getTileEntityNBT(BlockPos p_223134_1_) {
            return innerChunk.getTileEntityNBT(p_223134_1_);
        }

        @Override
        public void removeTileEntity(BlockPos p_177425_1_) {
            innerChunk.removeTileEntity(p_177425_1_);
        }

        @Override
        public ChunkPos getPos() {
            return innerChunk.getPos();
        }

        @Nullable
        @Override
        public BiomeContainer getBiomes() {
            return innerChunk.getBiomes();
        }

        @Override
        public Collection<Map.Entry<Heightmap.Type, Heightmap>> getHeightmaps() {
            return innerChunk.getHeightmaps();
        }

        @Nullable
        @Override
        public CompoundNBT getDeferredTileEntity(BlockPos p_201579_1_) {
            return innerChunk.getDeferredTileEntity(p_201579_1_);
        }

        @Override
        public Stream<BlockPos> getLightSources() {
            return innerChunk.getLightSources();
        }

        @Override
        public ITickList<Block> getBlocksToBeTicked() {
            return innerChunk.getBlocksToBeTicked();
        }

        @Override
        public ITickList<Fluid> getFluidsToBeTicked() {
            return innerChunk.getFluidsToBeTicked();
        }

        @Override
        public void setModified(boolean p_177427_1_) {
            innerChunk.setModified(p_177427_1_);
        }

        @Override
        public boolean isModified() {
            return innerChunk.isModified();
        }

        @Override
        public void setLastSaveTime(long p_177432_1_) {
            innerChunk.setLastSaveTime(p_177432_1_);
        }

        @Override
        @Nullable
        public StructureStart getStructureStart(String p_201585_1_) {
            return innerChunk.getStructureStart(p_201585_1_);
        }

        @Override
        public void putStructureStart(String p_201584_1_, StructureStart p_201584_2_) {
            innerChunk.putStructureStart(p_201584_1_, p_201584_2_);
        }

        @Override
        public Map<String, StructureStart> getStructureStarts() {
            return innerChunk.getStructureStarts();
        }

        @Override
        public void setStructureStarts(Map<String, StructureStart> p_201612_1_) {
            innerChunk.setStructureStarts(p_201612_1_);
        }

        @Override
        public LongSet getStructureReferences(String p_201578_1_) {
            return innerChunk.getStructureReferences(p_201578_1_);
        }

        @Override
        public void addStructureReference(String p_201583_1_, long p_201583_2_) {
            innerChunk.addStructureReference(p_201583_1_, p_201583_2_);
        }

        @Override
        public Map<String, LongSet> getStructureReferences() {
            return innerChunk.getStructureReferences();
        }

        @Override
        public void setStructureReferences(Map<String, LongSet> p_201606_1_) {
            innerChunk.setStructureReferences(p_201606_1_);
        }

        @Override
        public long getInhabitedTime() {
            return innerChunk.getInhabitedTime();
        }

        @Override
        public void setInhabitedTime(long p_177415_1_) {
            innerChunk.setInhabitedTime(p_177415_1_);
        }

        @Override
        public UpgradeData getUpgradeData() {
            return innerChunk.getUpgradeData();
        }

        @Override
        public ShortList[] getPackedPositions() {
            return innerChunk.getPackedPositions();
        }

        @Override
        public ChunkStatus getStatus() {
            return innerChunk.getStatus();
        }

        @Override
        public boolean hasLight() {
            return innerChunk.hasLight();
        }

        @Override
        public void setLight(boolean p_217305_1_) {
            innerChunk.setLight(p_217305_1_);
        }

        @Override
        @Nullable
        public ChunkSection getLastExtendedBlockStorage() {
            return innerChunk.getLastExtendedBlockStorage();
        }

        @Override
        public int getTopFilledSegment() {
            return innerChunk.getTopFilledSegment();
        }

        @Override
        public boolean isEmptyBetween(int p_76606_1_, int p_76606_2_) {
            return innerChunk.isEmptyBetween(p_76606_1_, p_76606_2_);
        }

        @Override
        public void markBlockForPostprocessing(BlockPos p_201594_1_) {
            innerChunk.markBlockForPostprocessing(p_201594_1_);
        }

        @Override
        public void func_201636_b(short p_201636_1_, int p_201636_2_) {
            innerChunk.func_201636_b(p_201636_1_, p_201636_2_);
        }

        @Override
        public BitSet getCarvingMask(GenerationStage.Carving p_205749_1_) {
            return innerChunk.getCarvingMask(p_205749_1_);
        }

        @Override
        public int getLightValue(BlockPos p_217298_1_) {
            return innerChunk.getLightValue(p_217298_1_);
        }

        @Override
        public int getMaxLightLevel() {
            return innerChunk.getMaxLightLevel();
        }

        @Override
        public int getHeight() {
            return innerChunk.getHeight();
        }

        @Override
        public BlockRayTraceResult rayTraceBlocks(RayTraceContext p_217299_1_) {
            return innerChunk.rayTraceBlocks(p_217299_1_);
        }

        @Override
        @Nullable
        public BlockRayTraceResult rayTraceBlocks(Vec3d p_217296_1_, Vec3d p_217296_2_, BlockPos p_217296_3_, VoxelShape p_217296_4_, BlockState p_217296_5_) {
            return innerChunk.rayTraceBlocks(p_217296_1_, p_217296_2_, p_217296_3_, p_217296_4_, p_217296_5_);
        }
        //endregiond
    }

    //region delegated
    @Override
    public long getSeed() {
        return inner.getSeed();
    }

    @Override
    public float getCurrentMoonPhaseFactor() {
        return inner.getCurrentMoonPhaseFactor();
    }

    @Override
    public float getCelestialAngle(float p_72826_1_) {
        return inner.getCelestialAngle(p_72826_1_);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public int getMoonPhase() {
        return inner.getMoonPhase();
    }

    @Override
    public ITickList<Block> getPendingBlockTicks() {
        return inner.getPendingBlockTicks();
    }

    @Override
    public ITickList<Fluid> getPendingFluidTicks() {
        return inner.getPendingFluidTicks();
    }

    @Override
    public World getWorld() {
        return inner.getWorld();
    }

    @Override
    public WorldInfo getWorldInfo() {
        return inner.getWorldInfo();
    }

    @Override
    public DifficultyInstance getDifficultyForLocation(BlockPos blockPos) {
        return inner.getDifficultyForLocation(blockPos);
    }

    @Override
    public Difficulty getDifficulty() {
        return inner.getDifficulty();
    }

    @Override
    public AbstractChunkProvider getChunkProvider() {
        return inner.getChunkProvider();
    }

    @Override
    public boolean chunkExists(int p_217354_1_, int p_217354_2_) {
        return inner.chunkExists(p_217354_1_, p_217354_2_);
    }

    @Override
    public Random getRandom() {
        return inner.getRandom();
    }

    @Override
    public void notifyNeighbors(BlockPos blockPos, Block block) {
        inner.notifyNeighbors(blockPos, block);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public BlockPos getSpawnPoint() {
        return inner.getSpawnPoint();
    }

    @Override
    public void playSound(@Nullable PlayerEntity playerEntity, BlockPos blockPos, SoundEvent soundEvent, SoundCategory soundCategory, float v, float v1) {
        inner.playSound(playerEntity, blockPos, soundEvent, soundCategory, v, v1);
    }

    @Override
    public void addParticle(IParticleData iParticleData, double v, double v1, double v2, double v3, double v4, double v5) {
        inner.addParticle(iParticleData, v, v1, v2, v3, v4, v5);
    }

    @Override
    public void playEvent(@Nullable PlayerEntity playerEntity, int i, BlockPos blockPos, int i1) {
        inner.playEvent(playerEntity, i, blockPos, i1);
    }

    @Override
    public void playEvent(int p_217379_1_, BlockPos p_217379_2_, int p_217379_3_) {
        inner.playEvent(p_217379_1_, p_217379_2_, p_217379_3_);
    }

    @Override
    public Stream<VoxelShape> getEmptyCollisionShapes(@Nullable Entity p_223439_1_, AxisAlignedBB p_223439_2_, Set<Entity> p_223439_3_) {
        return inner.getEmptyCollisionShapes(p_223439_1_, p_223439_2_, p_223439_3_);
    }

    @Override
    public boolean checkNoEntityCollision(@Nullable Entity p_195585_1_, VoxelShape p_195585_2_) {
        return inner.checkNoEntityCollision(p_195585_1_, p_195585_2_);
    }

    @Override
    public BlockPos getHeight(Heightmap.Type p_205770_1_, BlockPos p_205770_2_) {
        return inner.getHeight(p_205770_1_, p_205770_2_);
    }

    @Override
    public List<Entity> getEntitiesInAABBexcluding(@Nullable Entity entity, AxisAlignedBB axisAlignedBB, @Nullable Predicate<? super Entity> predicate) {
        return inner.getEntitiesInAABBexcluding(entity, axisAlignedBB, predicate);
    }

    @Override
    public <T extends Entity> List<T> getEntitiesWithinAABB(Class<? extends T> aClass, AxisAlignedBB axisAlignedBB, @Nullable Predicate<? super T> predicate) {
        return inner.getEntitiesWithinAABB(aClass, axisAlignedBB, predicate);
    }

    @Override
    public <T extends Entity> List<T> getLoadedEntitiesWithinAABB(Class<? extends T> p_225316_1_, AxisAlignedBB p_225316_2_, @Nullable Predicate<? super T> p_225316_3_) {
        return inner.getLoadedEntitiesWithinAABB(p_225316_1_, p_225316_2_, p_225316_3_);
    }

    @Override
    public List<? extends PlayerEntity> getPlayers() {
        return inner.getPlayers();
    }

    @Override
    public List<Entity> getEntitiesWithinAABBExcludingEntity(@Nullable Entity p_72839_1_, AxisAlignedBB p_72839_2_) {
        return inner.getEntitiesWithinAABBExcludingEntity(p_72839_1_, p_72839_2_);
    }

    @Override
    public <T extends Entity> List<T> getEntitiesWithinAABB(Class<? extends T> p_217357_1_, AxisAlignedBB p_217357_2_) {
        return inner.getEntitiesWithinAABB(p_217357_1_, p_217357_2_);
    }

    @Override
    public <T extends Entity> List<T> getLoadedEntitiesWithinAABB(Class<? extends T> p_225317_1_, AxisAlignedBB p_225317_2_) {
        return inner.getLoadedEntitiesWithinAABB(p_225317_1_, p_225317_2_);
    }

    @Override
    @Nullable
    public PlayerEntity getClosestPlayer(double p_190525_1_, double p_190525_3_, double p_190525_5_, double p_190525_7_, @Nullable Predicate<Entity> p_190525_9_) {
        return inner.getClosestPlayer(p_190525_1_, p_190525_3_, p_190525_5_, p_190525_7_, p_190525_9_);
    }

    @Override
    @Nullable
    public PlayerEntity getClosestPlayer(Entity p_217362_1_, double p_217362_2_) {
        return inner.getClosestPlayer(p_217362_1_, p_217362_2_);
    }

    @Override
    @Nullable
    public PlayerEntity getClosestPlayer(double p_217366_1_, double p_217366_3_, double p_217366_5_, double p_217366_7_, boolean p_217366_9_) {
        return inner.getClosestPlayer(p_217366_1_, p_217366_3_, p_217366_5_, p_217366_7_, p_217366_9_);
    }

    @Override
    @Nullable
    public PlayerEntity getClosestPlayer(double p_217365_1_, double p_217365_3_, double p_217365_5_) {
        return inner.getClosestPlayer(p_217365_1_, p_217365_3_, p_217365_5_);
    }

    @Override
    public boolean isPlayerWithin(double p_217358_1_, double p_217358_3_, double p_217358_5_, double p_217358_7_) {
        return inner.isPlayerWithin(p_217358_1_, p_217358_3_, p_217358_5_, p_217358_7_);
    }

    @Override
    @Nullable
    public PlayerEntity getClosestPlayer(EntityPredicate p_217370_1_, LivingEntity p_217370_2_) {
        return inner.getClosestPlayer(p_217370_1_, p_217370_2_);
    }

    @Override
    @Nullable
    public PlayerEntity getClosestPlayer(EntityPredicate p_217372_1_, LivingEntity p_217372_2_, double p_217372_3_, double p_217372_5_, double p_217372_7_) {
        return inner.getClosestPlayer(p_217372_1_, p_217372_2_, p_217372_3_, p_217372_5_, p_217372_7_);
    }

    @Override
    @Nullable
    public PlayerEntity getClosestPlayer(EntityPredicate p_217359_1_, double p_217359_2_, double p_217359_4_, double p_217359_6_) {
        return inner.getClosestPlayer(p_217359_1_, p_217359_2_, p_217359_4_, p_217359_6_);
    }

    @Override
    @Nullable
    public <T extends LivingEntity> T getClosestEntityWithinAABB(Class<? extends T> p_217360_1_, EntityPredicate p_217360_2_, @Nullable LivingEntity p_217360_3_, double p_217360_4_, double p_217360_6_, double p_217360_8_, AxisAlignedBB p_217360_10_) {
        return inner.getClosestEntityWithinAABB(
            p_217360_1_,
            p_217360_2_,
            p_217360_3_,
            p_217360_4_,
            p_217360_6_,
            p_217360_8_,
            p_217360_10_
        );
    }

    @Override
    @Nullable
    public <T extends LivingEntity> T func_225318_b(Class<? extends T> p_225318_1_, EntityPredicate p_225318_2_, @Nullable LivingEntity p_225318_3_, double p_225318_4_, double p_225318_6_, double p_225318_8_, AxisAlignedBB p_225318_10_) {
        return inner.func_225318_b(p_225318_1_, p_225318_2_, p_225318_3_, p_225318_4_, p_225318_6_, p_225318_8_, p_225318_10_);
    }

    @Override
    @Nullable
    public <T extends LivingEntity> T getClosestEntity(List<? extends T> p_217361_1_, EntityPredicate p_217361_2_, @Nullable LivingEntity p_217361_3_, double p_217361_4_, double p_217361_6_, double p_217361_8_) {
        return inner.getClosestEntity(p_217361_1_, p_217361_2_, p_217361_3_, p_217361_4_, p_217361_6_, p_217361_8_);
    }

    @Override
    public List<PlayerEntity> getTargettablePlayersWithinAABB(EntityPredicate p_217373_1_, LivingEntity p_217373_2_, AxisAlignedBB p_217373_3_) {
        return inner.getTargettablePlayersWithinAABB(p_217373_1_, p_217373_2_, p_217373_3_);
    }

    @Override
    public <T extends LivingEntity> List<T> getTargettableEntitiesWithinAABB(Class<? extends T> p_217374_1_, EntityPredicate p_217374_2_, LivingEntity p_217374_3_, AxisAlignedBB p_217374_4_) {
        return inner.getTargettableEntitiesWithinAABB(p_217374_1_, p_217374_2_, p_217374_3_, p_217374_4_);
    }

    @Override
    @Nullable
    public PlayerEntity getPlayerByUuid(UUID p_217371_1_) {
        return inner.getPlayerByUuid(p_217371_1_);
    }

    @Override
    public int getHeight(Heightmap.Type type, int i, int i1) {
        return inner.getHeight(type, i, i1);
    }

    @Override
    public int getSkylightSubtracted() {
        return inner.getSkylightSubtracted();
    }

    @Override
    public BiomeManager getBiomeManager() {
        return inner.getBiomeManager();
    }

    @Override
    public Biome getBiome(BlockPos p_226691_1_) {
        return inner.getBiome(p_226691_1_);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public int getBlockColor(BlockPos p_225525_1_, ColorResolver p_225525_2_) {
        return inner.getBlockColor(p_225525_1_, p_225525_2_);
    }

    @Override
    public Biome getNoiseBiome(int p_225526_1_, int p_225526_2_, int p_225526_3_) {
        return inner.getNoiseBiome(p_225526_1_, p_225526_2_, p_225526_3_);
    }

    @Override
    public Biome getNoiseBiomeRaw(int i, int i1, int i2) {
        return inner.getNoiseBiomeRaw(i, i1, i2);
    }

    @Override
    public boolean isRemote() {
        return inner.isRemote();
    }

    @Override
    public int getSeaLevel() {
        return inner.getSeaLevel();
    }

    @Override
    public Dimension getDimension() {
        return inner.getDimension();
    }

    @Override
    public boolean isAirBlock(BlockPos p_175623_1_) {
        return inner.isAirBlock(p_175623_1_);
    }

    @Override
    public boolean canBlockSeeSky(BlockPos p_175710_1_) {
        return inner.canBlockSeeSky(p_175710_1_);
    }

    @Override
    @Deprecated
    public float getBrightness(BlockPos p_205052_1_) {
        return inner.getBrightness(p_205052_1_);
    }

    @Override
    public int getStrongPower(BlockPos p_175627_1_, Direction p_175627_2_) {
        return inner.getStrongPower(p_175627_1_, p_175627_2_);
    }

    @Override
    public boolean hasWater(BlockPos p_201671_1_) {
        return inner.hasWater(p_201671_1_);
    }

    @Override
    public boolean containsAnyLiquid(AxisAlignedBB p_72953_1_) {
        return inner.containsAnyLiquid(p_72953_1_);
    }

    @Override
    public int getLight(BlockPos p_201696_1_) {
        return inner.getLight(p_201696_1_);
    }

    @Override
    public int getNeighborAwareLightSubtracted(BlockPos p_205049_1_, int p_205049_2_) {
        return inner.getNeighborAwareLightSubtracted(p_205049_1_, p_205049_2_);
    }

    @Override
    @Deprecated
    public boolean isBlockLoaded(BlockPos p_175667_1_) {
        return inner.isBlockLoaded(p_175667_1_);
    }

    @Override
    public boolean isAreaLoaded(BlockPos p_isAreaLoaded_1_, int p_isAreaLoaded_2_) {
        return inner.isAreaLoaded(p_isAreaLoaded_1_, p_isAreaLoaded_2_);
    }

    @Override
    @Deprecated
    public boolean isAreaLoaded(BlockPos p_175707_1_, BlockPos p_175707_2_) {
        return inner.isAreaLoaded(p_175707_1_, p_175707_2_);
    }

    @Override
    @Deprecated
    public boolean isAreaLoaded(int p_217344_1_, int p_217344_2_, int p_217344_3_, int p_217344_4_, int p_217344_5_, int p_217344_6_) {
        return inner.isAreaLoaded(p_217344_1_, p_217344_2_, p_217344_3_, p_217344_4_, p_217344_5_, p_217344_6_);
    }

    @Override
    public WorldLightManager getLightManager() {
        return inner.getLightManager();
    }

    @Override
    public int getLightFor(LightType p_226658_1_, BlockPos p_226658_2_) {
        return inner.getLightFor(p_226658_1_, p_226658_2_);
    }

    @Override
    public int getLightSubtracted(BlockPos p_226659_1_, int p_226659_2_) {
        return inner.getLightSubtracted(p_226659_1_, p_226659_2_);
    }

    @Override
    public boolean canSeeSky(BlockPos p_226660_1_) {
        return inner.canSeeSky(p_226660_1_);
    }

    @Override
    @Nullable
    public TileEntity getTileEntity(BlockPos blockPos) {
        return inner.getTileEntity(blockPos);
    }

    @Override
    public int getLightValue(BlockPos p_217298_1_) {
        return inner.getLightValue(p_217298_1_);
    }

    @Override
    public int getMaxLightLevel() {
        return inner.getMaxLightLevel();
    }

    @Override
    public int getHeight() {
        return inner.getHeight();
    }

    @Override
    public BlockRayTraceResult rayTraceBlocks(RayTraceContext p_217299_1_) {
        return inner.rayTraceBlocks(p_217299_1_);
    }

    @Override
    @Nullable
    public BlockRayTraceResult rayTraceBlocks(Vec3d p_217296_1_, Vec3d p_217296_2_, BlockPos p_217296_3_, VoxelShape p_217296_4_, BlockState p_217296_5_) {
        return inner.rayTraceBlocks(p_217296_1_, p_217296_2_, p_217296_3_, p_217296_4_, p_217296_5_);
    }

    @Override
    public WorldBorder getWorldBorder() {
        return inner.getWorldBorder();
    }

    @Override
    public boolean placedBlockWouldCollide(BlockState p_226663_1_, BlockPos p_226663_2_, ISelectionContext p_226663_3_) {
        return inner.placedBlockWouldCollide(p_226663_1_, p_226663_2_, p_226663_3_);
    }

    @Override
    public boolean checkNoEntityCollision(Entity p_226668_1_) {
        return inner.checkNoEntityCollision(p_226668_1_);
    }

    @Override
    public boolean hasNoCollisions(AxisAlignedBB p_226664_1_) {
        return inner.hasNoCollisions(p_226664_1_);
    }

    @Override
    public boolean hasNoCollisions(Entity p_226669_1_) {
        return inner.hasNoCollisions(p_226669_1_);
    }

    @Override
    public boolean hasNoCollisions(Entity p_226665_1_, AxisAlignedBB p_226665_2_) {
        return inner.hasNoCollisions(p_226665_1_, p_226665_2_);
    }

    @Override
    public boolean hasNoCollisions(@Nullable Entity p_226662_1_, AxisAlignedBB p_226662_2_, Set<Entity> p_226662_3_) {
        return inner.hasNoCollisions(p_226662_1_, p_226662_2_, p_226662_3_);
    }

    @Override
    public Stream<VoxelShape> getCollisionShapes(@Nullable Entity p_226667_1_, AxisAlignedBB p_226667_2_, Set<Entity> p_226667_3_) {
        return inner.getCollisionShapes(p_226667_1_, p_226667_2_, p_226667_3_);
    }

    @Override
    public Stream<VoxelShape> getCollisionShapes(@Nullable Entity p_226666_1_, AxisAlignedBB p_226666_2_) {
        return inner.getCollisionShapes(p_226666_1_, p_226666_2_);
    }

    @Override
    public int getMaxHeight() {
        return inner.getMaxHeight();
    }


    @Override
    public boolean destroyBlock(BlockPos p_175655_1_, boolean p_175655_2_) {
        return inner.destroyBlock(p_175655_1_, p_175655_2_);
    }

    @Override
    public boolean destroyBlock(BlockPos blockPos, boolean b, @Nullable Entity entity) {
        return inner.destroyBlock(blockPos, b, entity);
    }

    @Override
    public boolean addEntity(Entity p_217376_1_) {
        return inner.addEntity(p_217376_1_);
    }
    //endregion
}
