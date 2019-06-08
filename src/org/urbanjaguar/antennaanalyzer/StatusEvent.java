package org.urbanjaguar.antennaanalyzer;

import java.util.EventObject;

/**
 * Created by chris on 6/7/19.
 */
public class StatusEvent extends EventObject {
    private Status status;

    public StatusEvent (Object source, Status status) {
        super(source);
        this.status = status;
    }

    public Status status() {
        return status;
    }
}
