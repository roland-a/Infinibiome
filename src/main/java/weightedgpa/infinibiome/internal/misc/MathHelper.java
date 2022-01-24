package weightedgpa.infinibiome.internal.misc;

import weightedgpa.infinibiome.api.pos.PosInfo;
import weightedgpa.infinibiome.internal.floatfunc.FloatFunc;
import weightedgpa.infinibiome.internal.floatfunc.util.Interval;
import weightedgpa.infinibiome.api.pointsprovider.PointsProvider;

import java.awt.geom.Line2D;
import java.util.Random;
import java.util.function.Function;

public final class MathHelper {
    private MathHelper(){}

    public static Interval VALID_FRACTAL_VALUE = new Interval(
        Math.nextUp(0),
        Math.nextDown(1)
    );

    public static Interval VALID_SCALE = new Interval(
        Math.nextUp(0),
        Float.POSITIVE_INFINITY
    );

    public static <T> double getDistance(PosInfo<T> posInfo, T pos1, T pos2){
        return hypot(
            posInfo.getX(pos1) - posInfo.getX(pos2),
            posInfo.getZ(pos1) - posInfo.getZ(pos2)
        );
    }

    public static <T> double getDistanceSq(PosInfo<T> posInfo, T pos1, T pos2){
        double deltaX = posInfo.getX(pos1) - posInfo.getX(pos2);
        double deltaZ = posInfo.getZ(pos1) - posInfo.getZ(pos2);

        return deltaX * deltaX + deltaZ * deltaZ;
    }

    public static double lerp(double percent, double lowerPercentValue, double upperPercentValue){
        assert Interval.PERCENT.contains(percent): percent;
        assert Double.isFinite(lowerPercentValue): lowerPercentValue;
        assert Double.isFinite(upperPercentValue): upperPercentValue;

        return (lowerPercentValue * (1 - percent)) + (upperPercentValue * percent);
    }

    public static double inverseLerp(double value, double lowerPercentValue, double upperPercentValue){
        assert Double.isFinite(value): value;
        assert Double.isFinite(lowerPercentValue): lowerPercentValue;
        assert Double.isFinite(upperPercentValue): upperPercentValue;

        return (value - lowerPercentValue) / (upperPercentValue - lowerPercentValue);
    }

    public static double skew(double percent, double skew){
        assert Interval.PERCENT.contains(percent);
        assert Double.isFinite(skew);

        //uses the formula
        //f(x,a) = (a^x-1)/(a-1) for a!=1
        //f(x,a) = x for a=1
        //when x is inside [0,1], f(x) will be inside [0,1]
        //has the property f(x,a)=1-f(1-x,1/a)
        if (skew == 0) return percent;

        double a = convertToA(skew);

        return (Math.pow(a, percent) - 1) / (a - 1);
    }

    private static double convertToA(double value){
        if (value < 0){
            return -value + 1;
        }
        return 1 / value + 1;
    }

    public static double ease(double percent, double ease){
        assert Interval.PERCENT.contains(percent): percent;
        assert Double.isFinite(ease): ease;

        if (ease == 0) return percent;

        Interval lowerHalf = new Interval(0, 0.5f);
        Interval upperHalf = new Interval(0.5f, 1);

        if (lowerHalf.contains(percent)){
            //converts [0,1/2] to [0,1]
            double lowerHalfPercent = lowerHalf.mapInterval(percent, Interval.PERCENT);

            //skews it to the back
            lowerHalfPercent = skew(lowerHalfPercent, -ease);

            //convert [0,1] back to [0,1/2]
            return Interval.PERCENT.mapInterval(lowerHalfPercent, lowerHalf);
        }
        assert upperHalf.contains(percent);

        //converts [1/2,1] to [0,1]
        double upperHalfPercent = upperHalf.mapInterval(percent, Interval.PERCENT);

        //skews it to the front
        upperHalfPercent = skew(upperHalfPercent, ease);

        //converts [0,1] back to [1/2,1]
        return Interval.PERCENT.mapInterval(upperHalfPercent, upperHalf);
    }

