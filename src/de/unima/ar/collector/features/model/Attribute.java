package de.unima.ar.collector.features.model;

import de.unima.ar.collector.features.mathutils.LowPassFilter;

import java.util.ArrayList;
import java.util.List;

/**
 * Container that covers raw unprocessed but parsed sensor data. This containers are created by the attribute manager
 * to store the read sensor data. Subsequntly, these objects are pushed to the window manager to compute the
 * corresponding features.
 *
 * @author Timo Sztyler
 * @version 30.09.2016
 */
public class Attribute implements Comparable<Attribute> {
    private Sensor        sensor;
    private String        attribute;
    private long          start;     // ursprünglicher globaler startzeitpunkt dieser Datenreihe
    private List<Long>    times;     // timestamp
    private List<Double>  values;    // value
    private List<Double>  gravities; // gravity values
    private LowPassFilter lpf;

    private final Object valuesLock;


    public Attribute(Sensor sensor, String attribute, boolean lpf) {
        this.sensor = sensor;
        this.attribute = attribute;
        this.times = new ArrayList<>();
        this.values = new ArrayList<>();
        this.gravities = new ArrayList<>();
        this.start = 0;
        this.valuesLock = new Object();
        this.lpf = null;

        if (lpf) {
            this.lpf = new LowPassFilter();
        }
    }


    public void addValue(long timestamp, double value) {
        synchronized (this.valuesLock) {
            if (this.times.size() == 0) {
                this.start = timestamp;
            }

            this.times.add(timestamp - this.start);

            if (lpf == null) {
                this.values.add(value);
                this.gravities.add(Double.NaN);
            } else {
                Pair<double[], double[]> lpfValues = lpf.addSamples(new double[]{value}, (timestamp - start));
                this.values.add(lpfValues.getLeft()[0]);
                this.gravities.add(lpfValues.getRight()[0]);
            }
        }
    }


    public void addValues(List<Long> times, List<Double> values, List<Double> gravities) {
        if (times.size() == 0 || values.size() == 0) { return; }

        synchronized (this.valuesLock) {
            this.values.addAll(values);
            this.gravities.addAll(gravities);

            if (this.times.size() == 0) {
                this.start = times.get(0);
            }

            for (Long time : times) {
                this.times.add(time - this.start);
            }
        }
    }


    public Pair<List<Long>, Pair<List<Double>, List<Double>>> getEntries(int startPos, int endPos) {
        List<Long>   times     = new ArrayList<>();
        List<Double> values    = new ArrayList<>();
        List<Double> gravities = new ArrayList<>();

        if (startPos == -1 || endPos == -1) {
            return new Pair<>(times, new Pair<>(values, gravities));
        }

        synchronized (this.valuesLock) {
            times.addAll(this.times.subList(startPos, endPos));
            values.addAll(this.values.subList(startPos, endPos));
            gravities.addAll(this.gravities.subList(startPos, endPos));

            return new Pair<>(times, new Pair<>(values, gravities));
        }
    }


    public Pair<List<Long>, Pair<List<Double>, List<Double>>> getEntries() {
        synchronized (this.valuesLock) {
            return new Pair<>(this.times, new Pair<>(this.values, this.gravities));
        }
    }


    public double getFrequency() {
        synchronized (this.valuesLock) {
            long duration = this.times.get(this.times.size() - 1);
            return (1000.d * (double) this.times.size()) / (double) duration;
        }
    }


    public int getSize() {
        synchronized (this.valuesLock) {
            return this.values.size();
        }
    }


    public Long getLastTimestamp() {
        synchronized (this.valuesLock) {
            return this.times.get(this.times.size() - 1);
        }
    }


    public Sensor getSensor() {
        return this.sensor;
    }


    public String getAttribute() {
        return this.attribute;
    }


    public long getStartTimePoint() {
        return this.start;
    }


    @Override
    public int hashCode() {
        String uuid = this.getSensor().toString().concat(this.getAttribute());
        return uuid.hashCode();
    }


    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Attribute)) { return false; }

        Attribute a = (Attribute) o;

        return this.hashCode() == a.hashCode();
    }


    @Override
    public String toString() {
        return this.getAttribute() + ": " + this.getSize() + " (" + this.getSensor().toString() + ") ~" + this.getFrequency() + "Hz";
    }


    @Override
    public int compareTo(Attribute o) {
        return this.getAttribute().compareTo(o.getAttribute());
    }
}