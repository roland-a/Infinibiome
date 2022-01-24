package weightedgpa.infinibiome.api.generators;

import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.gen.feature.IFeatureConfig;
import net.minecraft.world.gen.feature.structure.Structure;
import weightedgpa.infinibiome.api.dependency.MultiDep;
import weightedgpa.infinibiome.api.generators.nonworldgen.Locatable;

import javax.annotation.Nullable;

/**
 * Objects that generate large structures
 */
public interface StructGen<T extends IFeatureConfig> extends InterChunkGen, Locatable, MultiDep {
    /**
     * If theres no structure start here, then return null
     *
     * If there is, return the struct config
     */
    @Nullable
    T hasStructureStartHere(ChunkPos chunkPos);

    Structure<T> getStruct();

    @Override
    default Timing getInterChunkTiming(){
        return InterChunkGenTimings.STRUCTS;
    }
}
