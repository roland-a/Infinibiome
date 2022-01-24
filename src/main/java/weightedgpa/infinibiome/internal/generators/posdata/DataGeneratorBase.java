package weightedgpa.infinibiome.internal.generators.posdata;

import net.minecraftforge.common.util.Lazy;
import weightedgpa.infinibiome.api.dependency.DependencyInjector;
import weightedgpa.infinibiome.api.generators.PosDataGen;
import weightedgpa.infinibiome.api.generators.Seed;
import weightedgpa.infinibiome.api.generators.Timing;
import weightedgpa.infinibiome.api.posdata.PosDataProvider;
import weightedgpa.infinibiome.internal.floatfunc.generators.RandomGen;

public abstract class DataGeneratorBase implements PosDataGen {
    protected final Seed seed;
    protected final RandomGen randomGen;
    protected final DependencyInjector di;
    protected final Lazy<PosDataProvider> posDataBeforeTiming;
    protected final Lazy<PosDataProvider> posDataAfterTiming;
    protected final Timing timing;

    protected DataGeneratorBase(DependencyInjector di, String seedBranch, Timing timing) {
        this.timing = timing;
        this.seed = di.get(Seed.class).newSeed(seedBranch);
        this.randomGen = new RandomGen(seed.newSeed("generatorBaseRandomGen"));

        this.di = di;
        this.posDataBeforeTiming = Lazy.of(() -> new PosDataProviderBase(di, timing));
        this.posDataAfterTiming = Lazy.of(() -> di.get(PosDataProvider.class));
    }

    @Override
    public Timing getTiming() {
        return timing;
    }
}
