package weightedgpa.infinibiome.internal.display;

import weightedgpa.infinibiome.api.posdata.PosDataKeys;
import weightedgpa.infinibiome.internal.floatfunc.util.Interval;
import weightedgpa.infinibiome.api.pos.BlockPos2D;

import java.awt.*;

public final class HeightMap extends DataMapBase {
    private static final Interval OCEAN_HEIGHT = new Interval(8, 63);
    private static final Interval LAND_HEIGHT = new Interval(63, 63+150);

    public HeightMap(){
        this(
            40,
            "2"
        );
    }

    private HeightMap(int scale, String seedBranch){
       super(scale, seedBranch);
    }

    @Override
    protected Color getColor(int posX, int posZ, int screenPixelX, int screenPixelZ) {
        if (posX % 1000 == 0 || posZ % 1000 == 0){
            return Color.BLACK;
        }

        double height = posData.get(
            PosDataKeys.MAPPED_HEIGHT,
            new BlockPos2D(posX, posZ)
        );

        try {
            if (height > 63){
                double v = LAND_HEIGHT.mapInterval(height, Interval.PERCENT);

                if (v > 1) v = 1;

                return new Color(
                    0f,
                    (float)v,
                    0f
                );
            }

            double v = OCEAN_HEIGHT.mapInterval(height, Interval.PERCENT);

            return new Color(
                0f,
                0f,
                (float)v
            );
        }
        catch (Throwable e){
            throw new RuntimeException(String.valueOf(height), e);
        }
    }
}
