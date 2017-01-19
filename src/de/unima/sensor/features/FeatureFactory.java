package de.unima.sensor.features;

import de.unima.sensor.features.controller.DataCenter;
import de.unima.sensor.features.controller.SCSystem;
import de.unima.sensor.features.model.Features;
import de.unima.sensor.features.model.SensorData;
import de.unima.sensor.features.model.SensorType;
import de.unima.sensor.features.model.Window;

import java.util.*;


/**
 * Main construct that has to be created to transform raw inertial sensor data to segmented windows.
 *
 * @author Timo Sztyler
 * @version 24.11.2016
 */
public class FeatureFactory {
    private SCSystem   sc;
    private DataCenter dc;
    private boolean    running;
    private long       windowForward;
    private long       windowBackward;

    public FeatureFactory() {
        this.running = false;
        this.windowForward = 0;
        this.windowBackward = 0;
    }

    public boolean addRecord(SensorType sensorType, long timestamp, float[] values, String... labels) {
        if (this.sc == null || this.dc == null || !this.running) { return false; }

        SensorData sd = new SensorData(sensorType);
        sd.addValues(timestamp, values);
        sd.addLabels(labels);

        dc.addRawData(sd);

        return true;
    }

    public NavigableMap<Long, Window> getWindows() {
        NavigableMap<Long, Window> newWindows = new TreeMap<>();
        if (dc == null) {
            return newWindows;
        }

        NavigableMap<Long, Window> windows = dc.getWindows();
        if (windows.size() == 0) {
            return newWindows;
        }

        long first = windows.firstKey();
        long last  = windows.lastKey();

        if (windowForward == 0 && windowBackward == 0) {
            windowForward = last;
            windowBackward = first;
            return windows;
        }

        if (windowForward < last) {
            newWindows.putAll(windows.subMap(windowForward, false, last, true));
        }

        if (windowBackward > first) {
            newWindows.putAll(windows.subMap(first, true, windowBackward, false));
        }

        return newWindows;
    }


    public NavigableMap<Long, Window> getAllWindows() {
        return new TreeMap<>(dc.getWindows());
    }


    public void start() {
        this.clear();
        this.sc = SCSystem.getInstance();
        this.dc = DataCenter.getInstance();
        this.running = true;
    }


    public void stop() {
        this.running = false;
        this.sc.shutdown();
    }


    public void clear() {
        SCSystem.clear();
        DataCenter.clear();
    }


    public boolean isRunning() {
        return this.running;
    }


    public void setConfig(FactoryProperties config) {
        // TODO
    }


