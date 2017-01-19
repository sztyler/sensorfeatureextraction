package de.unima.sensor.features;

import de.unima.sensor.features.model.Features;
import de.unima.sensor.features.model.SensorData;
import de.unima.sensor.features.model.SensorType;
import de.unima.sensor.features.model.Window;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * This class covers several methods concerning type conversion.
 *
 * @author Timo Sztyler
 * @version 24.11.2016
 */
public class Utils {
    public static void sleep(int i) {
        try {
            Thread.sleep(i);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }


    public static double[] toPrimitiveDouble(List<Double> values) {
        double[]         result   = new double[values.size()];
        Iterator<Double> iterator = values.iterator();

        for (int i = 0; i < result.length; i++) {
            result[i] = iterator.next();
        }

        return result;
    }


    public static long[] toPrimitiveLong(List<Long> values) {
        long[]         result   = new long[values.size()];
        Iterator<Long> iterator = values.iterator();

        for (int i = 0; i < result.length; i++) {
            result[i] = iterator.next();
        }

        return result;
    }


    public static double[] convertLongArrayToDoubleArray(long[] values) {
        double[] result = new double[values.length];

        for (int i = 0; i < result.length; i++) {
            result[i] = values[i];
        }

        return result;
    }


    public static boolean isWindowEmpty(Window w) {
        boolean result = true;

        Set<SensorType> sensorTypes = w.getSensorTypes();
        for (SensorType sensorType : sensorTypes) {
            Features feature = w.getFeatures(sensorType);
            String   line    = feature.toString();
            String   values  = line.substring(line.indexOf(";"), line.lastIndexOf(";"));
            values = values.replace(";", "").replace("0.00000", "");

            result = result && values.isEmpty();
        }

        return result;
    }


    public static String[] split(String s) {
        List<String> result = new ArrayList<>();

        int counter = 0;

        while (counter <= s.length()) {
            int pos = s.indexOf(",", counter);

            if (pos == -1) {
                pos = s.length();
            }

            String fragment = s.substring(counter, pos);
            result.add(fragment.substring(1, fragment.length() - 1));

            counter += fragment.length() + 1;
        }

        return result.toArray(new String[result.size()]);
    }


    public static boolean bet(double value, double... mm) {
        for (int i = 0; i < mm.length; i += 2) {
            if (value >= mm[i] && value < mm[i + 1]) { return true; }
        }
        return false;
    }

    public static List<SensorData> deepCopy(List<SensorData> original) {
        List<SensorData> clone = new ArrayList<>(original.size());

        for (SensorData sd : original) {
            clone.add(sd.clone());
        }

        return clone;
    }
}