package weightedgpa.infinibiome.internal.minecraftImpl;

import com.google.common.collect.Lists;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.FlowingFluidBlock;
import net.minecraft.fluid.Fluids;
import net.minecraft.fluid.IFluidState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.LightType;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.Biomes;
import net.minecraft.world.gen.surfacebuilders.SurfaceBuilder;
import net.minecraftforge.common.BiomeDictionary;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import weightedgpa.infinibiome.api.Infinibiome;
import weightedgpa.infinibiome.api.generators.ClimateConfig;
import weightedgpa.infinibiome.api.generators.nonworldgen.RunOnceEveryWorldStart;
import weightedgpa.infinibiome.internal.floatfunc.util.Interval;
import weightedgpa.infinibiome.api.dependency.DependencyInjector;
import weightedgpa.infinibiome.internal.generators.utils.GenHelper;
import weightedgpa.infinibiome.internal.minecraftImpl.commands.DebugCommand;
import weightedgpa.infinibiome.internal.misc.MCHelper;
import weightedgpa.infinibiome.api.pos.BlockPos2D;
import weightedgpa.infinibiome.api.posdata.*;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static weightedgpa.infinibiome.api.posdata.PosDataHelper.*;

public final class IBBiomes {
    private static final IBBiome[][][][] BIOMES = new IBBiome
        [TempType.LENGTH]
        [HumdType.LENGTH]
        [LandType.values().length]
        [AmpType.values().length];

    private static PosDataProvider posData = null;
    private static ClimateConfig climateConfig = null;

    private IBBiomes(){}

    public static void refresh(DependencyInjector di){
        posData = di.get(PosDataProvider.class);
        climateConfig = di.get(ClimateConfig.class);

        DebugCommand.registerDebugFunc(
            "f3_biome",
            "temperature",
            p -> getBiome(p, posData).getDefaultTemperature()
        );

        DebugCommand.registerDebugFunc(
            "f3_biome",
            "humidity",
            p -> getBiome(p, posData).getDownfall()
        );

        DebugCommand.registerDebugFunc(
            "f3_biome",
            "dictionary",
            p -> BiomeDictionary.getTypes(getBiome(p, posData))
        );
    }

    @SubscribeEvent
    public static void onRegisterBiomes(RegistryEvent.Register<Biome> event){
        System.out.println("registering biomes");

        generateBiomes(event);
    }

    private static void generateBiomes(RegistryEvent.Register<Biome> event){
        for (TempType temp: TempType.VALUES) {
            for (HumdType humd: HumdType.VALUES) {
                for (LandType landType: LandType.values()) {
                    for (AmpType ampType : AmpType.values()) {
                        IBBiome biome = new IBBiome(
                            temp,
                            humd,
                            landType,
                            ampType
                        );

                        biome.register(event);

                        BIOMES
                            [temp.ordinal]
                            [humd.ordinal]
                            [landType.ordinal()]
                            [ampType.ordinal()] = biome;
                    }
                }
            }
        }
    }

    public static Biome getBiome(BlockPos2D pos, PosDataProvider data){
        Optional<Biome> overrideBiome = data.get(PosDataKeys.OVERRIDE_BIOME, pos);

        if (overrideBiome.isPresent()) return overrideBiome.get();

        TempType tempType = TempType.get(pos);
        HumdType humdIndex = HumdType.get(pos);
        LandType landType = LandType.get(pos);
        AmpType ampType = AmpType.getAmp(pos);

        return BIOMES[tempType.ordinal][humdIndex.ordinal][landType.ordinal()][ampType.ordinal()];
    }

    private static final class IBBiome extends Biome{
        final TempType temp;
        final HumdType humd;
        final LandType landType;
        final AmpType ampType;

        IBBiome(TempType temp, HumdType humd, LandType landType, AmpType ampType) {
            super(initBuilder(temp.getApproxTemperature(), humd.getApproxHumidity(), landType));

            this.temp = temp;
            this.humd = humd;
            this.landType = landType;
            this.ampType = ampType;

            this.setRegistryName(
                Infinibiome.MOD_ID,
                toString()
            );
        }

        @Override
        public String toString() {
            return String.format("%s_%s_%s_%s_0_0_0_0",
                temp.ordinal,
                humd.ordinal,
                landType.ordinal(),
                ampType.ordinal()
            );
        }

