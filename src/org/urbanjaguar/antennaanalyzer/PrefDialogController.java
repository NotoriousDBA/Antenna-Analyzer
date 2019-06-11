package org.urbanjaguar.antennaanalyzer;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;

public class PrefDialogController {
    private AnalyzerController parent;
    public TextField numSteps;
    public TextField mcuName;
    public Button btnSave;
    public Button btnCancel;

    @FXML
    public void initialize() {
        numSteps.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d{1,4}")) {
                numSteps.setText(oldValue);
            }
        });
    }

    public void SaveClick(MouseEvent mouseEvent) {
        parent.config.setProperty("NUMSTEPS", numSteps.getText());
        parent.config.setProperty("PORTDESCRIPTION", mcuName.getText());
        PlatformHelper.run(() -> {parent.saveConfig();});
        close();
    }

    public void CancelClick(MouseEvent mouseEvent) {
        close();
    }

    private void close() {
        Stage stage = (Stage) btnCancel.getScene().getWindow();
        stage.close();
    }

    public void initData(AnalyzerController analyzerController) {
        parent = analyzerController;
        numSteps.setText(parent.config.getProperty("NUMSTEPS"));
        mcuName.setText(parent.config.getProperty("PORTDESCRIPTION"));
    }
}
