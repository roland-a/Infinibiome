package weightedgpa.infinibiome.api.generators.nonworldgen;

import weightedgpa.infinibiome.api.dependency.MultiDep;

/**
 * Objects that writes data on the default configuration file that is generated at the start of the minecraft menu screen
 *
 * @apiNote
 * Cannot retrieve objects that depend on the world seed, as this object will be created before any worlds are loaded
 */
public interface DefaultConfig extends MultiDep {}
