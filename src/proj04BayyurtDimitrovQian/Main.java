/*
 * File: proj04BayyurtDimitrovQian.Main.java
 * Names: Izge Bayyurt, Anton Dimitrov, Leo Qian
 * Class: CS361
 * Project 4
 * Date: 2/28/2022
 */

package proj04BayyurtDimitrovQian;


import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.stage.Stage;
import javafx.scene.Parent;
import javafx.scene.Scene;

import java.io.IOException;


/**
 * The Main Class for loading the fxml file and building the stage
 *
 * @author (Izge Bayyurt, Anton Dimitrov, Leo Qian)
 */

public class Main extends Application{

    @Override
    public void start(Stage primaryStage) {
        try {
            // Load fxml file
            FXMLLoader loader = new FXMLLoader((getClass().getResource("Main.fxml")));
            Parent root = loader.load();
            primaryStage.setTitle("Project 4");
            Scene scene = new Scene(root);

            //scene.getStylesheets().add(getClass().getResource("java-keywords.css").toExternalForm());

            // Load css file
            scene.getStylesheets().add(getClass().getResource("Main.css").toExternalForm());
            primaryStage.setScene(scene);

            // Set the minimum height and width of th main stage
            primaryStage.setMinHeight(250);
            primaryStage.setMinWidth(400);

            // attach an event handler with the close box of the primary stage
            Controller controller = loader.getController();
            primaryStage.setOnCloseRequest(event -> {
                controller.handleExitMenuItem(event);
                event.consume();
            });

            // Show the stage
            primaryStage.show();
        }
        catch (Exception e) {
            Controller controller = new Controller();
            controller.exceptionAlert(e);
        }


    }

    public static void main(String[] args){
        launch(args);
    }
}
