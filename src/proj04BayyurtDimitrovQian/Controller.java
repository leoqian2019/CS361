/*
 * File: proj04BayyurtDimitrovQian.Controller.java
 * Names: Izge Bayyurt, Anton Dimitrov, Leo Qian
 * Class: CS361
 * Project 4
 * Date: 2/28/2022
 */

package proj04BayyurtDimitrovQian;


import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.util.Date;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

import javafx.application.Platform;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.stage.FileChooser;
import org.fxmisc.flowless.VirtualizedScrollPane;
import org.fxmisc.richtext.CodeArea;
import org.fxmisc.richtext.LineNumberFactory;
import org.reactfx.Subscription;


/**
 * The Controller Class for handling menu items click events of the stage
 *
 * @author (Izge Bayyurt, Anton Dimitrov, Leo Qian)
 */
public class Controller {

    @FXML
    private TabPane tabPane;
    @FXML
    private MenuItem close;
    @FXML
    private MenuItem save;
    @FXML
    private MenuItem saveAs;
    @FXML
    private Menu edit;
    @FXML
    private CodeArea codeArea;
    // initialize the simple date format and stick to this format for the default new tab names
    private final SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MMddyy-hhmmss.SSS");


    /**
     * Handler method for about menu bar item. When the about item of the
     * menu bar is clicked, an alert window appears displaying basic information
     * about the application.
     */
    @FXML
    private void handleAboutMenuItem(Event event) {

        Alert aboutDialogBox = new Alert(AlertType.INFORMATION);

        aboutDialogBox.setTitle("About");
        aboutDialogBox.setHeaderText("About this Application");

        aboutDialogBox.setContentText(
                "Authors: Izge Bayyurt, Anton Dimitrov, Leo Qian"
                        + "\nLast Modified: Feb 28, 2022");

        aboutDialogBox.show();


    }

    /**
     * Handler method for about new bar item. When the new item of the
     * menu bar is clicked, ane new tab is opened with code area.
     *
     * @see new tab and CodeArea
     */
    @FXML
    private void handleNewMenuItem(Event event) {

        Tab newTab = new Tab();
        // trigger close menu item handler when tab is closed
        newTab.setOnCloseRequest((Event t) -> {
            handleCloseMenuItem(t);
        });

        // set new tabs with unique initial name
        newTab.setText(simpleDateFormat.format(new Date()));
        tabPane.getTabs().add(newTab);
        CodeArea cd = new CodeArea();
        VirtualizedScrollPane scrollPane = new VirtualizedScrollPane(cd);
        newTab.setContent(scrollPane);

        // add event handler to the code area when key is pressed
        cd.addEventHandler(KeyEvent.KEY_PRESSED, KE -> {
            textHighlight();
        });

        SingleSelectionModel<Tab> selectionModel = tabPane.getSelectionModel();
        selectionModel.select(newTab);

        // start text highlighting
        textHighlight();

        // set the edit menu and other affected menu item to be enabled
        edit.setDisable(false);
        for (MenuItem item:edit.getItems()) {
            item.setDisable(false);
        }
        close.setDisable(false);
        save.setDisable(false);
        saveAs.setDisable(false);

    }

    /**
     * Helper method to display error message to user when an exception is thrown
     *
     * @type this type is default since main need to use it to print possible exception message
     */
    void exceptionAlert(Exception ex){
        Alert alert = new Alert(AlertType.ERROR);
        alert.setTitle("Exception Alert");
        alert.setHeaderText("Thrown Exception");
        alert.setContentText("An exception has been thrown.");

        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        ex.printStackTrace(pw);
        pw.close();
        String exceptionText = sw.toString();

        Label label = new Label("The exception stacktrace is:");

        TextArea textArea = new TextArea(exceptionText);
        textArea.setEditable(false);
        textArea.setWrapText(true);

        textArea.setMaxWidth(Double.MAX_VALUE);
        textArea.setMaxHeight(Double.MAX_VALUE);
        GridPane.setVgrow(textArea, Priority.ALWAYS);
        GridPane.setHgrow(textArea, Priority.ALWAYS);

        GridPane expContent = new GridPane();
        expContent.setMaxWidth(Double.MAX_VALUE);
        expContent.add(label, 0, 0);
        expContent.add(textArea, 0, 1);

        alert.getDialogPane().setExpandableContent(expContent);

        alert.showAndWait();
    }

