package de.unima.ar.collector.features.example;

import de.unima.ar.collector.features.FE;
import de.unima.ar.collector.features.model.Sensor;

/**
 * Usage example how these libary has to be used to process acceleration data.
 *
 * @author Timo Sztyler
 * @version 30.09.2016
 */
public class UsageExample {
    public static void main(String[] args) {
        FE fe = new FE(Sensor.ACCELERATION);


    }
}
