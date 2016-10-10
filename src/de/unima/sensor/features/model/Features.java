package de.unima.sensor.features.model;

import de.unima.sensor.features.Utils;
import de.unima.sensor.features.mathutils.Measures;

import java.text.DecimalFormat;
import java.util.*;

/**
 * Feature Container. This object stores for a specific window all computed features, separated by axes.
 *
 * @author Timo Sztyler
 * @version 30.09.2016
 */
public class Features {
    private int id;

    // time-domain
    private Map<String, Double> meanValue;
    private Map<String, Double> varianceValue;
    private Map<String, Double> standardDeviationValue;
    private Map<String, Double> medianValue;
    private Map<String, Double> iqrValue;
    private Map<String, Double> madValue;
    private Map<String, Double> kurtosisValue;
    private Map<String, Double> correlationCoefficientValue;
    private Map<String, Double> gravityMeanValue;                                                                                                                                                          // result of a low pass filter
    private Map<String, Double> orientationValue;                                                                                                                                                          // azimut, pitch, roll
    private Map<String, Double> entropyValueTime;

    // frequency-domain
    private Map<String, Double> energyValue;
    private Map<String, Double> entropyValueFrequency;
    private Map<String, Double> meanDcValue;

    // high-level features
    private int discretizedOrientation;


    public Features(int id, Set<Attribute> data) {
        this.id = id;

        this.meanValue = new TreeMap<>();
        this.varianceValue = new TreeMap<>();
        this.standardDeviationValue = new TreeMap<>();
        this.medianValue = new TreeMap<>();
        this.iqrValue = new TreeMap<>();
        this.madValue = new TreeMap<>();
        this.kurtosisValue = new TreeMap<>();
        this.correlationCoefficientValue = new TreeMap<>();
        this.gravityMeanValue = new TreeMap<>();
        this.orientationValue = new TreeMap<>();
        this.entropyValueTime = new TreeMap<>();

        this.energyValue = new TreeMap<>();
        this.entropyValueFrequency = new TreeMap<>();
        this.meanDcValue = new TreeMap<>();

        this.discretizedOrientation = -1;

        calc(data);
    }


    private void calc(Set<Attribute> data)  // TODO this method assumes the following order: 0, 1, 2, attr_time
    {
        List<Attribute> sorted = new ArrayList<>(data);
        Collections.sort(sorted);

        int size = sorted.size();
        if (size == 0) { return; }

        double[][] accelerations = new double[size - 1][];
        double[][] gravities     = new double[size - 1][];

        for (int i = 0; i < sorted.size(); i++) {
            Attribute attr = sorted.get(i);

            if (!attr.getAttribute().equals("attr_time")) {
                accelerations[i] = Utils.toPrimitiveDouble(attr.getEntries().getRight().getLeft());
                gravities[i] = Utils.toPrimitiveDouble(attr.getEntries().getRight().getRight());
            }
        }

        // first
        for (int i = 0; i < accelerations.length; i++) {
            String label = sorted.get(i).getAttribute();

            // time-domain
            this.meanValue.put(label, Measures.meanValue(accelerations[i]));
            this.varianceValue.put(label, Measures.varianceValue(accelerations[i], this.meanValue.get(label), true));
            this.standardDeviationValue.put(label, Measures.standardDeviationValue(this.varianceValue.get(label)));
            this.medianValue.put(label, Measures.medianValue(accelerations[i]));
            this.iqrValue.put(label, Measures.iqrValue(accelerations[i]));
            this.madValue.put(label, Measures.matValue(accelerations[i], this.meanValue.get(label)));
            this.kurtosisValue.put(label, Measures.kurtosisValue(accelerations[i], this.meanValue.get(label)));
            this.gravityMeanValue.put(label, Measures.gravityMeanValue(gravities[i]));
            this.entropyValueTime.put(label, Measures.entropyValueTime(accelerations[i]));

            // frequency-domain
            double[] fftValues = Measures.fft(accelerations[i]);
            this.energyValue.put(label, Measures.energyValue(fftValues));
            this.entropyValueFrequency.put(label, Measures.entropyValueFrequency(fftValues));
            this.meanDcValue.put(label, Measures.meanDcValue(fftValues));
            // TODO DCT
        }

        // second - the following features require that the previous are already calculated for all axes
        for (int i = 0; i < accelerations.length; i++) {
            String label = sorted.get(i).getAttribute();

            // time-domain
            this.correlationCoefficientValue.put(label, Measures.correlationCoefficientValue(accelerations[i], accelerations[(i + 1) % accelerations.length], this.meanValue.get(label), this.meanValue.get(sorted.get((i + 1) % accelerations.length).getAttribute())));
            this.orientationValue.put(label, Measures.orientationValue(this.gravityMeanValue.get(sorted.get((i + 1) % gravities.length).getAttribute()), this.gravityMeanValue.get(label)));
        }

        // high-level features
        this.discretizedOrientation = Measures.discretizedOrientationValue(Utils.toPrimitiveDouble(new ArrayList<Double>(this.orientationValue.values())));
    }


