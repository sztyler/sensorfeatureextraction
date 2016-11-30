package de.unima.sensor.features.test;

import de.unima.sensor.features.FactoryProperties;
import de.unima.sensor.features.FeatureFactory;
import de.unima.sensor.features.model.Attribute;
import de.unima.sensor.features.model.Pair;
import de.unima.sensor.features.model.SensorType;
import de.unima.sensor.features.model.Window;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

import static org.junit.Assert.assertEquals;

public class WindowTest {
    @Test
    public void WindowOverlap() {
        // TODO
        // overlap = 0.0
        // overlap = 0.5
        // overlap = 0.25
        // overlap = 0.75
    }

    @Test
    public void InputOutputTest1() throws IOException, InterruptedException {
        FeatureFactory ff = new FeatureFactory();

        FactoryProperties.WINDOW_SIZE = 1000;
        FactoryProperties.WINDOW_OVERLAP = true;
        FactoryProperties.WINDOW_OVERLAP_SIZE = 0.5;

        ff.start();

        File fileAcc = new File("data/test/acc_walking_shin.csv");
        try (BufferedReader br = new BufferedReader(new FileReader(fileAcc))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] fragments = line.split(",");
                long     timestamp = Long.parseLong(fragments[1]);
                float[]  values    = new float[]{Float.parseFloat(fragments[2]), Float.parseFloat(fragments[3]), Float.parseFloat(fragments[4])};

                ff.addRecord(SensorType.ACCELEROMETER, timestamp, values, "walking", "shin");
            }
        }

        do {
            Thread.sleep(1000);
        } while (!ff.isIdle());

        int numWindows = ff.getWindows().size();
        assertEquals("", 843.0, numWindows, 0);

        int numWindows2 = ff.getAllWindows().size();
        assertEquals("", numWindows2, numWindows, 0);

        ff.stop();
        ff.clear();
    }

    @Test
    public void InputOutputTest2() throws IOException, InterruptedException {
        for(int i=0; i<100; i++) {

            FeatureFactory ff = new FeatureFactory();

            FactoryProperties.WINDOW_SIZE = 1000;
            FactoryProperties.WINDOW_OVERLAP = true;
            FactoryProperties.WINDOW_OVERLAP_SIZE = 0.5;

            ff.start();

            File fileAcc = new File("data/test/acc_walking_shin.csv");
            try (BufferedReader br = new BufferedReader(new FileReader(fileAcc))) {
                String line;
                while ((line = br.readLine()) != null) {
                    String[] fragments = line.split(",");
                    long     timestamp = Long.parseLong(fragments[1]);
                    float[]  values    = new float[]{Float.parseFloat(fragments[2]), Float.parseFloat(fragments[3]), Float.parseFloat(fragments[4])};

                    ff.addRecord(SensorType.ACCELEROMETER, timestamp, values, "walking", "shin");
                }
            }

            do {
                Thread.sleep(500);
            } while (!ff.isIdle());

            Map<String, List<Double>> everything = new HashMap<>();

            NavigableMap<Long, Window> windows = ff.getWindows();
            int                        cou     = -1;
            int                        cou2    = 0;
            for (Window window : windows.values()) {
                Set<SensorType> sensorTypes = window.getSensorTypes();

                assertEquals("", 1, sensorTypes.size(), 0);

                cou++;
                if (cou % 2 != 0) {
                    continue;
                }
                cou2++;

                for (SensorType st : sensorTypes) {
                    Set<Attribute> attributes = window.getEntries(st);

                    assertEquals("", 4, attributes.size(), 0);

                    for (Attribute attr : attributes) {
                        if (!everything.containsKey(attr.getAttribute())) {
                            everything.put(attr.getAttribute(), new ArrayList<Double>());
                        }

                        Pair<List<Long>, Pair<List<Double>, List<Double>>> values = attr.getEntries();
                        everything.get(attr.getAttribute()).addAll(values.getRight().getLeft());
                    }
                }
            }

            assertEquals("", 4, everything.size(), 0);

            for (List<Double> value : everything.values()) {
                assertEquals("", 21078, value.size(), 0);
            }

            ff.stop();
            // ff.clear();  // TODO
        }
    }
}