    /**
     * Randomly returns true or false depending on the chance.
     *
     * @param percent
     * The probability of this returning true.
     */
    public static boolean randomBool(double percent, Random random){
        assert Interval.PERCENT.contains(percent): percent;

        if (percent == 0.5){
            return random.nextBoolean();
        }

        double randomPercent = random.nextFloat();

        return randomPercent < percent;
    }

    public static int randomInt(int min, int maxExclusive, Random random){
        assert min < maxExclusive: min + " " + maxExclusive;

        return random.nextInt(maxExclusive - min) + min;
    }

    public static int randomRound(double value, Random random){
        assert Double.isFinite(value): value;
        assert value >= 0: value;

        if (value <= 0.5f){
            if (randomBool(value, random)) {
                return 1;
            }
            return 0;
        }

        int result = 0;

        int fullHalves = (int)(value / 0.5f);

        for (int i = 0; i < fullHalves; i++){
            if (randomBool(0.5f, random)){
                result += 1;
            }
        }

        double remaining = value - (fullHalves * 0.5f);

        if (randomBool(remaining, random)){
            result += 1;
        }

        return result;
    }

    /**
     * Returns the distance to the closest voronoi border.
     * Not 100% accurate
     *
     * @param pointsProvider
     * Provides the points for the voronoi diagram
     *
     * @param pointCount
     * How many points it will count to determine the distance to the closest voronoi border.
     * Higher values means more accurate results.
     * Higher values also means slower results.
     *
     * @throws IllegalArgumentException
     * If count is less than 2
     */
    public static <I> double getDistanceToVoronoiBorder(I center, PointsProvider<I> pointsProvider, int pointCount){
        assert pointCount >= 2: pointCount;

        PosInfo<I> posInfo = pointsProvider.getPosInfo();

        I cellCenter = null;
        double distanceToClosestBorderSq = Double.POSITIVE_INFINITY;

        for (I point: pointsProvider.getClosestPoints(center, pointCount)){
            if (cellCenter == null){
                cellCenter = point;
                continue;
            }

            //border between the center cell and the neighboring cell
            Line border = new Line(
                posInfo.getX(cellCenter),
                posInfo.getZ(cellCenter),
                posInfo.getX(point),
                posInfo.getZ(point)
            ).rotate90();

            //distance from pos to border
            double distanceToBorderSq = border.getDistanceToPointSq(
                posInfo.getX(center),
                posInfo.getZ(center)
            );

            if (distanceToBorderSq < distanceToClosestBorderSq){
                distanceToClosestBorderSq = distanceToBorderSq;
            }
        }

        assert distanceToClosestBorderSq != Double.POSITIVE_INFINITY;

        return Math.sqrt(distanceToClosestBorderSq);
    }

    private static class Line{
        double p1x;
        double p1z;
        double p2x;
        double p2z;

        Line(double p1x, double p1z, double p2x, double p2z) {
            this.p1x = p1x;
            this.p1z = p1z;
            this.p2x = p2x;
            this.p2z = p2z;
        }

        Line rotate90(){
            double midPointX = (p1x + p2x) / 2f;
            double midPointZ = (p1z + p2z) / 2f;

            double rotatedP1X = -(p1z - midPointZ) + midPointX;
            double rotatedP1Z =  (p1x - midPointX) + midPointZ;

            double rotatedP2X = -(p2z - midPointZ) + midPointX;
            double rotatedP2Z =  (p2x - midPointX) + midPointZ;

            return new Line(
                rotatedP1X,
                rotatedP1Z,
                rotatedP2X,
                rotatedP2Z
            );
        }

        double getDistanceToPointSq(double pointX, double pointZ){
            return new Line2D.Double(
                p1x,
                p1z,
                p2x,
                p2z
            )
            .ptLineDistSq(
                pointX,
                pointZ
            );
        }
    }

