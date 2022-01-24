package weightedgpa.infinibiome.internal.generators.interchunks.plant;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import net.minecraft.block.*;
import net.minecraft.state.properties.DoubleBlockHalf;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorld;
import net.minecraft.world.IWorldReader;
import org.apache.commons.lang3.Validate;
import org.jetbrains.annotations.Nullable;
import weightedgpa.infinibiome.api.dependency.DependencyInjector;
import weightedgpa.infinibiome.api.generators.InterChunkGen;
import weightedgpa.infinibiome.api.generators.InterChunkGenTimings;
import weightedgpa.infinibiome.api.generators.PlantGrowthConfig;
import weightedgpa.infinibiome.api.generators.Timing;
import weightedgpa.infinibiome.api.generators.nonworldgen.GroundBoneMealController;
import weightedgpa.infinibiome.api.generators.nonworldgen.Locatable;
import weightedgpa.infinibiome.api.generators.nonworldgen.PlantGrowthController;
import weightedgpa.infinibiome.api.pointsprovider.PointsProvider;
import weightedgpa.infinibiome.api.pos.BlockPos2D;
import weightedgpa.infinibiome.api.pos.InterChunkPos;
import weightedgpa.infinibiome.internal.floatfunc.FloatFunc;
import weightedgpa.infinibiome.internal.floatfunc.util.Interval;
import weightedgpa.infinibiome.internal.generators.utils.*;
import weightedgpa.infinibiome.internal.generators.utils.condition.Condition;
import weightedgpa.infinibiome.internal.generators.utils.condition.ConditionHelper;
import weightedgpa.infinibiome.internal.generators.utils.condition.ConditionList;
import weightedgpa.infinibiome.internal.generators.utils.condition.ConditionList.StrictOption;
import weightedgpa.infinibiome.internal.minecraftImpl.commands.DebugCommand;
import weightedgpa.infinibiome.internal.minecraftImpl.world.ClientUpdatingWorld;
import weightedgpa.infinibiome.internal.misc.Helper;
import weightedgpa.infinibiome.internal.misc.MCHelper;
import weightedgpa.infinibiome.internal.misc.MathHelper;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.function.BiPredicate;


abstract class PlantGenBase extends GeneratorBase implements InterChunkGen, GroundBoneMealController, PlantGrowthController, Locatable.HasPointsProvider {
    protected Config config;

    private final PlantGrowthConfig growthConfig;

    PlantGenBase(
        DependencyInjector di,
        String seedBranch
    ){
        super(di, seedBranch);

        this.growthConfig = di.get(PlantGrowthConfig.class);

        DebugCommand.registerDebugFunc(
            seedBranch,
            "groupCount",
            p -> getGroupCount(new InterChunkPos(p))
        );

        DebugCommand.registerDebugFunc(
            seedBranch,
            "rate",
            p ->
                config.rateFunc.getOutput(p) *
                config.conditions.getAllProbability(p, StrictOption.USE_LIKE_NON_STRICT)
        );

        DebugCommand.registerDebugFunc(
            seedBranch,
            "funcRate",
            p -> config.rateFunc.getOutput(p)
        );

        DebugCommand.registerDebugFunc(
            seedBranch,
            "conditions",
            p -> config.conditions._debug(p)
        );
    }

    @Override
    public final Timing getInterChunkTiming() {
        return InterChunkGenTimings.PLANTS;
    }

    @Override
    public final void generate(InterChunkPos interChunkPos, IWorld interChunks) {
        Random random = randomGen.getRandom(interChunkPos.getX(), interChunkPos.getZ());

        int count = getGroupCount(interChunkPos);

        double radius = Interval.PERCENT.mapInterval(random.nextFloat(), config.radius);
        double density = Interval.PERCENT.mapInterval(random.nextFloat(), config.density);

        double radiusSq = Math.pow(radius, 2);
        int radiusCeil = MathHelper.ceil(radius);

        for (int i = 0; i < count; i++){
            BlockPos2D clusterCenter = interChunkPos.getRandomCenterPos(random);

            for (int x = -radiusCeil; x <= radiusCeil; x++){
                for (int z = -radiusCeil; z <= radiusCeil; z++){
                    BlockPos2D plantPos2D = clusterCenter.offset(x, z);

                    if (!MathHelper.randomBool(density, random)){
                        continue;
                    }

                    if (MathHelper.getDistanceSq(BlockPos2D.INFO, clusterCenter, plantPos2D) > radiusSq){
                        continue;
                    }

                    int plantY = getPlantY(plantPos2D, interChunks);

                    BlockPos plantPos = plantPos2D.to3D(plantY);

                    tryPlacePlant(
                        plantPos,
                        interChunks,
                        random,
                        false
                    );
                }
            }
        }
    }

