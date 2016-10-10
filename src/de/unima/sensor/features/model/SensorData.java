package de.unima.sensor.features.model;

/**
 * Container class that covers a single record and a corresponding label of a specific sensor
 *
 * @author Timo Sztyler
 * @version 10.10.2016
 */
public class SensorData {
    private Sensor   sensor;
    private long     timestamp;
    private String[] labels;
    private float[]  values;


    public SensorData(Sensor sensor) {
        this.sensor = sensor;
    }


    public void addLabels(String... labels) {
        this.labels = labels;
    }


    public void addValues(long timestamp, float[] values) {
        this.timestamp = timestamp;
        this.values = values;
    }


    public Sensor getSensor() {
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
}