package de.unima.ar.collector.features.test;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import org.junit.Test;

import de.unima.ar.collector.features.Config;
import de.unima.ar.collector.features.mathutils.Measures;


public class MeasureTest
{
    @Test
    public void meanValue()
    {
        Config.WINDOW_MINIMUM_VALUES = 3;

        double[] values = { 6, 11, 7 };
        double mean = Measures.meanValue(values);

        assertEquals("", 8, mean, 0);

        double[] values2 = { 2, 7, 9 };
        double mean2 = Measures.meanValue(values2);

        assertEquals("", 6, mean2, 0);
    }


    @Test
    public void varianceValue()
    {
        Config.WINDOW_MINIMUM_VALUES = 5;

        double[] values = { 600, 470, 170, 430, 300 };

        double mean = Measures.meanValue(values);
        double variane = Measures.varianceValue(values, mean, true);

        assertEquals("", 27130, variane, 0);

        double[] values2 = { 17, 15, 23, 7, 9, 13 };

        double mean2 = Measures.meanValue(values2);
        double variane2 = Measures.varianceValue(values2, mean2, true);

        assertEquals("", 33.2, variane2, 0);
    }


    @Test
    public void standardDeviationValue()
    {
        Config.WINDOW_MINIMUM_VALUES = 5;

        double[] values = { 600, 470, 170, 430, 300 };

        double mean = Measures.meanValue(values);
        double variane = Measures.varianceValue(values, mean, true);
        double sd = Measures.standardDeviationValue(variane);

        assertEquals("", 164.71, sd, 0.01);

        double[] values2 = { 17, 15, 23, 7, 9, 13 };

        double mean2 = Measures.meanValue(values2);
        double variane2 = Measures.varianceValue(values2, mean2, true);
        double sd2 = Measures.standardDeviationValue(variane2);

        assertEquals("", 5.76, sd2, 0.01);
    }


    @Test
    public void medianValue()
    {
        Config.WINDOW_MINIMUM_VALUES = 9;

        double[] values = { 3, 13, 7, 5, 21, 23, 39, 23, 40, 23, 14, 12, 56, 23, 29 };
        double median = Measures.medianValue(values);

        assertEquals("", 23, median, 0);

        double[] values2 = { 13, 18, 13, 14, 13, 16, 14, 21, 13 };
        double median2 = Measures.medianValue(values2);

        assertEquals("", 14, median2, 0);
    }


    @Test
    public void iqrValue()
    {
        Config.WINDOW_MINIMUM_VALUES = 4;

        double[] values = { 5, 8, 4, 4, 6, 3, 8 };
        double iqr = Measures.iqrValue(values);

        assertEquals("", 3.5, iqr, 0);

        double[] values2 = { 9, 12, 15, 1, 2, 5, 6, 7, 18, 19, 27 };
        double iqr2 = Measures.iqrValue(values2);

        assertEquals("", 12, iqr2, 0);

        double[] values3 = { 1, 2, 3, 4 };
        double iqr3 = Measures.iqrValue(values3);

        assertEquals("", 2, iqr3, 0);
    }


    @Test
    public void matValue()
    {
        Config.WINDOW_MINIMUM_VALUES = 8;

        double[] values = { 3, 6, 6, 7, 8, 11, 15, 16 };

        double mean = Measures.meanValue(values);
        double mat = Measures.matValue(values, mean);

        assertEquals("", 3.75, mat, 0);

        double[] values2 = { 92, 83, 88, 94, 91, 85, 89, 90 };

        double mean2 = Measures.meanValue(values2);
        double mat2 = Measures.matValue(values2, mean2);

        assertEquals("", 2.75, mat2, 0);
    }


    @Test
    public void correlationCoefficientValue()
    {
        Config.WINDOW_MINIMUM_VALUES = 5;

        double[] valuesX = { 60, 61, 62, 63, 65 };
        double[] valuesY = { 3.1, 3.6, 3.8, 4, 4.1 };

        double meanX = Measures.meanValue(valuesX);
        double meanY = Measures.meanValue(valuesY);
        double cc = Measures.correlationCoefficientValue(valuesX, valuesY, meanX, meanY);

        assertEquals("", 0.9119, cc, 0.0001);

        double[] values2X = { 14.2, 16.4, 11.9, 15.2, 18.5, 22.1, 19.4, 25.1, 23.4, 18.1, 22.6, 17.2 };
        double[] values2Y = { 215, 325, 185, 332, 406, 522, 412, 614, 544, 421, 445, 408 };

        double mean2X = Measures.meanValue(values2X);
        double mean2Y = Measures.meanValue(values2Y);
        double cc2 = Measures.correlationCoefficientValue(values2X, values2Y, mean2X, mean2Y);

        assertEquals("", 0.9575, cc2, 0.0001);
    }


