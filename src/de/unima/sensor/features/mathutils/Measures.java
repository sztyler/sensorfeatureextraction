package de.unima.sensor.features.mathutils;

import de.unima.sensor.features.FactoryProperties;
import de.unima.sensor.features.Utils;
import org.jtransforms.fft.DoubleFFT_1D;

import java.util.*;

/**
 * This class covers all function that are necessary to compute the individual features for each window.
 *
 * @author Timo Sztyler
 * @version 19.01.2017
 */
public class Measures {
    // time-domain
    public static double meanValue(double[] values)  // Mittelwert
    {
        if (values.length < FactoryProperties.WINDOW_MINIMUM_VALUES) { return 0.0d; }

        double mean = 0.0d;

        for (double d : values) {
            if (FactoryProperties.ABSOLUT) {
                mean += Math.abs(d);
            } else {
                mean += d;
            }
        }
        mean /= (double) values.length;

        return mean;
    }


    // time-domain
    public static double varianceValue(double[] values, double mean, boolean sample) // korrigierte oder unverzerrte Stichprobenvarianz
    {
        if (values.length < FactoryProperties.WINDOW_MINIMUM_VALUES) { return 0.0d; }

        double varianz = 0.0d;

        for (double d : values) {
            if (FactoryProperties.ABSOLUT) {
                varianz += Math.pow((mean - Math.abs(d)), 2);
            } else {
                varianz += Math.pow((mean - d), 2);
            }
        }

        if (sample) {
            varianz /= (double) (values.length - 1);
        } else {
            varianz /= (double) (values.length);
        }

        return varianz;
    }


    // time-domain
    public static double standardDeviationValue(double varianz) // korrigierte Standardabweichung
    {
        if (varianz < 0) { return 0.0d; }

        return Math.sqrt(varianz);
    }


    // time-domain
    public static double medianValue(double[] values) {
        if (values.length < FactoryProperties.WINDOW_MINIMUM_VALUES) { return 0.0d; }

        List<Double> tmp = new ArrayList<>();
        for (double d : values) {
            if (FactoryProperties.ABSOLUT) {
                tmp.add(Math.abs(d));
            } else {
                tmp.add(d);
            }
        }
        Collections.sort(tmp);

        int pos = (tmp.size() - 1) / 2;

        if (tmp.size() % 2 != 0) { return tmp.get(pos); }

        return (tmp.get(pos) + tmp.get(pos + 1)) / 2.0d;
    }


    // time-domain
    public static double iqrValue(double[] values) {
        if (values.length < FactoryProperties.WINDOW_MINIMUM_VALUES) { return 0.0d; }

        List<Double> sortedList = new ArrayList<>();
        for (double value : values) {
            if (FactoryProperties.ABSOLUT) {
                sortedList.add(Math.abs(value));
            } else {
                sortedList.add(value);
            }
        }
        Collections.sort(sortedList);

        // Piecewise linear function - R-5
        int    size = sortedList.size();
        double q25  = size * 0.25d + 0.5d;
        double q75  = size * 0.75d + 0.5d;

        int posQ25 = (int) Math.floor(q25);
        int posQ75 = (int) Math.floor(q75);

        double v1 = sortedList.get(posQ25 - 1);
        double v2 = sortedList.get(posQ25);
        q25 = v1 + (q25 - posQ25) * (v2 - v1);

        double w1 = sortedList.get(posQ75 - 1);
        double w2 = sortedList.get(posQ75);
        q75 = w1 + (q75 - posQ75) * (w2 - w1);

        // Innerhalb des IQR liegen 50 % aller Messwerte
        return q75 - q25;  // interquartile range = Interquartilsabstand = Q3-Q1 = Q0,75-Q0,25

        // Im Verhältnis zu anderen Streuungsmaßen, wie beispielsweise Mittelwert, Median oder auch Modus,
        // ist der Interquartilsabstand (engl. interquartile range, IQR) am wenigsten anfällig für Ausreißer.
        // Daher ist der Interquartilsabstand neben der mittleren absoluten Abweichung einer der besten
        // robusten Schätzer.
        // http://math.stackexchange.com/questions/65391/calculating-interquartile-range
        // https://en.wikipedia.org/wiki/Quantile#Estimating_the_quantiles_of_a_population - Type R-5
    }


