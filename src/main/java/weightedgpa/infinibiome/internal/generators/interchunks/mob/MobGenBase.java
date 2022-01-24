package weightedgpa.infinibiome.internal.generators.interchunks.mob;

import net.minecraft.block.BlockState;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.passive.TurtleEntity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.IWorld;
import org.apache.commons.lang3.Validate;
import weightedgpa.infinibiome.api.dependency.DependencyInjector;
import weightedgpa.infinibiome.api.generators.InterChunkGen;
import weightedgpa.infinibiome.api.generators.InterChunkGenTimings;
import weightedgpa.infinibiome.api.generators.Timing;
import weightedgpa.infinibiome.api.generators.nonworldgen.Locatable;
import weightedgpa.infinibiome.api.pointsprovider.PointsProvider;
import weightedgpa.infinibiome.api.pos.BlockPos2D;
import weightedgpa.infinibiome.api.pos.InterChunkPos;
import weightedgpa.infinibiome.api.posdata.LandmassInfo;
import weightedgpa.infinibiome.api.posdata.PosDataKeys;
import weightedgpa.infinibiome.internal.floatfunc.IntFunc;
import weightedgpa.infinibiome.internal.floatfunc.generators.RandomGen;
import weightedgpa.infinibiome.internal.floatfunc.util.Interval;
import weightedgpa.infinibiome.internal.generators.utils.*;
import weightedgpa.infinibiome.internal.generators.utils.condition.Condition;
import weightedgpa.infinibiome.internal.generators.utils.condition.ConditionHelper;
import weightedgpa.infinibiome.internal.generators.utils.condition.ConditionList;
import weightedgpa.infinibiome.internal.minecraftImpl.commands.DebugCommand;
import weightedgpa.infinibiome.internal.minecraftImpl.world.NullWorld;
import weightedgpa.infinibiome.internal.misc.MCHelper;
import weightedgpa.infinibiome.internal.misc.MathHelper;
import weightedgpa.infinibiome.internal.misc.PosModCache;

import java.util.Random;
import java.util.function.Function;
import java.util.function.Predicate;


public abstract class MobGenBase extends GeneratorBase implements InterChunkGen, Locatable.HasPointsProvider {
    private static final int MAX_RETRIES = 4;

    protected Config config = null;

    private final PosModCache<InterChunkPos, Boolean> canSpawnAtInterChunk = new PosModCache<>(
        8,
        this::uncachedCanSpawnAtInterChunk,
        InterChunkPos.INFO
    );

    MobGenBase(DependencyInjector di, String seedBranch) {
        super(
            di,
            seedBranch
        );

        DebugCommand.registerDebugFunc(
            seedBranch,
            "conditions",
            p -> config.conditions._debug(p)
        );
    }

    boolean canSpawnAtInterChunk(InterChunkPos interChunkPos) {
        return canSpawnAtInterChunk.get(interChunkPos);
    }

    boolean uncachedCanSpawnAtInterChunk(InterChunkPos interChunkPos){
        Random random = randomGen.getRandom(interChunkPos.getX(), interChunkPos.getZ());

        double probability = config.conditions.getAllProbability(
            interChunkPos,
            ConditionList.StrictOption.FOUR_CORNER_CHECK
        );

        return MathHelper.randomBool(probability, random);
    }

    @Override
    public final Timing getInterChunkTiming() {
        return InterChunkGenTimings.MOBS;
    }

    @Override
    public final void generate(InterChunkPos interChunkPos, IWorld interChunks) {
        if (!canSpawnAtInterChunk(interChunkPos)) {
            return;
        }

        Random random = randomGen.getRandom(interChunkPos.getX(), interChunkPos.getZ());

        for (int i = 0; i < config.countFunc.getIntOutput(interChunkPos.getLowestCenterBlockPos()); i++) {
            for (int attempt = 0; attempt < MAX_RETRIES; attempt++) {
                BlockPos2D mobPos2D = interChunkPos.getRandomCenterPos(random);

                BlockPos mobPos = mobPos2D.to3D(getHeight(mobPos2D, interChunks));

                if (trySpawnMob(mobPos, interChunkPos, interChunks, random)) {
                    break;
                }
            }
        }
    }

