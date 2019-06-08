package org.urbanjaguar.antennaanalyzer;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created by chris on 6/7/19.
 */
public class Bands {
    public enum BAND {
        BAND10M,
        BAND12M,
        BAND15M,
        BAND17M,
        BAND20M,
        BAND30M,
        BAND40M,
        BAND60M,
        BAND80M,
        BAND160M
    }

    public static final ArrayList<String> BAND_LABEL = new ArrayList<>(Arrays.asList(
            "10m", "12m", "15m", "17m", "20m", "30m", "40m", "60m", "80m", "160m"));

    public static final ArrayList<Float> BAND_LOWER_FREQ = new ArrayList<>(Arrays.asList(
        new Float(28.0), new Float(24.89), new Float(21.0), new Float(18.068), new Float(14.0),
        new Float(10.1), new Float(7.0), new Float(5.3305), new Float(3.5), new Float(1.8)));

    public static final ArrayList<Float> BAND_UPPER_FREQ = new ArrayList<>(Arrays.asList(
            new Float(29.7), new Float(24.99), new Float(21.45), new Float(18.168), new Float(14.35),
            new Float(10.15), new Float(7.3), new Float(5.4065), new Float(4.0), new Float(2.0)));
}
