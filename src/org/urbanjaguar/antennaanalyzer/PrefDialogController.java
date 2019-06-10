package org.urbanjaguar.antennaanalyzer;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;

public class PrefDialogController {
    public TextField numSteps;
    public TextField mcuName;
    public Button btnSave;
    public Button btnCancel;

    @FXML
    public void initialize() {
        numSteps.setText(AnalyzerController.config.getProperty("NUMSTEPS"));
        mcuName.setText(AnalyzerController.config.getProperty("PORTDESCRIPTION"));
    }

    public void SaveClick(MouseEvent mouseEvent) {
        AnalyzerController.config.setProperty("NUMSTEPS", numSteps.getText());
        AnalyzerController.config.setProperty("PORTDESCRIPTION", mcuName.getText());
        AnalyzerController.saveConfig();
        close();
    }

    public void CancelClick(MouseEvent mouseEvent) {
        close();
    }

    private void close() {
        Stage stage = (Stage) btnCancel.getScene().getWindow();
        stage.close();
    }
}