    public boolean isIdle() {
        // still reading data?
        long size = DataCenter.getInstance().getRawDataLastModified();
        try {
            Thread.sleep(FactoryProperties.MANAGER_DATA_IDLE * 10);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        long size2 = DataCenter.getInstance().getRawDataLastModified();

        if (size != size2) { return false; }                                           // yes

        // still creating attributes?
        long size5 = DataCenter.getInstance().getAttributesLastModified();
        try {
            Thread.sleep(FactoryProperties.MANAGER_ATTRIBUTE_IDLE * 10);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        long size6 = DataCenter.getInstance().getAttributesLastModified();

        if (size5 != size6) { return false; }                                           // yes

        // still creating windows?
        long size3 = DataCenter.getInstance().getWindowsLastModified();
        try {
            Thread.sleep(FactoryProperties.MANAGER_WINDOW_IDLE * 10);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        long size4 = DataCenter.getInstance().getWindowsLastModified();

        if (size3 != size4) { return false; }                                           // yes

        // TODO check "lasttimestamp" in window

        return true;
    }


    public String getWindowsAsARFF(int targetClass) {
        String   csvFile = getWindowsAsCSV();
        String[] lines   = csvFile.split(System.lineSeparator());

        List<Window> windows = new ArrayList<>(this.getAllWindows().values());
        if (windows.size() == 0) {
            return "";
        }
        int total = windows.get(0).getLabels().length;

        Set<String> targetClasses = new TreeSet<>();
        for (int i = 1; i < lines.length; i++) {
            String[] elements = lines[i].split(",");
            targetClasses.add(elements[elements.length - (total - targetClass)]);
        }
        String formatted = Arrays.toString(targetClasses.toArray(new String[targetClasses.size()])).replace("[", "").replace("]", "").replace(" ", "");

        StringBuilder sb = new StringBuilder();
        sb.append("% 1. Motion Testdata").append(System.lineSeparator()).append("%").append(System.lineSeparator());
        sb.append("% 2. Sources:").append(System.lineSeparator()).append("%\t(a) Creator: FeatureFactory").append(System.lineSeparator());
        sb.append("%\t(b) Date: ").append(System.currentTimeMillis()).append(System.lineSeparator()).append("%").append(System.lineSeparator());
        sb.append("@RELATION motion").append(System.lineSeparator()).append(System.lineSeparator());
        String[] header = lines[0].split(",");
        for (String entry : header) {
            if (entry.trim().toLowerCase().equals("label_" + targetClass)) {
                sb.append("@ATTRIBUTE class {").append(formatted).append("}").append(System.lineSeparator());
            } else if (entry.trim().toLowerCase().startsWith("label_")) {
                sb.append("@ATTRIBUTE ").append(entry.trim()).append(" STRING").append(System.lineSeparator());
            } else {
                sb.append("@ATTRIBUTE ").append(entry.trim()).append(" NUMERIC").append(System.lineSeparator());
            }
        }
        sb.append(System.lineSeparator()).append("@DATA").append(System.lineSeparator());

        for (int i = 1; i < lines.length; i++) {
            sb.append(lines[i].replace(";", ",")).append(System.lineSeparator());
        }

        sb.setLength(sb.length() - (System.lineSeparator()).length());

        return sb.toString();
    }


    public String getWindowsAsCSV() {
        StringBuilder sb      = new StringBuilder();
        List<Window>  windows = new ArrayList<>(this.getAllWindows().values());

        if (windows.size() == 0) {
            return "";
        }

        // 1. Detect available/used sensor types and load headers
        NavigableMap<SensorType, String> sensorTypeHeaders = new TreeMap<>();
        for (Window window : windows) {
            for (SensorType sensorType : SensorType.values()) {
                window.getEntries(sensorType); // force refresh
                Features feature = window.getFeatures(sensorType);
                if (feature != null && !sensorTypeHeaders.containsKey(sensorType)) {
                    sensorTypeHeaders.put(sensorType, feature.getHeader());
                }
            }
        }

        // 2. Build Header
        for (SensorType sensorType : sensorTypeHeaders.keySet()) {
            String   header   = sensorTypeHeaders.get(sensorType);
            String[] elements = header.split(",");
            for (String element : elements) {
                sb.append(sensorType.toString().substring(0, 3).toLowerCase()).append("_").append(element.trim()).append(",");
            }
        }
        for (int i = 0; i < windows.get(0).getLabels().length; i++) {
            sb.append("label_").append(i).append(",");
        }
        sb.setLength(sb.length() - 1);
        sb.append(System.lineSeparator());

        // 3. Build File
        for (Window window : windows) {
            for (SensorType sensorType : sensorTypeHeaders.keySet()) {
                Features features = window.getFeatures(sensorType);

                if (features != null) {
                    sb.append(features.toString().replace(";", ",")).append(",");
                } else {
                    sb.append(window.getId()).append(",");
                    int numFeatures = sensorTypeHeaders.get(sensorType).split(", ").length;
                    for (int i = 0; i < numFeatures - 1; i++) {
                        sb.append("0.00000,");
                    }
                }
            }
            String targetClasses = Arrays.toString(window.getLabels()).replace("[", "").replace("]", "").replace(" ", "");
            sb.append(targetClasses).append(System.lineSeparator());
        }

        return sb.toString();
    }


    public String getWindowsAsSQL() {
        // TODO
        return null;
    }
}