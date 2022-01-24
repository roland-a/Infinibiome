package weightedgpa.infinibiome.api.generators;

import weightedgpa.infinibiome.internal.generators.chunks.*;
import weightedgpa.infinibiome.internal.generators.interchunks.*;
import weightedgpa.infinibiome.internal.generators.interchunks.struct.CoralReefGen;
import weightedgpa.infinibiome.internal.generators.interchunks.mob.*;
import weightedgpa.infinibiome.internal.generators.interchunks.ore.*;
import weightedgpa.infinibiome.internal.generators.interchunks.plant.*;
import weightedgpa.infinibiome.internal.generators.interchunks.plant.GrassGen;
import weightedgpa.infinibiome.internal.generators.nonworldgen.spawnList.OceanSpawnList;
import weightedgpa.infinibiome.internal.generators.nonworldgen.spawnList.MonsterSpawnList;
import weightedgpa.infinibiome.internal.generators.nonworldgen.spawnList.RiverLakeSpawnList;
import weightedgpa.infinibiome.internal.generators.nonworldgen.spawners.*;
import weightedgpa.infinibiome.internal.generators.posdata.*;
import weightedgpa.infinibiome.internal.generators.posdata.LandmassGen;
//import weightedgpa.infinibiome.internal.startup.struct.*;
import weightedgpa.infinibiome.internal.generators.interchunks.struct.*;
import weightedgpa.infinibiome.internal.generators.chunks.surface.*;
import weightedgpa.infinibiome.internal.generators.interchunks.tree.*;


/**
 * Allows for other mods to blacklist any default generators
 */
public final class DefaultGenerators {
    private DefaultGenerators(){}

    //surface
    public static final Class<CoarseDirtSurfaceGen> COARSE_DIRT_SURFACE = CoarseDirtSurfaceGen.class;
    public static final Class<GrassSurfaceGen> GRASS_BLOCK_SURFACE = GrassSurfaceGen.class;
    public static final Class<GravelSurfaceGen> GRAVEL_SURFACE = GravelSurfaceGen.class;
    public static final Class<MyceliumSurfaceGen> MYCELIUM_SURFACE = MyceliumSurfaceGen.class;
    public static final Class<PodzolSurfaceGen> PODZOL_SURFACE = PodzolSurfaceGen.class;
    public static final Class<SandSurfaceGen> SAND_SURFACE = SandSurfaceGen.class;
    public static final Class<SnowBlockSurfaceGen> SNOW_BLOCK_SURFACE = SnowBlockSurfaceGen.class;
    public static final Class<StoneSurfaceGen> STONE_SURFACE = StoneSurfaceGen.class;

    //misc chunks
    public static final Class<BaseTerrainGen> BASE_TERRAIN = BaseTerrainGen.class;
    public static final Class<BedrockGen> BEDROCK = BedrockGen.class;
    public static final Class<CaveGen> CAVE = CaveGen.class;
    public static final Class<CaveUnderwaterGen> CAVE_UNDERWATER = CaveUnderwaterGen.class;
    public static final Class<RavineGen> RAVINE = RavineGen.class;
    public static final Class<RavineUnderwaterGen> RAVINE_UNDERWATER = RavineUnderwaterGen.class;

    //mobs
    public static final Class<ChickenGen> CHICKEN = ChickenGen.class;
    public static final Class<CowGen> COW = CowGen.class;
    public static final Class<DonkeyGen> DONKEY = DonkeyGen.class;
    public static final Class<FoxGen> FOX = FoxGen.class;
    public static final Class<HorseGen> HORSE = HorseGen.class;
    public static final Class<LlamaGen> LLAMA = LlamaGen.class;
    public static final Class<MooshroomGen> MOOSHROOM = MooshroomGen.class;
    public static final Class<OcelotGen> OCELOT = OcelotGen.class;
    public static final Class<ParrotGen> PARROT = ParrotGen.class;
    public static final Class<PandaGen> PANDA = PandaGen.class;
    public static final Class<PigGen> PIG = PigGen.class;
    public static final Class<PolarBearGen> POLAR_BEAR = PolarBearGen.class;
    public static final Class<RabbitGen> RABBIT = RabbitGen.class;
    public static final Class<SheepGen> SHEEP = SheepGen.class;
    public static final Class<TurtleGen> TURTLE = TurtleGen.class;
    public static final Class<WolfGen> WOLF = WolfGen.class;

