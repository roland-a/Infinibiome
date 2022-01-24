package weightedgpa.infinibiome.internal.generators.nonworldgen.spawnList;

import net.minecraft.entity.EntityClassification;
import net.minecraft.entity.EntityType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorld;
import net.minecraft.world.biome.Biome;
import weightedgpa.infinibiome.api.dependency.DependencyInjector;
import weightedgpa.infinibiome.api.generators.nonworldgen.MobSpawnListModifier;
import weightedgpa.infinibiome.api.posdata.PosDataKeys;
import weightedgpa.infinibiome.api.posdata.PosDataProvider;
import weightedgpa.infinibiome.internal.misc.MCHelper;

import java.util.List;

public final class AmbientSpawnList implements MobSpawnListModifier {
    private final PosDataProvider posData;

    public AmbientSpawnList(DependencyInjector di) {
        this.posData = di.get(PosDataProvider.class);
    }

    @Override
    public void modifyList(BlockPos pos, EntityClassification creatureType, List<Biome.SpawnListEntry> spawnListEntries, IWorld world) {
        if (creatureType != EntityClassification.AMBIENT) return;

        if (pos.getY() > posData.get(PosDataKeys.MAPPED_HEIGHT, MCHelper.to2D(pos))) return;

        spawnListEntries.add(new Biome.SpawnListEntry(EntityType.BAT, 10, 1, 1));
    }
}
