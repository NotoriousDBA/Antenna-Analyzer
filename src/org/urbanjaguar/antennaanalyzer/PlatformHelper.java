package org.urbanjaguar.antennaanalyzer;

import javafx.application.Platform;

/**
 * Created by chris on 6/9/19.
 */
public class PlatformHelper {
    public static void run(Runnable treatment) {
        if (Platform.isFxApplicationThread()) {
            treatment.run();
        }
        else {
            Platform.runLater(treatment);
        }
    }
}
