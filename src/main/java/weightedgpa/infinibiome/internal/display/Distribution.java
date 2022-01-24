package weightedgpa.infinibiome.internal.display;

import weightedgpa.infinibiome.internal.floatfunc.FloatFunc;
import weightedgpa.infinibiome.internal.floatfunc.util.Interval;
import weightedgpa.infinibiome.api.generators.Seed;
import weightedgpa.infinibiome.api.pos.BlockPos2D;
import weightedgpa.infinibiome.internal.misc.Helper;
import weightedgpa.infinibiome.internal.misc.ProgressPrinter;

import java.applet.Applet;
import java.awt.*;
import java.util.concurrent.atomic.AtomicIntegerArray;

public final class Distribution extends Applet {
    private final FloatFunc<BlockPos2D> base;
    private final AtomicIntegerArray counter;

    public Distribution() {
        this(
            initNoise(),
            20,
            1000000000
        );
    }

    private static FloatFunc<BlockPos2D> initNoise(){
        return Helper.initUniformNoise(Seed.ROOT, Helper.COMMON_SCALE/100);
    }

    private Distribution(FloatFunc<BlockPos2D> base, int smoothness, int sampleSize) {
        this.base = base;
        this.counter = new AtomicIntegerArray(smoothness);

        assert base.getOutputInterval().canMapInterval();

        generateCounter(sampleSize);
    }

    private void generateCounter(int sampleSize){
        int length = (int)Math.sqrt(sampleSize);

        ProgressPrinter progressPrinter = new ProgressPrinter(sampleSize);

        Helper.iterXZParallel(
            length,
            length,
            (x, z) -> {
                BlockPos2D input = new BlockPos2D(x, z);

                double output = base.getOutput(input);

                int index = base.getOutputInterval().mapToIntInterval(
                    output,
                    0,
                    counter.length() - 1
                );

                counter.incrementAndGet(index);

                progressPrinter.incrementAndTryPrintProgress();
            }
        );
    }

    int getMaxCount(){
        int result = counter.get(0);

        for (int i = 1; i < counter.length(); i++){
            if (counter.get(i) > result){
                result = counter.get(i);
            }
        }
        return result;
    }

    @Override
    public void paint(Graphics g) {


        Interval xPixelInterval = new Interval(5, getWidth() - 5);
        Interval yPixelInterval = new Interval(5, getHeight() - 5);

        Interval indexInterval = new Interval(0, counter.length()-1);
        Interval countInterval = new Interval(0, getMaxCount());

        for (int y = 0; y < getHeight(); y += 2){
            g.setColor(Color.RED);
            g.fillRect(
               (int)xPixelInterval.getMidPoint(),
                y,
                1,
                1
            );
        }

        for (int i = 0; i < counter.length(); i++){
            int count = counter.get(i);

            int xPixel = (int)indexInterval.mapInterval(i, xPixelInterval);
            int yPixel = (int)countInterval.mapInterval(countInterval.getMax() - count, yPixelInterval);

            g.setColor(Color.BLACK);
            g.fillRect(xPixel, yPixel, 1, 1);
        }
    }
}

