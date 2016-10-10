package de.unima.sensor.features.controller;

import de.unima.sensor.features.FactoryProperties;
import de.unima.sensor.features.model.Attribute;
import de.unima.sensor.features.model.SensorType;
import de.unima.sensor.features.model.SensorData;
import de.unima.sensor.features.model.Window;

import java.util.*;
import java.util.Map.Entry;

/**
 * The data center has all references of all data, i.e., container such as attribute, window, features. In addition,
 * the attribute manager and window manager push their processed data to this data center.
 *
 * @author Timo Sztyler
 * @version 10.10.2016
 */
public class DataCenter {
    private static DataCenter DATACENTER = null;

    private List<SensorData>                rawSensorData;
    private Map<SensorType, Set<Attribute>> attributes;               // sensor, attribute, values
    private List<Window>                    windows;
    private NavigableMap<Long, Integer>     timeStamps;               // timestamp, position
    private NavigableMap<Long, String[]>    labels;                     // informationen über die Tätigkeiten der Person

    private long rawDataLastModified;
    private long attributesLastModified;
    private long windowsLastModified;

    private long requiredTime;
    private long requiredTimeWithSleep;

    private final Object rawDataLock;
    private final Object attributesLock;
    private final Object windowsLock;
    private final Object timeStampsLock;


    private DataCenter() {
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
        this.labels = new TreeMap<>();

        Locale.setDefault(Locale.US);
    }


    public static DataCenter getInstance() {
        if (DATACENTER == null) {
            DATACENTER = new DataCenter();
        }

        return DATACENTER;
    }


    public List<SensorData> getRawData() {
        synchronized (rawDataLock) {
            return new ArrayList<SensorData>(this.rawSensorData); // copy TODO
        }
    }


    public Set<Attribute> getAttributes(SensorType s) {
        synchronized (attributesLock) {
            if (!(this.attributes.containsKey(s))) { return new HashSet<Attribute>(); }

            return this.attributes.get(s);
        }
    }


    public List<Window> getWindows() {
        synchronized (windowsLock) {
            return this.windows;
        }
    }


    public NavigableMap<Long, Integer> getTimestamps() {
        synchronized (timeStampsLock) {
            return this.timeStamps;
        }
    }


    public Entry<Long, Integer> getTimestamp(Long key) {
        synchronized (timeStampsLock) {
            return new AbstractMap.SimpleEntry<>(key, this.timeStamps.get(key));
        }
    }


    public long getRawDataLastModified() {
        return rawDataLastModified;
    }


    public long getAttributesLastModified() {
        return attributesLastModified;
    }


    public long getWindowsLastModified() {
        return windowsLastModified;
    }


    public long getRequiredTime() {
        return this.requiredTime;
    }


    public long getRequiredTimeWithSleep() {
        return this.requiredTimeWithSleep;
    }


    public String[] getLabels(long key) {
        long floorKey = this.labels.floorKey(key);

        return this.labels.get(floorKey);
    }


    public void addRawData(SensorData sd) {
        synchronized (rawDataLock) {
            this.rawSensorData.add(sd);
            this.rawDataLastModified++;
        }
    }


    public void addAttribute(SensorType sen, String attr, long timestamp, double value) {
        synchronized (attributesLock) {
            if (!this.attributes.containsKey(sen)) {
                this.attributes.put(sen, new HashSet<Attribute>());
            }

            Set<Attribute> attributes = this.attributes.get(sen);
            Attribute      attribute  = new Attribute(sen, attr, FactoryProperties.LOWPASSFILTER);

            if (!(attributes.contains(attribute))) {
                attributes.add(attribute);
            }

            for (Attribute a : attributes) {
                if (a.equals(attribute)) {
                    attribute = a;  // change reference
                }
            }

            attribute.addValue(timestamp, value);

            if (attr.equals("attr_time")) {  // TODO kann zu problemen führen wenn wir mehre Sensoren einlesen und es diese Spalte mehr als einmal gibt
                this.addTimestamps(attribute.getLastTimestamp(), attribute.getSize() - 1);
            }

            this.attributesLastModified++;
        }
    }


    public void addWindow(Window w) {
        synchronized (windowsLock) {
            this.windows.add(w);
            this.windowsLastModified++;
        }
    }


    public void addTimestamps(long time, int pos) {
        synchronized (timeStampsLock) {
            this.timeStamps.put(time, pos);
        }
    }


    public void addLabels(long time, String... label) {
        if (this.labels.size() == 0) {
            time = 0;
        } else {
            time = time - this.labels.firstKey();
        }

        Long key = this.labels.floorKey(time);
        if (key != null) {
            String[] object = this.labels.get(key);
            if (Arrays.equals(label, object)) { return; }
        }

        this.labels.put(time, label);
    }


    public void increaseRequiredTime(long time) {
        this.requiredTime += time;
    }


    public void increaseRequiredTimeWithSleep(long time) {
        this.requiredTimeWithSleep += time;
    }


    public void clear() {
        DATACENTER = null;
    }


    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        sb.append("RawData: ").append(this.getRawData().size()).append(System.lineSeparator());
        sb.append("Attributes (Acc): ").append(this.getAttributes(SensorType.ACCELERATION).size()).append(System.lineSeparator());
        for (Attribute attr : this.getAttributes(SensorType.ACCELERATION)) {
            sb.append("> ").append(attr.toString()).append(System.lineSeparator());
        }
        sb.append("Windows: ").append(this.getWindows().size()).append(System.lineSeparator());

        return sb.toString();
    }
}