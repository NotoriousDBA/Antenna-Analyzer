package org.urbanjaguar.antennaanalyzer;

public class SweepInfo {
    private boolean valid;
    private int stepCount;
    private float lowSWRFreq, highSWRFreq, lowSWR, highSWR, averageSWR;
    private double totalSWR;

    public SweepInfo() {
        valid = false;
        stepCount = 0;
        lowSWRFreq = 0;
        highSWRFreq = 0;
        lowSWR = 999;
        highSWR = 0;
        averageSWR = 0;
        totalSWR = 0;
    }

    public boolean isValid() {
        return valid;
    }

    public void update(float freq, float swr) {
        ++stepCount;

        if (swr < lowSWR) {
            lowSWR = swr;
            lowSWRFreq = freq;
        }

        if (swr > highSWR) {
            highSWR = swr;
            highSWRFreq = freq;
        }

        totalSWR += swr;
        averageSWR = (float)(totalSWR/(float)stepCount);

        valid = true;
    }

    public float getLowSWRFreq() {
        return lowSWRFreq;
    }

    public float getHighSWRFreq() {
        return highSWRFreq;
    }

    public float getLowSWR() {
        return lowSWR;
    }

    public float getHighSWR() {
        return highSWR;
    }

    public float getAverageSWR() {
        return averageSWR;
    }
}
