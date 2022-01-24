package weightedgpa.infinibiome.internal.generators.nonworldgen;

import weightedgpa.infinibiome.api.dependency.DependencyInjector;
import weightedgpa.infinibiome.api.generators.Validator;

public final class Validators {
    private Validators(){}

    public static void validate(DependencyInjector di){
        di.getAll(Validator.class).forEach(Validator::checkIsValid);
    }
}
