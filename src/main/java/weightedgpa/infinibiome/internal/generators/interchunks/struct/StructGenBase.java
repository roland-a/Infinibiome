package weightedgpa.infinibiome.internal.generators.interchunks.struct;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.SharedSeedRandom;
import net.minecraft.util.math.*;
import net.minecraft.world.*;
import net.minecraft.world.gen.feature.IFeatureConfig;
import net.minecraft.world.gen.feature.NoFeatureConfig;
import net.minecraft.world.gen.feature.structure.Structure;
import org.apache.commons.lang3.Validate;
import weightedgpa.infinibiome.api.dependency.DependencyInjector;
import weightedgpa.infinibiome.api.generators.*;
import weightedgpa.infinibiome.api.generators.nonworldgen.Locatable;
import weightedgpa.infinibiome.api.pointsprovider.PointsProvider;
import weightedgpa.infinibiome.api.pos.BlockPos2D;
import weightedgpa.infinibiome.api.pos.InterChunkPos;
import weightedgpa.infinibiome.internal.generators.utils.*;
import weightedgpa.infinibiome.internal.generators.utils.condition.Condition;
import weightedgpa.infinibiome.internal.generators.utils.condition.ConditionHelper;
import weightedgpa.infinibiome.internal.generators.utils.condition.ConditionList;
import weightedgpa.infinibiome.internal.minecraftImpl.ChunkDataWriter;
import weightedgpa.infinibiome.internal.minecraftImpl.world.ChangeDetectingWorld;
import weightedgpa.infinibiome.internal.misc.MathHelper;

import javax.annotation.Nullable;
import java.util.Random;
import java.util.function.Function;

import static weightedgpa.infinibiome.internal.generators.utils.condition.ConditionHelper.onlyIfNotNear;

abstract class StructGenBase extends GeneratorBase implements StructGen, Locatable.HasPointsProvider {
    Config baseConfig = null;

    StructGenBase(DependencyInjector di, String seedBranch) {
        super(di, seedBranch);
    }

    protected void postGenerate(InterChunkPos interChunkPos, IWorld world){}

    @Override
    public final Structure getStruct() {
        return baseConfig.struct;
    }

    @Override
    public final void generate(InterChunkPos interChunkPos, IWorld world) {
        int lowestChunkX = interChunkPos.getLowestChunkPos().x;
        int lowestChunkZ = interChunkPos.getLowestChunkPos().z;

        boolean hasStruct = false;

        for (int x = 0; x <= 1; x++){
            for (int z = 0; z <= 1; z++){
                ChunkPos chunkPos = new ChunkPos(lowestChunkX + x, lowestChunkZ + z);

                boolean hasStructFlag = placeStructAtChunk(chunkPos, world);

                hasStruct |= hasStructFlag;
            }
        }

        if (hasStruct){
            postGenerate(interChunkPos, world);
        }
    }

    @SuppressWarnings({"unchecked"})
    private boolean placeStructAtChunk(ChunkPos chunkPos, IWorld world){
        if (isMarked(chunkPos)) return true;

        ChangeDetectingWorld worldWrapper = new ChangeDetectingWorld(world);

        SharedSeedRandom sharedSeed = new SharedSeedRandom(randomGen.getRandomInt(chunkPos.x, chunkPos.z));

        getStruct().place(
            worldWrapper,
            chunkGenerator,
            sharedSeed,
            chunkPos.asBlockPos(),
            null
        );

        if (worldWrapper.anyChange()){
            mark(chunkPos);
            return true;
        }
        return false;
    }

    private boolean isMarked(ChunkPos chunkPos){
        CompoundNBT nbt = ChunkDataWriter.readChunk(chunkPos);

        return nbt.contains(getNBTTag());
    }

    private void mark(ChunkPos chunkPos){
        ChunkDataWriter.write(
            chunkPos,
            nbt -> nbt.putBoolean(getNBTTag(), true)
        );
    }

    private String getNBTTag(){
        return "ib:" + getStruct().getStructureName();
    }

