package org.urbanjaguar.antennaanalyzer;

import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.input.MouseEvent;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Hashtable;
import java.util.Properties;

import static org.urbanjaguar.antennaanalyzer.Bands.BAND_LABEL;

public class AnalyzerController implements StatusListener, LogListener, DataListener {
    private static final String CONFIGFILE = "analyzer.config";
    public TableView<BandSweepInfo> tblSummary;
    public TableColumn<BandSweepInfo,String> tcRun, tcBand, tcLow, tcLowSWR, tcLowSWRFreq, tcHigh, tcHighSWR, tcHighSWRFreq, tcAverageSWR;
    public Label lblStatus;
    public TextArea analyzerLog;
    public TextField numSteps, highFreq, lowFreq;
    public MenuItem menuConnect;
    public TabPane tabPane;
    public MenuBar menuBar;
    private Analyzer analyzer;
    public Button btnStart, btnReset, btnClear;
    private int numBands = 0;
    private int runCount = 0;
    public Tab tabControlPanel, tabSummary;
    public MenuItem menuExit;
    private Hashtable<Bands.BAND,CheckBox> bandList;
    private Hashtable<Bands.BAND,Tab> bandTabs;
    private Hashtable<Bands.BAND,XYChart.Series<Number,Number>> bandSeries = new Hashtable<>();
    private Hashtable<Bands.BAND,SweepInfo> bandSweepInfo = new Hashtable<>();
    private Bands.BAND activeBand;
    public CheckBox cbAllBands, cb10m, cb12m, cb15m, cb17m, cb20m, cb30m, cb40m, cb60m, cb80m, cb160m, cbCustom;
    private Properties config;

    private Tab chartFactory(Bands.BAND band) {
        float lowFreq, highFreq;

        // Create the axes for the chart.
        NumberAxis freqAxis;

        if (band == Bands.BAND.CUSTOM) {
            lowFreq = Float.parseFloat(this.lowFreq.getText());
            highFreq = Float.parseFloat(this.highFreq.getText());
        } else {
            lowFreq = Bands.BAND_LOWER_FREQ.get(band);
            highFreq = Bands.BAND_UPPER_FREQ.get(band);
        }

        freqAxis = new NumberAxis(lowFreq, highFreq, (highFreq - lowFreq)/20);

        NumberAxis vswrAxis = new NumberAxis(1.0f, 5.0f, .25);

        freqAxis.setLabel("Frequency/MHz");
        freqAxis.setAnimated(false); // axis animations are removed
        vswrAxis.setLabel("SWR");
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
            bandChart.setTitle("SWR from " + lowFreq + "MHz to " + highFreq + "MHz");
        } else {
            bandChart.setTitle("SWR on the " + BAND_LABEL.get(band) + " Band");
        }

        // Create the tab for the chart.
        Tab bandTab = new Tab(BAND_LABEL.get(band));
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

