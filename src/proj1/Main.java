/*
* File: Main.java
* Names: Ricky Peng; Leo Qian; Eduardo Sosa; Dylan Tymkiw
* Class: CS 361
* Project 1
* Date: February 9
*/

package proj1;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Menu;
import javafx.scene.layout.VBox;
import javafx.scene.control.ToolBar;
import javafx.scene.control.TextInputDialog;
import javafx.stage.Stage;
import javafx.event.ActionEvent;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.TextArea;
import java.util.Optional;

/**
 * The {@code Main} class defines the main application for the project.
 *
 * @author Ricky Peng; Leo Qian; Eduardo Sosa; Dylan Tymkiw; Dale Skrien
 */
public class Main extends Application {
    /**
     * Start the stage for the window.
     * All elements in the window are defined in this method.
     * @param (parameter primaryStage) (object imported from the Application class for setting up stage)
     */
    @Override
    public void start(Stage primaryStage) {
        MenuBar menuBar = new MenuBar();

        Menu fileMenu = new Menu("File");

        MenuItem exit = new MenuItem("Exit");
        SeparatorMenuItem sep = new SeparatorMenuItem();
        MenuItem reset = new MenuItem("Reset");

        fileMenu.getItems().addAll(reset, sep, exit);
        menuBar.getMenus().add(fileMenu);

        // Initialize toolbars
        ToolBar toolBar = new ToolBar();

        Button helloButton = new Button("Hello");
        Button goodbyeButton = new Button("Goodbye");

        // Set styles of buttons
        helloButton.setStyle("-fx-background-color: #a4ec96; -fx-border-radius: 5 5 5 5; -fx-background-radius: 5 5 5 5; -fx-border-style: solid; -fx-border-width: 1; -fx-border-color: black;");
        goodbyeButton.setStyle("-fx-background-color: #f7c2cb; -fx-border-radius: 5 5 5 5; -fx-background-radius: 5 5 5 5; -fx-border-style: solid; -fx-border-width: 1; -fx-border-color: black;");

        TextInputDialog inputDialog = new TextInputDialog("");
        inputDialog.setTitle("Give me a number");
        inputDialog.setHeaderText("Give me an integer from 0 to 255");
        // Set action of helloButton
        helloButton.setOnAction((ActionEvent t) -> {
            Optional<String> result = inputDialog.showAndWait();
            // if the ok button is pressed, change the text of the hello button
            if (result.isPresent()){
                helloButton.setText(inputDialog.getEditor().getText());
            }

        });

        toolBar.getItems().addAll(helloButton, goodbyeButton);

        // add a textarea
        TextArea textArea = new TextArea("Sample text");

        VBox vBox = new VBox(menuBar, toolBar, textArea);
        Scene scene = new Scene(vBox, 300, 250);

        goodbyeButton.setOnAction((ActionEvent t) -> {
            // add goodbye to the text area
            textArea.appendText("\nGoodbye");
        });

        // Restore to initial contents
        reset.setOnAction((ActionEvent t) -> {
            helloButton.setText("Hello");
            goodbyeButton.setText("Goodbye");
        });

        // Exit application
        exit.setOnAction((ActionEvent t) -> {
            System.exit(0);
        });

        primaryStage.setScene(scene);
        primaryStage.setTitle("Project 01");
        primaryStage.show();
    }

    /**
     * Main method of the program
     * 
     * @param args command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }
}
