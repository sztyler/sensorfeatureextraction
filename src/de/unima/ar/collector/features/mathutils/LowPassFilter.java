package de.unima.ar.collector.features.mathutils;

import de.unima.ar.collector.features.model.Pair;

/**
 * Low-Pass-Filter to separate acceleration and gravity vector.
 *
 * @author Timo Sztyler
 * @version 30.09.2016
 */
public class LowPassFilter
{
    private double   timeConstant;
    private int      count;
    private double[] gravity;


    public LowPassFilter()
    {
        this.timeConstant = 1f;
        this.count = 0;
        this.gravity = new double[] { 0, 0, 0 };
    }


    public Pair<double[], double[]> addSamples(double[] input, long time)
    {
        double[] output = new double[input.length];
        double[] gravityCopy = new double[input.length];
        
        count++;
        double dt = 1 / (count / (time / 1000.0d));
        double alpha = timeConstant / (timeConstant + dt);

        for(int i = 0; i < input.length; i++) {
            gravity[i] = alpha * gravity[i] + (1 - alpha) * input[i];
            output[i] = input[i] - gravity[i];
        }
         
        System.arraycopy(gravity, 0, gravityCopy, 0, input.length);
                
        return new Pair<>(output, gravityCopy);
    }


    public void setTimeConstant(float timeConstant)
    {
        this.timeConstant = timeConstant;
    }


    public void reset()
    {
        this.count = 0;
    }
}