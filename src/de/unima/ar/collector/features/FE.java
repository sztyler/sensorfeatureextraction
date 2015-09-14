package de.unima.ar.collector.features;

import java.util.Arrays;
import java.util.List;

import de.unima.ar.collector.features.controller.DataCenter;
import de.unima.ar.collector.features.controller.SCSystem;
import de.unima.ar.collector.features.model.Action;
import de.unima.ar.collector.features.model.Sensor;
import de.unima.ar.collector.features.model.SensorData;
import de.unima.ar.collector.features.model.Window;


public class FE
{
    private Sensor     sensor;
    private SCSystem   sc;
    private DataCenter dc;
    private boolean    running;


    public FE(Sensor sensor)
    {
        this.sensor = sensor;
        this.running = false;
    }


    public boolean addAccelerationData(long timestamp, float[] values, Action.DEVICEPOSITIONS devicePosition, Action.HUMANPOSTURES humanPosture, String humanPosition, String humanActvitiy)
    {
        if(this.sc == null || this.dc == null || !this.running) { return false; }

        SensorData sd = new SensorData(sensor);
        sd.addValues(timestamp, values);

        Action action = new Action();
        action.setTime(timestamp);
        action.setDevicePosition(devicePosition);
        action.setHumanPosture(humanPosture);
        action.setHumanPosition(humanPosition);
        action.setHumanActvitiy(humanActvitiy);
        sd.addAction(action);

        dc.addRawData(sd);

        return true;
    }


    public List<Window> getWindows()
    {
        return dc.getWindows();
    }


    public void start()
    {
        this.sc = SCSystem.getInstance();
        this.dc = DataCenter.getInstance();
        this.running = true;
    }


    public void stop()
    {
        this.running = false;
        this.sc.shutdown();
    }


    public void clear()
    {
        sc.clear();
        dc.clear();
    }


    public boolean isRunning()
    {
        return this.running;
    }


    public void setConfig(Config config)
    {
        // TODO
    }


    public boolean isIdle()
    {
        // still reading data?
        long size = DataCenter.getInstance().getRawDataLastModified();
        try {
            Thread.sleep(Config.MANAGER_DATA_IDLE * 10);
        } catch(InterruptedException e) {
            e.printStackTrace();
        }
        long size2 = DataCenter.getInstance().getRawDataLastModified();

        if(size != size2) { return false; }                                           // yes

        // still creating attributes?
        long size5 = DataCenter.getInstance().getAttributesLastModified();
        try {
            Thread.sleep(Config.MANAGER_ATTRIBUTE_IDLE * 10);
        } catch(InterruptedException e) {
            e.printStackTrace();
        }
        long size6 = DataCenter.getInstance().getAttributesLastModified();

        if(size5 != size6) { return false; }                                           // yes

        // still creating windows?
        long size3 = DataCenter.getInstance().getWindowsLastModified();
        try {
            Thread.sleep(Config.MANAGER_WINDOW_IDLE * 10);
        } catch(InterruptedException e) {
            e.printStackTrace();
        }
        long size4 = DataCenter.getInstance().getWindowsLastModified();

        if(size3 != size4) { return false; }                                           // yes

        // TODO check "lasttimestamp" in window

        return true;
    }


    public String getWindowsAsARFF(Action.TYPE targetClass)
    {
        String csvFile = getWindowsAsCSV(targetClass);

        String[] lines = csvFile.split(System.lineSeparator());

        StringBuilder sb = new StringBuilder();
        sb.append("% 1. Motion Testdata" + System.lineSeparator() + "%" + System.lineSeparator());
        sb.append("% 2. Sources:" + System.lineSeparator() + "%\t(a) Creator: Timo Sztyler" + System.lineSeparator());
        sb.append("%\t(b) Date: " + System.currentTimeMillis() + System.lineSeparator() + "%" + System.lineSeparator());
        sb.append("@RELATION motion" + System.lineSeparator() + System.lineSeparator());
        String[] header = lines[0].split(",");
        for(String entry : header) {
            if(targetClass != null && entry.trim().toLowerCase().equals(targetClass.toString().toLowerCase())) {
                continue;
            }

            if(Arrays.toString(Action.TYPE.values()).replace(" ", "").indexOf(entry.trim().toUpperCase()) != -1) {
                sb.append("@ATTRIBUTE " + entry.trim() + " STRING" + System.lineSeparator());
                continue;
            }

            sb.append("@ATTRIBUTE " + entry.trim() + " NUMERIC" + System.lineSeparator());
        }
        sb.append("@ATTRIBUTE class {" + Action.getFormatedStringClass(targetClass) + "}" + System.lineSeparator());
        sb.append(System.lineSeparator() + "@DATA" + System.lineSeparator());

        for(int i = 1; i < lines.length; i++) {
            sb.append(lines[i].replace(";", ",") + System.lineSeparator());
        }

        sb.setLength(sb.length() - (System.lineSeparator()).length());

        return sb.toString();
    }


    public String getWindowsAsCSV(Action.TYPE targetClass)
    {
        StringBuilder sb = new StringBuilder();
        List<Window> windows = dc.getWindows();

        if(windows.size() > 0) {
            sb.append(windows.get(0).getFeatures().getHeader() + "," + Action.getFormatedStringType(targetClass) + System.lineSeparator());
        }

        for(Window window : windows) {
            sb.append(window.getFeatures().toString().replace(";", ",") + "," + window.getAction().getFormatedStringValues(targetClass) + System.lineSeparator());
        }

        if(sb.length() > 0) {
            sb.delete((sb.length() - (System.lineSeparator()).length()), sb.length());
        }

        return sb.toString();
    }


    public String getWindowsAsSQL()
    {
        // TODO
        return null;
    }
}