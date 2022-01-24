package weightedgpa.infinibiome.api.dependency;

@FunctionalInterface
public interface DependencyModule {
    void addToPre(DependencyInjector.Pre table);
}
