package org.urbanjaguar.antennaanalyzer;

import javafx.fxml.FXML;
import javafx.scene.control.TextArea;
import javafx.scene.input.MouseEvent;
import javafx.scene.control.Button;
import javafx.stage.Stage;

/**
 * Created by chris on 6/11/19.
 */
public class AboutController {
    public TextArea aboutText;

    @FXML
    public void initialize() {
        aboutText.setText(
            "Acknowledgements\n" +
            "\n" +
            "  Many thanks to Beric Dunn (K6BEZ) for designing the analyzer and making his\n" +
            "  design available for anyone to build. Many thanks as well to George (KJ6VU)\n" +
            "  and Jeremy (KF7IJZ) of the Ham Radio Workbench podcast for their work on the\n" +
            "  project, publishing it, and for inspiring me to build the analyzer and write\n" +
            "  this program.\n" +
            "\n" +
            "About\n" +
            "\n" +
            "  This simple front-end for controlling a K6BEZ antenna analyzer was written by\n" +
            "  me, Chris Mathes (KN4SFX).  You are free to use it for whatever purpose you\n" +
            "  want, and to change the code in any way you want to suit your own needs.  This\n" +
            "  program is provided \"as is\", with no warranties that it's fit for any\n" +
            "  purpose, etc. I hope you find it useful.\n" +
            "\n" +
            "Notes\n" +
            "\n" +
            "  This program is written in Java, using a cross-platform serial communications\n" +
            "  library called jSerialComm, written by Will Hedgecock. In theory it should run\n" +
            "  without modification on any computer running a modern version of Linux,\n" +
            "  Windows, or MacOSX.\n" +
            "\n" +
            "  The microcontroller called for in the analyzer project is a SparkFun Pro Micro\n" +
            "  (or clone), and this program expects it to report itself to the system as\n" +
            "  such. If the microcontroller you used does not, that's fine, but you'll have\n" +
            "  to edit the preferences in the program to tell it what name to look for when\n" +
            "  it scans the serial ports on your system looking for the analyzer. Look at the\n" +
            "  output in the log window on the control panel tab when the program is first\n" +
            "  started. The program scans all of the serial ports on the system and outputs\n" +
            "  the string each of them uses to identify themselves to the system. That's the\n" +
            "  value you need.\n" +
            "\n" +
            "  IMPORTANT: This program depends upon the analyzer having a custom version of\n" +
            "  the firmware that I wrote, which is available in the same GitHub repository\n" +
            "  where this program is stored. My version is basically functionally identical\n" +
            "  to the standard firmware, with the main difference being that all data\n" +
            "  transmitted between the PC and the analyzer is sent using null-terminated\n" +
            "  strings. If you want to keep using a non-standard version of the firmware\n" +
            "  other than mine, you can, but you'll have to modify it to read/write\n" +
            "  null-terminated strings, and to output the sweep data in the format this\n" +
            "  program is expecting. Just take a look at the source for my firmware to see\n" +
            "  how that works.\n"
        );
    }

    public void CloseClick(MouseEvent mouseEvent) {
        Stage stage = (Stage) ((Button)mouseEvent.getSource()).getScene().getWindow();
        stage.close();
    }
}
