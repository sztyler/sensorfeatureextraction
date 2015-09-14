package de.unima.ar.collector.features;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import de.unima.ar.collector.features.model.Features;
import de.unima.ar.collector.features.model.Window;


public class Utils
{
    public static void sleep(int i)
    {
        try {
            Thread.sleep(i);
        } catch(InterruptedException e) {
            e.printStackTrace();
        }
    }


    public static double[] toPrimitiveDouble(List<Double> values)
    {
        double[] result = new double[values.size()];
        Iterator<Double> iterator = values.iterator();

        for(int i = 0; i < result.length; i++) {
            result[i] = iterator.next().doubleValue();
        }

        return result;
    }


    public static long[] toPrimitiveLong(List<Long> values)
    {
        long[] result = new long[values.size()];
        Iterator<Long> iterator = values.iterator();

        for(int i = 0; i < result.length; i++) {
            result[i] = iterator.next().longValue();
        }

        return result;
    }


    public static double[] convertLongArrayToDoubleArray(long[] values)
    {
        double[] result = new double[values.length];

        for(int i = 0; i < result.length; i++) {
            result[i] = values[i];
        }

        return result;
    }


    // public static String csvToArff(String[] lines)
    // {
    // StringBuilder sb = new StringBuilder();
    //
    // sb.append("% 1. Motion Testdata" + System.lineSeparator() + "%" + System.lineSeparator());
    // sb.append("% 2. Sources:" + System.lineSeparator() + "%\t(a) Creator: Timo Sztyler" + System.lineSeparator());
    // sb.append("%\t(b) Date: " + System.currentTimeMillis() + System.lineSeparator() + "%" + System.lineSeparator());
    // sb.append("@RELATION motion" + System.lineSeparator() + System.lineSeparator());
    //
    // String[] header = lines[0].split(";");
    // for(String entry : header) {
    // if(entry.equals("class")) {
    // continue;
    // }
    //
    // sb.append("@ATTRIBUTE " + entry + " NUMERIC" + System.lineSeparator());
    // }
    // sb.append("@ATTRIBUTE class {" + Activity.allValues() + "}" + System.lineSeparator());
    //
    // sb.append(System.lineSeparator() + "@DATA" + System.lineSeparator());
    //
    // for(int i = 1; i < lines.length; i++) {
    // sb.append(lines[i].replace(";", ",") + System.lineSeparator());
    // }
    // sb.setLength(sb.length() - (System.lineSeparator()).length());
    //
    // return sb.toString();
    // }

    public static boolean isWindowEmpty(Window w)
    {
        Features feature = w.getFeatures();
        String line = feature.toString();
        String values = line.substring(line.indexOf(";"), line.lastIndexOf(";"));
        values = values.replace(";", "").replace("0.00000", "");

        return values.isEmpty();
    }


    public static String[] split(String s)
    {
        List<String> result = new ArrayList<>();

        int counter = 0;

        while(counter <= s.length()) {
            int pos = s.indexOf(",", counter);

            if(pos == -1) {
                pos = s.length();
            }

            String fragment = s.substring(counter, pos);
            result.add(fragment.substring(1, fragment.length() - 1));

            counter += fragment.length() + 1;
        }

        return result.toArray(new String[result.size()]);
    }


    public static boolean bet(double value, double... mm)
    {
        for(int i = 0; i < mm.length; i += 2) {
            if(value >= mm[i] && value < mm[i + 1]) { return true; }
        }
        return false;
    }
}