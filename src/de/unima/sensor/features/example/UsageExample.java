package de.unima.sensor.features.example;

import de.unima.sensor.features.FeatureFactory;
import de.unima.sensor.features.model.SensorType;
import de.unima.sensor.features.model.Window;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;
import java.util.NavigableMap;
import java.util.TreeMap;

/**
 * Usage example how these libary has to be used to process acceleration data.
 *
 * @author Timo Sztyler
 * @version 29.11.2016
 */
public class UsageExample {
    public static void main(String[] args) throws Exception {
        long start = System.currentTimeMillis();

        FeatureFactory ff = new FeatureFactory();
        ff.start();

        File fileAcc = new File("data/example/acc_walking_shin.csv");
        try (BufferedReader br = new BufferedReader(new FileReader(fileAcc))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] fragments = line.split(",");
                long     timestamp = Long.parseLong(fragments[1]);
                float[]  values    = new float[]{Float.parseFloat(fragments[2]), Float.parseFloat(fragments[3]), Float.parseFloat(fragments[4])};

                ff.addRecord(SensorType.ACCELEROMETER, timestamp, values, "walking", "shin", "mall", "shopping");
            }
        }

        File fileMag = new File("data/example/mag_walking_shin.csv");
        try (BufferedReader br = new BufferedReader(new FileReader(fileMag))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] fragments = line.split(",");
                long     timestamp = Long.parseLong(fragments[1]);
                float[]  values    = new float[]{Float.parseFloat(fragments[2]), Float.parseFloat(fragments[3]), Float.parseFloat(fragments[4])};

                ff.addRecord(SensorType.MAGNETOMETER, timestamp, values, "walking", "shin", "mall", "shopping");
            }
        }

        File fileGyr = new File("data/example/gyr_walking_shin.csv");
        try (BufferedReader br = new BufferedReader(new FileReader(fileGyr))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] fragments = line.split(",");
                long     timestamp = Long.parseLong(fragments[1]);
                float[]  values    = new float[]{Float.parseFloat(fragments[2]), Float.parseFloat(fragments[3]), Float.parseFloat(fragments[4])};

                ff.addRecord(SensorType.GYROSCOPE, timestamp, values, "walking", "shin", "mall", "shopping");
            }
        }


        NavigableMap<Long, Window> result = new TreeMap<>();
        do {
            result.putAll(ff.getWindows());
        } while (!ff.isIdle());

        result.putAll(ff.getWindows());
        for (Window window : result.values()) {
            System.out.println(window);
        }

        //String arffFile = ff.getWindowsAsARFF(0);
        //System.out.println(arffFile);

        ff.stop();
        ff.clear();

        System.out.println(System.currentTimeMillis()-start);
    }
}