    private int getGroupCount(InterChunkPos interChunkPos){
        double result = 1;

        result *= config.conditions.getAllProbability(
            interChunkPos,
            StrictOption.EXCLUDE
        );

        if (result == 0) return 0;

        result *= config.rateFunc.getOutput(interChunkPos.getLowestCenterBlockPos());

        if (result == 0) return 0;

        return MathHelper.randomRound(result, randomGen.getRandom(interChunkPos.getX(), interChunkPos.getZ()));
    }

    private int getPlantY(BlockPos2D pos, IWorldReader world){
        if (config.spawnsUnderwater){
            return MCHelper.getHighestTerrainHeight(pos, world) + 1;
        }
        return MCHelper.getHighestSurfaceHeight(pos, world) + 1;
    }

    private boolean tryPlacePlant(BlockPos plantPos, IWorld world, Random random, boolean isBoneMealed){
        List<@Nullable BlockState> plantBlocks;

        if (!isBoneMealed) {
            plantBlocks = config.plantBlockFunc.get(plantPos, world, random);
        } else {
            plantBlocks = config.boneMealPlantBlockFunc.get(plantPos, world, random);
        }

        if (!isValidAt(plantBlocks, plantPos, world, random, isBoneMealed)) return false;

        placePlantBlocks(plantBlocks, plantPos, world, isBoneMealed);

        return true;
    }

    private boolean isValidAt(List<@Nullable BlockState> plantBlocks, BlockPos plantPos, IWorldReader world, Random random, boolean isBoneMealed){
        if (plantBlocks.isEmpty()) return false;

        if (allNull(plantBlocks)) return false;

        double specificProbability = config.conditions.getAllProbability(
            MCHelper.to2D(plantPos),
            StrictOption.ONLY
        );

        if (!MathHelper.randomBool(specificProbability, random)) return false;

        if (!config.checkGroundFunc.test(plantPos.down(), world)) return false;

        if (!isValidSpace(plantBlocks, plantPos, world, isBoneMealed)) return false;

        //if (!isValidLight(plantPos, world)) return false;

        return true;
    }

    @SuppressWarnings("VariableNotUsedInsideIf")
    private boolean allNull(List<@Nullable BlockState> plantBlocks){
        for (BlockState block: plantBlocks){
            if (block != null) return false;
        }
        return true;
    }

    /*
    private boolean isValidLight(BlockPos plantPos, IWorldReader world){
        if (config.validLight.equals(Interval.ALL_VALUES)) return true;

        return config.validLight.contains(world.getLightValue(plantPos));
    }

     */

    private boolean isValidSpace(List<@Nullable BlockState> plantBlocks, BlockPos plantPos, IWorldReader world, boolean isBoneMealed){
        for (int y = 0; y < plantBlocks.size(); y++){
            if (plantBlocks.get(y) == null) continue;

            BlockPos scanPos = plantPos.up(y);
            BlockState block = world.getBlockState(scanPos);

            if (!canOverrideBlock(block, isBoneMealed)) return false;

            if (config.spawnsUnderwater && PlantHelper.iceAtPos(scanPos, world, posData)) return false;
        }
        return true;
    }

    private static final List<Block> grassBlocks = Lists.newArrayList(
        Blocks.GRASS,
        Blocks.TALL_GRASS,
        Blocks.FERN,
        Blocks.LARGE_FERN,
        Blocks.SEAGRASS,
        Blocks.TALL_SEAGRASS
    );