        @Override
        public float getTemperatureRaw(BlockPos pos) {
            double temperature;

            temperature = PosDataHelper.getTemperature(pos, posData);

            temperature = PosDataHelper.fuzzTemperature(temperature, MCHelper.to2D(pos), climateConfig);

            return ibTempToMCTemp(temperature);
        }

        @Override
        public boolean doesWaterFreeze(IWorldReader worldIn, BlockPos pos, boolean mustBeAtEdge) {
            if (isFrozenOcean(pos)){
                return Biomes.FROZEN_OCEAN.getTemperature(pos) < 0.15f;
            }
            if (!isFrozenLand(pos)) {
                return false;
            }
            if (pos.getY() < 0) {
                return false;
            }
            if (pos.getY() >= 256) {
                return false;
            }
            if (worldIn.getLightFor(LightType.BLOCK, pos) >= 10) {
                return false;
            }

            BlockState blockstate = worldIn.getBlockState(pos);
            IFluidState ifluidstate = worldIn.getFluidState(pos);

            if (!ifluidstate.getFluid().equals(Fluids.WATER)) {
                return false;
            }
            if (!(blockstate.getBlock() instanceof FlowingFluidBlock)){
                return false;
            }

            if (mustBeAtEdge) {
                return
                    !worldIn.hasWater(pos.west()) ||
                    !worldIn.hasWater(pos.east()) ||
                    !worldIn.hasWater(pos.north()) ||
                    !worldIn.hasWater(pos.south());
            }

            return true;

        }

        @Override
        public boolean doesSnowGenerate(IWorldReader worldIn, BlockPos pos) {
            if (!isFrozenLand(pos) && !isFrozenOcean(pos)) {
                return false;
            }
            if (pos.getY() < 0) {
                return false;
            }
            if (pos.getY() >= 256) {
                return false;
            }
            if (worldIn.getLightFor(LightType.BLOCK, pos) >= 10) {
                return false;
            }

            BlockState blockstate = worldIn.getBlockState(pos);

            return blockstate.isAir(worldIn, pos) && Blocks.SNOW.getDefaultState().isValidPosition(worldIn, pos);

        }

        static boolean isFrozenOcean(BlockPos pos){
            BlockPos2D pos2D = MCHelper.to2D(pos);

            if (!posData.get(PosDataKeys.LANDMASS_TYPE, pos2D).isOcean()) return false;

            double temperature;

            temperature = posData.get(PosDataKeys.TEMPERATURE, pos2D).fromHeight(pos.getY());

            temperature = fuzz(temperature, 10, new Random(), climateConfig);

            return GenHelper.LOWER_FREEZE_INTERVAL.contains(temperature);
        }

        static boolean isFrozenLand(BlockPos pos){
            BlockPos2D pos2D = MCHelper.to2D(pos);

            if (posData.get(PosDataKeys.LANDMASS_TYPE, pos2D).isOcean()) return false;

            double temperature;

            temperature = posData.get(PosDataKeys.TEMPERATURE, pos2D).fromHeight(pos.getY());

            temperature = fuzzTemperature(temperature, pos2D, climateConfig);

            return FREEZE_INTERVAL.contains(temperature);
        }

        static float ibTempToMCTemp(double ibTemp){
            if (FREEZE_INTERVAL.contains(ibTemp)) {
                //less than .15 do it can snow
                return (float)FREEZE_INTERVAL.mapInterval(
                    ibTemp,
                    0.00,
                    0.14
                );
            }
            if (COLD_INTERVAL.contains(ibTemp)){
                return (float)COLD_INTERVAL.mapInterval(
                    ibTemp,
                    0.15,
                    0.81
                );
            }
            //greater than .8 so snow golems cant place snow
            if (WARM_INTERVAL.contains(ibTemp)){
                return (float)WARM_INTERVAL.mapInterval(
                    ibTemp,
                    0.81,
                    1.00
                );
            }
            assert HOT_INTERVAL.contains(ibTemp): ibTemp;

            //greater than 1 so snow golems can melt
            return (float)WARM_INTERVAL.mapInterval(
                ibTemp,
                1.01,
                2.00
            );
        }

