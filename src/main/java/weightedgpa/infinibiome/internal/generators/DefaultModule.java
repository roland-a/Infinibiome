package weightedgpa.infinibiome.internal.generators;

import net.minecraft.world.biome.provider.BiomeProvider;
import weightedgpa.infinibiome.api.dependency.DependencyInjector;
import weightedgpa.infinibiome.api.dependency.DependencyModule;
import weightedgpa.infinibiome.api.generators.*;
import weightedgpa.infinibiome.api.posdata.*;
import weightedgpa.infinibiome.internal.generators.chunks.*;
import weightedgpa.infinibiome.internal.generators.chunks.surface.*;
import weightedgpa.infinibiome.internal.generators.interchunks.*;
import weightedgpa.infinibiome.internal.generators.interchunks.mob.*;
import weightedgpa.infinibiome.internal.generators.interchunks.ore.*;
import weightedgpa.infinibiome.internal.generators.interchunks.plant.*;
import weightedgpa.infinibiome.internal.generators.interchunks.struct.*;
import weightedgpa.infinibiome.internal.generators.interchunks.struct.CoralReefGen;
import weightedgpa.infinibiome.internal.generators.interchunks.tree.*;
import weightedgpa.infinibiome.internal.generators.nonworldgen.*;
import weightedgpa.infinibiome.internal.generators.nonworldgen.controllers.GroundBoneMealControllers;
import weightedgpa.infinibiome.internal.generators.nonworldgen.controllers.PlantGrowthControllers;
import weightedgpa.infinibiome.internal.generators.nonworldgen.controllers.SaplingControllers;
import weightedgpa.infinibiome.internal.generators.nonworldgen.spawnList.*;
import weightedgpa.infinibiome.internal.generators.nonworldgen.spawners.*;
import weightedgpa.infinibiome.internal.generators.posdata.*;
import weightedgpa.infinibiome.internal.minecraftImpl.IBBiomeProvider;
import weightedgpa.infinibiome.internal.minecraftImpl.IBBiomes;
import weightedgpa.infinibiome.internal.minecraftImpl.commands.LocateClimateCommand;
import weightedgpa.infinibiome.internal.minecraftImpl.commands.LocateObjectCommand;
import weightedgpa.infinibiome.internal.generators.posdata.PosDataProviderBase;

import static weightedgpa.infinibiome.api.generators.DefaultGenerators.*;


public final class DefaultModule implements DependencyModule {
    public static final DependencyModule INSTANCE = new DefaultModule();

    private DefaultModule(){}

    @Override
    public void addToPre(DependencyInjector.Pre table) {
        table.addItem(ConfigSorter.class, ConfigSorter::new);

        addChunks(table);

        addInterChunks(table);

        addPosDatas(table);

        addNonWorldGens(table);


        //terrainDebugFastMode(table);
    }

    //region chunk
    private void addChunks(DependencyInjector.Pre t){
        addSurfaces(t);

        t.addItem(ChunkGens.class, ChunkGens::new);
        t.addItem(BASE_TERRAIN, BaseTerrainGen::new);
        t.addItem(BEDROCK, BedrockGen::new);
        t.addItem(CAVE, CaveGen::new);
        t.addItem(CAVE_UNDERWATER, CaveUnderwaterGen::new);
        t.addItem(RAVINE, RavineGen::new);
        t.addItem(RAVINE_UNDERWATER, RavineUnderwaterGen::new);
    }

    private void addSurfaces(DependencyInjector.Pre t){
        t.addItem(SurfaceGens.class, SurfaceGens::new);

        t.addItems(COARSE_DIRT_SURFACE, CoarseDirtSurfaceGen.Type.values(), CoarseDirtSurfaceGen::new);
        t.addItem(GRAVEL_SURFACE, GravelSurfaceGen::new);
        t.addItem(GRASS_BLOCK_SURFACE, GrassSurfaceGen::new);
        t.addItem(MYCELIUM_SURFACE, MyceliumSurfaceGen::new);
        t.addItems(PODZOL_SURFACE, PodzolSurfaceGen.Type.values(), PodzolSurfaceGen::new);
        t.addItems(SAND_SURFACE, SandSurfaceGen.Type.values(), SandSurfaceGen::new);
        t.addItem(SNOW_BLOCK_SURFACE, SnowBlockSurfaceGen::new);
        t.addItem(STONE_SURFACE, StoneSurfaceGen::new);
    }
    //endregion