    // time-domain
    public static double matValue(double[] values, double mean) {
        if (values.length < FactoryProperties.WINDOW_MINIMUM_VALUES) { return 0.0d; }

        double mat = 0.0d;  // Mean absolute deviation = average absolute deviation

        for (double d : values) {
            if (FactoryProperties.ABSOLUT) {
                mat += Math.abs(Math.abs(d) - mean);
            } else {
                mat += Math.abs(d - mean);
            }
        }
        mat /= (double) (values.length);

        return mat; // The Mean Absolute Deviation (MAD) of a set of data is the average distance between each data value and the mean
    }


    // time-domain
    public static double kurtosisValue(double[] values, double mean) {
        if (values.length < FactoryProperties.WINDOW_MINIMUM_VALUES) { return 0.0d; }

        double varianz = Measures.varianceValue(values, mean, false);
        double sd      = Measures.standardDeviationValue(varianz);

        double kurtosis = 0.0d; // Wölbung, Kyrtosis, Kurtosis oder auch Kurtose

        // kurtosis
        for (double d : values) {
            if (FactoryProperties.ABSOLUT) {
                kurtosis += Math.pow((Math.abs(d) - mean) / sd, 4);
            } else {
                kurtosis += Math.pow((d - mean) / sd, 4);
            }
        }
        kurtosis /= (double) (values.length);

        if(Double.isNaN(kurtosis)) {
            return 0.0d;
        }

        return kurtosis;    // Maßzahl für die Steilheit bzw. Spitzigkeitâ€œ einer Häufigkeitsverteilung
    }


    // time-domain
    public static double correlationCoefficientValue(double[] x, double[] y, double meanX, double meanY) {
        if (x.length < FactoryProperties.WINDOW_MINIMUM_VALUES || x.length != y.length) { return 0.0d; }

        double cc;   // nach Pearson

        double a;
        double b;
        double sumAsq = 0.0d;
        double sumBsq = 0.0d;
        double sumAB  = 0.0d;

        for (int i = 0; i < x.length; i++) {
            if (FactoryProperties.ABSOLUT) {
                a = Math.abs(x[i]) - meanX;
                b = Math.abs(y[i]) - meanY;
            } else {
                a = x[i] - meanX;
                b = y[i] - meanY;
            }
            sumAsq += a * a;
            sumBsq += b * b;
            sumAB += a * b;
        }

        cc = (sumAB) / (Math.sqrt(sumAsq * sumBsq));

        if (Double.isNaN(cc)) {
            return 0.0d;
        }
        // Wenn der Wert der einen Variablen, z.B. von X um eine Einheit ansteigt, dann verÃ¤ndert sich der Wert der anderen Variablen, also Y, um Ï� Einheiten. Je nach dem Vorzeichen geht der Wert von Y um Ï� Einheiten nach oben oder nach unten
        // http://www.uni-wuerzburg.de/fileadmin/10040800/user_upload/hain/SPSS/Abhaengigkeit.pdf

        return cc;
    }


    // time-domain
    public static double gravityMeanValue(double[] values) {
        if (values.length < FactoryProperties.WINDOW_MINIMUM_VALUES) { return 0.0d; }

        double gravityMean = 0.0d;

        for (double d : values) {
            gravityMean += d;
        }
        gravityMean /= (double) values.length;

        if (Double.isNaN(gravityMean)) {
            return 0.0d;
        }

        return gravityMean;
    }