    /**
     * Handler for "open" menu item
     * When the "open" button is clicked, a fileChooserDialog appears,
     * and the user has to select a valid text file to proceed
     */
    @FXML
    private void handleOpenMenuItem(Event event){
        try {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Open your text file");

            // restrict the file type to only text files
            fileChooser.getExtensionFilters().addAll(
                    new FileChooser.ExtensionFilter("Text Files", "*.txt"),
                    new FileChooser.ExtensionFilter("Java Files","*.java")
            );
            File selectedFile = fileChooser.showOpenDialog(tabPane.getScene().getWindow());
            if (selectedFile != null) {
                // get the path of the file selected
                String filePath = selectedFile.getPath();
                // read the content of the file to a string
                String fileContent = new String(Files.readAllBytes(Paths.get(filePath)));
                // generate a new tab and put the file content into the text area
                handleNewMenuItem(event);
                // get the current tab
                Tab currentTab = getCurrentTab();
                // get the current CodeArea
                CodeArea codeArea = getCurrentCodeArea();
                // set the content of the codeArea
                codeArea.replaceText(fileContent);
                // set the title of the tab
                currentTab.setText(selectedFile.getName());
                // assign the path of the file to the userdata field
                currentTab.setUserData(filePath);
            }
        } catch(Exception ex){
            exceptionAlert(ex);
        }
    }

    /**
     * Handler for "Close" menu item
     * When the "Close" button is clicked, or when the tab is closed, the program would check
     * if any changes has been made since the last save event, a dialog appears asking if the user
     * wants to save again
     */
    @FXML
    private void handleCloseMenuItem(Event event) {

        // get the current tab
        Tab currentTab = getCurrentTab();
        // get content of CodeArea
        CodeArea codeArea = getCurrentCodeArea();
        String currentContent = codeArea.getText();

        // get the file path
        String filePath = (String) currentTab.getUserData();
        // check if changes has been made
        boolean changed = true;

        // check if the tab should be closed
        AtomicBoolean discardTab = new AtomicBoolean(false);

        // if file path is valid
        if (filePath != null) {
            // get the file associated with the current tab
            File file = new File(filePath);
            // check if the CodeArea has been modified
            if (file.exists()) {
                // check if the content of the file matches the content of the CodeArea
                try {
                    String fileContent = new String(Files.readAllBytes(Paths.get(filePath)));
                    // if it hasn't been modified, set changed to false
                    if (currentContent.equals(fileContent)) {
                        changed = false;
                    }
                } catch (Exception ex) {
                    exceptionAlert(ex);
                }
            }
        }

        // if it has been modified
        if (changed) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle(null);
            alert.setContentText("Do you want to save your tab before closing it?");
            ButtonType okButton = new ButtonType("Yes", ButtonBar.ButtonData.YES);
            ButtonType noButton = new ButtonType("No", ButtonBar.ButtonData.NO);
            ButtonType cancelButton = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
            alert.getButtonTypes().setAll(okButton, noButton, cancelButton);
            alert.showAndWait().ifPresent(type -> {
                if (type == okButton) {
                    // set the value of discardTab to status returned by save menu item handler
                    discardTab.set(handleSaveMenuItem(event));

                } else if (type == cancelButton) {
                    // if cancel button is clicked, do nothign
                    event.consume();

                } else {
                    // if the user chooses no, set discardTab to true
                    discardTab.set(true);
                }
            });
        } else {
            // if no change has been made, set discardTab to true
            discardTab.set(true);
        }