    //ores
    public static final Class<AndesiteOreGen> ANDESITE_ORE = AndesiteOreGen.class;
    public static final Class<ClayOreGen> CLAY_ORE = ClayOreGen.class;
    public static final Class<CoalOreGen> COAL_ORE = CoalOreGen.class;
    public static final Class<DiamondOreGen> DIAMOND_ORE = DiamondOreGen.class;
    public static final Class<DioriteOreGen> DIORITE_ORE = DioriteOreGen.class;
    public static final Class<DirtOreGen> DIRT_ORE = DirtOreGen.class;
    public static final Class<EmeraldOreGen> EMERALD_ORE = EmeraldOreGen.class;
    public static final Class<GoldOreGen> GOLD_ORE = GoldOreGen.class;
    public static final Class<GraniteOreGen> GRANITE_ORE = GraniteOreGen.class;
    public static final Class<GravelOreGen> GRAVEL_ORE = GravelOreGen.class;
    public static final Class<IronOreGen> IRON_ORE = IronOreGen.class;
    public static final Class<LapisOreGen> LAPIS_ORE = LapisOreGen.class;
    public static final Class<RedstoneOreGen> REDSTONE_ORE = RedstoneOreGen.class;

    //plants
    public static final Class<AlliumGen> ALLIUM = AlliumGen.class;
    public static final Class<AzureBluetGen> AZURE_BLUET = AzureBluetGen.class;
    public static final Class<BambooGen> BAMBOO = BambooGen.class;
    public static final Class<BerryBushGen> BERRY_BUSH = BerryBushGen.class;
    public static final Class<BlueOrchidGen> BLUE_ORCHID = BlueOrchidGen.class;
    public static final Class<CactusGen> CACTUS = CactusGen.class;
    public static final Class<CornFlowerGen> CORN_FLOWER = CornFlowerGen.class;
    public static final Class<DandelionGen> DANDELION = DandelionGen.class;
    public static final Class<DeadBushGen> DEAD_BUSH = DeadBushGen.class;
    public static final Class<FernGen> FERN = FernGen.class;
    public static final Class<GrassGen> GRASS = GrassGen.class;
    public static final Class<KelpeGen> KELPE = KelpeGen.class;
    public static final Class<LilacGen> LILAC = LilacGen.class;
    public static final Class<LilyGen> LILY = LilyGen.class;
    public static final Class<MelonGen> MELON = MelonGen.class;
    public static final Class<MushroomSmallGen> MUSHROOM_SMALL = MushroomSmallGen.class;

    //public static final Class<MushroomSmallUndergroundGen> MUSHROOM_SMALL_UNDERGROUND = MushroomSmallUndergroundGen.class;
    public static final Class<OxeyeDaisyGen> OXEYE_DAISY = OxeyeDaisyGen.class;
    public static final Class<PeonyGen> PEONY = PeonyGen.class;
    public static final Class<PoppyGen> POPPY = PoppyGen.class;
    public static final Class<PumpkinGen> PUMPKIN = PumpkinGen.class;
    public static final Class<RoseBushGen> ROSE_BUSH = RoseBushGen.class;
    public static final Class<SeaGrassGen> SEAGRASS = SeaGrassGen.class;
    public static final Class<SugarcaneGen> SUGARCANE = SugarcaneGen.class;
    public static final Class<SunflowerGen> SUNFLOWER = SunflowerGen.class;
    public static final Class<TulipGen> TULIP = TulipGen.class;
    public static final Class<ValleyLilyGen> VALLEY_LILY = ValleyLilyGen.class;
    public static final Class<VineGen> VINE = VineGen.class;