    private boolean canOverrideBlock(BlockState block, boolean boneMealed){
        boolean isAir = block.getBlock().equals(Blocks.AIR);
        boolean isMostlyAir = MCHelper.isMostlyAir(block);
        boolean isSnow = block.getBlock().equals(Blocks.SNOW);
        boolean isWater = block.getBlock().equals(Blocks.WATER);
        boolean isMostlyWater = MCHelper.isMostlyWater(block);
        boolean isPlant = MCHelper.isPlant(block.getBlock());
        boolean isGrass = grassBlocks.contains(block.getBlock());

        //if abovewater
        if (!config.spawnsUnderwater) {
            //can never override non air blocks
            if (!isMostlyAir) return false;

            if (!boneMealed && config.canOverridePlants){
                return isPlant || isAir || isSnow;
            }
            //if bonemealed or cant override
            //only plant that can be overridden is grass
            return isGrass || isAir || isSnow;
        }
        //if underwater
        {
            //can never override non water blocks
            if (!isMostlyWater) return false;

            if (!boneMealed && config.canOverridePlants) {
                return isPlant || isWater;
            }
            //if bonemealed or cant override
            return isGrass || isWater;
        }
    }

    private void placePlantBlocks(List<@Nullable BlockState> plantBlocks, BlockPos plantPos, IWorld world, boolean isBoneMealed){
        MCHelper.clearVertically(plantPos, world, b -> canOverrideBlock(b, isBoneMealed));

        for (int y = 0; y < plantBlocks.size(); y++){
            BlockState plantBlock = plantBlocks.get(y);

            if (plantBlock == null) continue;

            world.setBlockState(plantPos.up(y), plantBlock, MCHelper.DEFAULT_FLAG);
        }
        fixSnowyBlock(plantPos, world);
    }

    private void fixSnowyBlock(BlockPos plantPos, IWorld world){
        BlockState block = world.getBlockState(plantPos.down());

        if (block.has(SnowyDirtBlock.SNOWY) && block.get(SnowyDirtBlock.SNOWY)){
            world.setBlockState(plantPos.down(), block.with(SnowyDirtBlock.SNOWY, false), MCHelper.DEFAULT_FLAG);
        }
    }

    @Override
    public final boolean controlsPlant(BlockState block, boolean is2x2) {
        Block controlledBlock;

        if(config.sampleBlock instanceof StemGrownBlock){
            controlledBlock = ((StemGrownBlock)config.sampleBlock).getStem();
        }
        else {
            controlledBlock = config.sampleBlock;
        }

        return controlledBlock.equals(block.getBlock());
    }

    @Override
    public final boolean isValidGrowth(BlockPos pos, IWorld world) {
        boolean canDuplicate = BlockTags.TALL_FLOWERS.contains(config.sampleBlock);

        if (canDuplicate){
            return growthConfig.allowFlowerDuplication;
        }

        if (isGrowable(pos, world)) return true;

        if (config.sampleBlock instanceof SugarCaneBlock) return true;

        if (config.sampleBlock instanceof CactusBlock) return true;

        return false;
    }

    private boolean isGrowable(BlockPos pos, IWorld world){
        Block growingBlock;

        if (config.sampleBlock instanceof StemGrownBlock){
            growingBlock = ((StemGrownBlock) config.sampleBlock).getStem();
        }
        else {
            growingBlock = config.sampleBlock;
        }

        if (!(growingBlock instanceof IGrowable)) return false;

        return ((IGrowable) growingBlock).canGrow(world, pos, world.getBlockState(pos), false);
    }

    @Override
    public final boolean canGrowWithBonemeal(BlockPos pos, IWorld world, Random random) {
        if (growthConfig.allowPlantUniformGrowth){
            return true;
        }

        if (config.conditions.canBeHere(MCHelper.to2D(pos))){
            return true;
        }

        return MathHelper.randomBool(0.25, random);
    }

    @Override
    public final double getGroundBonemealChance(BlockPos2D pos) {
        if (!config.conditions.canBeHere(pos)) return 0;

        double radius = config.radius.getMidPoint();
        double density = config.density.getMidPoint();

        double approxCount = (radius + 0.5) * (radius + 0.5) * Math.PI * density;

        if (approxCount == 0) return 0;

        return (1 / approxCount) * 1/4d;
    }