        static float ibHumdToMCHumd(double ibHumd){
            if (DRY_INTERVAL.contains(ibHumd)){
                return 0f;
            }
            if (SEMI_DRY_INTERVAL.contains(ibHumd)){
                return (float)SEMI_DRY_INTERVAL.mapInterval(
                    ibHumd,
                    0.0,
                    0.4
                );
            }
            if (SEMI_WET_INTERVAL.contains(ibHumd)){
                return (float)SEMI_WET_INTERVAL.mapInterval(
                    ibHumd,
                    0.4,
                    0.8
                );
            }
            assert WET_INTERVAL.contains(ibHumd);

            return (float)WET_INTERVAL.mapInterval(
                ibHumd,
                0.8,
                1.0
            );
        }

        void register(RegistryEvent.Register<Biome> event){
            event.getRegistry().register(this);

            List<BiomeDictionary.Type> types = new ArrayList<>();

            types.add(BiomeDictionary.Type.OVERWORLD);

            types.addAll(
                temp.getTypes()
            );
            types.addAll(
                humd.getTypes()
            );
            types.addAll(
                Arrays.asList(landType.types)
            );
            types.addAll(
                Arrays.asList(ampType.types)
            );

            BiomeDictionary.addTypes(
                this,
                types.toArray(new BiomeDictionary.Type[0])
            );
        }
        //region init
        static Builder initBuilder(double temperature, double humidity, LandType landType){
            Biome.Builder builder = new Biome.Builder();

            setCategory(builder, temperature, humidity, landType);
            setPrecipitationType(builder, temperature, humidity);
            setDownFall(builder, humidity);
            setTemp(builder, temperature);
            setDummyDatas(builder);

            return builder;
        }

        static void setCategory(Biome.Builder builder, double temperature, double humidity, LandType landType){
            if (landType == LandType.RIVER){
                builder.category(Category.RIVER);
                return;
            }
            if (landType == LandType.BEACH){
                builder.category(Category.BEACH);
                return;
            }
            if (landType == LandType.OCEAN){
                builder.category(Category.OCEAN);
                return;
            }
            if (HOT_INTERVAL.contains(temperature) && SEMI_DRY_INTERVAL.contains(humidity)){
                builder.category(Category.SAVANNA);
                return;
            }
            if (HOT_INTERVAL.contains(temperature) && WET_INTERVAL.contains(humidity)){
                builder.category(Category.JUNGLE);
                return;
            }
            if (DRY_INTERVAL.contains(humidity)) {
                builder.category(Biome.Category.DESERT);
                return;
            }
            if (FREEZE_INTERVAL.contains(temperature)) {
                builder.category(Category.ICY);
                return;
            }
            if (COLD_INTERVAL.contains(temperature)){
                builder.category(Category.TAIGA);
                return;
            }

            builder.category(Biome.Category.FOREST);
        }

        static void setPrecipitationType(Biome.Builder builder, double temperature, double humidity) {
            if (DRY_INTERVAL.contains(humidity)) {
                builder.precipitation(Biome.RainType.NONE);
                return;
            }
            if (FREEZE_INTERVAL.contains(temperature)) {
                builder.precipitation(Biome.RainType.SNOW);
                return;
            }
            builder.precipitation(Biome.RainType.RAIN);

        }

        static void setDownFall(Biome.Builder builder, double humidity){
            builder.downfall(
                ibHumdToMCHumd(humidity)
            );
        }

        static void setTemp(Biome.Builder builder, double temperature){
            builder.temperature(
                ibTempToMCTemp(temperature)
            );
        }

        static void setDummyDatas(Biome.Builder builder){
            builder
                .waterColor(4159204)
                .waterFogColor(329011)
                .surfaceBuilder(
                    SurfaceBuilder.DEFAULT,
                    SurfaceBuilder.GRASS_DIRT_GRAVEL_CONFIG
                )
                .depth(1)
                .scale(1)
                .parent(null);
        }
        //endregion
    }

    //cant use enums as getFoliageColor doesn't use coordinates
    private static class TempType {
        static final int LENGTH = 20;

        static final List<TempType> VALUES =
            IntStream
                .range(0, LENGTH)
                .mapToObj(TempType::new)
                .collect(Collectors.toList());

        final int ordinal;

        private TempType(int ordinal) {
            this.ordinal = ordinal;
        }

        static TempType get(BlockPos2D pos){
            double temperature = PosDataHelper.getTemperature(pos, posData);

            return VALUES.get(
                Interval.PERCENT.mapToIntInterval(
                    temperature,
                    0,
                    LENGTH - 1
                )
            );
        }

        double getApproxTemperature(){
            return ordinal/ (double)(LENGTH - 1);
        }

