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
    public void WindowOverlap() throws IOException, InterruptedException {
        double[] overlap = new double[]{0.1, 0.5, 0.25, 0.75, 0.11};
        long[]   size    = new long[]{100, 250, 500, 1000, 1371, 9999};
        int[]    results = new int[]{4689, 8439, 5627, 16877, 4742, 1876, 3375, 2245, 6696, 1893, 938, 1687, 1125, 3373, 949, 469, 843, 563, 1685, 475, 342, 615, 410, 1228, 346, 47, 84, 56, 166, 48};
        //long[]   size    = new long[]{100, 250, 500, 1000, 1371, 1, 9999};
        //int[]    results = new int[]{4689, 8439, 5627, 16877, 4742, 1876, 3375, 2245, 6696, 1893, 938, 1687, 1125, 3373, 949, 469, 843, 563, 1685, 475, 342, 615, 410, 1228, 346, 421982, 421982, 421982, 421982, 421982, 47, 84, 56, 166, 48};

        for (int i = 0; i < size.length; i++) {
            for (int j = 0; j < overlap.length; j++) {
                FeatureFactory ff = new FeatureFactory();

                FactoryProperties.WINDOW_SIZE = size[i];
                FactoryProperties.WINDOW_OVERLAP = true;
                FactoryProperties.WINDOW_OVERLAP_SIZE = overlap[j];

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
                assertEquals("", results[j + (i * overlap.length)], numWindows, 0);

                ff.stop();
                ff.clear();
            }
        }
    }

    @Test
    public void GetWindowMethods() throws IOException, InterruptedException {
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
    public void ReadAttributeValues1() throws IOException, InterruptedException {
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
        for (Window window : windows.values()) {
            Set<SensorType> sensorTypes = window.getSensorTypes();

            assertEquals("", 1, sensorTypes.size(), 0);

            cou++;
            if (cou % 2 != 0) {
                continue;
            }

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
        ff.clear();
    }

    @Test
    public void ReadAttributeValues2() throws IOException, InterruptedException {
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

        do {
            Thread.sleep(500);
        } while (!ff.isIdle());

        Map<SensorType, Map<String, List<Double>>> everything = new HashMap<>();

        NavigableMap<Long, Window> windows = ff.getWindows();
        int                        cou     = -1;
        for (Window window : windows.values()) {
            Set<SensorType> sensorTypes = window.getSensorTypes();

            cou++;
            if (cou % 2 != 0) {
                continue;
            }

            for (SensorType st : sensorTypes) {
                Set<Attribute> attributes = window.getEntries(st);

                assertEquals("", 4, attributes.size(), 0);

                for (Attribute attr : attributes) {
                    if (!everything.containsKey(st)) {
                        everything.put(st, new HashMap<String, List<Double>>());
                    }

                    if (!everything.get(st).containsKey(attr.getAttribute())) {
                        everything.get(st).put(attr.getAttribute(), new ArrayList<Double>());
                    }

                    Pair<List<Long>, Pair<List<Double>, List<Double>>> values = attr.getEntries();
                    everything.get(st).get(attr.getAttribute()).addAll(values.getRight().getLeft());
                }
            }
        }

        assertEquals("", 3, everything.size(), 0);

        Map<SensorType, Integer> result = new HashMap<>();
        result.put(SensorType.ACCELEROMETER, 21078);
        result.put(SensorType.GYROSCOPE, 31946);
        result.put(SensorType.MAGNETOMETER, 31946);

        for (SensorType st : everything.keySet()) {
            assertEquals("", 4, everything.get(st).keySet().size(), 0);
            for (List<Double> value : everything.get(st).values()) {
                assertEquals("", result.get(st), value.size(), 0);
            }
        }

        ff.stop();
        ff.clear();
    }

    @Test
    public void NoOverlap() throws IOException, InterruptedException {
        long[] size   = new long[]{100, 250, 500, 1000, 1371, 9999};
        int[]  result = new int[]{4220, 1688, 844, 422, 308, 43};

        for (int i = 0; i < size.length; i++) {
            FeatureFactory ff = new FeatureFactory();

            FactoryProperties.WINDOW_SIZE = size[i];
            FactoryProperties.WINDOW_OVERLAP = false;

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
            assertEquals("", result[i], numWindows, 0);

            ff.stop();
            ff.clear();
        }
    }

    @Test
    public void multipleSensorTypeDataOverlap1() throws IOException, InterruptedException {
        double[] overlap = new double[]{0.1, 0.5, 0.25, 0.75, 0.11};
        long[]   size    = new long[]{100, 250, 500, 1000, 1371, 9999};
        int[]    results = new int[]{7107, 12791, 8529, 25581, 7187, 2844, 5116, 3403, 10150, 2869, 1422, 2558, 1706, 5114, 1438, 711, 1279, 854, 2556, 720, 519, 933, 622, 1863, 525, 72, 128, 86, 254, 73};

        for (int i = 0; i < size.length; i++) {
            for (int j = 0; j < overlap.length; j++) {
                FeatureFactory ff = new FeatureFactory();

                FactoryProperties.WINDOW_SIZE = size[i];
                FactoryProperties.WINDOW_OVERLAP = true;
                FactoryProperties.WINDOW_OVERLAP_SIZE = overlap[j];

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

                do {
                    Thread.sleep(1000);
                } while (!ff.isIdle());

                int numWindows = ff.getWindows().size();

                if (numWindows == results[j + (i * overlap.length)] + 1) {
                    System.out.println(ff.getAllWindows());
                }

                assertEquals("", results[j + (i * overlap.length)], numWindows, 0);

                ff.stop();
                ff.clear();
            }
        }
    }

    @Test
    public void multipleSensorTypeDataOverlap2() throws IOException, InterruptedException {
        double[] overlap = new double[]{0.1, 0.5, 0.25, 0.75, 0.11};
        long[]   size    = new long[]{100, 250, 500, 1000, 1371, 9999};
        int[]    results = new int[]{7107, 12791, 8528, 25580, 7187, 2843, 5116, 3402, 10149, 2868, 1422, 2558, 1706, 5114, 1438, 711, 1279, 853, 2556, 719, 519, 932, 622, 1862, 524, 71, 127, 85, 253, 72};

        for (int i = 0; i < size.length; i++) {
            for (int j = 0; j < overlap.length; j++) {
                FeatureFactory ff = new FeatureFactory();

                FactoryProperties.WINDOW_SIZE = size[i];
                FactoryProperties.WINDOW_OVERLAP = true;
                FactoryProperties.WINDOW_OVERLAP_SIZE = overlap[j];

                ff.start();

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

                do {
                    Thread.sleep(1000);
                } while (!ff.isIdle());

                int numWindows = ff.getWindows().size();

                assertEquals("", results[j + (i * overlap.length)], numWindows, 0);

                ff.stop();
                ff.clear();
            }
        }
    }

    @Test
    public void multipleSensorTypeDataOverlap3() throws IOException, InterruptedException {
        double[] overlap = new double[]{0.1, 0.5, 0.25, 0.75, 0.11};
        long[]   size    = new long[]{100, 250, 500, 1000, 1371, 9999};
        int[]    results = new int[]{7107, 12791, 8528, 25580, 7187, 2843, 5116, 3402, 10149, 2868, 1422, 2558, 1706, 5114, 1438, 711, 1279, 853, 2556, 719, 519, 932, 622, 1862, 524, 71, 127, 85, 253, 72};

        for (int i = 0; i < size.length; i++) {
            for (int j = 0; j < overlap.length; j++) {
                FeatureFactory ff = new FeatureFactory();

                FactoryProperties.WINDOW_SIZE = size[i];
                FactoryProperties.WINDOW_OVERLAP = true;
                FactoryProperties.WINDOW_OVERLAP_SIZE = overlap[j];

                ff.start();

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

                do {
                    Thread.sleep(1000);
                } while (!ff.isIdle());

                int numWindows = ff.getWindows().size();

                assertEquals("", results[j + (i * overlap.length)], numWindows, 0);

                ff.stop();
                ff.clear();
            }
        }
    }
}
