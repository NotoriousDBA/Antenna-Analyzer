package org.urbanjaguar.antennaanalyzer;

import java.util.Scanner;

/**
 * Created by chris on 6/8/19.
 */
public class FreqSWR {
    private Float frequency;
    private Float VSWR;

    public String toString() {
        return frequency.toString() + "|" + VSWR.toString();
    }

    public FreqSWR (float frequency, float VSWR) {
        this.frequency = frequency;
        this.VSWR = VSWR;
    }

    public FreqSWR (String rawData) {
        Scanner getData = new Scanner(rawData);
        getData.useDelimiter("\\|");
        this.frequency = getData.nextLong()/1000000.0f;
        this.VSWR = getData.nextInt()/1000.0f;
        getData.close();
    }

    public float getFrequency() {
        return frequency;
    }

    public float getVSWR() {
        return VSWR;
    }
}