    @Override
    public final void spawnFromGroundBoneMeal(BlockPos centerPos, IWorld world, Random random) {
        world = new ClientUpdatingWorld(world);

        BlockPos2D centerPos2D = MCHelper.to2D(centerPos);

        if (!config.conditions.canBeHere(MCHelper.to2D(centerPos))) return;

        if (!tryPlacePlant(centerPos, world, random, true)) return;

        double radius = Interval.PERCENT.mapInterval(random.nextFloat(), config.radius);
        double density = Interval.PERCENT.mapInterval(random.nextFloat(), config.density);

        IWorld finalWorld = world;
        Helper.placeClusterWithUnknownHeight(
            centerPos,
            FloatFunc.constFunc(radius),
            world,
            p -> {
                if (MCHelper.to2D(p).equals(centerPos2D)) return;

                if (!MathHelper.randomBool(density, random)) return;

                tryPlacePlant(p, finalWorld, random, true);
            }
        );
    }

    @Override
    public final void checkIsValid() {
        Validate.notNull(config);
    }

    @Override
    public final PointsProvider<BlockPos2D> getAllLocations() {
        return GenHelper.getCommonPredicateSearcher(
            32,
            config.spawnsUnderwater,
            config.conditions,
            posData
        );
    }

    Config.PlantGenStep initConfig(){
        return new Config().new PlantGenStep();
    }

    @FunctionalInterface
    interface PlantBlockFunc{
        List<@Nullable BlockState> get(BlockPos pos, IWorldReader world, Random random);
    }

    @SuppressWarnings("InstanceVariableMayNotBeInitialized")
    class Config{
        private PlantBlockFunc plantBlockFunc;
        private PlantBlockFunc boneMealPlantBlockFunc;
        private Block sampleBlock;

        private FloatFunc<BlockPos2D> rateFunc;
        private Interval radius;
        private Interval density;
        
        private boolean spawnsUnderwater = false;
        private boolean canOverridePlants = true;
        //private Interval validLight = Interval.ALL_VALUES;

        private BiPredicate<BlockPos, IWorldReader> checkGroundFunc =
            (groundPos, world) -> config.sampleBlock.getDefaultState().isValidPosition(
                world,
                groundPos.up()
            );

        ConditionList conditions = new ConditionList();

        class PlantGenStep {
            UnderwaterStep setPlant(Block plantBlock){
                return setPlant(plantBlock.getDefaultState());
            }

            UnderwaterStep setPlant(BlockState plantBlock){
                List<BlockState> plantBlocks;

                if (plantBlock.has(DoublePlantBlock.HALF)){
                    plantBlocks = ImmutableList.of(
                        plantBlock.with(DoublePlantBlock.HALF, DoubleBlockHalf.LOWER),
                        plantBlock.with(DoublePlantBlock.HALF, DoubleBlockHalf.UPPER)
                    );
                }
                else {
                    plantBlocks = ImmutableList.of(plantBlock);
                }

                return setPlant(plantBlocks);
            }

            UnderwaterStep setPlant(List<BlockState> plantBlocks){
                assert !plantBlocks.isEmpty();

                plantBlockFunc = (a, b, c) -> plantBlocks;
                boneMealPlantBlockFunc = plantBlockFunc;

                return new SampleStep().setSampleBlock(
                    plantBlocks.get(0).getBlock()
                );
            }

            SampleStep setPlantBlockFunc(PlantBlockFunc plantBlockFunc){
                Config.this.plantBlockFunc = plantBlockFunc;
                Config.this.boneMealPlantBlockFunc = (pos, world, rand) -> Collections.emptyList();

                return new SampleStep();
            }
        }

        class SampleStep{
            UnderwaterStep setSampleBlock(Block sampleBlock){
                Config.this.sampleBlock = sampleBlock;

                if (sampleBlock.getBlock() instanceof StemGrownBlock){
                    setCheckOnlyDirt();
                }

                return new UnderwaterStep();
            }
        }

        class UnderwaterStep {
            RateStep setAboveWater(){
                spawnsUnderwater = false;

                return new RateStep();
            }

            RateStep setUnderwater(){
                spawnsUnderwater = true;

                return new RateStep();
            }
        }

        class RateStep {
            RadiusStep setRate(FloatFunc<BlockPos2D> rateFunc){
                Validate.isTrue(new Interval(0, 255).containsAll(rateFunc.getOutputInterval()));

                Config.this.rateFunc = rateFunc;

                return new RadiusStep();
            }

            RadiusStep setRate(Interval rate){
                return setRate(
                    PlantHelper.getRateFunc(seed, rate, 0)
                );
            }

            RadiusStep setWithCommonRate(){
                return setRate(
                    PlantHelper.getCommonClusterRateFunc(seed)
                );
            }

