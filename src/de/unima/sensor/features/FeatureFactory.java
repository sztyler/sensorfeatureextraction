package de.unima.sensor.features;

import de.unima.sensor.features.controller.DataCenter;
import de.unima.sensor.features.controller.SCSystem;
import de.unima.sensor.features.model.Sensor;
import de.unima.sensor.features.model.SensorData;
import de.unima.sensor.features.model.Window;

import java.util.Arrays;
import java.util.List;


/**
 * Main construct that has to be created to transform raw acceleration data to segmented windows.
 *
 * @author Timo Sztyler
 * @version 10.10.2016
 */
public class FeatureFactory {
    private Sensor     sensor;
    private SCSystem   sc;
    private DataCenter dc;
    private boolean    running;


    public FeatureFactory(Sensor sensor) {
        this.sensor = sensor;
        this.running = false;
    }

    public boolean addAccelerationData(long timestamp, float[] values) {
        return this.addAccelerationData(timestamp, values);
    }

    public boolean addAccelerationData(long timestamp, float[] values, String... labels) {
        if (this.sc == null || this.dc == null || !this.running) { return false; }

        SensorData sd = new SensorData(sensor);
        sd.addValues(timestamp, values);
        sd.addLabels(labels);

        dc.addRawData(sd);

        return true;
    }


    public List<Window> getWindows() {
        return dc.getWindows();
    }


    public void start() {
        this.sc = SCSystem.getInstance();
        this.dc = DataCenter.getInstance();
        this.running = true;
    }


    public void stop() {
        this.running = false;
        this.sc.shutdown();
    }


    public void clear() {
        sc.clear();
        dc.clear();
    }


    public boolean isRunning() {
        return this.running;
    }


    public void setConfig(Config config) {
        // TODO
    }


    public boolean isIdle() {
        // still reading data?
        long size = DataCenter.getInstance().getRawDataLastModified();
        try {
            Thread.sleep(Config.MANAGER_DATA_IDLE * 10);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        long size2 = DataCenter.getInstance().getRawDataLastModified();

        if (size != size2) { return false; }                                           // yes

        // still creating attributes?
        long size5 = DataCenter.getInstance().getAttributesLastModified();
        try {
            Thread.sleep(Config.MANAGER_ATTRIBUTE_IDLE * 10);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        long size6 = DataCenter.getInstance().getAttributesLastModified();

        if (size5 != size6) { return false; }                                           // yes

        // still creating windows?
        long size3 = DataCenter.getInstance().getWindowsLastModified();
        try {
            Thread.sleep(Config.MANAGER_WINDOW_IDLE * 10);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        long size4 = DataCenter.getInstance().getWindowsLastModified();

        if (size3 != size4) { return false; }                                           // yes

        // TODO check "lasttimestamp" in window

        return true;
    }


        public String getWindowsAsARFF(int targetClass) {
            String csvFile = getWindowsAsCSV();
            String[] lines = csvFile.split(System.lineSeparator());

            StringBuilder sb = new StringBuilder();
            sb.append("% 1. Motion Testdata").append(System.lineSeparator()).append("%").append(System.lineSeparator());
            sb.append("% 2. Sources:").append(System.lineSeparator()).append("%\t(a) Creator: Timo Sztyler").append(System.lineSeparator());
            sb.append("%\t(b) Date: ").append(System.currentTimeMillis()).append(System.lineSeparator()).append("%").append(System.lineSeparator());
            sb.append("@RELATION motion").append(System.lineSeparator()).append(System.lineSeparator());
            String[] header = lines[0].split(",");
            for (String entry : header) {
                if (targetClass != null && entry.trim().toLowerCase().equals(targetClass.toString().toLowerCase())) {
                    continue;
                }

                if (Arrays.toString(Action.TYPE.values()).replace(" ", "").contains(entry.trim().toUpperCase())) {
                    sb.append("@ATTRIBUTE ").append(entry.trim()).append(" STRING").append(System.lineSeparator());
                    continue;
                }

                sb.append("@ATTRIBUTE ").append(entry.trim()).append(" NUMERIC").append(System.lineSeparator());
            }
            sb.append("@ATTRIBUTE class {").append(Action.getFormatedStringClass(targetClass)).append("}").append(System.lineSeparator());
            sb.append(System.lineSeparator()).append("@DATA").append(System.lineSeparator());

            for (int i = 1; i < lines.length; i++) {
                sb.append(lines[i].replace(";", ",")).append(System.lineSeparator());
            }

            sb.setLength(sb.length() - (System.lineSeparator()).length());

            return sb.toString();
        }


    public String getWindowsAsCSV() {
        StringBuilder sb      = new StringBuilder();
        List<Window>  windows = dc.getWindows();

        if (windows.size() > 0) {
            sb.append(windows.get(0).getFeatures().getHeader()).append(", ");

            for (int i = 0; i < windows.get(0).getLabels().length; i++) {
                sb.append("label_").append(i).append(", ");
            }

            sb.setLength(sb.length() - 1);
            sb.append(System.lineSeparator());
        }

        for (Window window : windows) {
            String targetClasses = Arrays.toString(window.getLabels()).replace("[", "").replace("]", "").replace(" ", "");
            sb.append(window.getFeatures().toString().replace(";", ",")).append(",").append(targetClasses).append(System.lineSeparator());
        }

        if (sb.length() > 0) {
            sb.delete((sb.length() - (System.lineSeparator()).length()), sb.length());
        }

        return sb.toString();
    }


    public String getWindowsAsSQL() {
        // TODO
        return null;
    }
}