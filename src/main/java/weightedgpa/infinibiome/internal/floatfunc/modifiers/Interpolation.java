package weightedgpa.infinibiome.internal.floatfunc.modifiers;

import weightedgpa.infinibiome.api.pos.IntPosInfo;
import weightedgpa.infinibiome.internal.floatfunc.FloatFunc;
import weightedgpa.infinibiome.internal.floatfunc.util.Interval;
import weightedgpa.infinibiome.internal.misc.MathHelper;
import weightedgpa.infinibiome.internal.misc.PosModCache;
import weightedgpa.infinibiome.internal.misc.Log2helper;

public final class Interpolation<I> implements FloatFunc<I> {
    private final FloatFunc<I> base;
    private final IntPosInfo<I> posinfo;
    private final int gridLengthLog2;
    private final int gridLength;

    private final PosModCache<GridPos, Double> gridCornerCache;
    private final PosModCache<GridPos, Grid> gridDataCache;

    public Interpolation(FloatFunc<I> base, int gridLength, int cacheLength, IntPosInfo<I> posInfo) {
        this.base = base;
        this.posinfo = posInfo;
        this.gridLength = gridLength;
        this.gridLengthLog2 = Log2helper.asLog2(gridLength);

        this.gridDataCache = new PosModCache<>(
            cacheLength,
            Grid::new,
            GridPos.INFO
        );

        this.gridCornerCache = new PosModCache<>(
            cacheLength,
            p -> base.getOutput(
                posinfo.build(
                    Log2helper.mult(p.x, gridLengthLog2),
                    Log2helper.mult(p.z, gridLengthLog2)
                )
            ),
            GridPos.INFO
        );
    }

    @Override
    public double getOutput(I input) {
        int posX = posinfo.getIntX(input);
        int posZ = posinfo.getIntZ(input);

        int modX = Log2helper.mod(posX, gridLengthLog2);
        int modZ = Log2helper.mod(posZ, gridLengthLog2);

        int divX = Log2helper.floorDiv(posX, gridLengthLog2);
        int divZ = Log2helper.floorDiv(posZ, gridLengthLog2);

        return gridDataCache.get(
            new GridPos(divX, divZ)
        )
        .values[modX][modZ];
    }

    private double s_curve(double t) {
        return t * t * (3 - t - t);
    }

    @Override
    public Interval getOutputInterval() {
        return base.getOutputInterval();
    }

    private static class GridPos {
        static IntPosInfo<GridPos> INFO = new IntPosInfo<GridPos>() {
            @Override
            public int getIntX(GridPos pos) {
                return pos.x;
            }

            @Override
            public int getIntZ(GridPos pos) {
                return pos.z;
            }

            @Override
            public int getLog2Scale() {
                throw new UnsupportedOperationException();
            }

            @Override
            public GridPos build(int x, int z) {
                throw new UnsupportedOperationException();
            }
        };

        private final int x;
        private final int z;

        GridPos(int x, int z) {
            this.x = x;
            this.z = z;
        }
    }

    private class Grid{
        private final double[][] values = new double[gridLength][gridLength];

        private Grid(GridPos pos){
            double v00 = gridCornerCache.get(
                pos
            );
            double v10 = gridCornerCache.get(
                new GridPos(pos.x+1, pos.z)
            );
            double v01 = gridCornerCache.get(
                new GridPos(pos.x, pos.z+1)
            );
            double v11 = gridCornerCache.get(
                new GridPos(pos.x+1, pos.z+1)
            );

            for (int x = 0; x < gridLength; x++){
                for (int z = 0; z < gridLength; z++){
                    double percentX = s_curve(x / (double)gridLength);
                    double percentZ = s_curve(z / (double)gridLength);

                    values[x][z] = MathHelper.lerp(
                        percentX,
                        MathHelper.lerp(
                            percentZ,
                            v00,
                            v01
                        ),
                        MathHelper.lerp(
                            percentZ,
                            v10,
                            v11
                        )
                    );
                }
            }
        }
    }
}