    // time-domain
    public static Double orientationValue(double a, double b) {
        // if(values.length < FactoryProperties.WINDOW_MINIMUM_VALUES) { return 0.0d; }

        // int pos = values.length / 2 + 1;
        // double a = values[pos];
        // double b = tmp[pos];

        // double a = gravityValue(values);
        // double b = gravityValue(tmp);

        double c = Math.sqrt(a * a + b * b);

        if (a == 0.0d || c == 0.0d || Double.isNaN(c)) { return 0.0d; }

        double orientation = (180.0d / Math.PI) * Math.acos((a * a + c * c - b * b) / (2 * a * c));

        if (b < 0.0d) {
            orientation *= -1;
        }

        return orientation;
    }


    // time-domain
    public static double entropyValueTime(double[] values) {
        if (values.length == 0) { return 0.0d; }

        double               entropy  = 0.0d;
        Map<Double, Integer> relative = new HashMap<>();

        for (double value : values) {
            if (!relative.containsKey(value)) {
                relative.put(value, 0);
            }
            relative.put(value, relative.get(value) + 1);
        }

        for (Double key : relative.keySet()) {
            entropy += (relative.get(key) / (double) values.length) * Math.log(((double) values.length) / relative.get(key));
        }

        return entropy;
    }


    // frequency-domain
    public static double energyValue(double[] values) // Parseval's theorem - https://de.wikipedia.org/wiki/Satz_von_Parseval
    {
        if (values.length <= 2) { return 0.0d; }

        double energy = 0.0d;

        double[] fftValues = new double[(values.length) - 2]; // -2 because we exclude the DC component, which is the first component of the transformed series
        System.arraycopy(values, 2, fftValues, 0, (values.length) - 2);

        for (int i = 0; i <= (fftValues.length / 2) - 1; i++) {
            double re = fftValues[2 * i];
            double im = fftValues[2 * i + 1];
            energy += ((re * re) + (im * im));

            // double magnitude = Math.sqrt((re * re) + (im * im));
            // energy += magnitude * magnitude;
        }

        return energy / (double) ((fftValues.length / 2) - 2);
    }


    // frequency-domain
    public static double entropyValueFrequency(double[] values) {
        if (((values.length / 2) - 2) <= 0) { return 0.0d; }

        double   entropy   = 0.0d;
        double[] fftValues = new double[(values.length / 2) - 2]; // -2 because we exclude the DC component, which is the first component of the transformed series
        System.arraycopy(values, 2, fftValues, 0, (values.length / 2) - 2);

        Map<Double, Integer> relative = new HashMap<>();

        for (int i = 0; i <= (fftValues.length / 2) - 1; i++) {
            double re           = fftValues[2 * i];
            double im           = fftValues[2 * i + 1];
            double vectorLength = Math.sqrt((re * re) + (im * im));

            if (!relative.containsKey(vectorLength)) {
                relative.put(vectorLength, 0);
            }
            relative.put(vectorLength, relative.get(vectorLength) + 1);
        }

        for (Double key : relative.keySet()) {
            entropy += (relative.get(key) / (double) (fftValues.length / 2)) * Math.log(((double) (fftValues.length / 2)) / relative.get(key));
        }

        return entropy;
    }


    // frequency-domain
    public static double meanDcValue(double[] values) {
        if (values.length == 0) { return 0.0d; }

        double re = values[0];
        double im = values[1];
        return Math.sqrt((re * re) + (im * im));
    }


    // time-domain -> frequency-domain
    public static double[] fft(double[] values) // The difference between this method and the method provided by wolframalpha is a scaling factor of sqrt(N) and inverse
    {
        if (values.length == 0) { return new double[0]; }

        double[] result = new double[values.length * 2];
        System.arraycopy(values, 0, result, 0, values.length);

        DoubleFFT_1D fft = new DoubleFFT_1D(values.length);
        fft.realForwardFull(result);    // Die FOURIERkoeffizienten repräsentieren ein diskretes Spektrum. Für reelle Funktionen sind sie symmetrisch.
        // fft.realInverseFull(result, false);

        // DC component: http://dsp.stackexchange.com/questions/10254/why-is-x0-the-dc-component/10255#10255

        return result;
    }


