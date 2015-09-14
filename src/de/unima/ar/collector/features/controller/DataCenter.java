package de.unima.ar.collector.features.controller;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NavigableMap;
import java.util.Set;
import java.util.TreeMap;

import de.unima.ar.collector.features.Config;
import de.unima.ar.collector.features.model.Action;
import de.unima.ar.collector.features.model.Attribute;
import de.unima.ar.collector.features.model.Sensor;
import de.unima.ar.collector.features.model.SensorData;
import de.unima.ar.collector.features.model.Window;


public class DataCenter
{
    private static DataCenter DATACENTER = null;

    private List<SensorData>            rawSensorData;
    private Map<Sensor, Set<Attribute>> attributes;               // sensor, attribute, values
    private List<Window>                windows;
    private NavigableMap<Long, Integer> timeStamps;               // timestamp, position
    private NavigableMap<Long, Action>  actions;                     // informationen über die Tätigkeiten der Person

    private long rawDataLastModified;
    private long attributesLastModified;
    private long windowsLastModified;

    private long requiredTime;
    private long requiredTimeWithSleep;

    private final Object rawDataLock;
    private final Object attributesLock;
    private final Object windowsLock;
    private final Object timeStampsLock;


    private DataCenter()
    {
        this.rawSensorData = new ArrayList<>();
        this.attributes = new HashMap<>();
        this.windows = new ArrayList<>();

        this.rawDataLock = new Object();
        this.attributesLock = new Object();
        this.windowsLock = new Object();
        this.timeStampsLock = new Object();

        this.rawDataLastModified = 0;
        this.attributesLastModified = 0;
        this.windowsLastModified = 0;
        this.requiredTime = 0;

        this.timeStamps = new TreeMap<>();
        this.actions = new TreeMap<>();

        Locale.setDefault(Locale.US);
    }


    public static DataCenter getInstance()
    {
        if(DATACENTER == null) {
            DATACENTER = new DataCenter();
        }

        return DATACENTER;
    }


    public List<SensorData> getRawData()
    {
        synchronized(rawDataLock) {
            return new ArrayList<SensorData>(this.rawSensorData); // copy TODO
        }
    }


    public Set<Attribute> getAttributes(Sensor s)
    {
        synchronized(attributesLock) {
            if(!(this.attributes.containsKey(s))) { return new HashSet<Attribute>(); }

            return this.attributes.get(s);
        }
    }


    public List<Window> getWindows()
    {
        synchronized(windowsLock) {
            return this.windows;
        }
    }


    public NavigableMap<Long, Integer> getTimestamps()
    {
        synchronized(timeStampsLock) {
            return this.timeStamps;
        }
    }


    public Entry<Long, Integer> getTimestamp(Long key)
    {
        synchronized(timeStampsLock) {
            return new AbstractMap.SimpleEntry<>(key, this.timeStamps.get(key));
        }
    }


    public long getRawDataLastModified()
    {
        return rawDataLastModified;
    }


    public long getAttributesLastModified()
    {
        return attributesLastModified;
    }


    public long getWindowsLastModified()
    {
        return windowsLastModified;
    }


    public long getRequiredTime()
    {
        return this.requiredTime;
    }


    public long getRequiredTimeWithSleep()
    {
        return this.requiredTimeWithSleep;
    }


    public Action getAction(long key)
    {
        long floorKey = this.actions.floorKey(key);

        return this.actions.get(floorKey);
    }


    public void addRawData(SensorData sd)
    {
        synchronized(rawDataLock) {
            this.rawSensorData.add(sd);
            this.rawDataLastModified++;
        }
    }


    public void addAttribute(Sensor sen, String attr, long timestamp, double value)
    {
        synchronized(attributesLock) {
            if(!this.attributes.containsKey(sen)) {
                this.attributes.put(sen, new HashSet<Attribute>());
            }

            Set<Attribute> attributes = this.attributes.get(sen);
            Attribute attribute = new Attribute(sen, attr, Config.LOWPASSFILTER);

            if(!(attributes.contains(attribute))) {
                attributes.add(attribute);
            }

            for(Attribute a : attributes) {
                if(a.equals(attribute)) {
                    attribute = a;  // change reference
                }
            }

            attribute.addValue(timestamp, value);

            if(attr.equals("attr_time")) {  // TODO kann zu problemen führen wenn wir mehre Sensoren einlesen und es diese Spalte mehr als einmal gibt
                this.addTimestamps(attribute.getLastTimestamp(), attribute.getSize() - 1);
            }

            this.attributesLastModified++;
        }
    }


    public void addWindow(Window w)
    {
        synchronized(windowsLock) {
            this.windows.add(w);
            this.windowsLastModified++;
        }
    }


    public void addTimestamps(long time, int pos)
    {
        synchronized(timeStampsLock) {
            this.timeStamps.put(time, pos);
        }
    }


    public void addAction(long time, Action action)
    {
        if(this.actions.size() == 0) {
            time = 0;
        } else {
            time = time - this.actions.get(0l).getTime();
        }

        Long key = this.actions.floorKey(time);
        if(key != null) {
            Action object = this.actions.get(key);
            if(action.equals(object)) { return; }
        }

        this.actions.put(time, action);
    }


    public void increaseRequiredTime(long time)
    {
        this.requiredTime += time;
    }


    public void increaseRequiredTimeWithSleep(long time)
    {
        this.requiredTimeWithSleep += time;
    }


    public void clear()
    {
        DATACENTER = null;
    }


    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();

        sb.append("RawData: " + this.getRawData().size() + System.lineSeparator());
        sb.append("Attributes (Acc): " + this.getAttributes(Sensor.ACCELERATION).size() + System.lineSeparator());
        for(Attribute attr : this.getAttributes(Sensor.ACCELERATION)) {
            sb.append("> " + attr.toString() + System.lineSeparator());
        }
        sb.append("Windows: " + this.getWindows().size() + System.lineSeparator());

        return sb.toString();
    }
}