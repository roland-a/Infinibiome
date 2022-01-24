package weightedgpa.infinibiome.internal.floatfunc.modifiers;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.util.GeometricShapeFactory;
import org.apache.commons.lang3.Validate;
import weightedgpa.infinibiome.api.pos.BlockPos2D;
import weightedgpa.infinibiome.api.pos.IntPosInfo;
import weightedgpa.infinibiome.internal.misc.Pair;
import weightedgpa.infinibiome.internal.misc.Log2helper;
import weightedgpa.infinibiome.internal.misc.ProgressPrinter;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.function.Function;

public final class SeamlessGrid implements Serializable {
    private final int gridLength;
    private final int gridLengthLog2;
    private final int smoothLength;

    private final float[][][][] overlaps;

    private final RelativeGrid[] allRelativeGrid;

    private SeamlessGrid(int gridLength, int smoothRadius){
        Validate.isTrue(
            gridLength > 0,
            "gridLength must be greater than 0"
        );
        Validate.isTrue(
            smoothRadius > 0,
            "smoothRadius must be greater than 0"
        );
        Validate.isTrue(
            smoothRadius <= gridLength,
            "transitionRadius must not be greater than gridLength"
        );

        this.gridLengthLog2 = Log2helper.asLog2(gridLength);
        this.gridLength = gridLength;
        this.smoothLength = smoothRadius;
        this.allRelativeGrid = initAllGrids();
        this.overlaps = new float[gridLength][gridLength][3][3];
    }

    /**
     * Generates a new table.
     * Will take a very long time.
     *
     * @param gridLength The length of every grid inside the seamlessGrid.
     * @param smoothRadius The transitioning length at the borders of a seamLessGrid.
     * @return The newly generated SeamlessGrid
     */
    public static SeamlessGrid generate(int gridLength, int smoothRadius) {
        SeamlessGrid result = new SeamlessGrid(
            gridLength,
            smoothRadius
        );

        result.generateOverlaps();

        return result;
    }

    public static SeamlessGrid deserialize(DataInput input) throws IOException {
        int gridLength = input.readInt();
        int smoothRadius = input.readInt();

        SeamlessGrid result = new SeamlessGrid(gridLength, smoothRadius);

        result.loadOverlaps(input);

        return result;
    }

    @SuppressWarnings("unused")
    public void serialize(DataOutput output) throws IOException {
        output.writeInt(gridLength);
        output.writeInt(smoothLength);

        for (RelativeSmoothingCircle pos: allPossibleRelativeSmoothingCircles()){
            for (RelativeGrid grid: allRelativeGrid){
                output.writeFloat(
                    overlaps
                        [pos.getArrayX()]
                        [pos.getArrayZ()]
                        [grid.getArrayX()]
                        [grid.getArrayZ()]
                );
            }
        }
    }

    public <I> double getValue(
        I pos,
        Function<I, Double> gridLowestPosToValue,
        IntPosInfo<I> posInfo
    ){
        RelativeSmoothingCircle smoothingCircle = new RelativeSmoothingCircle(pos, posInfo);

        double aggregatedValue = 0;

        for (RelativeGrid grid: allRelativeGrid){
            double percent = getOverlap(smoothingCircle, grid);

            if (percent == 0){
                continue;
            }

            I gridLowestPos = grid.getNonRelativeGridLowestPos(pos, posInfo);

            double baseOutput = gridLowestPosToValue.apply(gridLowestPos);

            aggregatedValue += baseOutput * percent;
        }

        return aggregatedValue;
    }

    @SuppressWarnings({"NonConstantStringShouldBeStringBuffer", "StringConcatenationInLoop"})
    public String _debugValue(
        BlockPos2D pos,
        Function<? super BlockPos2D, Double> gridLowestPosToValue
    ){
        List<Pair<Double, Double>> outputWithPercents = new ArrayList<>();

        RelativeSmoothingCircle smoothingCircle = new RelativeSmoothingCircle(pos, BlockPos2D.INFO);

        for (RelativeGrid grid: allRelativeGrid){
            double percent = getOverlap(smoothingCircle, grid);

            BlockPos2D gridLowestPos = grid.getNonRelativeGridLowestPos(pos, BlockPos2D.INFO);

            double output = gridLowestPosToValue.apply(gridLowestPos);

            outputWithPercents.add(new Pair<>(output, percent));
        }

        outputWithPercents.sort(Comparator.comparing(p -> -p.second));

        String result = "";

        for (Pair<Double, Double> outputWithPercent: outputWithPercents){
            if (outputWithPercent.second < 0.1f){
                continue;
            }

            result += String.format(
                "%s%%: %.2f, ", Math.round(outputWithPercent.second* 100), outputWithPercent.first
            );
        }

        return result;
    }