    //region interchunks
    private void addInterChunks(DependencyInjector.Pre t){
        addMobs(t);
        addOres(t);
        addPlants(t);
        addStructs(t);
        addTrees(t);

        t.addItem(InterChunkGens.class, InterChunkGens::new);
        t.addItem(BEE_HIVE, BeehiveGen::new);
        t.addItem(SNOW_PLACER, SnowGen::new);
        t.addItem(SURFACE_POOL_WATER, SurfacePoolWaterGen::new);
        t.addItem(SURFACE_POOL_LAVA, SurfacePoolLavaGen::new);
        t.addItems(LIQUID_ORE, LiquidOre.Type.values(), LiquidOre::new);
        t.addItem(WATER_DOT, WaterDot::new);
        t.addItems(UNDERGROUND_POOL, UndergroundPoolGen.Type.values(), UndergroundPoolGen::new);
    }

    private void addMobs(DependencyInjector.Pre t){
        t.addItem(CHICKEN, ChickenGen::new);
        t.addItem(COW, CowGen::new);
        t.addItem(DONKEY, DonkeyGen::new);
        t.addItem(FOX, FoxGen::new);
        t.addItem(HORSE, HorseGen::new);
        t.addItem(LLAMA, LlamaGen::new);
        t.addItem(MOOSHROOM, MooshroomGen::new);
        t.addItem(OCELOT, OcelotGen::new);
        t.addItem(PARROT, ParrotGen::new);
        t.addItem(PANDA, PandaGen::new);
        t.addItem(PIG, PigGen::new);
        t.addItem(POLAR_BEAR, PolarBearGen::new);
        t.addItem(RABBIT, RabbitGen::new);
        t.addItem(SHEEP, SheepGen::new);
        t.addItem(TURTLE, TurtleGen::new);
        t.addItem(WOLF, WolfGen::new);
    }

    private void addOres(DependencyInjector.Pre t){
        t.addItem(ANDESITE_ORE, AndesiteOreGen::new);
        t.addItem(AndesiteOreGen.Config.class, AndesiteOreGen.Config::new);
        t.addItem(CLAY_ORE, ClayOreGen::new);
        t.addItem(ClayOreGen.Config.class, ClayOreGen.Config::new);
        t.addItem(COAL_ORE, CoalOreGen::new);
        t.addItem(CoalOreGen.Config.class, CoalOreGen.Config::new);
        t.addItem(DIAMOND_ORE, DiamondOreGen::new);
        t.addItem(DiamondOreGen.Config.class, DiamondOreGen.Config::new);
        t.addItem(DIORITE_ORE, DioriteOreGen::new);
        t.addItem(DioriteOreGen.Config.class, DioriteOreGen.Config::new);
        t.addItem(DIRT_ORE, DirtOreGen::new);
        t.addItem(DirtOreGen.Config.class, DirtOreGen.Config::new);
        t.addItem(EMERALD_ORE, EmeraldOreGen::new);
        t.addItem(EmeraldOreGen.Config.class, EmeraldOreGen.Config::new);
        t.addItem(GOLD_ORE, GoldOreGen::new);
        t.addItem(GoldOreGen.Config.class, GoldOreGen.Config::new);
        t.addItem(GRANITE_ORE, GraniteOreGen::new);
        t.addItem(GraniteOreGen.Config.class, GraniteOreGen.Config::new);
        t.addItem(GRAVEL_ORE, GravelOreGen::new);
        t.addItem(GravelOreGen.Config.class, GravelOreGen.Config::new);
        t.addItem(IRON_ORE, IronOreGen::new);
        t.addItem(IronOreGen.Config.class, IronOreGen.Config::new);
        t.addItem(LAPIS_ORE, LapisOreGen::new);
        t.addItem(LapisOreGen.Config.class, LapisOreGen.Config::new);
        t.addItem(REDSTONE_ORE, RedstoneOreGen::new);
        t.addItem(RedstoneOreGen.Config.class, RedstoneOreGen.Config::new);
    }

