package de.unima.ar.collector.features.example;

import de.unima.ar.collector.features.FE;
import de.unima.ar.collector.features.model.Action;
import de.unima.ar.collector.features.model.Sensor;
import de.unima.ar.collector.features.model.Window;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

/**
 * Usage example how these libary has to be used to process acceleration data.
 *
 * @author Timo Sztyler
 * @version 30.09.2016
 */
public class UsageExample {
    public static void main(String[] args) throws Exception {
        FE fe = new FE(Sensor.ACCELERATION);
        fe.start();

        File file = new File("data/example/acc_data.csv");
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] fragments = line.split(",");
                long     timestamp = Long.parseLong(fragments[1]);
                float[]  values    = new float[]{Float.parseFloat(fragments[2]), Float.parseFloat(fragments[3]), Float.parseFloat(fragments[4])};

                fe.addAccelerationData(timestamp, values, Action.DEVICEPOSITIONS.SHIN, Action.HUMANPOSTURES.WALKING, "mall", "shopping");
            }
        }

        List<Window> result = new ArrayList<>();
        do {
            result.addAll(fe.getWindows());
            Thread.sleep(1000);
        } while (!fe.isIdle());
        for (Window window : result) {
            System.out.println(window);
        }

        String arffFile = fe.getWindowsAsARFF(Action.TYPE.HUMANPOSTURES);
        System.out.println(arffFile);

        fe.stop();
        fe.clear();
    }
}