    //structures
    public static final Class<BuriedTreasureGen> BURIED_TREASURE = BuriedTreasureGen.class;
    public static final Class<CoralReefGen> CORAL_REEF = CoralReefGen.class;
    public static final Class<DesertPyramidGen> DESERT_PYRAMID = DesertPyramidGen.class;
    public static final Class<DesertWellGen> DESERT_WELL = DesertWellGen.class;
    public static final Class<DungeonGen> DUNGEON = DungeonGen.class;
    public static final Class<FossilGen> FOSSIL = FossilGen.class;
    public static final Class<IceburgGen> ICEBURG = IceburgGen.class;
    public static final Class<IglooGen> IGLOO = IglooGen.class;
    public static final Class<JunglePyramidGen> JUNGLE_PYRAMID = JunglePyramidGen.class;
    public static final Class<IllagerMansionGen> ILLAGER_MANSION = IllagerMansionGen.class;
    public static final Class<MineshaftGen> MINESHAFT = MineshaftGen.class;
    public static final Class<MushroomBigGen> MUSHROOM_BIG = MushroomBigGen.class;
    public static final Class<OceanMonumentGen> OCEAN_MONUMENT = OceanMonumentGen.class;
    public static final Class<IllagerOutpostGen> ILLAGER_OUTPOST = IllagerOutpostGen.class;
    public static final Class<OceanRuinGen> OCEAN_RUINS = OceanRuinGen.class;
    public static final Class<ShipWreckGen> SHIP_WRECK = ShipWreckGen.class;
    public static final Class<StrongholdGen> STRONGHOLD = StrongholdGen.class;
    public static final Class<VillageGen> VILLAGE = VillageGen.class;
    public static final Class<WitchHutGen> WITCH_HUT = WitchHutGen.class;

    //posData
    public static final Class<LandmassGen> LANDMASS = LandmassGen.class;
    public static final Class<HeightGen> HEIGHT = HeightGen.class;
    public static final Class<ClimateGen> CLIMATE = ClimateGen.class;
    public static final Class<LakeGen> LAKE = LakeGen.class;
    public static final Class<MushroomIslandGen> MUSHROOM_ISLAND = MushroomIslandGen.class;
    public static final Class<RiverGen> RIVER = RiverGen.class;

    //trees
    public static final Class<AcaciaGen> ACACIA = AcaciaGen.class;
    public static final Class<BirchGen> BIRCH = BirchGen.class;
    public static final Class<DarkOakGen> DARK_OAK = DarkOakGen.class;
    public static final Class<IceSpikeGen> ICE_SPIKES = IceSpikeGen.class;
    public static final Class<JungleBigGen> JUNGLE_BIG = JungleBigGen.class;
    public static final Class<JungleSmallGen> JUNGLE_SMALL = JungleSmallGen.class;
    public static final Class<OakBigGen> OAK_BIG = OakBigGen.class;
    public static final Class<OakSmallGen> OAK_SMALL = OakSmallGen.class;
    public static final Class<SpruceBigGen> SPRUCE_BIG = SpruceBigGen.class;
    public static final Class<SpruceSmallGen> SPRUCE_SMALL = SpruceSmallGen.class;

    //misc interChunkGens
    public static final Class<BeehiveGen> BEE_HIVE = BeehiveGen.class;
    public static final Class<SnowGen> SNOW_PLACER = SnowGen.class;
    public static final Class<SurfacePoolLavaGen> SURFACE_POOL_LAVA = SurfacePoolLavaGen.class;
    public static final Class<SurfacePoolWaterGen> SURFACE_POOL_WATER = SurfacePoolWaterGen.class;
    public static final Class<LiquidOre> LIQUID_ORE = LiquidOre.class;
    public static final Class<UndergroundPoolGen> UNDERGROUND_POOL = UndergroundPoolGen.class;
    public static final Class<WaterDot> WATER_DOT = WaterDot.class;

    //misc
    public static final Class<CatSpawners> CAT_SPAWNER = CatSpawners.class;
    public static final Class<MonsterSpawnList> MONSTER_SPAWN_LIST = MonsterSpawnList.class;
    public static final Class<OceanSpawnList> FISH_SPAWN_LIST = OceanSpawnList.class;
    public static final Class<PatrolSpawner> PATROL_SPAWNER = PatrolSpawner.class;
    public static final Class<PhantomSpawner> PHANTOM_SPAWNER = PhantomSpawner.class;
    public static final Class<RaidSpawner> RAID_SPAWNER = RaidSpawner.class;
    public static final Class<RiverLakeSpawnList> RIVER_LAKE_SPAWNS = RiverLakeSpawnList.class;
    public static final Class<WandererSpawner> WANDERER_SPAWNER = WandererSpawner.class;
}
