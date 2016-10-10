package de.unima.sensor.features.model;

import de.unima.sensor.features.FactoryProperties;
import de.unima.sensor.features.controller.DataCenter;

import java.util.*;

/**
 * Window Container. This object represents a certain time interval where this object stores the corresponding
 * acceleration data, computed features, and label.
 *
 * @author Timo Sztyler
 * @version 10.10.2016
 */
public class Window implements Comparable<Window> {
    private int                             id;
    private long                            start;
    private long                            end;
    private String[]                        labels;
    private Map<SensorType, Set<Attribute>> data;                // sensor, attribute, and values of this window
    private Features                        features;


    public Window(int id, long start, long end, String... labels) {
        this.id = id;
        this.start = start;
        this.end = end;
        this.labels = labels;
        this.data = new HashMap<>();
        this.features = null;
    }


    public void build() {
        if (this.data.size() == 0) {
            System.err.println("Error! Build not possible! First, you have to use 'addSensor'.");
            return;
        }

        for (SensorType sensor : data.keySet()) {
            this.data.get(sensor).addAll(getData(sensor));
        }

        // calculation of the features
        this.features = new Features(this.id, this.data.get(SensorType.ACCELERATION));
    }


    public void addSensor(SensorType sensor) {
        if (this.data.containsKey(sensor)) { return; }

        this.data.put(sensor, new HashSet<Attribute>());
    }


    public int getId() {
        return id;
    }


    public long getStart() {
        return start;
    }


    public long getEnd() {
        return end;
    }


    public String[] getLabels() {
        return this.labels;
    }


    public Set<Attribute> getEntries(SensorType sensor) {
        return this.data.get(sensor);
    }


    public Features getFeatures() {
        return this.features;
    }


    private Set<Attribute> getData(SensorType sensor) {
        Set<Attribute> result = new HashSet<>();

        DataCenter dc = DataCenter.getInstance();

        NavigableMap<Long, Integer> timeStamps = dc.getTimestamps();

        long startTimestamp = timeStamps.ceilingKey(this.start);
        int  startPosition  = -1;
        if (!(startTimestamp >= (this.start + FactoryProperties.WINDOW_SIZE))) {
            startPosition = dc.getTimestamp(startTimestamp).getValue();
        }

        long endTimestamp = timeStamps.floorKey(this.end);
        int  endPosition  = -1;
        if (!(endTimestamp < (this.end - FactoryProperties.WINDOW_SIZE))) {
            endPosition = dc.getTimestamp(endTimestamp).getValue();
        }

        Set<Attribute> attrs = dc.getAttributes(sensor);
        for (Attribute attr : attrs) {
            Pair<List<Long>, Pair<List<Double>, List<Double>>> entries = attr.getEntries(startPosition, endPosition);

            Attribute newAttr = new Attribute(sensor, attr.getAttribute(), false);
            newAttr.addValues(entries.getLeft(), entries.getRight().getLeft(), entries.getRight().getRight());

            result.add(newAttr);
        }

        return result;
    }


    @Override
    public int hashCode() {
        return 0;
    }


    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Window)) { return false; }

        Window w = (Window) o;

        if (this.hashCode() == w.hashCode()) { return true; }

        return false;
    }


    @Override
    public int compareTo(Window o) {
        if (this.getStart() > o.getStart() && this.getEnd() > o.getEnd()) { return 1; }

        if (this.getStart() < o.getStart() && this.getEnd() < o.getEnd()) { return -1; }

        return 0;
    }


    @Override
    public String toString() {
        return "Window " + this.getId() + " : " + Arrays.toString(this.labels) + " (" + this.getStart() + " - " + this.getEnd() + ") | " + this.getData(SensorType.ACCELERATION).iterator().next().getSize() + System.lineSeparator();
    }
}