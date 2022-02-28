/*
 * File: proj04BayyurtDimitrovQian.Main.java
 * Names: Izge Bayyurt, Anton Dimitrov, Leo Qian
 * Class: CS361
 * Project 4
 * Date: 2/28/2022
 */

package proj4BayyurtDimitrovQian;


import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;
import javafx.scene.Parent;
import javafx.scene.Scene;
import org.fxmisc.richtext.CodeArea;
import org.fxmisc.richtext.LineNumberFactory;

import java.io.IOException;
import java.time.Duration;
import java.util.Collection;
import java.util.Collections;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

import org.fxmisc.flowless.VirtualizedScrollPane;
import org.fxmisc.richtext.CodeArea;
import org.fxmisc.richtext.GenericStyledArea;
import org.fxmisc.richtext.LineNumberFactory;
import org.fxmisc.richtext.model.Paragraph;
import org.fxmisc.richtext.model.StyleSpans;
import org.fxmisc.richtext.model.StyleSpansBuilder;
import org.reactfx.collection.ListModification;
import org.reactfx.Subscription;

/**
 * The Main Class for loading the fxml file and building the stage
 *
 * @author (Izge Bayyurt, Anton Dimitrov, Leo Qian)
 */
public class Main extends Application{
    @Override
    public void start(Stage primaryStage) throws IOException {

        // Load fxml file
        FXMLLoader loader = new FXMLLoader((getClass().getResource("Main.fxml")));
        Parent root = loader.load();
        primaryStage.setTitle("Project 4");
        Scene scene = new Scene(root);

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

    public static void main(String[] args){
        launch(args);
    }
}
