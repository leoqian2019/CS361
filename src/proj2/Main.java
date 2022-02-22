/*
 * File: proj2EnglishQianYuZhang.Main.java
 * Names: Nick English, Leo Qian, Alex Yu, Chloe Zhang
 * Class: CS361
 * Project 2
 * Date: 2/14/2022
 */

package proj2;

import javafx.application.Application;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextInputDialog;
import javafx.stage.Stage;
import javafx.event.ActionEvent;

import java.util.Objects;
import java.util.Optional;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;

/**
 * Main class to start the project and handle events
 */
public class Main extends Application {
    @FXML
    private Button Hello;
    @FXML
    private TextArea textArea;

    /**
     * Constructs the application and links files.
     *
     * @param primaryStage stage to build the scene on
     * @throws Exception
     */
    @Override
    public void start(Stage primaryStage) throws Exception {

        Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("Main.fxml")));
        int width = 300;
        Scene scenery = new Scene(root, width, 275);
        scenery.getStylesheets().add("proj2/Main.css");

        primaryStage.setTitle("proj2EnglishQianYuZhang");
        primaryStage.setScene(scenery);
        primaryStage.show();
    }

    /**
     * Runs dialog popup when Hello is clicked.
     *
     * @param event the ActionEvent that triggered the handler.
     */
    @FXML
    private void handleHello(ActionEvent event) {
        TextInputDialog inputDialog = new TextInputDialog();
        inputDialog.setTitle("Give me a number");
        inputDialog.setHeaderText("Give me an integer from 0 to 255");
        Optional<String> result = inputDialog.showAndWait();
        if (result.isPresent()) {
            Hello.setText(inputDialog.getEditor().getText());
        }
    }

    /**
     * Handles response to Goodbye clicks.
     *
     * @param event the ActionEvent that triggered the handler.
     */
    @FXML
    private void handleGoodbye(ActionEvent event) {
        textArea.setText(textArea.getText() + "\nGoodbye");
    }

    /**
     * Handles resetting the scene.
     *
     * @param event the ActionEvent that triggered the handler.
     */
    @FXML
    private void handleReset(ActionEvent event) {
        textArea.setText("Sample text");
        Hello.setText("Hello");
    }

    /**
     * Handles exiting the window.
     *
     * @param event the ActionEvent that triggered the handler.
     */
    @FXML
    private void handleExit(ActionEvent event) {
        Platform.exit();
    }

    /**
     * Main method launches the application.
     *
     * @param args launch arguments passed on run.
     */
    public static void main(String[] args) {
        launch(args);
    }
}
