/*
 * File: proj10EnglishHillisonQian.Main.java
 * Names: Nick English, Nico Hillison, Leo Qian
 * Class: CS361
 * Project 10
 * Date: 5/5/2022
 */

package proj10EnglishHillisonQian;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * Main class initializes the base Application.
 *
 */
public class Main extends Application {
    /**
     * Constructs the base elements on the stage.
     *
     * @param stage the stage on which to build the Application.
     */
    @Override
    public void start(Stage stage) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(Main.class.getResource("Main.fxml"));
            Scene scene = new Scene(fxmlLoader.load(), 1000, 1000);
            scene.getStylesheets().add(BantamHighlighter.class.getResource(
                    "java-keywords.css").toExternalForm());
            stage.setTitle("Project 10 Nick, Nico, Leo");
            stage.setScene(scene);
            stage.show();
            stage.setOnCloseRequest(E -> {
                ((Controller) fxmlLoader.getController()).handleExit();
                E.consume();
            });
        }catch(Exception e){
            AlertHandler alertHandler = new AlertHandler();
            alertHandler.showErrorAlert("This project failed to find the fxml or css file.\n" +
                    "Ensure all files are in the same location.","Fatal Error on Launch");
        }
    }

    /**
     * Initializes the Application.
     *
     * @param args args passed on run.
     */
    public static void main(String[] args) {
        launch();
    }
}