    private void addPlants(DependencyInjector.Pre t){
        t.addItem(PlantGrowthConfig.class, PlantGrowthConfig::new);

        t.addItem(ALLIUM, AlliumGen::new);
        t.addItem(AZURE_BLUET, AzureBluetGen::new);
        t.addItem(BAMBOO, BambooGen::new);
        t.addItem(CORAL_REEF, CoralReefGen::new);
        //t.addItem(BeetRoot.class, BeetRoot::new);
        t.addItem(BERRY_BUSH, BerryBushGen::new);
        t.addItem(BLUE_ORCHID, BlueOrchidGen::new);
        //t.addItem(Carrot.class, Carrot::new);
        t.addItem(CORN_FLOWER, CornFlowerGen::new);
        t.addItem(CACTUS, CactusGen::new);
        t.addItem(DANDELION, DandelionGen::new);
        t.addItem(DEAD_BUSH, DeadBushGen::new);
        t.addItems(FERN, GrassType.values(), FernGen::new);
        t.addItems(GRASS, GrassType.values(), GrassGen::new);
        t.addItem(KELPE, KelpeGen::new);
        t.addItem(LILAC, LilacGen::new);
        t.addItem(LILY, LilyGen::new);
        t.addItem(MELON, MelonGen::new);
        t.addItems(MUSHROOM_SMALL, MushroomSmallGen.Type.values(), MushroomSmallGen::new);
        t.addItem(OXEYE_DAISY, OxeyeDaisyGen::new);
        t.addItem(PEONY, PeonyGen::new);
        t.addItem(POPPY, PoppyGen::new);
        //t.addItem(Potato.class, Potato::new);
        t.addItem(PUMPKIN, PumpkinGen::new);
        t.addItem(ROSE_BUSH, RoseBushGen::new);
        t.addItems(SEAGRASS, GrassType.values(), SeaGrassGen::new);
        t.addItem(SUGARCANE, SugarcaneGen::new);
        t.addItem(SUNFLOWER, SunflowerGen::new);
        t.addItems(TULIP, TulipGen.Type.values(), TulipGen::new);
        t.addItem(VALLEY_LILY, ValleyLilyGen::new);
        t.addItems(VINE, VineGen.Type.values(), VineGen::new);
        //t.addItem(Wheat.class, Wheat::new);
    }

    private void addStructs(DependencyInjector.Pre t){
        t.addItem(StructGens.class, StructGens::new);
        t.addItem(BURIED_TREASURE, BuriedTreasureGen::new);
        t.addItem(BuriedTreasureGen.Config.class, BuriedTreasureGen.Config::new);
        t.addItem(DESERT_PYRAMID, DesertPyramidGen::new);
        t.addItem(DesertPyramidGen.Config.class, DesertPyramidGen.Config::new);
        t.addItem(DESERT_WELL, DesertWellGen::new);
        t.addItem(DesertWellGen.Config.class, DesertWellGen.Config::new);
        t.addItem(DUNGEON, DungeonGen::new);
        t.addItem(DungeonGen.Config.class, DungeonGen.Config::new);
        t.addItem(FOSSIL, FossilGen::new);
        t.addItems(ICEBURG, IceburgGen.Type.values(), IceburgGen::new);
        t.addItem(IGLOO, IglooGen::new);
        t.addItem(IglooGen.Config.class, IglooGen.Config::new);
        t.addItem(JUNGLE_PYRAMID, JunglePyramidGen::new);
        t.addItem(JunglePyramidGen.Config.class, JunglePyramidGen.Config::new);
        t.addItem(ILLAGER_MANSION, IllagerMansionGen::new);
        t.addItem(IllagerMansionGen.Config.class, IllagerMansionGen.Config::new);
        t.addItem(MINESHAFT, MineshaftGen::new);
        t.addItem(MineshaftGen.Config.class, MineshaftGen.Config::new);
        t.addItems(MUSHROOM_BIG, MushroomBigGen.Type.values(), MushroomBigGen::new);
        t.addItem(OCEAN_MONUMENT, OceanMonumentGen::new);
        t.addItem(OceanMonumentGen.Config.class, OceanMonumentGen.Config::new);
        t.addItem(ILLAGER_OUTPOST, IllagerOutpostGen::new);
        t.addItem(IllagerOutpostGen.Config.class, IllagerOutpostGen.Config::new);
        t.addItem(OCEAN_RUINS, OceanRuinGen::new);
        t.addItem(OceanRuinGen.Config.class, OceanRuinGen.Config::new);
        t.addItem(SHIP_WRECK, ShipWreckGen::new);
        t.addItem(ShipWreckGen.Config.class, ShipWreckGen.Config::new);
        t.addItem(STRONGHOLD, StrongholdGen::new);
        t.addItem(StrongholdGen.Config.class, StrongholdGen.Config::new);
        t.addItem(VILLAGE, VillageGen::new);
        t.addItem(VillageGen.Config.class, VillageGen.Config::new);
        t.addItem(WITCH_HUT, WitchHutGen::new);
        t.addItem(WitchHutGen.Config.class, WitchHutGen.Config::new);
    }