            MushroomIslandStep setScatteredRate(Interval interval){
                return setScatteredRate(
                    PlantHelper.getRateFunc(seed, interval, PlantHelper.COMMON_SCATTERED_SKEW)
                );
            }

            MushroomIslandStep setScatteredRate(FloatFunc<BlockPos2D> rateFunc){
                return this
                    .setRate(rateFunc)
                    .setRadius(PlantHelper.SCATTERED_RADIUS)
                    .setDensity(PlantHelper.SCATTERED_DENSITY);
            }
        }

        class RadiusStep {
            DensityStep setRadius(Interval radius){
                Validate.isTrue(new Interval(0, 7).containsAll(radius));

                Config.this.radius = radius;

                return new DensityStep();
            }

            DensityStep setWithCommonRadius(){
                return setRadius(
                    PlantHelper.COMMON_RADIUS
                );
            }
        }

        class DensityStep {
            MushroomIslandStep setDensity(Interval density){
                Validate.isTrue(Interval.PERCENT.containsAll(density));

                Config.this.density = density;

                return new MushroomIslandStep();
            }

            MushroomIslandStep setWithCommonDensity(){
                return setDensity(
                    PlantHelper.COMMON_DENSITY
                );
            }
        }

        class MushroomIslandStep{
            TemperatureStep neverInMushroomIsland(){
                conditions = conditions.add(
                    ConditionHelper.onlyInMushroomIsland(di).invert()
                );

                return new TemperatureStep();
            }

            TemperatureStep alsoInMushroomIsland(){
                return new TemperatureStep();
            }
        }

        class TemperatureStep {
            HumidityStep anyNonFreezingTemperature(){
                return setTemperature(
                    GenHelper.NOT_FREEZING
                );
            }

            HumidityStep setTemperature(Interval intervals){
                conditions = conditions.add(
                    ConditionHelper.onlyInTemperature(
                        di,
                        intervals
                    )
                );
                return new HumidityStep();
            }

            public HumidityStep anyTemperatureIncludingFreezing() {
                return new HumidityStep();
            }
        }
        
        class HumidityStep{
            RegionStep anyHumidity(){
                return new RegionStep();
            }
            
            RegionStep setHumdity(Interval interval){
                conditions = conditions.add(
                    ConditionHelper.onlyInHumidity(
                        di,
                        interval
                    )
                );
                
                return new RegionStep();
            }
        }

        class RegionStep{
            ConditionStep setSpawnRegion(double rate){
                conditions = conditions.add(
                    ConditionHelper.onlyInRegion(
                        seed,
                        rate
                    )
                );

                return new ConditionStep();
            }

            ConditionStep setNoSpawnRegion(){
                return new ConditionStep();
            }
        }

        class ConditionStep {
            Config setExtraConditions(Condition extraCondition0, Condition... extraConditions){
                conditions = conditions.add(extraCondition0);
                conditions = conditions.add(extraConditions);

                return Config.this;
            }

            Config setNoExtraConditions(){
                return Config.this;
            }
        }

        Config setNoGroundBoneMeal(){
            boneMealPlantBlockFunc = (a, b, c) -> Collections.emptyList();

            return this;
        }

        Config setBoneMealPlantFunc(PlantBlockFunc plantBlockFunc){
            boneMealPlantBlockFunc = plantBlockFunc;

            return this;
        }

        Config setCantOverridePlants() {
            this.canOverridePlants = false;

            return this;
        }

        Config setNoGroundCheck(){
            return setCheckGroundFunc((blockPos, worldReader) -> true);
        }

        Config setCheckOnlyDirt(){
            return setCheckGroundFunc(
                (groundPos, worldReader) -> {
                    Block groundBlock = worldReader.getBlockState(groundPos).getBlock();

                    return Arrays.asList(
                        Blocks.GRASS_BLOCK,
                        Blocks.PODZOL,
                        Blocks.COARSE_DIRT
                    )
                    .contains(groundBlock);
                }
            );
        }

        Config setCheckGroundFunc(BiPredicate<BlockPos, IWorldReader> checkGroundFunc){
            this.checkGroundFunc = checkGroundFunc;

            return this;
        }

        /*
        Config setValidLight(Interval validLight) {
            this.validLight = validLight;

            return this;
        }

         */
    }
}
