package weightedgpa.infinibiome.internal.pos;

import weightedgpa.infinibiome.api.pos.PosInfo;
import weightedgpa.infinibiome.internal.misc.MathHelper;
import weightedgpa.infinibiome.internal.misc.Log2helper;

import java.util.Random;

@SuppressWarnings("PublicMethodNotExposedInInterface")
public final class Grid<I> {
    private final int centerX;
    private final int centerZ;

    private int offsetX = 0;
    private int offsetZ = 0;

    private final int gridLengthLog2;
    private final int gridLength;


    private final PosInfo<I> posInfo;

    public Grid(I pos, int gridLengthLog2, PosInfo<I> posInfo){
        this.centerX = Log2helper.floorDiv(MathHelper.floor(posInfo.getX(pos)), gridLengthLog2);
        this.centerZ = Log2helper.floorDiv(MathHelper.floor(posInfo.getZ(pos)), gridLengthLog2);

        this.gridLength = Log2helper.toNormal(gridLengthLog2);
        this.gridLengthLog2 = gridLengthLog2;
        this.posInfo = posInfo;
    }

    public int getGridX() {
        return centerX + offsetX;
    }

    public int getGridZ() {
        return centerZ + offsetZ;
    }

    public I getLowest(){
        return posInfo.build(
            Log2helper.mult(getGridX(), gridLengthLog2),
            Log2helper.mult(getGridZ(), gridLengthLog2)
        );
    }

    public I randomPos(Random random){
        int lowestX = Log2helper.mult(getGridX(), gridLengthLog2);
        int lowestZ = Log2helper.mult(getGridZ(), gridLengthLog2);

        //exclusive
        int highestX = lowestX + gridLength;
        int highestZ = lowestZ + gridLength;

        int randomX = MathHelper.randomInt(lowestX, highestX, random);
        int randomZ = MathHelper.randomInt(lowestZ, highestZ, random);

        return posInfo.build(randomX, randomZ);
    }

    public void setOffset(int x, int z) {
        offsetX = x;
        offsetZ = z;
    }

    @Override
    public String toString() {
        return "Grid{" +
            "gridX=" + centerX +
            ", gridZ=" + centerZ +
            ", offsetX=" + offsetX +
            ", offsetZ=" + offsetZ +
            ", gridLength=" + gridLengthLog2 +
            '}';
    }
}
