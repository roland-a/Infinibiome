package weightedgpa.infinibiome.internal.generators.utils;

import net.minecraft.world.gen.ChunkGenerator;
import weightedgpa.infinibiome.api.dependency.DependencyInjector;
import weightedgpa.infinibiome.api.generators.Seed;
import weightedgpa.infinibiome.api.generators.Validator;
import weightedgpa.infinibiome.internal.floatfunc.generators.RandomGen;
import weightedgpa.infinibiome.api.posdata.PosDataProvider;

public abstract class GeneratorBase implements Validator {
    protected final Seed seed;
    protected final RandomGen randomGen;
    protected final DependencyInjector di;
    protected final PosDataProvider posData;
    protected final ChunkGenerator<?> chunkGenerator;

    protected GeneratorBase(DependencyInjector di, String seedBranch) {
        this.seed = di.get(Seed.class).newSeed(seedBranch);
        this.randomGen = new RandomGen(seed.newSeed("generatorBaseRandomGen"));

        this.di = di;
        this.posData = di.get(PosDataProvider.class);
        this.chunkGenerator = di.get(ChunkGenerator.class);
    }

    @Override
    public void checkIsValid() {}
}
