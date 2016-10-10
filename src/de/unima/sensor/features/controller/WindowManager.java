package de.unima.sensor.features.controller;

import de.unima.sensor.features.Config;
import de.unima.sensor.features.Utils;
import de.unima.sensor.features.model.Attribute;
import de.unima.sensor.features.model.Sensor;
import de.unima.sensor.features.model.Window;

import java.util.Set;

/**
 * Window Manager. This class takes care of all created windows but also the creation and storing of new windows.
 *
 * @author Timo Sztyler
 * @version 10.10.2016
 */
public class WindowManager implements Runnable {
    private long    timeStamp;
    private boolean isRunning;
    private int     alreadyRead;
    private int     windowCounter;


    public WindowManager() {
        this.timeStamp = 0;
        this.isRunning = true;
        this.alreadyRead = 0;
        this.windowCounter = 0;
    }


    public void shutdown() {
        this.isRunning = false;
    }


    private void create() {
        DataCenter dc     = DataCenter.getInstance();
        Sensor     sensor = Sensor.ACCELERATION;

        while (this.isRunning) {
            long tmpTime = dc.getAttributesLastModified();

            if (tmpTime <= this.timeStamp) {
                Utils.sleep(Config.MANAGER_WINDOW_IDLE);
                continue;
            }
            this.timeStamp = tmpTime;

            Set<Attribute> attrs = dc.getAttributes(sensor);

            if (attrs.size() == 0) {
                Utils.sleep(Config.MANAGER_WINDOW_IDLE);
                continue;
            }

            long end = attrs.iterator().next().getLastTimestamp();

            if ((end - alreadyRead) < Config.WINDOW_SIZE) {
                Utils.sleep(Config.MANAGER_WINDOW_IDLE);
                continue;
            }

            String[] labels = dc.getLabels((alreadyRead + (Config.WINDOW_SIZE / 2)));

            Window window = new Window(this.windowCounter, alreadyRead, alreadyRead + Config.WINDOW_SIZE, labels);
            window.addSensor(sensor);
            window.build();
            dc.addWindow(window);
            this.timeStamp = 0;

            if (Config.WINDOW_OVERLAP) {
                alreadyRead += Config.WINDOW_SIZE * Config.WINDOW_OVERLAP_SIZE;
            } else {
                alreadyRead += Config.WINDOW_SIZE;
            }

            this.windowCounter++;
        }

        System.out.println("> WindowManager terminated!");
    }


    @Override
    public void run() {
        this.create();
    }
}