    public int getID() {
        return this.id;
    }


    public Map<String, Double> getMeanValue() {
        return this.meanValue;
    }


    public Map<String, Double> getVarianceValue() {
        return this.varianceValue;
    }


    public Map<String, Double> getStandardDeviationValue() {
        return this.standardDeviationValue;
    }


    public Map<String, Double> getMedianValue() {
        return this.medianValue;
    }


    public Map<String, Double> getIQRValue() {
        return this.iqrValue;
    }


    public Map<String, Double> getMADValue() {
        return this.madValue;
    }


    public Map<String, Double> getKurtosisValue() {
        return this.kurtosisValue;
    }


    public Map<String, Double> getCorrelationCoefficientValue() {
        return this.correlationCoefficientValue;
    }


    public Map<String, Double> getGravityValue() {
        return this.gravityMeanValue;
    }


    public Map<String, Double> getEnergyValue() {
        return this.energyValue;
    }


    public Map<String, Double> getEntropyFrequencyValue() {
        return this.entropyValueFrequency;
    }


    public Map<String, Double> getMeanDCValue() {
        return this.meanDcValue;
    }


    public Map<String, Double> getOrientationValue() {
        return this.orientationValue;
    }


    public int getDiscretizedOrientation() {
        return this.discretizedOrientation;
    }


    public Map<String, Double> getEntropyTimeValue() {
        return this.entropyValueTime;
    }


    public NavigableMap<String, Map<String, Double>> get() {
        NavigableMap<String, Map<String, Double>> data = new TreeMap<>();

        data.put("Mean", meanValue);
        data.put("Variance", varianceValue);
        data.put("SD", standardDeviationValue);
        data.put("Median", medianValue);
        data.put("IQR", iqrValue);
        data.put("MAD", madValue);
        data.put("Kurtosis", kurtosisValue);
        data.put("CC", correlationCoefficientValue);
        data.put("Gravity", gravityMeanValue);
        data.put("AOrient", orientationValue);
        data.put("Energy", energyValue);
        data.put("EntropyF", entropyValueFrequency);
        data.put("MeanDC", meanDcValue);
        data.put("EntropyT", entropyValueTime);

        Map<String, Double> test = new TreeMap<>();
        test.put("0", (double) this.discretizedOrientation);
        test.put("1", (double) this.discretizedOrientation);
        test.put("2", (double) this.discretizedOrientation);
        data.put("Aaa", test);

        return data;
    }


    public String getHeader() {
        StringBuilder sb = new StringBuilder();

        String[] attrs = new String[]{"id", "mean", "sd", "variance", "median", "iqr", "mad", "kurtosis", "energy", "entropyF", "meanDC", "cc", "gravity", "orientation", "entropyT", "orientationDZ"};
        for (String attr : attrs) {
            if (attr.equals("orientationDZ") || attr.equals("id")) {
                sb.append(attr).append(", ");
                continue;
            }

            sb.append(attr).append("_x, ").append(attr).append("_y, ").append(attr).append("_z, ");
        }

        return sb.delete(sb.length() - 2, sb.length()).toString();
    }


    @Override
    public String toString() {
        DecimalFormat df = new DecimalFormat("####0.00000");

        StringBuilder sb = new StringBuilder();
        sb.append(id).append(";");

        for (Double value : meanValue.values()) {
            sb.append(df.format(value)).append(";");
        }

        for (Double value : standardDeviationValue.values()) {
            sb.append(df.format(value)).append(";");
        }

        for (Double value : varianceValue.values()) {
            sb.append(df.format(value)).append(";");
        }

        for (Double value : medianValue.values()) {
            sb.append(df.format(value)).append(";");
        }

        for (Double value : iqrValue.values()) {
            sb.append(df.format(value)).append(";");
        }

        for (Double value : madValue.values()) {
            sb.append(df.format(value)).append(";");
        }

        for (Double value : kurtosisValue.values()) {
            sb.append(df.format(value)).append(";");
        }

        for (Double value : energyValue.values()) {
            sb.append(df.format(value)).append(";");
        }

        for (Double value : entropyValueFrequency.values()) {
            sb.append(df.format(value)).append(";");
        }

        for (Double value : meanDcValue.values()) {
            sb.append(df.format(value)).append(";");
        }

        for (Double value : correlationCoefficientValue.values()) {
            sb.append(df.format(value)).append(";");
        }

        for (Double value : gravityMeanValue.values()) {
            sb.append(df.format(value)).append(";");
        }

        for (Double value : orientationValue.values()) {
            sb.append(df.format(value)).append(";");
        }

        for (Double value : entropyValueTime.values()) {
            sb.append(df.format(value)).append(";");
        }

        sb.append(discretizedOrientation).append(";");

        sb = sb.deleteCharAt(sb.length() - 1);

        return sb.toString();
    }
}