    private void addTrees(DependencyInjector.Pre t){
        t.addItem(TreeGens.class, TreeGens::new);
        t.addItem(ACACIA, AcaciaGen::new);
        t.addItem(BIRCH, BirchGen::new);
        t.addItem(DARK_OAK, DarkOakGen::new);
        t.addItem(ICE_SPIKES, IceSpikeGen::new);
        t.addItem(JUNGLE_BIG, JungleBigGen::new);
        t.addItem(JUNGLE_SMALL, JungleSmallGen::new);
        t.addItem(OAK_BIG, OakBigGen::new);
        t.addItem(OAK_SMALL, OakSmallGen::new);
        t.addItem(SPRUCE_BIG, SpruceBigGen::new);
        t.addItems(SPRUCE_SMALL, SpruceSmallGen.Type.values(), SpruceSmallGen::new);
    }
    //endregion

    private void addPosDatas(DependencyInjector.Pre t){
        t.addItem(PosDataProvider.class, PosDataProviderBase::new);
        t.addItem(CLIMATE, ClimateGen::new);
        t.addItem(ClimateConfig.class, ClimateConfig::new);
        t.addItem(HEIGHT, HeightGen::new);
        t.addItem(LAKE, LakeGen::new);
        t.addItem(LANDMASS, LandmassGen::new);
        t.addItem(LandmassGen.Config.class, LandmassGen.Config::new);
        t.addItem(MUSHROOM_ISLAND, MushroomIslandGen::new);
        t.addItem(MushroomIslandGen.Config.class, MushroomIslandGen.Config::new);
        t.addItem(RIVER, RiverGen::new);
    }

    //region nonworld
    private void addNonWorldGens(DependencyInjector.Pre t){
        addSpawners(t);
        addSpawnList(t);
        addRefreshers(t);

        t.addItem(BiomeProvider.class, IBBiomeProvider::new);
    }

    private void addSpawners(DependencyInjector.Pre t){
        t.addItem(MobSpawners.class, MobSpawners::new);
        t.addItem(PATROL_SPAWNER, PatrolSpawner::new);
        t.addItem(PHANTOM_SPAWNER, PhantomSpawner::new);
        t.addItem(RAID_SPAWNER, RaidSpawner::new);
        t.addItem(WANDERER_SPAWNER, WandererSpawner::new);
        t.addItem(CAT_SPAWNER, CatSpawners::new);
    }

    private void addSpawnList(DependencyInjector.Pre t){
        t.addItem(MobSpawnListModifiers.class, MobSpawnListModifiers::new);
        t.addItem(AmbientSpawnList.class, AmbientSpawnList::new);
        t.addItem(MONSTER_SPAWN_LIST, MonsterSpawnList::new);
        t.addItem(FISH_SPAWN_LIST, OceanSpawnList::new);
        t.addItem(RIVER_LAKE_SPAWNS, RiverLakeSpawnList::new);
    }

    private void addRefreshers(DependencyInjector.Pre t){
        t.refresh(IBBiomes::refresh);
        t.refresh(LocateClimateCommand::refresh);
        t.refresh(LocateObjectCommand::refresh);
        t.refresh(GroundBoneMealControllers::refresh);
        t.refresh(PlantGrowthControllers::refresh);
        t.refresh(SaplingControllers::refresh);
        t.refresh(ConfigIOImpl::saveConfig);
    }
    //endregion

    private void terrainDebugFastMode(DependencyInjector.Pre t){
        //blacklist everything but these items
        t.blacklist(
            o -> {
                if (o instanceof LandmassGen) return false;

                if (o instanceof PosDataFiller) return false;

                if (o instanceof HeightGen) return false;

                if (o instanceof BaseTerrainGen) return false;

                if (o instanceof SurfaceGens) return false;

                if (o instanceof GrassSurfaceGen) return false;

                return true;
            }
        );

        t.addItem(
            PosDataGen.class,
            PosDataFiller::new
        );
    }

    private class PosDataFiller implements PosDataGen {
        PosDataFiller(DependencyInjector di) {
        }

        @Override
        public Timing getTiming() {
            return PosDataTimings.CLIMATE;
        }

        @Override
        public void generateData(PosDataTable data) {
            /*
            data.set(
                PosDataKeys.LANDMASS_TYPE,
                new LandmassInfo.Land(0)
            );

             */
            data.set(
                PosDataKeys.TEMPERATURE,
                new ClimateValue(
                    PosDataHelper.FREEZE_INTERVAL.getMin(),
                    0
                )
            );
            data.set(
                PosDataKeys.HUMIDITY,
                new ClimateValue(
                    PosDataHelper.SEMI_DRY_INTERVAL.getMax(),
                    0
                )
            );
        }
    }
}
