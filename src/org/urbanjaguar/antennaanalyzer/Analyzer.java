package org.urbanjaguar.antennaanalyzer;

import com.fazecast.jSerialComm.SerialPort;

import java.util.ArrayList;

/**
 * Created by chris on 6/7/19.
 */
public class Analyzer {
    private SerialPort analyzer;
    private String portDescription;
    private int lowFreq;
    private int highFreq;
    private int numSteps;
    private Status status;
    private ArrayList<StatusListener> statusListeners;
    private ArrayList<LogListener> logListeners;

    public Analyzer(String portDescription) {
        this.portDescription = portDescription;
        this.lowFreq = 0;
        this.highFreq = 0;
        this.numSteps = 0;
        this.analyzer = null;
        this.status = Status.DISCONNECTED;
        this.statusListeners = new ArrayList<>();
        this.logListeners = new ArrayList<>();
    }

    public synchronized void addLogListener (LogListener listener) {
        logListeners.add(listener);
    }

    public synchronized void removeLogListener (LogListener listener) {
        logListeners.remove(listener);
    }

    public synchronized void addStatusListener (StatusListener listener) {
        statusListeners.add(listener);
    }

    public synchronized void removeStatusListener (StatusListener listener) {
        statusListeners.remove(listener);
    }

    private synchronized void fireLogEvent(String message) {
        LogEvent _message = new LogEvent(this, message);
        for (LogListener listener:logListeners) {
            listener.logReceived(_message);
        }
    }

    private synchronized void fireStatusEvent() {
        StatusEvent status = new StatusEvent(this, this.status);
        for (StatusListener listener:statusListeners) {
            listener.StatusReceived(status);
        }
    }

    private synchronized void setStatus (Status status) {
        this.status = status;
        fireStatusEvent();
    }

    public synchronized void connect() {
        // Look for an analyzer connect to the system.
        analyzer = findAnalyzer();

        if (analyzer != null) {
            fireLogEvent("found analyzer\nopening port");
            if (analyzer.openPort()) {
                fireLogEvent("connected");
                setStatus(Status.CONNECTED);
            } else {
                fireLogEvent("open failed!");
                setStatus(Status.DISCONNECTED);
            }
        } else {
            fireLogEvent("analyzer not found");
            setStatus(Status.DISCONNECTED);
        }
    }

    private synchronized SerialPort findAnalyzer() {
        String message;
        String portDesc;
        String portName;
        SerialPort analyzer = null;

        for (SerialPort port: SerialPort.getCommPorts()) {
            portDesc = port.getPortDescription();
            portName = port.getSystemPortName();
            message = "port: " + portName + " description: " + portDesc;
            if (portDesc.equals(this.portDescription) && analyzer == null) {
                analyzer = port;
                message = message + " <-----";
            }
            fireLogEvent(message);
        }

        return analyzer;
    }

    public int getLowFreq() {
        return lowFreq;
    }

    public void setLowFreq(int lowFreq) {
        this.lowFreq = lowFreq;
    }

    public int getHighFreq() {
        return highFreq;
    }

    public void setHighFreq(int highFreq) {
        this.highFreq = highFreq;
    }

    public int getNumSteps() {
        return numSteps;
    }

    public void setNumSteps(int numSteps) {
        this.numSteps = numSteps;
    }
}
