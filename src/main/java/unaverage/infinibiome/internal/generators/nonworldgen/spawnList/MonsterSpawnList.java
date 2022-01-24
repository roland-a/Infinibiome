package weightedgpa.infinibiome.internal.generators.nonworldgen.spawnList;

import net.minecraft.entity.EntityClassification;
import net.minecraft.entity.EntityType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorld;
import net.minecraft.world.biome.Biome;
import weightedgpa.infinibiome.api.generators.nonworldgen.MobSpawnListModifier;
import weightedgpa.infinibiome.api.dependency.DependencyInjector;
import weightedgpa.infinibiome.api.pos.BlockPos2D;
import weightedgpa.infinibiome.internal.misc.MCHelper;
import weightedgpa.infinibiome.api.posdata.PosDataHelper;
import weightedgpa.infinibiome.api.posdata.PosDataKeys;
import weightedgpa.infinibiome.api.posdata.PosDataProvider;

import java.util.List;

public final class MonsterSpawnList implements MobSpawnListModifier {
    private final PosDataProvider posData;

    public MonsterSpawnList(DependencyInjector di) {
        this.posData = di.get(PosDataProvider.class);
    }

    @Override
    public void modifyList(BlockPos pos, EntityClassification creatureType, List<Biome.SpawnListEntry> spawnListEntries, IWorld world) {
        if (creatureType != EntityClassification.MONSTER) return;

        BlockPos2D pos2D = MCHelper.to2D(pos);

        if (posData.get(PosDataKeys.IS_MUSHROOM_ISLAND, pos2D)) return;

        double temperature = PosDataHelper.getTemperature(pos, posData);
        double humidity = PosDataHelper.getHumidity(pos, posData);

        spawnListEntries.add(new Biome.SpawnListEntry(getZombieType(humidity), 100, 5, 5));
        spawnListEntries.add(new Biome.SpawnListEntry(EntityType.SPIDER, 100, 5, 5));
        spawnListEntries.add(new Biome.SpawnListEntry(getSkeletonType(temperature), 100, 1, 1));
        spawnListEntries.add(new Biome.SpawnListEntry(EntityType.ZOMBIE_VILLAGER, 5, 1, 1));
        spawnListEntries.add(new Biome.SpawnListEntry(EntityType.CREEPER, 100, 1, 1));
        spawnListEntries.add(new Biome.SpawnListEntry(EntityType.ENDERMAN, 10, 1, 1));

        boolean isWet = PosDataHelper.WET_INTERVAL.contains(humidity);
        boolean isOcean = posData.get(PosDataKeys.LANDMASS_TYPE, pos2D).isOcean();
        boolean isLakeOrRiver = PosDataHelper.isUnderwaterPortionOfLakeOrRiver(pos2D, posData);

        if (isWet){
            spawnListEntries.add(new Biome.SpawnListEntry(EntityType.SLIME, 100, 1, 1));
            spawnListEntries.add(new Biome.SpawnListEntry(EntityType.WITCH, 5, 1, 1));
        }
        if (isOcean){
            spawnListEntries.add(new Biome.SpawnListEntry(EntityType.GUARDIAN, 1, 1, 1));
        }
        if (isLakeOrRiver || isOcean){
            spawnListEntries.add(new Biome.SpawnListEntry(EntityType.DROWNED, 1000, 5, 5));
        }
    }

    private EntityType<?> getZombieType(double humidity){
        if (PosDataHelper.DRY_INTERVAL.contains(humidity)){
            return EntityType.HUSK;
        }
        return EntityType.ZOMBIE;
    }

    private EntityType<?> getSkeletonType(double temperature){
        if (PosDataHelper.FREEZE_INTERVAL.contains(temperature)){
            return EntityType.STRAY;
        }
        return EntityType.SKELETON;
    }
}
