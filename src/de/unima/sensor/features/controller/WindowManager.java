package de.unima.sensor.features.controller;

import de.unima.sensor.features.FactoryProperties;
import de.unima.sensor.features.model.Attribute;
import de.unima.sensor.features.model.SensorType;
import de.unima.sensor.features.model.Window;

import java.util.Set;

/**
 * Window Manager. This class takes care of all created windows but also the creation and storing of new windows.
 *
 * @author Timo Sztyler
 * @version 01.12.2016
 */
class WindowManager implements Runnable {
    private boolean isRunning;
    private long[]  readingProgress;
    private int     windowForward;
    private int     windowBackward;


    WindowManager() {
        this.isRunning = true;
        this.readingProgress = new long[SensorType.values().length];
        this.windowForward = 0;
        this.windowBackward = -1;
    }


    void shutdown() {
        this.isRunning = false;
    }


    private void create() {
        DataCenter dc    = DataCenter.getInstance();
        long       shift = (long) (FactoryProperties.WINDOW_SIZE * (FactoryProperties.WINDOW_OVERLAP ? FactoryProperties.WINDOW_OVERLAP_SIZE : 0));

        while (this.isRunning) {
            for (int i = 0; i < SensorType.values().length; i++) {
                SensorType     sensorType = SensorType.values()[i];
                Set<Attribute> attrs      = dc.getAttributes(sensorType);

                if (attrs.size() == 0) {
                    continue;
                }

                Attribute attr              = attrs.iterator().next();  // grep first attribute, all attributes have the same length
                long      attrAbsoluteStart = attr.getStartTimePoint();
                long      attrAbsoluteEnd   = attr.getLastTimestamp();

                if (readingProgress[i] == 0) {
                    readingProgress[i] = attrAbsoluteStart;
                }

                String[] labels = dc.getLabels(readingProgress[i] + (FactoryProperties.WINDOW_SIZE / 2));

                Long windowStart = dc.getWindows().floorKey(readingProgress[i]);
                Long windowEnd   = windowStart != null ? (windowStart + FactoryProperties.WINDOW_SIZE) : null;
                //System.out.println(Arrays.toString(readingProgress)+ " -- " + dc.getWindows().size()+" -- "+windowStart);

                if (windowStart == null && dc.getWindows().size() > 0 && attrAbsoluteStart < dc.getWindows().firstKey()) {   // create empty window before the first one
                    long firstWindowStart = dc.getWindows().firstKey();

                    long   newWindowStart = firstWindowStart - (FactoryProperties.WINDOW_SIZE - shift);
                    long   newWindowEnd   = firstWindowStart + shift;
                    Window window         = new Window(this.windowBackward, newWindowStart, newWindowEnd, labels);
                    dc.addWindow(newWindowStart, window);

                    this.windowBackward--;
                    dc.increaseWindowsLastModified();
                } else if (windowStart == null || (readingProgress[i] == (windowEnd) && windowEnd <= attrAbsoluteEnd)) { // create window after the last one
                    long   newWindowStart = windowEnd == null ? attrAbsoluteStart : readingProgress[i] - shift;
                    long   newWindowEnd   = newWindowStart + FactoryProperties.WINDOW_SIZE;
                    Window window         = new Window(this.windowForward, newWindowStart, newWindowEnd, labels);
                    window.addSensor(sensorType);
                    dc.addWindow(newWindowStart, window);

                    readingProgress[i] = newWindowEnd < attrAbsoluteEnd ? newWindowEnd : attrAbsoluteEnd;
                    this.windowForward++;
                    dc.increaseWindowsLastModified();
                } else if (readingProgress[i] >= windowStart && readingProgress[i] < windowEnd) {    // there is also a window that fits
                    Window window = dc.getWindows().get(windowStart);
                    window.addSensor(sensorType);

                    readingProgress[i] = windowEnd;// < attrAbsoluteEnd ? windowEnd : attrAbsoluteEnd;
                    if (!windowStart.equals(dc.getWindows().floorKey(readingProgress[i] - shift))) {    // next windows already exists and overlap is active
                        readingProgress[i] -= shift;
                    }
                    dc.increaseWindowsLastModified();
                } else if (attrAbsoluteStart > windowEnd) {   // create empty window after the last one
                    long   newWindowStart = windowEnd - shift;
                    long   newWindowEnd   = newWindowStart + FactoryProperties.WINDOW_SIZE;
                    Window window         = new Window(this.windowForward, newWindowStart, newWindowEnd, labels);
                    dc.addWindow(newWindowStart, window);

                    this.windowForward++;
                    dc.increaseWindowsLastModified();
                }
            }
        }

        System.out.println("> WindowManager terminated!");
    }


    @Override
    public void run() {
        this.create();
    }
}