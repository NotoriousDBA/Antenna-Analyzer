package org.urbanjaguar.antennaanalyzer;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

public class Main extends Application {
javafx.scene.image.Image foo;
    @Override
    public void start(Stage primaryStage) throws Exception{
        Parent root = FXMLLoader.load(getClass().getResource("AntennaAnalyzer.fxml"));
        primaryStage.setTitle("K6BEZ Antenna Analyzer");
        primaryStage.getIcons().add(new Image(Main.class.getResourceAsStream("analyzer32.png")));
        primaryStage.getIcons().add(new Image(Main.class.getResourceAsStream("analyzer64.png")));
        primaryStage.getIcons().add(new Image(Main.class.getResourceAsStream("analyzer128.png")));
        primaryStage.getIcons().add(new Image(Main.class.getResourceAsStream("analyzer256.png")));
        primaryStage.setScene(new Scene(root, 800, 600));
        primaryStage.show();
    }


    public static void main(String[] args) {
        launch(args);
    }
}