    public static double fractal(
        Function<Double, Double> scaleToOutput,
        Interval interval,
        double octaves,
        double peristence,
        double lacunarity
    ){
        assert octaves >= 1: octaves;
        assert Double.isFinite(octaves): octaves;

        assert VALID_FRACTAL_VALUE.contains(lacunarity): lacunarity;
        assert VALID_FRACTAL_VALUE.contains(peristence): peristence;

        double currentScale = 1;
        double currentAmp = 1;

        double maxValue = interval.getMax();
        double minValue = interval.getMin();
        double cumulativeValue = 0;

        for (int i = 0; i < floor(octaves); i++){
            cumulativeValue += scaleToOutput.apply(currentScale) * currentAmp;

            currentAmp *= peristence;
            currentScale *= lacunarity;

            maxValue += maxValue * currentAmp;
            minValue += minValue * currentAmp;
        }

        double remainingIteration = octaves - Math.floor(octaves);

        cumulativeValue += scaleToOutput.apply(currentScale) * currentAmp * remainingIteration;

        maxValue += maxValue * currentAmp * remainingIteration;
        minValue += minValue * currentAmp * remainingIteration;

        return new Interval(minValue, maxValue).mapInterval(cumulativeValue, interval);
    }

    public static double scaleLimitToOctaves(
        double scale,
        double scaleLimit,
        double lacunarity
    ){
        assert VALID_SCALE.contains(scaleLimit): scaleLimit;
        assert VALID_SCALE.contains(scale): scale;
        assert VALID_FRACTAL_VALUE.contains(lacunarity): lacunarity;
        assert scale >= scaleLimit: scale + " " + scaleLimit;

        return Math.log(scaleLimit/scale)/Math.log(lacunarity) + 1;
    }

    public static <I > double gradientTowardsPoint(I pos, double radius, PointsProvider<I> clusterCenters){
        assert radius >= 0: radius;
        assert Double.isFinite(radius): radius;

        return gradientTowardsPoint(
            pos,
            FloatFunc.constFunc(radius),
            clusterCenters
        );
    }

    public static <I> double gradientTowardsPoint(I pos, FloatFunc<I> radiusFunc, PointsProvider<I> clusterCenters){
        PosInfo<I> posInfo = clusterCenters.getPosInfo();

        double result = 0;
        double maxSearchDistance = radiusFunc.getOutputInterval().getMax();

        for (I clusterCenter: clusterCenters.getBoundedPoints(pos, maxSearchDistance)){
            double radius = radiusFunc.getOutput(clusterCenter);

            double distanceFromCenter = getDistance(posInfo, pos, clusterCenter);

            //too far away from the cluster center
            if (distanceFromCenter > radius){
                continue;
            }

            //how far away from the cluster's center
            //0 is the furthest away from cluster
            //1 is closest to center
            double percentFromCluster = inverseLerp(
                distanceFromCenter,
                radius,
                0
            );

            result = Math.max(result, percentFromCluster);
        }

        return result;
    }

    public static int floor(double value) {
        int i = (int)value;
        
        return value < (double)i ? i - 1 : i;
    }

    public static int ceil(double value) {
        int i = (int)value;
        
        return value > (double)i ? i + 1 : i;
    }

    //copy and pasted from fastmath
    public static double hypot(double x, double y) {
        if (Double.isInfinite(x) || Double.isInfinite(y)) {
            return Double.POSITIVE_INFINITY;
        }
        if (Double.isNaN(x) || Double.isNaN(y)) {
            return Double.NaN;
        }

        int expX = Math.getExponent(x);
        int expY = Math.getExponent(y);

        if (expX > expY + 27) {
            // y is neglectible with respect to x
            return Math.abs(x);
        }
        if (expY > expX + 27) {
            // x is neglectible with respect to y
            return Math.abs(y);
        }

        // find an intermediate scale to avoid both overflow and underflow
        int middleExp = (expX + expY) / 2;

        // scale parameters without losing precision
        double scaledX = Math.scalb(x, -middleExp);
        double scaledY = Math.scalb(y, -middleExp);

        // compute scaled hypotenuse
        double scaledH = Math.sqrt(scaledX * scaledX + scaledY * scaledY);

        // remove scaling
        return Math.scalb(scaledH, middleExp);
    }
}
