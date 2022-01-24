package weightedgpa.infinibiome.api.generators;

import weightedgpa.infinibiome.api.dependency.MultiDep;

/**
 * Objects that needs validation after creation
 *
 * validations are run when dependency injector refreshes
 */
public interface Validator extends MultiDep {
    void checkIsValid();
}