    // high-level features
    public static int discretizedOrientationValue(double[] orientation) // 0 = azimut, 1 = roll, 2 = pitch - https://upload.wikimedia.org/wikipedia/commons/thumb/a/a1/Eulerangles.svg/2000px-Eulerangles.svg.png
    {
        double roll  = orientation[1];
        double pitch = orientation[2];

        ////
        if (Utils.bet(roll, -22.5, 22.5, 157.5, 180.00, -180.00, -157.5) && Utils.bet(pitch, 67.5, 112.5, -112.5, -67.5)) {
            return 0;
        }

        if (Utils.bet(roll, 22.5, 67.5, -157.5, -112.5) && Utils.bet(pitch, 67.5, 112.5, -112.5, -67.5)) { return 1; }

        if (Utils.bet(roll, 67.5, 112.5, -112.5, -67.5) && Utils.bet(pitch, 67.5, 112.5, -112.5, -67.5)) { return 2; }

        if (Utils.bet(roll, 112.5, 157.5, -67.5, -22.5) && Utils.bet(pitch, 67.5, 112.5, -112.5, -67.5)) { return 3; }

        ////
        if (Utils.bet(roll, -157.5, -112.5, 22.5, 67.5) && Utils.bet(pitch, 112.5, 157.5, -67.5, -22.5)) { return 4; }

        if (Utils.bet(roll, -112.5, -67.5, 67.5, 112.5) && Utils.bet(pitch, 112.5, 157.5, -67.5, -22.5)) { return 5; }

        if (Utils.bet(roll, -67.5, -22.5, 112.5, 157.5) && Utils.bet(pitch, 112.5, 157.5, -67.5, -22.5)) { return 6; }

        if (Utils.bet(roll, 157.5, 180.0, -180.0, -157.5, -22.5, 22.5) && Utils.bet(pitch, 112.5, 157.5, -67.5, -22.5)) {
            return 7;
        }

        //////
        if (Utils.bet(roll, -157.5, -112.5, 22.5, 67.5) && Utils.bet(pitch, 157.5, 180.0, -180.0, -157.5, -22.5, 22.5)) {
            return 8;
        }

        if (Utils.bet(roll, -112.5, -67.5, 67.5, 112.5) && Utils.bet(pitch, 157.5, 180.0, -180.0, -157.5, -22.5, 22.5)) {
            return 9;
        }

        if (Utils.bet(roll, -67.5, -22.5, 112.5, 157.5) && Utils.bet(pitch, 157.5, 180.0, -180.0, -157.5, -22.5, 22.5)) {
            return 10;
        }

        if (Utils.bet(roll, 157.5, 180.0, -180.0, -157.5, -22.5, 22.5) && Utils.bet(pitch, 157.5, 180.0, -180.0, -157.5, -22.5, 22.5)) {
            return 11;
        }

        ////
        if (Utils.bet(roll, -157.5, -112.5, 22.5, 67.5) && Utils.bet(pitch, -157.5, -112.5, 22.5, 67.5)) { return 12; }

        if (Utils.bet(roll, -112.5, -67.5, 67.5, 112.5) && Utils.bet(pitch, -157.5, -112.5, 22.5, 67.5)) { return 13; }

        if (Utils.bet(roll, -67.5, -22.5, 112.5, 157.5) && Utils.bet(pitch, -157.5, -112.5, 22.5, 67.5)) { return 14; }

        if (Utils.bet(roll, 157.5, 180.0, -180.0, -157.5, -22.5, 22.5) && Utils.bet(pitch, -157.5, -112.5, 22.5, 67.5)) {
            return 15;
        }

        return -1;
    }
}