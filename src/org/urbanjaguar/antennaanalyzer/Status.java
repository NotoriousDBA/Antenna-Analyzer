package org.urbanjaguar.antennaanalyzer;

/**
 * Created by chris on 6/7/19.
 */
public class Status {
    public static final Status CONNECTED = new Status("CONNECTED");
    public static final Status DISCONNECTED = new Status("DISCONNECTED");
    public static final Status START = new Status("START");
    public static final Status STOP = new Status("STOP");

    private Status(String status) {
        this.status = status;
    }

    private String status;

    public String toString() {
        return status;
    }
}
