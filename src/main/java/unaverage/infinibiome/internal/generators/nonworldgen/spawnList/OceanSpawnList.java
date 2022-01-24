package weightedgpa.infinibiome.internal.generators.nonworldgen.spawnList;

import net.minecraft.entity.EntityClassification;
import net.minecraft.entity.EntityType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorld;
import net.minecraft.world.biome.Biome;
import weightedgpa.infinibiome.api.generators.nonworldgen.MobSpawnListModifier;
import weightedgpa.infinibiome.api.dependency.DependencyInjector;
import weightedgpa.infinibiome.internal.generators.utils.GenHelper;
import weightedgpa.infinibiome.internal.misc.MCHelper;
import weightedgpa.infinibiome.api.pos.BlockPos2D;
import weightedgpa.infinibiome.api.posdata.PosDataHelper;
import weightedgpa.infinibiome.api.posdata.PosDataKeys;
import weightedgpa.infinibiome.api.posdata.PosDataProvider;

import java.util.List;

public final class OceanSpawnList implements MobSpawnListModifier {
    private final PosDataProvider data;

    public OceanSpawnList(DependencyInjector di){
        this.data = di.get(PosDataProvider.class);
    }

    @Override
    public void modifyList(BlockPos pos, EntityClassification creatureType, List<Biome.SpawnListEntry> spawnListEntries, IWorld world) {
        if (creatureType != EntityClassification.WATER_CREATURE) return;

        BlockPos2D pos2D = MCHelper.to2D(pos);

        if (!data.get(PosDataKeys.LANDMASS_TYPE, pos2D).isOcean()) return;

        if (pos.getY() < data.get(PosDataKeys.MAPPED_HEIGHT, pos2D)) return;

        double temperature = PosDataHelper.getTemperature(pos, data);
        
        if (GenHelper.UPPER_HOT_INTERVAL.contains(temperature)){
            spawnListEntries.add(new Biome.SpawnListEntry(EntityType.SQUID, 10, 4, 4));
            spawnListEntries.add(new Biome.SpawnListEntry(EntityType.PUFFERFISH, 15, 1, 3));
            spawnListEntries.add(new Biome.SpawnListEntry(EntityType.TROPICAL_FISH, 25, 8, 8));
            spawnListEntries.add(new Biome.SpawnListEntry(EntityType.DOLPHIN, 2, 1, 2));
        }
        else if (GenHelper.LOWER_HOT_INTERVAL.contains(temperature)){
            spawnListEntries.add(new Biome.SpawnListEntry(EntityType.SQUID, 10, 1, 2));
            spawnListEntries.add(new Biome.SpawnListEntry(EntityType.COD, 15, 3, 6));
            spawnListEntries.add(new Biome.SpawnListEntry(EntityType.PUFFERFISH, 5, 1, 3));
            spawnListEntries.add(new Biome.SpawnListEntry(EntityType.TROPICAL_FISH, 25, 8, 8));
            spawnListEntries.add(new Biome.SpawnListEntry(EntityType.DOLPHIN, 2, 1, 2));
        }
        else if (PosDataHelper.WARM_INTERVAL.contains(temperature)){
            spawnListEntries.add(new Biome.SpawnListEntry(EntityType.SQUID, 1, 1, 4));
            spawnListEntries.add(new Biome.SpawnListEntry(EntityType.COD, 10, 3, 6));
            spawnListEntries.add(new Biome.SpawnListEntry(EntityType.DOLPHIN, 1, 1, 2));
        }
        else if (PosDataHelper.COLD_INTERVAL.contains(temperature) || GenHelper.UPPER_FREEZE_INTERVAL.contains(temperature)){
            spawnListEntries.add(new Biome.SpawnListEntry(EntityType.SQUID, 3, 1, 4));
            spawnListEntries.add(new Biome.SpawnListEntry(EntityType.COD, 15, 3, 6));
            spawnListEntries.add(new Biome.SpawnListEntry(EntityType.SALMON, 15, 1, 5));
        }
        else {
            assert GenHelper.LOWER_FREEZE_INTERVAL.contains(temperature);

            spawnListEntries.add(new Biome.SpawnListEntry(EntityType.SQUID, 1, 1, 4));
            spawnListEntries.add(new Biome.SpawnListEntry(EntityType.SALMON, 15, 1, 5));
        }
    }
}
