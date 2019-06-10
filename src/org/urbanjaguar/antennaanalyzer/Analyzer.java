package org.urbanjaguar.antennaanalyzer;

import com.fazecast.jSerialComm.SerialPort;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Scanner;

/**
 * Created by chris on 6/7/19.
 */
public class Analyzer {
    private SerialPort analyzer;
    private String portDescription;
    private Status status;
    private ArrayList<StatusListener> statusListeners;
    private ArrayList<LogListener> logListeners;
    private ArrayList<DataListener> dataListeners;

    public Analyzer(String portDescription) {
        this.portDescription = portDescription;
        this.analyzer = null;
        this.status = Status.DISCONNECTED;
        this.statusListeners = new ArrayList<>();
        this.logListeners = new ArrayList<>();
        this.dataListeners = new ArrayList<>();
    }

    public boolean isConnected() {
        return status != Status.DISCONNECTED;
    }

    synchronized void addDataListener (DataListener listener) {
        dataListeners.add(listener);
    }

    synchronized void removeDataListener (DataListener listener) {
        dataListeners.remove(listener);
    }

    synchronized void addLogListener (LogListener listener) {
        logListeners.add(listener);
    }

    synchronized void removeLogListener (LogListener listener) {
        logListeners.remove(listener);
    }

    synchronized void addStatusListener(StatusListener listener) {
        statusListeners.add(listener);
    }

    synchronized void removeStatusListener (StatusListener listener) {
        statusListeners.remove(listener);
    }

    private synchronized void fireDataEvent(FreqSWR data) {
        DataEvent _data = new DataEvent(this, data);
        for (DataListener listener:dataListeners) {
            listener.dataReceived(_data);
        }
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

    synchronized void connect (String portDescription) {
        this.portDescription = portDescription;
        connect();
    }
    synchronized void connect() {
        if (analyzer != null) {
            try {
                analyzer.closePort();
            } catch (Exception e) {
                // Ignore
            }
            analyzer = null;
        }

        // Look for an analyzer connect to the system.
        analyzer = findAnalyzer();

        if (analyzer != null) {
            fireLogEvent("found analyzer\nopening port");
            if (analyzer.openPort()) {
                fireLogEvent("connected");
                analyzer.setComPortTimeouts(
                        SerialPort.TIMEOUT_READ_SEMI_BLOCKING, 500, 500);
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

        fireLogEvent("starting port scan");

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

        fireLogEvent("scan complete");

        return analyzer;
    }

    synchronized void sweep(Float lowFreq, Float highFreq, Integer numSteps) {
        String input;
        String command;

        // For writing the commands to tell the analyzer to start the sweep.
        OutputStream out = analyzer.getOutputStream();

        // For reading the results of the sweep from the analyzer.
        InputStream in = analyzer.getInputStream();
        Scanner inputScanner = new Scanner(in);
        inputScanner.useDelimiter("\0");

        command = String.format("%08dA\0%08dB\0%04dN\0S\0",
                (long)(lowFreq * 1000000.0f), (long)(highFreq * 1000000.0f), numSteps);

        setStatus(Status.START);
        fireLogEvent("starting sweep from " + lowFreq + " to " + highFreq + " (" + numSteps + " steps)");

        try {
            out.write(command.getBytes());
        } catch (IOException e) {
            fireLogEvent("IOException writing to analyzer: " + e.toString());
            try {in.close();} catch (IOException e2) {;}
            try {out.close();} catch (IOException e2) {;}
            analyzer.closePort();
            analyzer = null;
            setStatus(Status.DISCONNECTED);
        }

        if (status.equals(Status.START)) {
            while (true) {
                input = inputScanner.next();

                if (input.equals("End")) {
                    fireLogEvent("sweep finished");
                    break;
                }

                FreqSWR data = new FreqSWR(input);
                fireDataEvent(data);
            }

            try {
                in.close();
                out.close();
            } catch (IOException e) {
                fireLogEvent("IOException closing analyzer streams: " + e.toString());
                analyzer.closePort();
                analyzer = null;
                setStatus(Status.DISCONNECTED);
            }
        }

        if (status.equals(Status.START)) {
            setStatus(Status.STOP);
        }
    }
}
