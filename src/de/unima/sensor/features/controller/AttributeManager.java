package de.unima.sensor.features.controller;

import de.unima.sensor.features.FactoryProperties;
import de.unima.sensor.features.Utils;
import de.unima.sensor.features.model.SensorData;

import java.util.List;

/**
 * This class takes care of parsing the sensor data. The attribute manager is started by SCSystem and gets the data
 * which has to be processed from the data center. Processed data is pushed back to the data center.
 *
 * @author Timo Sztyler
 * @version 29.11.2016
 */
public class AttributeManager implements Runnable {
    private long    timeStamp;
    private boolean isRunning;
    private int     alreadyRead;


    public AttributeManager() {
        this.timeStamp = 0;
        this.isRunning = true;
        this.alreadyRead = 0;
    }


    public void shutdown() {
        this.isRunning = false;
    }


    private void parse() {
        DataCenter dc = DataCenter.getInstance();

        while (this.isRunning) {
            long tmpTime = dc.getRawDataLastModified();

            // verify last modification
            if (this.timeStamp >= tmpTime) { // TODO SLEEP NOTIFY
                Utils.sleep(FactoryProperties.MANAGER_ATTRIBUTE_IDLE);
                continue;
            }
            this.timeStamp = tmpTime;

            // load data
            List<SensorData> data    = dc.getRawData();
            List<SensorData> subData = data.subList(alreadyRead, data.size());

            // do job
            parse(subData);

            // job complete
            this.alreadyRead += subData.size();

            Utils.sleep(FactoryProperties.MANAGER_ATTRIBUTE_IDLE);
        }

        System.out.println("> AttributeManager terminated!");
    }


    private void parse(List<SensorData> data) {
        DataCenter dc = DataCenter.getInstance();

        for (SensorData sd : data) {
            float[] values = sd.getValues();
            for (int i = 0; i < values.length; i++) {
                dc.addAttribute(sd.getSensor(), String.valueOf(i), sd.getTimestamp(), values[i]);
            }

            dc.addAttribute(sd.getSensor(), "attr_time", sd.getTimestamp(), sd.getTimestamp());
            dc.addLabels(sd.getTimestamp(), sd.getLabels());
        }
    }


    @Override
    public void run() {
        this.parse();
    }
}