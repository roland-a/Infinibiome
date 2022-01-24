package weightedgpa.infinibiome.internal.generators.interchunks.struct;

import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.IFeatureConfig;
import net.minecraft.world.gen.feature.structure.Structure;
import weightedgpa.infinibiome.api.dependency.SingleDep;
import weightedgpa.infinibiome.api.generators.StructGen;
import weightedgpa.infinibiome.api.dependency.DependencyInjector;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public final class StructGens implements SingleDep {
    private final List<StructGen<?>> structs = new ArrayList<>();

    private final Map<Feature<?>, Boolean> hasStructCache = new HashMap<>();;

    public StructGens(DependencyInjector di){
        di.getAll(StructGen.class).forEach(
            structs::add
        );
    }

    public boolean containsStruct(Structure<?> struct){
        return hasStructCache.computeIfAbsent(
            struct,
            (__) -> structs.stream().anyMatch(s -> s.getStruct().equals(struct))
        );
    }

    @Nullable
    public <T extends IFeatureConfig> T hasStructureStartHere(Feature<T> struct, ChunkPos chunkPos2D){
        for (StructGen<?> p : structs) {
            if (p.getStruct().equals(struct)) {
               return (T)p.hasStructureStartHere(chunkPos2D);
            }
        }
        return null;
    }

    public void forEachStructs(Consumer<StructGen<?>> structConsumer){
        structs.forEach(structConsumer);
    }
}
