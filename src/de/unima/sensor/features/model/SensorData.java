package de.unima.sensor.features.model;

/**
 * Container class that covers a single record and a corresponding label of a specific sensor
 *
 * @author Timo Sztyler
 * @version 24.11.2016
 */
public class SensorData implements Cloneable {
    private SensorType sensor;
    private long       timestamp;
    private String[]   labels;
    private float[]    values;


    public SensorData(SensorType sensor) {
        this.sensor = sensor;
    }


    public void addLabels(String... labels) {
        this.labels = labels;
    }


    public void addValues(long timestamp, float[] values) {
        this.timestamp = timestamp;
        this.values = values;
    }


    public SensorType getSensor() {
        return this.sensor;
    }


    public long getTimestamp() {
        return this.timestamp;
    }


    public String[] getLabels() {
        return this.labels;
    }


    public float[] getValues() {
        return this.values;
    }

    public SensorData clone() {
        try {
            super.clone();
        } catch (CloneNotSupportedException e) {
            return null;
        }

        SensorData clone = new SensorData(this.sensor);

        float[] copyValues = new float[this.values.length];
        System.arraycopy(this.values, 0, copyValues, 0, this.values.length);
        clone.addValues(this.timestamp, copyValues);

        String[] copyLabels = new String[this.labels.length];
        System.arraycopy(this.labels, 0, copyLabels, 0, this.labels.length);
        clone.addLabels(copyLabels);

        return clone;
    }
}