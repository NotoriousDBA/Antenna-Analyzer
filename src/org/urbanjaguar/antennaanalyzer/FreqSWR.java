package org.urbanjaguar.antennaanalyzer;

import java.util.Scanner;

/**
 * Created by chris on 6/8/19.
 */
public class FreqSWR {
    private Double frequency;
    private Double VSWR;

    public String toString() {
        return frequency.toString() + "|" + VSWR.toString();
    }

    public FreqSWR (double frequency, double VSWR) {
        this.frequency = frequency;
        this.VSWR = VSWR;
    }

    public FreqSWR (String rawData) {
        Scanner getData = new Scanner(rawData);
        getData.useDelimiter("\\|");
        this.frequency = getData.nextLong()/1000000.0d;
        this.VSWR = getData.nextInt()/1000.0d;
        getData.close();
    }

    public double getFrequency() {
        return frequency;
    }

    public double getVSWR() {
        return VSWR;
    }
}
