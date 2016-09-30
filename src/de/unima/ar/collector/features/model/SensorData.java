package de.unima.ar.collector.features.model;

/**
 * Container class that covers a single record and a corresponding label of a specific sensor
 *
 * @author Timo Sztyler
 * @version 30.09.2016
 */
public class SensorData {
    private Sensor  sensor;
    private long    timestamp;
    private Action  action;
    private float[] values;


    public SensorData(Sensor sensor) {
        this.sensor = sensor;
    }


    public void addAction(Action action) {
        this.action = action;
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


    public Action getAction() {
        return this.action;
    }


    public float[] getValues() {
        return this.values;
    }
}