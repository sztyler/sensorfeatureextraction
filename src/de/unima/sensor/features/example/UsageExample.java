package de.unima.sensor.features.example;

import de.unima.sensor.features.FeatureFactory;
import de.unima.sensor.features.model.Sensor;
import de.unima.sensor.features.model.Window;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

/**
 * Usage example how these libary has to be used to process acceleration data.
 *
 * @author Timo Sztyler
 * @version 10.10.2016
 */
public class UsageExample {
    public static void main(String[] args) throws Exception {
        FeatureFactory ff = new FeatureFactory(Sensor.ACCELERATION);
        ff.start();

        File file = new File("data/example/acc_data.csv");
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] fragments = line.split(",");
                long     timestamp = Long.parseLong(fragments[1]);
                float[]  values    = new float[]{Float.parseFloat(fragments[2]), Float.parseFloat(fragments[3]), Float.parseFloat(fragments[4])};

                ff.addAccelerationData(timestamp, values, "walking", "shin", "mall", "shopping");
            }
        }

        List<Window> result = new ArrayList<>();
        do {
            result.addAll(ff.getWindows());
            Thread.sleep(1000);
        } while (!ff.isIdle());
        for (Window window : result) {
            System.out.println(window);
        }

        String arffFile = ff.getWindowsAsARFF(0);
        System.out.println(arffFile);

        ff.stop();
        ff.clear();
    }
}
