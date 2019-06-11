package org.urbanjaguar.antennaanalyzer;

public class SweepInfo {
    private boolean valid;
    private int numSteps, stepCount;
    private float lowSWRFreq, highSWRFreq, lowSWR, highSWR, centerSWR, averageSWR;
    private double totalSWR;

    public SweepInfo(int numSteps) {
        valid = false;
        this.numSteps = numSteps;
        stepCount = 0;
        lowSWRFreq = 0;
        highSWRFreq = 0;
        lowSWR = 999;
        highSWR = 0;
        centerSWR = 0;
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

        if (stepCount == (int)(numSteps/2)) {
            valid = true;
            centerSWR = swr;
        }

        totalSWR += swr;
        averageSWR = (float)(totalSWR/(float)stepCount);
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

    public float getCenterSWR() {
        return centerSWR;
    }

    public float getAverageSWR() {
        return averageSWR;
    }
}
