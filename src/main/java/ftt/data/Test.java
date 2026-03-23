package ftt.data;


import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class Test {

    public static void main(String args[]) {
        try {
            FTTConnector test = new FTTConnector("Random 2", FTTConnector.SERVER);
            test.addProperties("x 1s");
            test.addProperties("x");
            test.addProperties("line 1");
            test.addProperties("line 2");
            test.addProperties("line 3");

            AtomicInteger count = new AtomicInteger(0);
            AtomicInteger c2 = new AtomicInteger(0);
            AtomicBoolean rep = new AtomicBoolean(true);

            ScheduledExecutorService ex = Executors.newSingleThreadScheduledExecutor();
            ex.scheduleWithFixedDelay(() -> {
                test.send("x+",count.getAndIncrement());
            }, 0, 1000, TimeUnit.MILLISECONDS);
            ex.scheduleWithFixedDelay(() -> {
                if(Math.abs(c2.get()) > 50) rep.set(!rep.get());
                int x = rep.get()?c2.getAndIncrement():c2.getAndDecrement();
                test.send("x", x);
            }, 0, 60, TimeUnit.MILLISECONDS);
            ex.scheduleWithFixedDelay(() -> {
                test.send("line 1", new Random().nextInt(100)+200);
            }, 0, 40, TimeUnit.MILLISECONDS);
            ex.scheduleWithFixedDelay(() -> {
                test.send("line 2", new Random().nextInt(1000));
            }, 0, 40, TimeUnit.MILLISECONDS);
            ex.scheduleWithFixedDelay(() -> {
                test.send("line 3", count.doubleValue());
            }, 0, 40, TimeUnit.MILLISECONDS);
        } catch (Exception ex ) {
            ex.printStackTrace();
        }
    }
}