    @Test
    public void kurtosisValue()
    {
        Config.WINDOW_MINIMUM_VALUES = 6;

        double[] values = { 1, 12, 13, 23, 64, 10 };

        double mean = Measures.meanValue(values);
        double kurtosis = Measures.kurtosisValue(values, mean);

        assertEquals("", 3.54612, kurtosis, 0.0001);

        double[] values2 = { 0.0176, -0.0620, 0.2467, 0.4599, -0.0582, 0.4694, 0.0001, -0.2873 };

        double mean2 = Measures.meanValue(values2);
        double kurtosis2 = Measures.kurtosisValue(values2, mean2);

        assertEquals("", 1.55251, kurtosis2, 0.00001);
    }


    @Test
    public void energyValue()    // sollte in ordnung sein
    {
        double[] values = { 0.0176, -0.0620, 0.2467, 0.4599, -0.0582, 0.4694, 0.0001, -0.2873 };

        double[] fftValues = Measures.fft(values);
        double energy = Measures.energyValue(fftValues);

        assertEquals("", 0.80883, energy, 0.00001);

        double[] values2 = { 0, 1, 0, 0 };

        double[] fftValues2 = Measures.fft(values2);
        double energy2 = Measures.energyValue(fftValues2);

        assertEquals("", 3, energy2, 0);
    }


    @Test
    public void meanDcValue()    // sollte in ordnung sein, wenn ich DC richtig verstanden habe
    {
        double[] values = { 0.0176, -0.0620, 0.2467, 0.4599, -0.0582, 0.4694, 0.0001, -0.2873 };

        double[] fftValues = Measures.fft(values);
        double meandc = Measures.meanDcValue(fftValues);

        assertEquals("", 0.7862, meandc, 0);

        double[] values2 = { 0, 1, 0, 0 };

        double[] fftValues2 = Measures.fft(values2);
        double meandc2 = Measures.meanDcValue(fftValues2);

        assertEquals("", 1, meandc2, 0);
    }


    @Test
    public void entropyValueTime()
    {
        double[] values = { 0.23044191, 1.1109096, 1.1109096, 0.9187749, 0.19153613, 0.30645782, 0.22924481, 0.26755205 };

        double entropyTime = Measures.entropyValueTime(values);

        assertEquals("", 1.90615, entropyTime, 0.00001);
    }


    @Test
    public void entropyValueFrequency()
    {
        double[] values = { 0.23044191, 1.1109096, 1.1109096, 0.9187749, 0.19153613, 0.30645782, 0.22924481, 0.26755205 };
        double[] fftValues = Measures.fft(values);

        double entropyFrequency = Measures.entropyValueFrequency(fftValues);

        assertEquals("", 1.09861, entropyFrequency, 0.00001);
    }


    @Test
    public void orientationValue()
    {
        double[] values = { -0.04373932, -0.05805126, 9.81220274 };

        double pitch = Measures.orientationValue(values[0], values[2]);
        double roll = Measures.orientationValue(values[2], values[1]);
        double azimut = Measures.orientationValue(values[1], values[0]);

        assertEquals("", 90.2554, pitch, 0.0001);
        assertEquals("", -0.338971, roll, 0.000001);
        assertEquals("", -143.003, azimut, 0.001);

        double[] values2 = { -3.4070970, 9.02862289, -1.0375770 };

        double pitch2 = Measures.orientationValue(values2[0], values2[2]);
        double roll2 = Measures.orientationValue(values2[2], values2[1]);
        double azimut2 = Measures.orientationValue(values2[1], values2[0]);

        assertEquals("", -163.063, pitch2, 0.001);
        assertEquals("", 96.5557, roll2, 0.0001);
        assertEquals("", -20.6748, azimut2, 0.0001);
    }


    @Test
    public void gravityMeanValue()
    {
        double[] values = { 0.0176, -0.0620, 0.2467, 0.4599, -0.0582, 0.4694, 0.0001, -0.2873 };

        double gravityMean = Measures.gravityMeanValue(values);

        assertEquals("", 0.098274, gravityMean, 0.000001);
    }


    @Test
    public void discretizedOrientationValue()    // TODO
    {
        assertEquals("", 1, 2, 0);
    }


    @Test
    public void fft()    // The difference between this answer and the answer provided by wolframalpha is a scaling factor of sqrt(N) and inverse
    {
        double[] values = { 0, 1, 2, 3, 4, 5, 6, 7 };
        double[] fft = Measures.fft(values);

        double[] result = new double[] { 28.0000, 0.0000, -4.0000, 9.6569, -4.0000, 4.0000, -4.0000, 1.6569, -4.0000, 0.0000, -4.0000, -1.6569, -4.0000, -4.0000, -4.0000, -9.6569 };
        assertArrayEquals(fft, result, 0.0001);

        double[] values2 = { 0.0176, -0.0620, 0.2467, 0.4599, -0.0582, 0.4694, 0.0001, -0.2873 };
        double[] fft2 = Measures.fft(values2);

        double[] result2 = new double[] { 0.7862, 0.0000, -0.8283, -0.3991, -0.2874, -0.2348, 0.9799, 0.0940, -0.3737, 0.0000, 0.9799, -0.0940, -0.2874, 0.2348, -0.8283, 0.3991 };
        assertArrayEquals(fft2, result2, 0.0001);
    }
}