    @Nullable
    @Override
    public final IFeatureConfig hasStructureStartHere(ChunkPos chunkPos) {
        if (!shouldSpawnAt(chunkPos)) return null;

        return baseConfig.configFunc.apply(chunkPos);
    }

    private boolean shouldSpawnAt(ChunkPos pos){
        Random random = randomGen.getRandom(pos.x, pos.z);

        double probability = baseConfig.conditions.getAllProbability(
            pos,
            ConditionList.StrictOption.USE_LIKE_NON_STRICT
        );

        return MathHelper.randomBool(
            probability,
            random
        );
    }

    @Override
    public final Timing getInterChunkTiming() {
        return InterChunkGenTimings.STRUCTS;
    }

    @Override
    public final void checkIsValid() {
        Validate.notNull(baseConfig);
    }

    @Override
    public final PointsProvider<BlockPos2D> getAllLocations() {
        return baseConfig.locations.filterOutput(
            this::shouldSpawnAt
        )
        .mapPoints(
            BlockPos2D.INFO
        );
    }

    final Config.StructureStep initConfig(){
        return new Config().new StructureStep();
    }

    /*
    @FunctionalInterface
    interface PostGenFunc {
        void run(InterChunkPos pos, IWorld world);
    }

     */

    @SuppressWarnings("InstanceVariableMayNotBeInitialized")
    class Config {
        private Structure<?> struct;
        private Function<ChunkPos, ? extends IFeatureConfig> configFunc;
        private PointsProvider<ChunkPos> locations;
        ConditionList conditions = new ConditionList(
            ConditionHelper.onlyWhereInfibiomeGenAllowed(di)
        );

        //private PostGenFunc postGenFunc = (pos, world) -> {};

        Config(){
            if (!(StructGenBase.this instanceof IllagerMansionGen)){
                conditions = conditions.add(
                    onlyIfNotNear(
                        di,
                        10,
                        (m, p) -> m.baseConfig.conditions.canBeHere(p),
                        IllagerMansionGen.class
                    )
                    .noConflict()
                );
            }
        }


        class StructureStep{
            <T extends IFeatureConfig> ChanceStep withStructAndFunc(Structure<T> struct, Function<ChunkPos, T> configFunc){
                Config.this.struct = struct;
                Config.this.configFunc = configFunc;

                return new ChanceStep();
            }

            <T extends IFeatureConfig> ChanceStep withStruct(Structure<T> struct, T config){
                return withStructAndFunc(struct, __ -> config);
            }

            ChanceStep withStruct(Structure<NoFeatureConfig> struct){
                return withStruct(struct, IFeatureConfig.NO_FEATURE_CONFIG);
            }
        }

        class ChanceStep{
            ExtraConditionStep withChance(Class<? extends StructConfigBase> configClass, int separation){
                return withPoints(
                    ConditionHelper.initSeparatedChunkLocations(
                        seed,
                        separation,
                        di.getAll(configClass).get(0).rate
                    )
                );
            }

            ExtraConditionStep withPoints(PointsProvider<ChunkPos> points){
                locations = points;

                conditions = conditions.add(
                    /*
                    ConditionHelper.onlyInPoints(
                        locations.mapPoints(InterChunkPos.INFO)
                    )
                  */
                    //todo replace with commented
                    new Condition.BoolInterpolated() {
                        @Override
                        public boolean passes(BlockPos2D pos) {
                            return locations.hasPoint(pos.toChunkPos());
                        }
                    }
                );

                return new ExtraConditionStep();
            }
        }

        class ExtraConditionStep{
            Config addExtraConditions(Condition extraCondition0, Condition... extraConditions){
                conditions = conditions.add(extraCondition0);
                conditions = conditions.add(extraConditions);

                return Config.this;
            }

            Config noExtraCondition() {
                return Config.this;
            }
        }

        /*
        Config setPostGenFunc(PostGenFunc postGenFunc){
            Config.this.postGenFunc = postGenFunc;

            return this;
        }

         */
    }
}
