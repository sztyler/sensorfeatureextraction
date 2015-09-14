package de.unima.ar.collector.features;

public class Config
{
    // Window Manager
    public static long    WINDOW_SIZE           = 1000;     // milliseconds
    public static boolean WINDOW_OVERLAP        = true;
    public static double  WINDOW_OVERLAP_SIZE   = 0.5;      // percent, between 0 and 1
    public static int     WINDOW_MINIMUM_VALUES = 10;       // soviele Werte sollten mindestens in einem Fenster sein

    // Threading
    public static int MANAGER_DATA_IDLE      = 250; // milliseconds
    public static int MANAGER_ATTRIBUTE_IDLE = 100; // milliseconds
    public static int MANAGER_WINDOW_IDLE    = 50;  // milliseconds

    // Extraction
    public static final boolean LOWPASSFILTER = true;

    // Measures
    public static final boolean ABSOLUT = true;
}