        // if discardTab is true, remove the tab
        if (discardTab.get()) {
            tabPane.getTabs().remove(getCurrentTab());

            // if no tab is available in the selection, set the edit menu
            //      and other affected menu items to be disabled
            if (getCurrentTab() == null) {
                edit.setDisable(true);
                for (MenuItem item:edit.getItems()) {
                    item.setDisable(true);
                }
                close.setDisable(true);
                save.setDisable(true);
                saveAs.setDisable(true);
            }
        }


    }

    /**
     * Handler for "save" menu item
     * When the "save" button is clicked, if the file of the name of the tab exist in the directory, it will
     * overwrite the file with the content in the code area of the current tab
     *
     * If that file didn't exist, it will call the save as menu item for the user to put in a new name
     *
     * @return returns true if the file is saved, and false if not saved
     */
    @FXML
    private boolean handleSaveMenuItem(Event event) {
        // get the current tab
        Tab currentTab = getCurrentTab();

        // get the userdata of the tab (file path)
        String filePath = (String) currentTab.getUserData();

        // if user data contains valid information
        if (filePath != null) {
            File file = new File(filePath);
            // if the file exist, overwrite it with content of the text area
            if (file.exists()) {
                // get content of CodeArea
                CodeArea codeArea = getCurrentCodeArea();
                String content = codeArea.getText();

                // save the content of the current tab and return the status
                return saveFile(content, file);
            }
            // otherwise, call save as and return its result
            else {
                return handleSaveAsMenuItem(event);
            }

        }
        // otherwise, call save as and return its result
        else {
            return handleSaveAsMenuItem(event);
        }


    }

    /**
     * Handler for "save as" menu item
     * When the "save as" button is clicked, a save as window appears asking the user to enter
     * a file name for the text file and if the file exist, the prompt will ask user whether to overwrite
     * After file is created successfully, the user will see a prompt, and if not, the user will also see an error
     * message; At the same time, the tab name will be changed to the file path saved
     *
     * @Give credit to http://java-buddy.blogspot.com/
     *
     * @return return true when file is saved, otherwise, return false
     */
    @FXML
    private boolean handleSaveAsMenuItem(Event event) {
        // get the current tab
        Tab currentTab = getCurrentTab();
        // get the current CodeArea
        CodeArea codeArea = getCurrentCodeArea();

        // initiate a new file chooser
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save as");

        //Set extension filter
        FileChooser.ExtensionFilter txtFilter =
                new FileChooser.ExtensionFilter("TXT files (*.txt)", "*.txt");
        FileChooser.ExtensionFilter javaFilter =
                new FileChooser.ExtensionFilter("JAVA files (*.java)","*.java");
        fileChooser.getExtensionFilters().addAll(txtFilter,javaFilter);
        //Show save file dialog
        File file = fileChooser.showSaveDialog(tabPane.getScene().getWindow());

        if (file != null) {
            Alert alert;
            // if file is saved correctly, show an alert box with corresponding message
            if (saveFile(codeArea.getText(), file)) {
                alert = new Alert(AlertType.INFORMATION);
                alert.setTitle("Success");
                alert.setHeaderText(null);
                alert.setContentText("Successfully created " + file.getPath());
                alert.show();
                // change the name of the tab to the file name and set the user data field to file path
                currentTab.setText(file.getName());
                currentTab.setUserData(file.getPath());

                // return true when the process is successful
                return true;
                // otherwise, show error message and return false
            } else {
                alert = new Alert(AlertType.ERROR);
                alert.setTitle("Error");
                alert.setHeaderText(null);
                alert.setContentText("Failed creating " + file.getPath());
                alert.show();

                // return false when the process is not successful
                return false;
            }
        } else {
            // return false when no file is created
            return false;
        }

    }

    /**
     * Helper method for creating a new file
     *
     * @param (content) (the string content of the new file being created)
     * @param (file)    (the file variable passed by handleSaveAsMenuItem function indicating the
     *                  file the user want to save to is valid)
     * @return returns true if file created successfully and false if error occurs
     */
    private boolean saveFile(String content, File file) {
        try {
            FileWriter fileWriter;
            fileWriter = new FileWriter(file);
            fileWriter.write(content);
            fileWriter.close();
            return true;
        } catch (Exception ex) {
            return false;
        }

    }

    /**
     * Handler method for exit menu bar item. When exit item of the menu
     * bar is clicked, the window disappears and the application quits after going
     * through each tab and ask user about the unsaved change.
     *
     * If the user clicked cancel at any point, the operation is stopped
     *
     * @param event An ActionEvent object that gives information about the event
     *              and its source.
     *
     * @type this is a method with default type since the Main class needs to access it
     */
    @FXML
    public void handleExitMenuItem(Event event) {
        while (getCurrentTab() != null) {
            Tab previousTab = getCurrentTab();
            handleCloseMenuItem(event);
            Tab currentTab = getCurrentTab();
            // if the tab is not closed, stop the operation of this function
            if (previousTab.equals(currentTab)) {
                return;
            }

        }
        Platform.exit();
    }


    /**
     * Helper method to get the current selected tab
     */
    private Tab getCurrentTab() {
        return tabPane.getSelectionModel().getSelectedItem();
    }

    /**
     * Helper method to get the virtualized scroll pane in the current selected tab
     */
    private VirtualizedScrollPane getCurrentVScrollPane(){
        return (VirtualizedScrollPane) getCurrentTab().getContent();
    }

    /**
     * Helper method to get the code area in the current selected tab
     */
    private CodeArea getCurrentCodeArea(){
        return (CodeArea) getCurrentVScrollPane().getContent();
    }

    /**
     * Handler method for "Undo" in the Edit menu
     * Undo the previous CodeArea edition
     */
    @FXML
    private void handleUndo(Event event) {
        getCurrentCodeArea().undo();
    }

    /**
     * Handler method for "Redo" in the Edit menu
     * Redo the previous CodeArea edition
     */
    @FXML
    private void handleRedo(Event event) {
        getCurrentCodeArea().redo();
    }

    /**
     * Handler method for "Cut" in the Edit menu
     * Cut all the selected text in the CodeArea of the current Tab
     */
    @FXML
    private void handleCut(Event event) {
        getCurrentCodeArea().cut();
    }

    /**
     * Handler method for "Copy" in the Edit menu
     * Copy the selected text from the CodeArea of the current Tab to the clipboard
     */
    @FXML
    private void handleCopy(Event event) {
        getCurrentCodeArea().cut();
    }

    /**
     * Handler method for "Paste" in the Edit menu
     * Paste text from the clipboard to the CodeArea of the current Tab
     */
    @FXML
    private void handlePaste(Event event) {
        getCurrentCodeArea().paste();
    }

    /**
     * Handler method for "Select all" in the Edit menu
     * Select all the text in the CodeArea of the current Tab
     */
    @FXML
    private void handleSelectAll(Event event) {
        getCurrentCodeArea().selectAll();
    }

    /**
     * Helper method for Highlighting code in code area
     */
    @FXML
    private void textHighlight() {
        CodeArea codeArea = getCurrentCodeArea();
        KeywordHighlighter keywordHighlighter =
                new KeywordHighlighter(codeArea, Executors.newSingleThreadExecutor());
        codeArea.setParagraphGraphicFactory(LineNumberFactory.get(codeArea));
        Subscription cleanupWhenDone = codeArea.multiPlainChanges()
                .successionEnds(Duration.ofMillis(500))
                .retainLatestUntilLater(keywordHighlighter.getExecutor())
                .supplyTask(keywordHighlighter::computeHighlightingAsync)
                .awaitLatest(codeArea.multiPlainChanges())
                .filterMap(t -> {
                    if (t.isSuccess()) {
                        return Optional.of(t.get());
                    } else {
                        t.getFailure().printStackTrace();
                        return Optional.empty();
                    }
                })
                .subscribe(keywordHighlighter::applyHighlighting);
    }


    public static void main(String[] args) {

    }
}
