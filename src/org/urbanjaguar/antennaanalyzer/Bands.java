package org.urbanjaguar.antennaanalyzer;

import java.util.Hashtable;

import static org.urbanjaguar.antennaanalyzer.Bands.BAND.*;

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
        BAND160M,
        CUSTOM
    }

    public static final Hashtable<BAND,String> BAND_LABEL = new Hashtable<BAND,String>() {{
        put(BAND10M, "10m");
        put(BAND12M, "12m");
        put(BAND15M, "15m");
        put(BAND17M, "17m");
        put(BAND20M, "20m");
        put(BAND30M, "30m");
        put(BAND40M, "40m");
        put(BAND60M, "60m");
        put(BAND80M, "80m");
        put(BAND160M, "160m");
        put(CUSTOM, "Custom");
    }};

    public static final Hashtable<BAND,Float> BAND_LOWER_FREQ = new Hashtable<BAND,Float>() {{
        put(BAND10M, 28.0f);
        put(BAND12M, 24.89f);
        put(BAND15M, 21.0f);
        put(BAND17M, 18.068f);
        put(BAND20M, 14.0f);
        put(BAND30M, 10.1f);
        put(BAND40M, 7.0f);
        put(BAND60M, 5.3305f);
        put(BAND80M, 3.5f);
        put(BAND160M, 1.8f);
    }};

    public static final Hashtable<BAND,Float> BAND_UPPER_FREQ = new Hashtable<BAND,Float>() {{
        put(BAND10M, 29.7f);
        put(BAND12M, 24.99f);
        put(BAND15M, 21.45f);
        put(BAND17M, 18.168f);
        put(BAND20M, 14.35f);
        put(BAND30M, 10.15f);
        put(BAND40M, 7.3f);
        put(BAND60M, 5.4065f);
        put(BAND80M, 4.0f);
        put(BAND160M, 2.0f);
    }};
}
