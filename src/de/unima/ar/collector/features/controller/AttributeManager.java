package de.unima.ar.collector.features.controller;

import java.util.ArrayList;
import java.util.List;

import de.unima.ar.collector.features.Config;
import de.unima.ar.collector.features.Utils;
import de.unima.ar.collector.features.model.SensorData;


public class AttributeManager implements Runnable
{
    private long    timeStamp;
    private boolean isRunning;
    private int     alreadyRead;


    public AttributeManager()
    {
        this.timeStamp = 0;
        this.isRunning = true;
        this.alreadyRead = 0;
    }


    public void shutdown()
    {
        this.isRunning = false;
    }


    private void parse()
    {
        DataCenter dc = DataCenter.getInstance();

        while(this.isRunning) {
            long tmpTime = dc.getRawDataLastModified();

            // verify last modification
            if(this.timeStamp >= tmpTime) { // TODO SLEEP NOTIFY
                Utils.sleep(Config.MANAGER_ATTRIBUTE_IDLE);
                continue;
            }
            this.timeStamp = tmpTime;

            // load data
            List<SensorData> tmp = dc.getRawData();
            List<SensorData> copy = new ArrayList<>(tmp);   // TODO real copy necessary????
            copy = tmp.subList(alreadyRead, tmp.size());

            // do job
            parse(copy);

            // job complete
            this.alreadyRead += copy.size();

            Utils.sleep(Config.MANAGER_ATTRIBUTE_IDLE);
        }

        System.out.println("> AttributeManager terminated!");
    }


    private void parse(List<SensorData> data)
    {
        DataCenter dc = DataCenter.getInstance();

        for(SensorData sd : data) {
            float[] values = sd.getValues();
            for(int i = 0; i < values.length; i++) {
                dc.addAttribute(sd.getSensor(), String.valueOf(i), sd.getTimestamp(), values[i]);
            }

            dc.addAttribute(sd.getSensor(), "attr_time", sd.getTimestamp(), sd.getTimestamp());
            dc.addAction(sd.getTimestamp(), sd.getAction());
        }
    }


    @Override
    public void run()
    {
        this.parse();
    }
}