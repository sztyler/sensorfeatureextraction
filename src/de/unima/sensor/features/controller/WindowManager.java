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
 * @version 29.11.2016
 */
public class WindowManager implements Runnable {
    private boolean isRunning;
    private int[]   alreadyRead;
    private int     windowForward;
    private int     windowBackward;


    public WindowManager() {
        this.isRunning = true;
        this.alreadyRead = new int[SensorType.values().length];
        this.windowForward = 0;
        this.windowBackward = -1;
    }


    public void shutdown() {
        this.isRunning = false;
    }


    private void create() {
        DataCenter dc = DataCenter.getInstance();

        while (this.isRunning) {
            for (int i = 0; i < SensorType.values().length; i++) {
                SensorType     sensorType = SensorType.values()[i];
                Set<Attribute> attrs      = dc.getAttributes(sensorType);

                if (attrs.size() == 0) {
                    continue;
                }

                long startValue = attrs.iterator().next().getStartTimePoint();
                long end        = attrs.iterator().next().getLastTimestamp();

                if (((end - startValue) - alreadyRead[i]) < FactoryProperties.WINDOW_SIZE) {
                    continue;
                }

                String[] labels = dc.getLabels((alreadyRead[i] + (FactoryProperties.WINDOW_SIZE / 2)));

                startValue += alreadyRead[i];

                Long windowKey      = dc.getWindows().floorKey(startValue);
                long windowKeyRange = windowKey != null ? windowKey + (long) (FactoryProperties.WINDOW_SIZE * FactoryProperties.WINDOW_OVERLAP_SIZE) : -1;

                if (windowKey == null && dc.getWindows().size() > 0 && startValue < dc.getWindows().firstKey()) {   // create empty window before the first one
                    long   first  = dc.getWindows().firstKey();
                    long   diff   = (long) (FactoryProperties.WINDOW_SIZE * FactoryProperties.WINDOW_OVERLAP_SIZE); // TODO
                    Window window = new Window(this.windowBackward, first - diff, first + diff, labels);
                    this.windowBackward--;
                    dc.addWindow(first - diff, window);
                } else if (windowKey == null || startValue == windowKeyRange) { // create window after the last one
                    Window window = new Window(this.windowForward, startValue, startValue + FactoryProperties.WINDOW_SIZE, labels);
                    this.windowForward++;
                    window.addSensor(sensorType);
                    window.build();

                    if (FactoryProperties.WINDOW_OVERLAP) {
                        alreadyRead[i] += FactoryProperties.WINDOW_SIZE * FactoryProperties.WINDOW_OVERLAP_SIZE;
                    } else {
                        alreadyRead[i] += FactoryProperties.WINDOW_SIZE;
                    }
                    dc.addWindow(startValue, window);
                } else if (startValue >= windowKey && startValue < windowKeyRange) {    // there is also a window that fits
                    Window window = dc.getWindows().get(windowKey);
                    window.addSensor(sensorType);
                    window.build();
                    alreadyRead[i] += windowKeyRange - startValue;
                } else if (startValue > windowKeyRange) {   // create empty window after the last one
                    Window window = new Window(this.windowForward, windowKeyRange, windowKeyRange + FactoryProperties.WINDOW_SIZE, labels);
                    this.windowForward++;
                    dc.addWindow(windowKeyRange, window);
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