package org.urbanjaguar.antennaanalyzer;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Properties;

public class AnalyzerController implements StatusListener, LogListener {
    private static final String CONFIGFILE = "analyzer.config";
    public Label lblStatus;
    public ProgressBar progress;
    public TextArea analyzerLog;
    public TextField numSteps;
    public MenuItem menuConnect;
    private Analyzer analyzer;
    public TextField highFreq;
    public TextField lowFreq;
    public Button btnStart;
    public Button btnReset;
    private int numBands = 0;
    public Tab tabControlPanel;
    public Tab tabSummary;
    public MenuItem menuExit;
    private ArrayList<CheckBox> bandList;
    public CheckBox cbAllBands;
    public CheckBox cb10m;
    public CheckBox cb12m;
    public CheckBox cb15m;
    public CheckBox cb17m;
    public CheckBox cb20m;
    public CheckBox cb30m;
    public CheckBox cb40m;
    public CheckBox cb60m;
    public CheckBox cb80m;
    public CheckBox cb160m;
    public CheckBox cbCustom;
    private Properties config;

    private void getDefaultConfig(Properties config) {
        config.setProperty("NUMSTEPS", "100");
        config.setProperty("PORTDESCRIPTION", "SparkFun Pro Micro");
    }

    private void loadConfig() {
        config = new Properties();
        getDefaultConfig(config);

        try {
            FileInputStream in = new FileInputStream(CONFIGFILE);
            config.load(in);
            in.close();
        } catch (FileNotFoundException e) {
            // do nothing
        } catch (IOException e) {
            Alert alert = new Alert(Alert.AlertType.ERROR, "Error reading " + CONFIGFILE + ": \n" + e.toString(),ButtonType.CLOSE);
            alert.showAndWait();
        }
    }

    @FXML
    public void initialize() {
        loadConfig();
        numSteps.setText(config.getProperty("NUMSTEPS"));
        bandList = new ArrayList<>(Arrays.asList(cb10m, cb12m, cb15m, cb17m, cb20m, cb30m, cb40m, cb60m, cb80m, cb160m));
        analyzer = new Analyzer(config.getProperty("PORTDESCRIPTION"));
        analyzer.addLogListener(this);
        analyzer.addStatusListener(this);
        analyzer.connect();
    }

    @Override
    public void logReceived(LogEvent event) {
        analyzerLog.appendText(event.message());
    }

    @Override
    public void StatusReceived(StatusEvent event) {
        switch (event.status().toString()) {
            case "CONNECTED":
                lblStatus.setText("Analyzer Connected");
                break;
            case "DISCONNECTED":
                lblStatus.setText("Analyzer Not Connected");
                break;
            case "START":
                lblStatus.setText("Sweep Started");
                break;
            case "STOP":
                lblStatus.setText("Sweep Finished");
                break;
        }
    }

    public void AllBandsClick(MouseEvent mouseEvent) {
        for (CheckBox band:bandList) {
            band.setSelected(cbAllBands.isSelected());
        }

        if (cbAllBands.isSelected()) {
            cbCustom.setSelected(false);
            CustomAction(null);
            numBands = 10;
            btnStart.setDisable(false);
        } else {
            numBands = 0;
            if (cbCustom.isSelected() == false) {
                btnStart.setDisable(true);
            }
        }
    }

    public void BandClick(MouseEvent mouseEvent) {
        if (((CheckBox)mouseEvent.getSource()).isSelected()) {
            if (++numBands == 10) {
                cbAllBands.setSelected(true);
            }
            cbCustom.setSelected(false);
            CustomAction(null);
            btnStart.setDisable(false);
        } else {
            if (cbAllBands.isSelected()) {
                cbAllBands.setSelected(false);
            }
            if (--numBands == 0 && !cbCustom.isSelected()) {
                btnStart.setDisable(true);
            }
        }
    }

    public void CustomAction(ActionEvent actionEvent) {
        if (cbCustom.isSelected()) {
            cbAllBands.setSelected(false);
            AllBandsClick(null);
            lowFreq.setDisable(false);
            highFreq.setDisable(false);
            btnStart.setDisable(false);
        } else {
            lowFreq.setDisable(true);
            highFreq.setDisable(true);
            if (numBands == 0) {
                btnStart.setDisable(true);
            }
        }
    }

    public void Connect(ActionEvent actionEvent) {
        analyzer.connect();
    }
}
