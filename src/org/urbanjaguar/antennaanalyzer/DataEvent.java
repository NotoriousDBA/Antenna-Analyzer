package org.urbanjaguar.antennaanalyzer;

import java.util.EventObject;

/**
 * Created by chris on 6/8/19.
 */
public class DataEvent extends EventObject {
    private FreqSWR data;

    public DataEvent (Object source, FreqSWR data) {
        super(source);
        this.data = data;
    }

    public FreqSWR data() {
        return data;
    }
}
