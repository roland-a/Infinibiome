package weightedgpa.infinibiome.internal.generators.nonworldgen.spawnList;

import net.minecraft.entity.EntityClassification;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorld;
import net.minecraft.world.biome.Biome;
import weightedgpa.infinibiome.api.dependency.DependencyInjector;
import weightedgpa.infinibiome.api.dependency.SingleDep;
import weightedgpa.infinibiome.api.generators.nonworldgen.MobSpawnListModifier;

import java.util.Comparator;
import java.util.List;

public final class MobSpawnListModifiers implements SingleDep {
    private final List<MobSpawnListModifier> mobSpawnModifierEntries;

    public MobSpawnListModifiers(DependencyInjector di){
        this.mobSpawnModifierEntries = di.getAll(MobSpawnListModifier.class);

        mobSpawnModifierEntries.sort(
            Comparator.comparing(MobSpawnListModifier::getMobSpawnModifierTiming)
        );
    }

    public void modifyList(BlockPos pos, EntityClassification creatureType, List<Biome.SpawnListEntry> spawnListEntries, IWorld world){
        for (MobSpawnListModifier spawnModifier: mobSpawnModifierEntries){
            spawnModifier.modifyList(pos, creatureType, spawnListEntries, world);
        }
    }
}
