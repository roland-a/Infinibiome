package weightedgpa.infinibiome.internal.display;

import net.minecraft.world.biome.Biomes;
import net.minecraft.world.biome.Biome;
import weightedgpa.infinibiome.api.posdata.LandmassInfo;
import weightedgpa.infinibiome.internal.floatfunc.util.Interval;
import weightedgpa.infinibiome.api.posdata.PosDataKeys;
import weightedgpa.infinibiome.api.posdata.PosDataHelper;
import weightedgpa.infinibiome.api.pos.BlockPos2D;

import java.awt.Color;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


public final class BiomeMap extends DataMapBase {
    private static final Map<Interval, Color> tempToColor = new HashMap<>();
    private static final Map<Interval, Color> humdToColor = new HashMap<>();
    private static final Map<Biome, Color> biomeToColor = new HashMap<>();

    Map<Class<LandmassInfo>, Integer> counter = new ConcurrentHashMap<>();

    public BiomeMap(){
        this(
            10,
            "2"
        );
    }

    @Override
    protected void onFinished() {
        int total = 0;

        for (Class<LandmassInfo> c: counter.keySet()){
            total += counter.get(c);
        }

        for (Class<LandmassInfo> c: counter.keySet()){
            System.out.println(c + " " + (double)counter.get(c) / total);
        }
    }

    private BiomeMap(int scale, String seedBranch){
        super(scale, seedBranch);

        setSize(800, 800);
    }

    static {
        initTempMap();
        initHumdMap();
        initBiomeMap();
    }

    private static void initTempMap(){
        tempToColor.put(
            PosDataHelper.FREEZE_INTERVAL,
            new Color(255,255,255)
        );

        tempToColor.put(
            PosDataHelper.COLD_INTERVAL,
            new Color(70, 100, 100)
        );

        tempToColor.put(
            PosDataHelper.WARM_INTERVAL,
            new Color(0, 150, 0)
        );

        tempToColor.put(
            PosDataHelper.HOT_INTERVAL,
            new Color(0, 70, 0)
        );
    }

    private static void initHumdMap(){
        humdToColor.put(
            PosDataHelper.DRY_INTERVAL,
            new Color(250, 200, 0)
        );

        humdToColor.put(
            PosDataHelper.SEMI_DRY_INTERVAL,
            new Color(150, 200, 0)
        );

        humdToColor.put(
            PosDataHelper.SEMI_WET_INTERVAL,
            new Color(100, 255, 0)
        );

        humdToColor.put(
            PosDataHelper.WET_INTERVAL,
            new Color(0,70,0)
        );

    }

    private static void initBiomeMap(){
        biomeToColor.put(
            Biomes.MUSHROOM_FIELDS, new Color(120, 0, 120)
        );

        biomeToColor.put(
            Biomes.MUSHROOM_FIELD_SHORE, new Color(50, 0, 100)
        );

        biomeToColor.put(
            Biomes.RIVER, new Color(0, 0, 255)
        );

        biomeToColor.put(
            Biomes.FROZEN_RIVER, new Color(0, 255, 255)
        );

        biomeToColor.put(
            Biomes.BEACH, new Color(255, 255, 0)
        );

        biomeToColor.put(
            Biomes.SNOWY_BEACH, new Color(255, 255, 0)
        );

        biomeToColor.put(
            Biomes.OCEAN, new Color(0, 0, 255)
        );

        biomeToColor.put(
            Biomes.DEEP_OCEAN, new Color(0, 0, 100)
        );
    }

    @Override
    protected Color getColor(int posX, int posZ, int screenPixelX, int screenPixelZ) {
        if (posX == 0 && posZ == 0) return Color.RED;

        if (posX % 1000 == 0 || posZ % 1000 == 0){
            return Color.BLACK;
        }


        LandmassInfo landmassInfo = posData.get(PosDataKeys.LANDMASS_TYPE, new BlockPos2D(posX, posZ));

        counter.put(
            (Class<LandmassInfo>)landmassInfo.getClass(),
            counter.getOrDefault(landmassInfo.getClass(), 0) + 1
        );

        if (landmassInfo.isBeach()){
            return Color.YELLOW;
        }
        if (landmassInfo.isLand()){
            return new Color(
                0f,
                (float) (1 - landmassInfo.getTransitionToBeach()),
                0f
            );
        }
        return new Color(
            0f,
            0f,
            (float) (1 - landmassInfo.getTransitionToBeach())
        );

        /*
        Biome biome = IBBiomes.getBiome(new BlockPos2D(posX, posZ), data);

        if (biomeToColor.containsKey(biome)){
            return biomeToColor.get(biome);
        }

        if (data.get(PosDataKeys.IS_RIVER_WATER, new BlockPos2D(posX, posZ))){
            return Color.BLUE;
        }

        {
            float temperature = data.get(PosDataKeys.TEMPERATURE, new BlockPos2D(posX, posZ)).fromHeight(63);

            return getFromClimateValue(temperature, tempToColor);
        }


        /*
        {
            float humidity = blockPosData.get(DefaultPosDataKeys.HUMIDITY, BlockPos2D.init(posX, posZ))
                .fromHeight(63);

            return getFromClimateValue(humidity, humdToColor);
        }

         */
    }

    Color getFromClimateValue(double climateValue, Map<Interval, Color> map){
        for (Map.Entry<Interval, Color> e: map.entrySet()){
            if (e.getKey().contains(climateValue)){
                return e.getValue();
            }
        }
        return Color.BLACK;
    }

}