package weightedgpa.infinibiome.internal.minecraftImpl;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.provider.BiomeProvider;
import net.minecraft.world.gen.feature.structure.Structure;
import net.minecraftforge.common.util.Lazy;
import weightedgpa.infinibiome.api.dependency.DependencyInjector;
import weightedgpa.infinibiome.api.dependency.SingleDep;
import weightedgpa.infinibiome.api.generators.nonworldgen.SpawnPointBlacklist;
import weightedgpa.infinibiome.api.posdata.PosDataKeys;
import weightedgpa.infinibiome.api.posdata.PosDataProvider;
import weightedgpa.infinibiome.api.pos.BlockPos2D;
import weightedgpa.infinibiome.internal.generators.interchunks.struct.StructGens;
import weightedgpa.infinibiome.internal.generators.utils.PredicateSearcher;

import java.util.*;

public final class IBBiomeProvider extends BiomeProvider implements SingleDep {
    private final PosDataProvider data;

    private final Lazy<StructGens> structGens;
    private final Lazy<List<SpawnPointBlacklist>> spawnPointBlacklists;

    public IBBiomeProvider(DependencyInjector di){
        super(new HashSet<>());

        this.data = di.get(PosDataProvider.class);
        this.structGens = Lazy.of(() -> di.get(StructGens.class));
        this.spawnPointBlacklists = Lazy.of(() -> di.getAll(SpawnPointBlacklist.class));
    }

    @Override
    public List<Biome> getBiomesToSpawnIn() {
        return Collections.emptyList();
    }

    @Override
    public Biome getNoiseBiome(int x, int y, int z) {
        BlockPos2D pos2D = new BlockPos2D(x * 4, z * 4);

        return IBBiomes.getBiome(pos2D, data);
    }

    @Override
    public boolean hasStructure(Structure<?> structure) {
        return structGens.get().containsStruct(structure);
    }

    @Override
    public BlockPos func_225531_a_(int x, int y, int z, int dist, List<Biome> list, Random rand) {
        if (x == 0 && z == 0 && dist == 256){
            return searchSpawn();
        }
        return super.func_225531_a_(x, y, z, dist, list, rand);
    }

    private BlockPos searchSpawn() {
        return new PredicateSearcher<>(
            64,
            p -> {
                if (data.get(PosDataKeys.LANDMASS_TYPE, p).isOcean()) return false;

                for (SpawnPointBlacklist blackList: spawnPointBlacklists.get()){
                    if (!blackList.canSpawnHere(p)) return false;
                }

                return true;
            },
            BlockPos2D.INFO
        )
        .getClosestPoint(
            new BlockPos2D(0, 0)
        )
        .to3D(0);
    }
}
