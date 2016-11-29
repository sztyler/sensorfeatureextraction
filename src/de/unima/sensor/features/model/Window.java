package de.unima.sensor.features.model;

import de.unima.sensor.features.FactoryProperties;
import de.unima.sensor.features.controller.DataCenter;

import java.util.*;

/**
 * Window Container. This object represents a certain time interval where this object stores the corresponding
 * acceleration data, computed features, and label.
 *
 * @author Timo Sztyler
 * @version 24.11.2016
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
        this.features = new Features(this.id, this.data.get(this.data.entrySet().iterator().next().getKey()));   // TODO
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


    public Set<SensorType> getSensorTypes() {
        return this.data.keySet();
    }


    private Set<Attribute> getData(SensorType sensor) {
        Set<Attribute> result = new HashSet<>();

        DataCenter dc = DataCenter.getInstance();

        NavigableMap<Long, Integer> timeStamps = dc.getTimestamps(sensor);

        Long startTimestamp = timeStamps.ceilingKey(this.start);
        if (startTimestamp == null) {
            return result;
        }

        int startPosition = -1;
        if (!(startTimestamp >= (this.start + FactoryProperties.WINDOW_SIZE))) {
            startPosition = dc.getTimestamp(sensor, startTimestamp).getValue();
        }

        Long endTimestamp = timeStamps.floorKey(this.end);
        if (endTimestamp == null) {
            return result;
        }
        int  endPosition  = -1;
        if (!(endTimestamp < (this.end - FactoryProperties.WINDOW_SIZE))) {
            endPosition = dc.getTimestamp(sensor, endTimestamp).getValue();
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

        return this.hashCode() == w.hashCode();
    }


    @Override
    public int compareTo(Window o) {
        if (this.getStart() > o.getStart() && this.getEnd() > o.getEnd()) { return 1; }

        if (this.getStart() < o.getStart() && this.getEnd() < o.getEnd()) { return -1; }

        return 0;
    }


    @Override
    public String toString() {
        String s = "Window " + this.getId() + " : [";
        for (SensorType sensorType : this.data.keySet()) {
            s += sensorType.toString().substring(0, 3) + ": " + (this.getData(sensorType).size() > 0 ? this.getData(sensorType).iterator().next().getSize() : "0") + ", ";
        }
        if(this.data.isEmpty()) {
            s += "no data available, ";
        }
        s = s.substring(0, s.length() - 2) + "] | " + Arrays.toString(this.labels) + " | (" + this.getStart() + " - " + this.getEnd() + ") | " + System.lineSeparator();

        return s;
    }
}