    int getHeight(BlockPos2D mobPos2D, IWorld world) {
        int maxHeight = Integer.MIN_VALUE;

        for (int x = 0; x < getXLength(); x++) {
            for (int z = 0; z < getZLength(); z++) {
                BlockPos2D scannedPos = mobPos2D.offset(x, z);

                int scannedHeight = MCHelper.getHighestTerrainHeight(scannedPos, world) + 1;

                if (scannedHeight > maxHeight) {
                    maxHeight = scannedHeight;
                }

            }
        }
        return maxHeight;
    }

    private boolean enoughAir(BlockPos mobPos3D, IWorld world) {
        for (int x = 0; x < getXLength(); x++) {
            for (int y = 0; y < getYLength(); y++) {
                for (int z = 0; z < getZLength(); z++) {
                    BlockState block = world.getBlockState(mobPos3D.up(y));

                    if (!config.canSpawnUnderwater && MCHelper.isMostlyWater(block)) {
                        return false;
                    }

                    if (!MCHelper.isMostlyAir(block) && !MCHelper.isMostlyWater(block)) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    private int getXLength() {
        return MathHelper.ceil(
            getBox().getXSize()
        );
    }

    private int getYLength() {
        return MathHelper.ceil(
            getBox().getYSize()
        );

    }

    private int getZLength() {
        return MathHelper.ceil(
            getBox().getZSize()
        );
    }

    private AxisAlignedBB getBox() {
        return config.sampleEntity.getBoundingBox();
    }

    private boolean trySpawnMob(BlockPos mobPos, InterChunkPos interChunkPos, IWorld world, Random random) {
        if (!enoughAir(mobPos, world)) {
            return false;
        }

        if (world.getBlockState(mobPos.down()).getMaterial().isLiquid()) {
            return false;
        }

        //System.out.println(this.getClass() + "{");
        spawnAdult(mobPos, interChunkPos, world);
        //System.out.println("}");

        //System.out.println("baby{");
        trySpawnBaby(mobPos, interChunkPos, world, random);
        //System.out.println("}");

        return true;
    }

    private void spawnAdult(BlockPos mobPos, InterChunkPos interChunkPos, IWorld world) {
        AnimalEntity mob = config.getEntityFunc.get(mobPos, interChunkPos, world);

        if (mob instanceof TurtleEntity){
            //System.out.println("turtle at " + mobPos);
        }

        mob.enablePersistence();

        MCHelper.spawnEntity(mob, mobPos, world);
    }

    private void trySpawnBaby(BlockPos mobPos3D, InterChunkPos interChunkPos, IWorld world, Random random) {
        if (!MathHelper.randomBool(config.babyChance, random)) {
            return;
        }

        AnimalEntity baby = config.getEntityFunc.get(mobPos3D, interChunkPos, world.getWorld());

        baby.setGrowingAge(-24000);

        baby.enablePersistence();

        MCHelper.spawnEntity(baby, mobPos3D, world);
    }

    @Override
    public void checkIsValid()    {
        Validate.notNull(config);
    }

    @Override
    public PointsProvider<BlockPos2D> getAllLocations() {
        return new PredicateSearcher<>(
            1,
            p ->
            canSpawnAtInterChunk(p) && !(!config.canSpawnUnderwater && posData.get(PosDataKeys.LANDMASS_TYPE, p.getLowestCenterBlockPos()).isOcean()),
            InterChunkPos.INFO
        )
        .mapPoints(
            BlockPos2D.INFO
        );
    }

    Config.EntityFuncStep initConfig() {
        return new Config().new EntityFuncStep();
    }

    interface GetEntityFunc {
        AnimalEntity get(BlockPos pos, InterChunkPos interchunk, IWorld world);
    }

    @SuppressWarnings("InstanceVariableMayNotBeInitialized")
    class Config {
        private Config() {}

        private GetEntityFunc getEntityFunc;
        private AnimalEntity sampleEntity;

        private IntFunc<BlockPos2D> countFunc;
        private double babyChance;
        private boolean canSpawnUnderwater;

        ConditionList conditions = new ConditionList();

        class EntityFuncStep {
            CountStep getEntity(Function<IWorld, AnimalEntity> getEntity) {
                getEntityFunc = (b, a, world) -> getEntity.apply(world);

                sampleEntity = getEntity.apply(new NullWorld());

                return new CountStep();
            }

            CountStep getEntity(GetEntityFunc getEntityFunc) {
                Config.this.getEntityFunc = getEntityFunc;

                sampleEntity = getEntityFunc.get(
                    BlockPos.ZERO,
                    new InterChunkPos(new ChunkPos(0, 0)),
                    new NullWorld()
                );


                return new CountStep();
            }
        }

        class CountStep {
            BabyChanceStep setGroupCount(int count) {
                return setGroupCount(IntFunc.constFunc(count));
            }

            BabyChanceStep setGroupCount(int minCount, int maxCount) {
                return setGroupCount(
                    new RandomGen(seed).asPercentFloatFunc(BlockPos2D.INFO).mapToIntInterval(
                        minCount,
                        maxCount
                    )
                );
            }

            BabyChanceStep setGroupCount(IntFunc<BlockPos2D> count) {
                Validate.isTrue(count.getOutputInterval().getMin() >= 1);

                Config.this.countFunc = count;

                return new BabyChanceStep();
            }
        }

        class BabyChanceStep {
            UnderwaterStep setBabyChance(double chance) {
                Validate.isTrue(chance >= 0);

                Config.this.babyChance = chance;

                return new UnderwaterStep();
            }
        }

        class UnderwaterStep {
            MushroomIslandStep alwaysAboveWater() {
                canSpawnUnderwater = false;

                return new MushroomIslandStep();
            }

            MushroomIslandStep includingUnderwater() {
                canSpawnUnderwater = true;

                return new MushroomIslandStep();
            }
        }

        class MushroomIslandStep {
            WalkableSlopeStep neverInMushroomIsland() {
                conditions = conditions.add(
                    ConditionHelper.onlyInMushroomIsland(di).invert()
                );

                return new WalkableSlopeStep();
            }

            WalkableSlopeStep onlyInMushroomIsland() {
                conditions = conditions.add(
                    ConditionHelper.onlyInMushroomIsland(di)
                );

                return new WalkableSlopeStep();
            }
        }

        class WalkableSlopeStep {
            LandmassInfoStep anySlopeIncludingHigh() {
                return new LandmassInfoStep();
            }

            LandmassInfoStep anyNonHighSlope() {
                return anySlopeIncludingHigh();

                /*
                return setSlope(
                    new Interval(0, 1)
                );

                 */
            }

            LandmassInfoStep setSlope(Interval slope){
                conditions = conditions.add(
                    ConditionHelper.onlyInSlope(
                        di,
                        15,
                        slope
                    )
                );
                return new LandmassInfoStep();
            }

        }

        class LandmassInfoStep {
            TemperatureStep onlyOnNonBeachLand() {
                return setLandMass(
                    l -> l.isLand()
                );
            }

            TemperatureStep inLandOrBeach() {
                return setLandMass(
                    l -> !l.isOcean()
                );
            }

            TemperatureStep setLandMass(Predicate<LandmassInfo> landmassType){
                conditions = conditions.add(
                    ConditionHelper.onlyInLandMass(
                        di,
                        landmassType
                    )
                );

                return new TemperatureStep();
            }
        }

        class TemperatureStep {
            HumdityStep anyTemperatureIncludingFreezing(){
                return new HumdityStep();
            }

            HumdityStep anyNonFreezingTemp(){
                return setTemperature(
                    GenHelper.NOT_FREEZING
                );
            }

            HumdityStep setTemperature(Interval temperature) {
                conditions = conditions.add(
                    ConditionHelper.onlyInTemperature(
                        di,
                        temperature
                    )
                );

                return new HumdityStep();
            }
        }

        class HumdityStep {
            ChanceStep anyNonDesertHumidity() {
                conditions = conditions.add(
                    ConditionHelper.onlyInHumidity(
                        di,
                        GenHelper.NOT_DESERT
                    )
                );

                return new ChanceStep();
            }

            ChanceStep anyHumidityIncludingDesert() {
                return new ChanceStep();
            }

            ChanceStep setHumidity(Interval humidity) {
                conditions = conditions.add(
                    ConditionHelper.onlyInHumidity(
                        di,
                        humidity
                    )
                );

                return new ChanceStep();
            }
        }

        class ChanceStep {
            ConditionsStep setChancePerChunk(double chance) {
                Validate.isTrue(chance > 0);

                conditions = conditions.add(
                    ConditionHelper.chancePerChunk(
                        chance
                    )
                );

                return new ConditionsStep();
            }
        }

        class ConditionsStep {
            Config addExtraConditions(Condition extraCondition0, Condition... extraConditions) {
                conditions = conditions.add(extraCondition0);
                conditions = conditions.add(extraConditions);

                return Config.this;
            }

            Config noExtraConditions() {
                return Config.this;
            }
        }

    }
}
