package weightedgpa.infinibiome.internal.misc;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public final class DeadlockDetector {
    private static final boolean ENABLED = false;

    private final long checkInterval = TimeUnit.SECONDS.toMillis(5);
    private long timeLastChecked = 0;
    private final Map<Long, CurrentInfo> map = new HashMap<>();

    private final Thread thread = new Thread(
        () -> {
            if (!ENABLED) return;

            while (true){
                check();
            }
        }
    );

    public DeadlockDetector(){
        if (ENABLED){
            thread.start();
        }
    }

    private void check(){
        if (!ENABLED) return;

        if (timeLastChecked + checkInterval > System.currentTimeMillis()) return;

        for (CurrentInfo c: map.values()){
            c.alertIfDeadlock();
        }
        timeLastChecked = System.currentTimeMillis();
    }

    public void setCurrentRunningGenerator(Object obj){
        if (!ENABLED) return;

        Thread t = Thread.currentThread();

        assert map.get(t.getId()) == null;

        map.put(
            t.getId(),
            new CurrentInfo(obj.getClass())
        );

    }

    public void currentGeneratorFinished(){
        if (!ENABLED) return;

        map.remove(Thread.currentThread().getId());
    }

    @Override
    protected void finalize() {
        if (!ENABLED) return;

        thread.interrupt();
    }

    private class CurrentInfo{
        private final long MAX_TIME = TimeUnit.SECONDS.toMillis(5);
        private final Class<?> runningGen;
        private final long time;

        CurrentInfo(Class<?> runningGen){
            this.runningGen = runningGen;
            this.time = System.currentTimeMillis();
        }

        void alertIfDeadlock(){
            if (time + MAX_TIME > System.currentTimeMillis()) return;

            System.out.println(runningGen + " may have deadlocked the world");
        }
    }

}
