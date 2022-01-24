package weightedgpa.infinibiome.internal.generators.nonworldgen.spawnList;

import net.minecraft.entity.EntityClassification;
import net.minecraft.entity.EntityType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorld;
import net.minecraft.world.biome.Biome;
import weightedgpa.infinibiome.api.generators.nonworldgen.MobSpawnListModifier;
import weightedgpa.infinibiome.api.dependency.DependencyInjector;
import weightedgpa.infinibiome.api.posdata.PosDataHelper;
import weightedgpa.infinibiome.internal.misc.MCHelper;
import weightedgpa.infinibiome.api.pos.BlockPos2D;
import weightedgpa.infinibiome.api.posdata.PosDataKeys;
import weightedgpa.infinibiome.api.posdata.PosDataProvider;

import java.util.List;

public final class RiverLakeSpawnList implements MobSpawnListModifier {
    private final PosDataProvider data;

    public RiverLakeSpawnList(DependencyInjector di){
        this.data = di.get(PosDataProvider.class);
    }

    @Override
    public void modifyList(BlockPos pos, EntityClassification creatureType, List<Biome.SpawnListEntry> spawnListEntries, IWorld world) {
        if (creatureType != EntityClassification.WATER_CREATURE) return;

        if (!PosDataHelper.isUnderwaterPortionOfLakeOrRiver(MCHelper.to2D(pos), data)) return;

        spawnListEntries.add(
            new Biome.SpawnListEntry(EntityType.SALMON, 1, 1, 1)
        );
    }
}