        List<BiomeDictionary.Type> getTypes(){
            double temperature = getApproxTemperature();

            if (FREEZE_INTERVAL.contains(temperature)){
                return Lists.newArrayList(
                    BiomeDictionary.Type.SNOWY,
                    BiomeDictionary.Type.COLD
                );
            }
            if (COLD_INTERVAL.contains(temperature)){
                return Lists.newArrayList(
                    BiomeDictionary.Type.COLD
                );
            }
            if (WARM_INTERVAL.contains(temperature)){
                return Lists.newArrayList();
            }
            assert HOT_INTERVAL.contains(temperature);

            return Lists.newArrayList(
                BiomeDictionary.Type.HOT
            );
        }
    }

    private static class HumdType {
        static final int LENGTH = 20;

        static final List<HumdType> VALUES =
            IntStream
                .range(0, LENGTH)
                .mapToObj(HumdType::new)
                .collect(Collectors.toList());

        final int ordinal;

        private HumdType(int ordinal) {
            this.ordinal = ordinal;
        }

        static HumdType get(BlockPos2D pos){
            double humidity = PosDataHelper.getHumidity(pos, posData);

            return VALUES.get(
                Interval.PERCENT.mapToIntInterval(
                    humidity,
                    0,
                    LENGTH - 1
                )
            );
        }

        double getApproxHumidity(){
            return ordinal/ (double)(LENGTH - 1);
        }

        List<BiomeDictionary.Type> getTypes(){
            double temperature = getApproxHumidity();

            if (DRY_INTERVAL.contains(temperature)){
                return Lists.newArrayList(
                    BiomeDictionary.Type.DRY
                );
            }
            if (SEMI_DRY_INTERVAL.contains(temperature)){
                return Lists.newArrayList();
            }
            if (SEMI_WET_INTERVAL.contains(temperature)){
                return Lists.newArrayList();
            }
            assert WET_INTERVAL.contains(temperature);

            return Lists.newArrayList(
                BiomeDictionary.Type.WET
            );
        }
    }

    private enum LandType {
        NONE(),
        BEACH(BiomeDictionary.Type.BEACH),
        OCEAN(BiomeDictionary.Type.OCEAN),
        RIVER(BiomeDictionary.Type.RIVER),;

        final BiomeDictionary.Type[] types;

        LandType(BiomeDictionary.Type... types) {
            this.types = types;
        }

        static LandType get(BlockPos2D pos){
            LandmassInfo landmassInfo = posData.get(PosDataKeys.LANDMASS_TYPE, pos);

            if (landmassInfo.isBeach()){
                return BEACH;
            }
            if (posData.get(PosDataKeys.HEIGHT_MODIFIED_BY_RIVER, pos)){
                return RIVER;
            }
            if (landmassInfo.isOcean()){
                return OCEAN;
            }
            return NONE;
        }
    }

    private enum AmpType{
        NONE(),
        FLAT(BiomeDictionary.Type.PLAINS),
        HILLS(BiomeDictionary.Type.HILLS),
        MOUNTAIN(BiomeDictionary.Type.MOUNTAIN);

        final BiomeDictionary.Type[] types;

        AmpType(BiomeDictionary.Type... types) {
            this.types = types;
        }

        static AmpType getAmp(BlockPos2D pos){
            double amp = posData.get(PosDataKeys.AMP, pos);

            if (amp < 0) return NONE;

            if (amp < 15) return FLAT;

            if (amp < 80) return HILLS;

            return MOUNTAIN;
        }
    }

    private enum TreeType{
        NONE(),
        JUNGLE(BiomeDictionary.Type.JUNGLE),
        SPRUCE(BiomeDictionary.Type.COLD),
        DARK(BiomeDictionary.Type.SPOOKY),
        SAVANNAH(BiomeDictionary.Type.SAVANNA);

        final BiomeDictionary.Type[] types;

        TreeType(BiomeDictionary.Type... types) {
            this.types = types;
        }
    }

    private enum TreeDensity{
        NONE(),
        SPARSE(BiomeDictionary.Type.SPARSE),
        FOREST(BiomeDictionary.Type.FOREST),
        DENSE(BiomeDictionary.Type.DENSE, BiomeDictionary.Type.FOREST);

        final BiomeDictionary.Type[] types;

        TreeDensity(BiomeDictionary.Type... types) {
            this.types = types;
        }
    }
}

