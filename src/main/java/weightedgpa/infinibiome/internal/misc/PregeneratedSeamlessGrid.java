package weightedgpa.infinibiome.internal.misc;

import weightedgpa.infinibiome.internal.floatfunc.modifiers.SeamlessGrid;

import java.io.IOException;


public final class PregeneratedSeamlessGrid {
    private PregeneratedSeamlessGrid() {}

    public static final SeamlessGrid TABLE_256_256;
    //public static final SeamlessGrid TABLE_256_128;
    //public static final SeamlessGrid TABLE_256_64;

    static {
        try {
            TABLE_256_256 = SeamlessGrid.deserialize(
                Helper.getResource("/seamlessGrid256_256")
            );
            /*
            TABLE_256_128 = SeamlessGrid.deserialize(
                Helper.getResource("/seamlessGrid256_128")
            );
            TABLE_256_64 = SeamlessGrid.deserialize(
                Helper.getResource("/seamlessGrid256_64")
            );

             */

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
