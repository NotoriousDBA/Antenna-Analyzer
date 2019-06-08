package org.urbanjaguar.antennaanalyzer;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Hashtable;
import java.util.Properties;

public class AnalyzerController implements StatusListener, LogListener, DataListener {
    private static final String CONFIGFILE = "analyzer.config";
    public Label lblStatus;
    public ProgressBar progress;
    public TextArea analyzerLog;
    public TextField numSteps, highFreq, lowFreq;
    public MenuItem menuConnect;
    public TabPane tabPane;
    private Analyzer analyzer;
    public Button btnStart, btnReset;
    private int numBands = 0;
    public Tab tabControlPanel, tabSummary;
    public MenuItem menuExit;
    private Hashtable<Bands.BAND,CheckBox> bandList;
    private Hashtable<Bands.BAND,Tab> bandTabs;
    private Hashtable<Bands.BAND,XYChart.Series<Number,Number>> bandSeries = new Hashtable<>();
    private Bands.BAND activeBand;
    public CheckBox cbAllBands, cb10m, cb12m, cb15m, cb17m, cb20m, cb30m, cb40m, cb60m, cb80m, cb160m, cbCustom;
    private Properties config;

    private Tab chartFactory(Bands.BAND band) {
        // Create the axes for the chart.
        NumberAxis freqAxis;

        if (band == Bands.BAND.CUSTOM) {
            freqAxis = new NumberAxis(
                    Float.parseFloat(lowFreq.getText()),
                    Float.parseFloat(highFreq.getText()),
                    .0005f);
        } else {
            freqAxis = new NumberAxis(
                    Bands.BAND_LOWER_FREQ.get(band),
                    Bands.BAND_UPPER_FREQ.get(band),
                   .05f);
        }

        NumberAxis vswrAxis = new NumberAxis(1.0f, 5.0f, .25);

        freqAxis.setLabel("Frequency/MHz");
        freqAxis.setAnimated(false); // axis animations are removed
        vswrAxis.setLabel("VSWR");
        vswrAxis.setAnimated(false); // axis animations are removed

        XYChart.Series<Number,Number> data = new XYChart.Series<>();
        bandSeries.put(band,data);

        // Create the chart.
        LineChart<Number,Number> bandChart = new LineChart<>(freqAxis, vswrAxis);
        bandChart.setAnimated(false);
        bandChart.getData().add(data);
        bandChart.setCreateSymbols(false);
        bandChart.setLegendVisible(false);

        if (band == Bands.BAND.CUSTOM) {
            bandChart.setTitle("VSWR from " + lowFreq + " to " + highFreq);
        } else {
            bandChart.setTitle("VSWR on the " + Bands.BAND_LABEL.get(band) + " Band");
        }

        // Create the tab for the chart.
        Tab bandTab = new Tab(Bands.BAND_LABEL.get(band));
        bandTab.setContent(bandChart);
        bandTab.setClosable(false);
        bandTab.setDisable(false);

        return bandTab;
    }

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
        bandList = new Hashtable<Bands.BAND,CheckBox>() {{
            put(Bands.BAND.BAND10M, cb10m);
            put(Bands.BAND.BAND12M, cb12m);
            put(Bands.BAND.BAND15M, cb15m);
            put(Bands.BAND.BAND17M, cb17m);
            put(Bands.BAND.BAND20M, cb20m);
            put(Bands.BAND.BAND30M, cb30m);
            put(Bands.BAND.BAND40M, cb40m);
            put(Bands.BAND.BAND60M, cb60m);
            put(Bands.BAND.BAND80M, cb80m);
            put(Bands.BAND.BAND160M, cb160m);
            put(Bands.BAND.CUSTOM, cbCustom);
        }};
        bandTabs = new Hashtable<>();
        activeBand = null;
        analyzer = new Analyzer(config.getProperty("PORTDESCRIPTION"));
        analyzer.addLogListener(this);
        analyzer.addStatusListener(this);
        analyzer.addDataListener(this);
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
        for (CheckBox band: bandList.values()) {
            if (band != cbCustom) {
                band.setSelected(cbAllBands.isSelected());
            }
        }

        if (cbAllBands.isSelected()) {
            cbCustom.setSelected(false);
            CustomAction(null);
            numBands = 10;
            btnStart.setDisable(false);
        } else {
            numBands = 0;
            if (!cbCustom.isSelected()) {
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

    @Override
    public void dataReceived(DataEvent event) {
        double freq, vswr;
        freq = event.data().getFrequency();
        vswr = event.data().getVSWR();
        bandSeries.get(activeBand).getData().add(new XYChart.Data<>(freq, vswr));
    }

    public void ResetClick(MouseEvent mouseEvent) {
        numSteps.setText(config.getProperty("NUMSTEPS"));
    }

    public void StartClick(MouseEvent mouseEvent) {
        // Run a sweep.
        btnStart.setDisable(true);

        if (analyzer.isConnected()) {
            for (Bands.BAND band: Bands.BAND.values()) {
                if (bandList.get(band).isSelected()) {
                    activeBand = band;
                    Tab bandTab = chartFactory(band);
                    bandTabs.put(band, bandTab);
                    tabPane.getTabs().add(bandTab);
                    tabPane.getSelectionModel().select(bandTab);

                    // Run the sweep for this band.
                    if (band == Bands.BAND.CUSTOM) {
                        analyzer.sweep(
                                Float.parseFloat(lowFreq.getText()),
                                Float.parseFloat(highFreq.getText()),
                                Integer.parseInt(numSteps.getText()));
                    } else {
                        analyzer.sweep(
                                Bands.BAND_LOWER_FREQ.get(band),
                                Bands.BAND_UPPER_FREQ.get(band),
                                Integer.parseInt(numSteps.getText()));
                    }

                    if (!analyzer.isConnected()) {
                        break;
                    }
                }
            }
        }
    }
}
