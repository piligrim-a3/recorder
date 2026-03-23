package ftt.data;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class TestChar {

    public static void main(String args[]) {
        try {
            FTTConnector test = new FTTConnector("Char client", FTTConnector.CLIENT);

            test.find();


            ScheduledExecutorService ex = Executors.newSingleThreadScheduledExecutor();
            ex.scheduleWithFixedDelay(() -> {

                test.getDetective().getClients().forEach(client -> {
                    System.out.println(client.getName());
                    System.out.println(client.getProperties());
                    if(client.getName().startsWith("recorder")) {
                        test.requestChartData(client);
                    }
                });

            }, 0, 1000, TimeUnit.MILLISECONDS);

        } catch (Exception ex ) {
            ex.printStackTrace();
        }
    }

}
