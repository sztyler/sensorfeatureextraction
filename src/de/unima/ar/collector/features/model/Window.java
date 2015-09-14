package de.unima.ar.collector.features.model;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.Set;

import de.unima.ar.collector.features.Config;
import de.unima.ar.collector.features.controller.DataCenter;


public class Window implements Comparable<Window>
{
    private int                         id;
    private long                        start;
    private long                        end;
    private Action                      action;
    private Map<Sensor, Set<Attribute>> data;                // sensor, attribute, and values of this window
    private Features                    features;


    public Window(int id, long start, long end, Action action)
    {
        this.id = id;
        this.start = start;
        this.end = end;
        this.action = action;
        this.data = new HashMap<>();
        this.features = null;
    }


    public void build()
    {
        if(this.data.size() == 0) {
            System.err.println("Error! Build not possible! First, you have to use 'addSensor'.");
            return;
        }

        for(Sensor sensor : data.keySet()) {
            this.data.get(sensor).addAll(getData(sensor));
        }

        // calculation of the features
        this.features = new Features(this.id, this.data.get(Sensor.ACCELERATION));
    }


    public void addSensor(Sensor sensor)
    {
        if(this.data.containsKey(sensor)) { return; }

        this.data.put(sensor, new HashSet<Attribute>());
    }


    public int getId()
    {
        return id;
    }


    public long getStart()
    {
        return start;
    }


    public long getEnd()
    {
        return end;
    }


    public Action getAction()
    {
        return this.action;
    }


    public Set<Attribute> getEntries(Sensor sensor)
    {
        return this.data.get(sensor);
    }


    public Features getFeatures()
    {
        return this.features;
    }


    private Set<Attribute> getData(Sensor sensor)
    {
        Set<Attribute> result = new HashSet<>();

        DataCenter dc = DataCenter.getInstance();

        NavigableMap<Long, Integer> timeStamps = dc.getTimestamps();

        long startTimestamp = timeStamps.ceilingKey(this.start);
        int startPosition = -1;
        if(!(startTimestamp >= (this.start + Config.WINDOW_SIZE))) {
            startPosition = dc.getTimestamp(startTimestamp).getValue();
        }

        long endTimestamp = timeStamps.floorKey(this.end);
        int endPosition = -1;
        if(!(endTimestamp < (this.end - Config.WINDOW_SIZE))) {
            endPosition = dc.getTimestamp(endTimestamp).getValue();
        }

        Set<Attribute> attrs = dc.getAttributes(sensor);
        for(Attribute attr : attrs) {
            Pair<List<Long>, Pair<List<Double>, List<Double>>> entries = attr.getEntries(startPosition, endPosition);

            Attribute newAttr = new Attribute(sensor, attr.getAttribute(), false);
            newAttr.addValues(entries.getLeft(), entries.getRight().getLeft(), entries.getRight().getRight());

            result.add(newAttr);
        }

        return result;
    }


    @Override
    public int hashCode()
    {
        return 0;
    }


    @Override
    public boolean equals(Object o)
    {
        if(!(o instanceof Window)) { return false; }

        Window w = (Window) o;

        if(this.hashCode() == w.hashCode()) { return true; }

        return false;
    }


    @Override
    public int compareTo(Window o)
    {
        if(this.getStart() > o.getStart() && this.getEnd() > o.getEnd()) { return 1; }

        if(this.getStart() < o.getStart() && this.getEnd() < o.getEnd()) { return -1; }

        return 0;
    }


    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();

        sb.append("Window " + this.getId() + " : " + this.getAction().getHumanPosture() + " (" + this.getStart() + " - " + this.getEnd() + ") | " + this.getData(Sensor.ACCELERATION).iterator().next().getSize() + System.lineSeparator());

        return sb.toString();
    }
}