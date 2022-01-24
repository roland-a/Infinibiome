package weightedgpa.infinibiome.api.generators.nonworldgen;

import net.minecraft.entity.EntityClassification;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorld;
import net.minecraft.world.biome.Biome;
import weightedgpa.infinibiome.api.dependency.MultiDep;
import weightedgpa.infinibiome.api.generators.Timing;

import java.util.List;

/**
 * Modifies the spawn-list entry at a position
 */
public interface MobSpawnListModifier extends MultiDep {
    void modifyList(BlockPos pos, EntityClassification creatureType, List<Biome.SpawnListEntry> spawnListEntries, IWorld world);

    default Timing getMobSpawnModifierTiming(){
        return MobSpawnListModifierTiming.NORMAL;
    }
}