        lowFreq.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d{0,2}([\\.]\\d{0,6})?")) {
                lowFreq.setText(oldValue);
            }
        });

        highFreq.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d{0,2}([\\.]\\d{0,6})?")) {
                highFreq.setText(oldValue);
            }
        });

        numSteps.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d{1,4}")) {
                numSteps.setText(oldValue);
            }
        });

        tblSummary.setEditable(true);

        tcRun = new TableColumn<>("Run");
        tcRun.setEditable(true);
        tcRun.setCellFactory(TextFieldTableCell.forTableColumn());
        tcRun.setOnEditCommit(
            new EventHandler<TableColumn.CellEditEvent<BandSweepInfo, String>>() {
                @Override
                public void handle(TableColumn.CellEditEvent<BandSweepInfo, String> t) {
                    ((BandSweepInfo) t.getTableView().getItems().get(
                        t.getTablePosition().getRow())
                        ).setRunNumber(t.getNewValue());
                }
            }
        );

        tcBand = new TableColumn<>("Band");
        tcLow = new TableColumn<>("Low SWR");
        tcLowSWR = new TableColumn<>("SWR");
        tcLowSWRFreq = new TableColumn<>("Frequency");
        tcHigh = new TableColumn<>("High SWR");
        tcHighSWR = new TableColumn<>("SWR");
        tcHighSWRFreq = new TableColumn<>("Frequency");
        tcAverageSWR = new TableColumn<>("Average SWR");

        tcRun.setCellValueFactory(new PropertyValueFactory<>("runNumber"));
        tcBand.setCellValueFactory(new PropertyValueFactory<>("bandName"));
        tcLowSWR.setCellValueFactory(new PropertyValueFactory<>("lowSWR"));
        tcLowSWRFreq.setCellValueFactory(new PropertyValueFactory<>("lowSWRFreq"));
        tcHighSWR.setCellValueFactory(new PropertyValueFactory<>("highSWR"));
        tcHighSWRFreq.setCellValueFactory(new PropertyValueFactory<>("highSWRFreq"));
        tcAverageSWR.setCellValueFactory(new PropertyValueFactory<>("averageSWR"));

        tcLow.getColumns().addAll(tcLowSWR, tcLowSWRFreq);
        tcHigh.getColumns().addAll(tcHighSWR, tcHighSWRFreq);

        tblSummary.getColumns().addAll(tcRun, tcBand, tcLow, tcHigh, tcAverageSWR);
    }

    @Override
    public void logReceived(LogEvent event) {
        PlatformHelper.run(() -> {
            analyzerLog.appendText(event.message());
        });
    }

    private void updateStatus (String status) {
        PlatformHelper.run(() -> {
            lblStatus.setText(status);
        });
    }

    @Override
    public void StatusReceived(StatusEvent event) {
        switch (event.status().toString()) {
            case "CONNECTED":
                updateStatus("Analyzer Connected");
                break;
            case "DISCONNECTED":
                updateStatus("Analyzer Not Connected");
                break;
            case "START":
                updateStatus("Sweep Started");
                break;
            case "STOP":
                updateStatus("Sweep Finished");
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
        float freq, vswr;
        freq = event.data().getFrequency();
        vswr = event.data().getVSWR();
        bandSweepInfo.get(activeBand).update(freq, vswr);
        PlatformHelper.run(() -> {
            bandSeries.get(activeBand).getData().add(new XYChart.Data<>(freq, vswr));
        });
    }

    private void preSweep () {
        // Disable all of the controls that should be disabled during a sweep.
        btnStart.setDisable(true);
        cbAllBands.setDisable(true);
        for (CheckBox bandBox: bandList.values()) {
            bandBox.setDisable(true);
        }
        menuBar.setDisable(true);
        numSteps.setDisable(true);
        btnReset.setDisable(true);
        lowFreq.setDisable(true);
        highFreq.setDisable(true);

        // Remove all existing band tabs.
        for (Tab bandTab:bandTabs.values()) {
            bandTab.setClosable(true);
            tabPane.getTabs().remove(bandTab);
        }
        bandTabs.clear();
    }

    private void runSweep() {
        Platform.runLater(this::preSweep);
        for (Bands.BAND band: Bands.BAND.values()) {
            if (bandList.get(band).isSelected()) {
                activeBand = band;

                bandSweepInfo.put(band, new SweepInfo());

                Platform.runLater(() -> {
                    Tab bandTab = chartFactory(band);
                    bandTabs.put(band, bandTab);
                    tabPane.getTabs().add(bandTab);
                    tabPane.getSelectionModel().select(bandTab);
                });

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
            } else {
                bandSweepInfo.remove(band);
            }
        }
        Platform.runLater(this::postSweep);
        if (analyzer.isConnected()) ++runCount;
    }

    private void postSweep () {
        // Re-enable controls after a sweep.
        btnStart.setDisable(false);
        cbAllBands.setDisable(false);
        for (CheckBox bandBox: bandList.values()) {
            bandBox.setDisable(false);
        }
        btnReset.setDisable(false);
        menuBar.setDisable(false);
        numSteps.setDisable(false);
        CustomAction(null);
        // Populate data in table on summary tab.
        if (analyzer.isConnected()) {
            tabSummary.setDisable(false);
            for (Bands.BAND band : Bands.BAND.values()) {
                if (bandSweepInfo.containsKey(band) && bandSweepInfo.get(band).isValid()) {
                    tblSummary.getItems().add(new BandSweepInfo(runCount, band, bandSweepInfo.get(band)));
                }
            }
        }
    }

    public void ResetClick(MouseEvent mouseEvent) {
        numSteps.setText(config.getProperty("NUMSTEPS"));
    }

    public void StartClick(MouseEvent mouseEvent) {
        if (analyzer.isConnected()) {
            Task<Void> sweep = new Task<Void>() {
                @Override
                public Void call() {
                    runSweep();
                    return null;
                }
            };
            new Thread(sweep).start();
        }
    }

    public void ClearClick(MouseEvent mouseEvent) {
        tblSummary.getItems().clear();
        tabSummary.setDisable(true);
        runCount = 0;
        tabPane.getSelectionModel().select(0);
    }

    public static class BandSweepInfo {
        private final SimpleStringProperty runNumber;
        private final SimpleStringProperty bandName;
        private final SimpleStringProperty lowSWR;
        private final SimpleStringProperty lowSWRFreq;
        private final SimpleStringProperty highSWR;
        private final SimpleStringProperty highSWRFreq;
        private final SimpleStringProperty averageSWR;

        private BandSweepInfo (int runNumber, Bands.BAND band, SweepInfo sweepInfo) {
            this.runNumber = new SimpleStringProperty(Integer.toString(runNumber));
            this.bandName = new SimpleStringProperty(Bands.BAND_LABEL.get(band));
            this.lowSWR = new SimpleStringProperty(String.format("%1.2f:1", sweepInfo.getLowSWR()));
            this.lowSWRFreq = new SimpleStringProperty(String.format("%2.6f", sweepInfo.getLowSWRFreq()));
            this.highSWR = new SimpleStringProperty(String.format("%1.2f:1", sweepInfo.getHighSWR()));
            this.highSWRFreq = new SimpleStringProperty(String.format("%2.6f", sweepInfo.getHighSWRFreq()));
            this.averageSWR = new SimpleStringProperty(String.format("%1.2f:1", sweepInfo.getAverageSWR()));
        }

        public String getRunNumber() {
            return runNumber.get();
        }

        public void setRunNumber(String runNumber) {
            this.runNumber.set(runNumber);
        }

        public String getBandName() {
            return bandName.get();
        }

        public void setBandName(String bandName) {
            this.bandName.set(bandName);
        }

        public String getLowSWR() {
            return lowSWR.get();
        }

        public void setLowSWR(String lowSWR) {
            this.lowSWR.set(lowSWR);
        }

        public String getLowSWRFreq() {
            return lowSWRFreq.get();
        }

        public void setLowSWRFreq(String lowSWRFreq) {
            this.lowSWRFreq.set(lowSWRFreq);
        }

        public String getHighSWR() {
            return highSWR.get();
        }

        public void setHighSWR(String highSWR) {
            this.highSWR.set(highSWR);
        }

        public String getHighSWRFreq() {
            return highSWRFreq.get();
        }

        public void setHighSWRFreq(String highSWRFreq) {
            this.highSWRFreq.set(highSWRFreq);
        }

        public String getAverageSWR() {
            return averageSWR.get();
        }

        public void setAverageSWR(String averageSWR) {
            this.averageSWR.set(averageSWR);
        }
    }
}