    private Collection<RelativeSmoothingCircle> allPossibleRelativeSmoothingCircles(){
        List<RelativeSmoothingCircle> result = new ArrayList<>();

        for (int x = 0; x < gridLength; x++){
            for (int z = 0; z < gridLength; z++){
                result.add(new RelativeSmoothingCircle(x, z));
            }
        }

        return result;
    }

    private RelativeGrid[] initAllGrids(){
        List<RelativeGrid> result = new ArrayList<>();

        for (int x = -1; x <= 1; x++){
            for (int z = -1; z <= 1; z++){
                result.add(new RelativeGrid(x, z));
            }
        }

        return result.toArray(new RelativeGrid[0]);
    }

    private double getOverlap(RelativeSmoothingCircle smoothingCircle, RelativeGrid grid){
        return overlaps
            [smoothingCircle.getArrayX()]
            [smoothingCircle.getArrayZ()]
            [grid.getArrayX()]
            [grid.getArrayZ()];
    }

    private void loadOverlaps(DataInput input) throws IOException {
        for (RelativeSmoothingCircle smoothingCircle: allPossibleRelativeSmoothingCircles()) {
            for (RelativeGrid grid: allRelativeGrid) {
                overlaps
                    [smoothingCircle.getArrayX()]
                    [smoothingCircle.getArrayZ()]
                    [grid.getArrayX()]
                    [grid.getArrayZ()] = input.readFloat();
            }
        }
    }

    private void generateOverlaps(){
        ProgressPrinter progressPrinter = new ProgressPrinter((long) gridLength * gridLength * 3 * 3);

        for (RelativeSmoothingCircle smoothingCircle : allPossibleRelativeSmoothingCircles()){
            for (RelativeGrid grid: allRelativeGrid){
                double overlap = generateOverlap(smoothingCircle, grid);

                overlaps
                    [smoothingCircle.getArrayX()]
                    [smoothingCircle.getArrayZ()]
                    [grid.getArrayX()]
                    [grid.getArrayZ()] = (float) overlap;

                progressPrinter.incrementAndTryPrintProgress();
            }
        }
    }

    private double generateOverlap(RelativeSmoothingCircle smoothingCircle, RelativeGrid grid){
        double intersectionArea = smoothingCircle.getGeometry().intersection(grid.getGeometry()).getArea();

        return intersectionArea / smoothingCircle.getGeometry().getArea();
    }

    private class RelativeGrid {
        final int lowestX;
        final int lowestZ;

        final int gridX;
        final int gridZ;

        private Object geometry = null;

        RelativeGrid(int gridX, int gridZ){
            this.gridX = gridX;
            this.gridZ = gridZ;

            this.lowestX = gridX * gridLength;
            this.lowestZ = gridZ * gridLength;
        }
        
        //converts this to a nonRelative grid, then gets its lowestPos
        <I> I getNonRelativeGridLowestPos(I pos, IntPosInfo<I> posInfo){
            //todo simplify
            int nonRelativeCenterGridLowestX = posInfo.getIntX(pos) - Log2helper.mod(posInfo.getIntX(pos), gridLengthLog2);
            int nonRelativeCenterGridLowestZ = posInfo.getIntZ(pos) - Log2helper.mod(posInfo.getIntZ(pos), gridLengthLog2);

            return posInfo.build(
                nonRelativeCenterGridLowestX + lowestX,
                nonRelativeCenterGridLowestZ + lowestZ
            );
        }

        int getArrayX(){
            return gridX + 1;
        }

        int getArrayZ(){
            return gridZ + 1;
        }

        Geometry getGeometry(){
            if (geometry == null){
                GeometricShapeFactory factory = new GeometricShapeFactory();

                factory.setSize(
                    gridLength
                );

                double centerX = lowestX + gridLength / 2.0;
                double centerZ = lowestZ + gridLength / 2.0;

                factory.setCentre(
                    new Coordinate(
                        centerX,
                        centerZ
                    )
                );

                this.geometry = factory.createRectangle();
            }

            return (Geometry) geometry;
        }
    }

    private class RelativeSmoothingCircle {
        private final int centerX;
        private final int centerZ;

        private Object circleGeometry = null;

        RelativeSmoothingCircle(int centerX, int centerZ){
            this.centerX = centerX;
            this.centerZ = centerZ;
        }

        <T> RelativeSmoothingCircle(T pos, IntPosInfo<T> posInfo){
            this.centerX = Log2helper.mod(posInfo.getIntX(pos), gridLengthLog2);
            this.centerZ = Log2helper.mod(posInfo.getIntZ(pos), gridLengthLog2);
        }

        int getArrayX() {
            return centerX;
        }

        int getArrayZ() {
            return centerZ;
        }

        Geometry getGeometry(){
            if (circleGeometry == null){
                GeometricShapeFactory factory = new GeometricShapeFactory();

                factory.setCentre(
                    new Coordinate(centerX, centerZ)
                );

                factory.setSize(
                    smoothLength * 2
                );

                this.circleGeometry = factory.createCircle();
            }
            return (Geometry) circleGeometry;
        }
    }
}
