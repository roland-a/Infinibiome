package weightedgpa.infinibiome.api.generators;

import weightedgpa.infinibiome.api.dependency.MultiDep;
import weightedgpa.infinibiome.api.posdata.PosDataProvider;
import weightedgpa.infinibiome.api.posdata.PosDataTable;

/**
 * Objects that writes positional data to {@link PosDataProvider} at every position
 *
 * @apiNote
 * This cannot normally use {@link PosDataProvider} without throwing a stack overflow.
 */
public interface PosDataGen extends MultiDep {
    /**
     * Determines when this run compared to other PosDataGens
     *
     * @apiNote
     * Must always return the same Timing.
     * Must be unique compared to other running posDataGens
     */
    Timing getTiming();

    /**
     * Reads and writes data at a position.
     *
     * @apiNote
     * Must do the same writes for the same position and seed
     */
    void generateData(PosDataTable data);
}
