/*
 * File: proj03EllmerLoverudeQian.Main.java
 * Names: Ian Ellmer, Leo Qian, Jasper Loverude
 * Class: CS361
 * Project 3
 * Date: 2/15/2022
 */

package proj03EllmerLoverudeQian;


import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.stage.Stage;
import javafx.scene.Parent;
import javafx.scene.Scene;

/**
 * The Main Class for loading the fxml file and building the stage
 *
 * @author (Ian Ellmer, Leo Qian, Jasper Loverude)
 */
public class Main extends Application{
    @Override
    public void start(Stage primaryStage) throws java.io.IOException {

        // Load fxml file
        Parent root = FXMLLoader.load(getClass().getResource("Main.fxml"));
        primaryStage.setTitle("Project 3");
        Scene scene = new Scene(root);

        // Load css file
        scene.getStylesheets().add(getClass().getResource("Main.css").toExternalForm());
        primaryStage.setScene(scene);

        // Set the minimum height and width of th main stage
        primaryStage.setMinHeight(250);
        primaryStage.setMinWidth(300);

        // Show the stage
        primaryStage.show();

    }

    public static void main(String[] args){
        launch(args);
    }
}
