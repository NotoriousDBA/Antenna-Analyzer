package org.urbanjaguar.antennaanalyzer;

import java.util.EventObject;

/**
 * Created by chris on 6/7/19.
 */
public class LogEvent extends EventObject {
    private String message;

    public LogEvent (Object source, String message) {
        super(source);
        this.message = message + "\n";
    }

    public String message() {
        return message;
    }

    public String toString() {
        return message;
    }
}
