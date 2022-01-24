package weightedgpa.infinibiome.internal.floatfunc.util;

import org.apache.commons.lang3.Validate;
import weightedgpa.infinibiome.internal.floatfunc.FloatFunc;
import weightedgpa.infinibiome.api.pos.BlockPos2D;
import weightedgpa.infinibiome.internal.misc.Helper;
import weightedgpa.infinibiome.internal.misc.ProgressPrinter;

import java.io.*;
import java.util.concurrent.atomic.AtomicLongArray;
import java.util.stream.IntStream;
import java.util.stream.LongStream;

/**
 * Gets the percentile of a floatFunc
 *
 * Takes a long time to generate a table
 */
public final class PercentileTable {
    public final float[] valueToPercentile;
    private final Interval interval;

    private PercentileTable(DataInput file) throws IOException {
        this.valueToPercentile = new float[file.readInt()];

        for (int i = 0; i < valueToPercentile.length; i++){
            valueToPercentile[i] = file.readFloat();
        }

        this.interval = new Interval(file.readFloat(), file.readFloat());
    }

    private PercentileTable(FloatFunc<? super BlockPos2D> base, int fineness, long sampleSize){
        this.interval = base.getOutputInterval();

        AtomicLongArray counter = generateCounter(
            base,
            fineness,
            sampleSize
        );

        printCounterStat(counter);

        this.valueToPercentile = generateTable(
            counter,
            fineness,
            sampleSize
        );
    }

    private PercentileTable(int fineness, float[] values){
        //System.out.println(getRawNoiseInterval(values));

        this.interval = getRawNoiseInterval(values);

        AtomicLongArray counter = generateCounter(values, fineness);

        printCounterStat(counter);

        this.valueToPercentile = generateTable(counter, fineness, values.length);
    }

    public static PercentileTable generate(FloatFunc<BlockPos2D> base, int fineness, long sampleSize) {
        return new PercentileTable(base, fineness, sampleSize);
    }

    public static PercentileTable generate(float[] values, int fineness) {
        return new PercentileTable(fineness, values);
    }

    public double rawValueToPercentile(double rawValue){
        assert interval.contains(rawValue): rawValue + " not in " + interval;

        int index = interval.mapToIntInterval(
            rawValue,
            0,
            valueToPercentile.length - 1
        );

        return valueToPercentile[index];
    }

    public double percentileToRawNoise(double percentile){
        int resultIndex = 0;
        double resultDist = Math.abs(valueToPercentile[resultIndex] - percentile);

        for (int thisIndex = 1; thisIndex < valueToPercentile.length; thisIndex++){
            double thisDist = Math.abs(valueToPercentile[thisIndex] - percentile);

            if (thisDist < resultDist){
                resultIndex = thisIndex;
                resultDist = thisDist;
            }
        }

        //System.out.println(resultDist);

        return Interval.PERCENT.mapInterval(
            resultIndex / (double)valueToPercentile.length,
            interval
        );
    }

    public Interval getRawNoiseInterval(){
        return interval;
    }

    /**
     * Creates a new instance from serialized data
     * 
     * @param input
     * The serialized data
     * 
     * @return
     * The new instance
     */
    public static PercentileTable deserialize(DataInput input){
        try {
            return new PercentileTable(input);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    /**
     * Serializes this into a DataOutput.
     * Used so this wont need to be generated again.
     * 
     * @param output
     * The DataOutput that will contain the serialized version of this.
     */
    public void serialize(DataOutput output){
        try {
            ProgressPrinter progressPrinter = new ProgressPrinter(valueToPercentile.length);

            output.writeInt(valueToPercentile.length);

            for (int i = 0; i < valueToPercentile.length; i++) {
                output.writeFloat((float) valueToPercentile[i]);

                progressPrinter.incrementAndTryPrintProgress();
            }

            output.writeFloat((float) interval.getMin());
            output.writeFloat((float) interval.getMax());
        }
        catch (IOException e){
            throw new UncheckedIOException(e);
        }
    }

    private AtomicLongArray generateCounter(FloatFunc<? super BlockPos2D> base, int fineness, long sampleSize) {
        AtomicLongArray result = new AtomicLongArray(fineness);

        int length = (int)Math.sqrt(sampleSize);


        ProgressPrinter progressPrinter = new ProgressPrinter(sampleSize);

        Helper.iterXZParallel(
            length * 32,
            length / 32,
            (x, z) -> {
                BlockPos2D input = new BlockPos2D(x, z);

                double output = base.getOutput(input);

                result.incrementAndGet(
                    interval.mapToIntInterval(
                        output,
                        0,
                        result.length() - 1
                    )
                );

                progressPrinter.incrementAndTryPrintProgress();
            }
        );

        return result;
    }

    private void printCounterStat(AtomicLongArray counter){
        //System.out.println(counter);
    }

    private AtomicLongArray generateCounter(float[] values, int fineness) {
        AtomicLongArray result = new AtomicLongArray(fineness);

        for (double v: values){
            result.incrementAndGet(
                interval.mapToIntInterval(
                    v,
                    0,
                    result.length() - 1
                )
            );
        }

        return result;
    }

    private float[] generateTable(AtomicLongArray counter, int fineness, long sampleSize){
        assert counter.length() == fineness;

        float[] result = new float[fineness];

        ProgressPrinter progressPrinter = new ProgressPrinter(result.length);

        long accumulation = 0;

        for (int i = 0; i < result.length; i++){
            accumulation += counter.get(i);

            double percentile = accumulation / (double)sampleSize;

            assert percentile >= 0;
            assert percentile <= 1;

            result[i] = (float) percentile;

            progressPrinter.incrementAndTryPrintProgress();
        }
        return result;
    }

    private Interval getRawNoiseInterval(float[] values){
        double min = Double.MAX_VALUE;
        double max = Double.MIN_VALUE;

        for (double v: values){
            if (v < min) min = v;

            if (v > max) max = v;
        }

        return new Interval(min, max);
    }
}
