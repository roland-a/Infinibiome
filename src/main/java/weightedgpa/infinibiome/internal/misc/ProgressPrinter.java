package weightedgpa.infinibiome.internal.misc;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

public final class ProgressPrinter {
    private AtomicLong iterations = new AtomicLong();
    private final long total;

    private AtomicLong lastPrintTime = new AtomicLong();
    private AtomicBoolean printLock = new AtomicBoolean(false);

    public ProgressPrinter(long total) {
        assert total > 0;

        this.total = total;
    }

    public void incrementAndTryPrintProgress(){
        iterations.incrementAndGet();

        if (System.currentTimeMillis() < lastPrintTime.get() + 1000){
            return;
        }

        if (!printLock.compareAndSet(false, true)){
            return;
        }

        lastPrintTime.set(System.currentTimeMillis());

        double percent = iterations.get() / (double)total;

        System.out.printf("%.2f%%\n", percent * 100);

        printLock.set(false);
    }
}
