package weightedgpa.infinibiome.internal.generators.posdata;

import weightedgpa.infinibiome.api.posdata.PosDataKeys;
import weightedgpa.infinibiome.api.posdata.PosDataProvider;
import weightedgpa.infinibiome.api.posdata.PosDataTable;
import weightedgpa.infinibiome.internal.misc.MCHelper;
import weightedgpa.infinibiome.internal.misc.MathHelper;

final class PosDataGenHelper {
    private PosDataGenHelper(){}

    static double fixHeight(double newHeight, double previousHeight, PosDataTable table){
        if ((int)previousHeight != MCHelper.WATER_HEIGHT) return newHeight;

        if ((int)newHeight != MCHelper.WATER_HEIGHT - 1) return newHeight;

        return previousHeight;
    }
}
