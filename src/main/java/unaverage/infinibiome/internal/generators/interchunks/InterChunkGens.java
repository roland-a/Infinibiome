package weightedgpa.infinibiome.internal.generators.interchunks;

import net.minecraft.world.IWorld;
import weightedgpa.infinibiome.api.dependency.DependencyInjector;
import weightedgpa.infinibiome.api.dependency.SingleDep;
import weightedgpa.infinibiome.api.pos.InterChunkPos;

import weightedgpa.infinibiome.api.generators.InterChunkGen;
import weightedgpa.infinibiome.internal.generators.utils.SortedRandomizedList;
import weightedgpa.infinibiome.internal.minecraftImpl.world.NoClientUpdatingWorld;
import weightedgpa.infinibiome.internal.misc.DeadlockDetector;

import java.util.Comparator;

public final class InterChunkGens implements SingleDep {
    private final SortedRandomizedList<InterChunkGen> interChunkGens;

    private final DeadlockDetector deadlockDetector = new DeadlockDetector();

    public InterChunkGens(DependencyInjector di) {
        this.interChunkGens = new SortedRandomizedList<>(
            di.getAll(InterChunkGen.class),
            Comparator.comparing(InterChunkGen::getInterChunkTiming)
        );
    }

    public void generateAll(InterChunkPos interChunkPos, IWorld world){
        world = new NoClientUpdatingWorld(world);

        IWorld finalWorld = world;
        interChunkGens.forEachItem(
            resourceGen -> {
               perInterChunkGen(resourceGen, interChunkPos, finalWorld);
               return true;
           }
       );
    }

    private void perInterChunkGen(InterChunkGen interChunkGen, InterChunkPos pos, IWorld world){
        try {
            deadlockDetector.setCurrentRunningGenerator(interChunkGen);
            interChunkGen.generate(pos, world);
            deadlockDetector.currentGeneratorFinished();
        } catch (Throwable e){
            throw new RuntimeException(interChunkGen.toString(), e);
        }
    }

}
