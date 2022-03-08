/*
 * File: proj5CorrellHilisonQian.Main.java
 * Names: Leo Qian, Cassidy Correl, Nico Hillison
 * Class: CS361
 * Project 5
 * Date: 3/7/2022
 */

package proj5CorrellHilisonQian;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
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
            Scene scene = new Scene(fxmlLoader.load(), 500, 500);
            scene.getStylesheets().add(JavaKeywordsAsync.class.getResource("java-keywords.css").toExternalForm());
            stage.setTitle("Project 5 Cassidy, Nick, Philipp");
            stage.setScene(scene);
            stage.show();
            stage.setOnCloseRequest(E -> ((Controller) fxmlLoader.getController()).handleExit(E));
        }
        catch(Exception e){
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("Error Opening Editor");

            alert.showAndWait();

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