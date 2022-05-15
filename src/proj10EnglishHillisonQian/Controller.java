/*
 * File: proj10EnglishHillisonQian.Controller.java
 * Names: Nick English, Nico Hillison, Leo Qian
 * Class: CS361
 * Project 10
 * Date: 5/5/2022
 */

package proj10EnglishHillisonQian;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.paint.Color;
import org.fxmisc.flowless.VirtualizedScrollPane;
import org.fxmisc.richtext.CodeArea;
import org.fxmisc.richtext.StyleClassedTextArea;
import proj10EnglishHillisonQian.bantam.printer.Converter;
import proj10EnglishHillisonQian.bantam.semant.SemanticAnalyzer;
import proj10EnglishHillisonQian.bantam.util.Error;
import proj10EnglishHillisonQian.bantam.printer.PrettyPrinter;
import proj10EnglishHillisonQian.bantam.util.ErrorHandler;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * Controller handles ActionEvents for the Application.
 *
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
    private Button compile;
    @FXML
    private Button compileAndRun;
    @FXML
    private Button stop;
    @FXML
    private StyleClassedTextArea console;
    @FXML
    private ContextMenu codeContextMenu;
    @FXML
    private ContextMenu tabContextMenu;
    @FXML
    private Menu font;

    /** Stores the File contained by each tab.*/
    private final HashMap<Tab, File> tabFileMap = new HashMap<>();

    /** Stores whether the contents of each tab have changed since last save.*/
    private final HashMap<Tab, Boolean> textHasChangedMap = new HashMap<>();

    /** Helper objects for the Controller to use */
    private TabHelper tabHelper;
    private FileController fileController;
    private DialogHelper dialogHelper;
    private AlertHandler alertHandler;

    /** records new tabs for untitled tab naming. */
    private int numNewTabs = 1;
    /** fields storing the global style of all tabs */
    private String fontFamily;
    private int fontSize = 13;

    /** Thread to run compile and run in */
    private Thread currentThread;
    private Thread outerThread;
    private boolean compileSuccess;

    /** Objects to control output and input of the console */
    public static OutputStream outputStream;
    private String outputString = "";

    /** PrettyPrinter to format code nicely */
    private PrettyPrinter printer;

    /** SemanticAnalyzer to run scan/parse/analyze */
    private SemanticAnalyzer analyzer;

    /**
     *
     * Loads initial content on launch.
     */
    @FXML
    public void initialize() {
        tabHelper = new TabHelper(tabPane, textHasChangedMap, codeContextMenu);
        fileController = new FileController(tabHelper);
        alertHandler = new AlertHandler();
        dialogHelper = new DialogHelper(tabPane, tabHelper, fileController, tabFileMap,
                                        textHasChangedMap, alertHandler, tabContextMenu);
        tabHelper.createCodeAreaForTab(tabHelper.getCurrentTab());
        tabHelper.getCurrentTab().setTooltip(new Tooltip("Untitled"));
        tabHelper.getCurrentTab().setContextMenu(tabContextMenu);
        stop.setDisable(true);
        console.setOnKeyPressed(event -> {
            handleInput(event);
        });
        // assign to the style filed a default style
        this.fontFamily = tabHelper.getCurrentTab().getStyle();

        this.printer = new PrettyPrinter();
        this.analyzer = new SemanticAnalyzer(new ErrorHandler());
    }

    /**
     * Displays about dialog.
     *
     */
    @FXML
    private void handleAbout(){
        dialogHelper.aboutDialog();
    }

    /**
     * Handles saving under a specified filepath.
     *
     *
     */
    @FXML
    private boolean handleSaveAs(Event event){
        String tabName;
        if (event != null){
            try{
                Tab tabTosave = (Tab) event.getSource();
                tabName = tabTosave.getText();
                event.consume();
            }
            catch (ClassCastException e){
                event.consume();
                tabName = tabHelper.getCurrentTabTitle();
            }
        }
        else {
            tabName = tabHelper.getCurrentTabTitle();
        }
        return dialogHelper.saveAsDialog(tabName);
    }

    /**
     * Saves a file if saved previously, else prompts to save as.
     */
    @FXML
    private void handleSave(){
        Tab currentTab = tabHelper.getCurrentTab();

        if(!tabFileMap.containsKey(currentTab)){
            handleSaveAs(null);
        }else{
            boolean saved = fileController.saveCurrentFile(tabFileMap.get(currentTab)
            );
            if(saved){textHasChangedMap.put(currentTab, false);}
        }
    }

    /**
     * Opens a new Tab with a CodeArea.
     *
     *
     */
    @FXML
    private void handleNew(){
        Tab newTab = new Tab("Untitled Tab " + numNewTabs++);
        newTab.setOnCloseRequest(e -> handleClose(e));
        newTab.setTooltip(new Tooltip(newTab.getText()));
        newTab.setContextMenu(tabContextMenu);

        tabPane.getTabs().add(newTab);
        tabPane.getSelectionModel().select(newTab);

        tabHelper.createCodeAreaForTab(newTab);
        tabHelper.getCurrentCodeArea().setStyle(this.fontFamily);
        tabHelper.getCurrentCodeArea().setStyle("-fx-font-size: " + this.fontSize + ";");

        if(tabPane.getTabs().size() != 0){
            close.setDisable(false);
            save.setDisable(false);
            saveAs.setDisable(false);
            compile.setDisable(false);
            compileAndRun.setDisable(false);
            for (MenuItem item : edit.getItems())
            {
                item.setDisable(false);
            }
            for(MenuItem item: font.getItems()){
                item.setDisable(false);
            }
        }
    }

    /**
     * Closes a tab if all changes have been saved, or prompts the
     * user to save progress before closing.
     * @return whether to continue with closing
     */
    @FXML
    private boolean handleClose(Event event){
        Tab tabToClose = tabHelper.getCurrentTab();
        if (event != null) {
            try{
                tabToClose = (Tab) event.getSource();
                event.consume();
            }
            catch (ClassCastException e){
                event.consume();
            }
        }
        if (tabToClose == null){
            return false;
        }
        if(textHasChangedMap.get(tabToClose) == false){
            tabPane.getTabs().remove(tabToClose);
        }else{
            Optional<ButtonType> result = dialogHelper.closeDialog(tabToClose.getText());
            if (result.get().getText().equals("Yes")){
                handleSave();
                return handleClose(event); // remove tab if saved, else repeat
            } else if (result.get().getText().equals("No")){
                tabPane.getTabs().remove(tabToClose);
            } else {
                return false; // user pressed cancel
            }
        }
        if(tabPane.getTabs().size() == 0){
            close.setDisable(true);
            save.setDisable(true);
            saveAs.setDisable(true);
            compile.setDisable(true);
            compileAndRun.setDisable(true);
            for (MenuItem item : edit.getItems())
            {
                item.setDisable(true);
            }
            for(MenuItem item: font.getItems()){
                item.setDisable(true);
            }
        }
        return true;
    }

    /**
     * Opens a file into a new Tab and CodeArea.
     *
     *
     */
    @FXML
    private void handleOpen(){
        dialogHelper.openDialog();
        tabHelper.getCurrentTab().setOnCloseRequest(this::handleClose);
        tabHelper.getCurrentCodeArea().setStyle(this.fontFamily);
        tabHelper.getCurrentCodeArea().setStyle("-fx-font-size: " + this.fontSize + ";");

        if(tabPane.getTabs().size() != 0){
            close.setDisable(false);
            save.setDisable(false);
            saveAs.setDisable(false);
            compile.setDisable(false);
            compileAndRun.setDisable(false);
            for (MenuItem item : edit.getItems())
            {
                item.setDisable(false);
            }
            for(MenuItem item: font.getItems()){
                item.setDisable(false);
            }
        }

    }

    /**
     * Closes each tab and the Application after checking whether unsaved changes exist.
     */
    @FXML
    public void handleExit(){
        boolean closing = true;
        while (closing){
            closing = handleClose(null);
        }
        if (tabHelper.getCurrentTab() == null) {
            System.exit(0);
        }
    }

    @FXML
    /** Handles undo menu item */
    private void handleUndo(){ tabHelper.getCurrentCodeArea().undo(); }

    @FXML
    /** Handles redo menu item */
    private void handleRedo(){
        tabHelper.getCurrentCodeArea().redo();
    }

    @FXML
    /** Handles cut menu item */
    private void handleCut(){
        tabHelper.getCurrentCodeArea().cut();
    }

    @FXML
    /** Handles copy menu item */
    private void handleCopy(){
        tabHelper.getCurrentCodeArea().copy();
    }

    @FXML
    /** Handles paste menu item */
    private void handlePaste(){
        tabHelper.getCurrentCodeArea().paste();
    }


    @FXML
    /** Handles selectAll menu item */
    private void handleSelectAll(){
        tabHelper.getCurrentCodeArea().selectAll();
    }

    @FXML
    /** Swaps between light and dark mode */
    private void changeVisualMode(){
        Scene scene = console.getScene();
        String main = getClass().getResource("Main.css").toExternalForm();
        String dark = getClass().getResource("DarkMode.css").toExternalForm();

        if(scene.getStylesheets().contains(main) || scene.getStylesheets().size() == 1){
            scene.getStylesheets().remove(BantamHighlighter.class.getResource(
                    "java-keywords.css").toExternalForm());  // Make DarkMode bottommost
            scene.getStylesheets().remove(main);
            scene.getStylesheets().add(dark);
            scene.getStylesheets().add(BantamHighlighter.class.getResource(
                    "java-keywords.css").toExternalForm()); // Add keywords back
            console.setBackground(new Background(new BackgroundFill(Color.rgb(60, 63, 65)
                    , CornerRadii.EMPTY, Insets.EMPTY)));
        }else{
            scene.getStylesheets().remove(dark);
            scene.getStylesheets().add(main);
            console.setBackground(new Background(new BackgroundFill(Color.rgb(255,255,255)
                    , CornerRadii.EMPTY, Insets.EMPTY)));
        }

    }

    @FXML
    /** Handles stop button */
    private void stop(){
        if(currentThread != null){
            currentThread.interrupt();
        }
    }

    /** Takes the string given to the console and writes it to the output stream
     * @param key the key pressed.
     */
    private void handleInput(KeyEvent key) {
        if (key.getCode() != KeyCode.ENTER){
            if(key.getText() != null && key.getText() != ""){
                outputString += key.getText();
            }
        }
        else {
            try {
                outputStream.write(outputString.getBytes(StandardCharsets.UTF_8));
                outputStream.write(10); // 10 is the bytecode for new line
                outputStream.flush();
                outputString = "";
            } catch (IOException ex) {
                this.alertHandler.showErrorAlert("System Failed to Register Input",
                        "System Error");
            }
        }
    }

    @FXML
    /** Handles compile button*/
    private Thread compile() {
        File currentFile = tabFileMap.get(tabHelper.getCurrentTab());

        // if the current file hasn't been saved before, save it first
        if (currentFile == null) {
            // if saving process is cancelled, do not continue with compiling
            if (!handleSaveAs(null))
                ;
                //return false;
            else
                currentFile = tabFileMap.get(tabHelper.getCurrentTab());
        }

        // if the file has been changed since last save, give the save prompt
        else if (textHasChangedMap.get(tabHelper.getCurrentTab())) {
            Optional<ButtonType> saveResult = dialogHelper.saveDialog();
            if (saveResult.get().getText().equals("Yes")) {
                handleSave();
            } else if (saveResult.get().getText().equals("No")) {
                // if user presses no, don't do anything
            } else {
                return null; // user pressed cancel, quit the method
            }
        }

        stop.setDisable(false);

        final String[] message = {""};
        Converter converter = new Converter();
        converter.convert(currentFile.getPath());
        File finalCurrentFile = new File("src/tmp/Main.java");

        // Start in new thread
        this.currentThread = new Thread(() -> {

            Compiler comp = new Compiler(finalCurrentFile, console);
            comp.start();
            this.compileSuccess = false;

            while (true) {
                if (Thread.interrupted()) {
                    comp.interrupt();
                    break;
                }
                if (!comp.isAlive()) {
                    break;
                } else {
                    try {
                        comp.join(1);
                    } catch (InterruptedException e) {
                        this.alertHandler.showErrorAlert("Compilation interrupted, " +
                                        "exiting.", "Process interruption!");
                        break;
                    }
                }
            }

            if (comp.hasErrorMessage())
                message[0] = comp.getErrorMessage();


            stop.setDisable(true);

            Platform.runLater(() ->{
                    // display the compilation result message
                    console.append("******************\n", "");
                if (message[0].length() > 0) {
                    console.append(message[0], "");
                    //return false;
                } else {
                    console.append("Compilation successful!\n", "");
                }
                console.requestFollowCaret();
            });
            if(message[0].equals("")){
                this.compileSuccess = true;
            }
        });

        currentThread.start();
        return currentThread;




    }

    @FXML
    /** Handles compile and run button*/
    private void compileAndRun(){
        this.outerThread = new Thread(() -> {
            Thread compile = compile();
            // if we have error in compiling, do not run
            if(compile == null){
                outerThread.interrupt();
            }

            while(compile.isAlive()){
                try{
                    compile.join(1);
                }catch (Exception e){
                    outerThread.interrupt();
                }
            }

            final String[] message = {"",""};

            if(!this.compileSuccess){
                System.out.println("stop run");
                outerThread.stop();
            }

            this.currentThread = outerThread;

            stop.setDisable(false);

            File currentFile = new File("src/tmp/Main.java");

            //stop.setDisable(false);
            Runner run = new Runner(currentFile, console, this);
            run.start();

            while (true) {
                if (Thread.interrupted()) {
                    run.interrupt();
                    break;
                }
                if(!run.isAlive()){
                    try {
                        outputStream.close();
                    } catch (IOException e) {
                        message[1] = "Error closing output stream";
                    }

                    break;
                }else {
                    try {
                        run.join(1);
                    } catch (InterruptedException e) {
                        message[1] = "Run was stopped or failed";
                        break;
                    }
                }
            }

            if (run.hasErrorMessage())
                message[0] = run.getErrorMessage();
            run.stop();

            stop.setDisable(true);

            Platform.runLater(()->{
                if (message[0].length()>0){
                    console.append(message[0], "");
                }else if(message[1].length()>0){
                    console.append(message[1],"");
                }
                else{
                    console.append("Run Successful!\n", "");
                }

                console.append("******************\n\n", "");
                console.requestFollowCaret();
            });
        });
        outerThread.start();


    }

    /**
     * Handles check button.
     * Check if the current file is a valid bantam program.
     */
    @FXML
    public void check(){
        // make sure tab is saved before continuing
        Tab currentTab = tabHelper.getCurrentTab();
        if(textHasChangedMap.get(currentTab) || tabFileMap.get(currentTab) == null){
            Optional<ButtonType> result = dialogHelper.saveDialog();
            if (result.get().getText().equals("Yes")){
                handleSave();
            } else {
                this.alertHandler.showErrorAlert("File must be saved to proceed" +
                        ".", "Warning");
                return;
            }
        }

        // run the analysis
        console.append("\nChecking " + tabHelper.getCurrentTabTitle() + "\n", console.getStyle());
        String filePath = tabFileMap.get(tabHelper.getCurrentTab()).getPath();
        Object result = this.analyzer.analyze(filePath);
        if(result == null){
            ErrorHandler errorHandler = analyzer.getErrorHandler();
            List<Error> errors = errorHandler.getErrorList();
            console.append("Checking Failed, Errors Found\n", console.getStyle());
            for (Error error : errors) {
                console.append("\t" + error.toString() + "\n", console.getStyle());
                console.requestFollowCaret();
            }
        } else {
            console.append("Checking Successful\n", console.getStyle());
        }
    }

    /**
     * comment out the lines selected; otherwise, uncomment the line
     * @param actionEvent event object
     */
    @FXML
    public void handleToggleComment(ActionEvent actionEvent) {
        actionEvent.consume();
        CodeArea currentArea = tabHelper.getCurrentCodeArea();
        String selection = currentArea.getSelectedText();
        Pattern commentPattern = Pattern.compile("\\/\\/");
        Matcher matcher = commentPattern.matcher(selection);
        if(matcher.find()){
            String replaced = selection.replaceAll("\\/\\/ ?","");
            currentArea.replaceSelection(replaced);
        }else{
            String replaced = selection.replaceAll("\\n","\n// ");
            if(currentArea.getCaretPosition() == 0 || currentArea.getText(currentArea.getCaretPosition()-1,
                    currentArea.getCaretPosition()).equals("\n")){
                replaced = "// "+replaced;
            }
            currentArea.replaceSelection(replaced);
        }
    }

    /**
     * tab will be added after each newline symbol in the selection
     * if the caret stops right before the newline symbol, a tab symbol will also be added there
     * @param actionEvent event object
     */
    @FXML
    public void handleIndent(ActionEvent actionEvent) {
        actionEvent.consume();
        CodeArea currentArea = tabHelper.getCurrentCodeArea();
        String selection = currentArea.getSelectedText();
        String replaced = selection.replaceAll("\\n","\n\t");
        // if the caret stays 1 character right of a new line char or is at the starting of the code area
        if(currentArea.getCaretPosition() == 0 ||
                currentArea.getText(currentArea.getCaretPosition()-1,
                currentArea.getCaretPosition()).equals("\n")){
            replaced = "\t"+replaced;
        }
        currentArea.replaceSelection(replaced);
    }

    /**
     * tabs selected will be removed after this handler is called
     * @param actionEvent event object
     */
    @FXML
    public void handleUnindent(ActionEvent actionEvent) {
        actionEvent.consume();
        CodeArea currentArea = tabHelper.getCurrentCodeArea();
        String selection = currentArea.getSelectedText();

        // remove at max one tab from each line
        String[] lines = selection.split("\\n");
        String toReturn = "";
        for (int i = 0; i < lines.length; i++) {
            String line = lines[i];
            String replaced = line.replaceFirst("\\t","");
            if(i != 0){
                toReturn += "\n"+replaced;
            }
            else {
                toReturn += replaced;
            }
        }
        currentArea.replaceSelection(toReturn);


    }

    /**
     * change the font to the selected font from the menu
     * @param actionEvent event object
     */
    @FXML
    public void changeFont(ActionEvent actionEvent) {
        actionEvent.consume();
        String source = ((MenuItem) actionEvent.getSource()).getId();
        this.fontFamily = "-fx-font-family: " + source + ";";
        for(Tab t: tabPane.getTabs()) {
            ((VirtualizedScrollPane<CodeArea>) ((AnchorPane) t.getContent())
                    .getChildren().get(0)).getContent()
                    .setStyle(this.fontFamily);
        }
    }

    /**
     * Increase global font size of my code areas
     */
    @FXML
    public void increaseFontSize(){
        this.fontSize += 1;
        String style = "-fx-font-size: " + this.fontSize + ";";
        for(Tab t: tabPane.getTabs()) {
            ((VirtualizedScrollPane<CodeArea>) ((AnchorPane) t.getContent())
                    .getChildren().get(0)).getContent()
                    .setStyle(style);
        }
    }

    /**
     * Decrease global font size of my code areas
     */
    @FXML
    public void decreaseFontSize(){
        this.fontSize -= 1;
        String style = "-fx-font-size: " + this.fontSize + ";";
        for(Tab t: tabPane.getTabs()) {
            ((VirtualizedScrollPane<CodeArea>) ((AnchorPane) t.getContent())
                    .getChildren().get(0)).getContent()
                    .setStyle(style);
        }
    }

    /**
     * Replaces the contents of the current code area with a
     * PrettyPrinted version.
     */
    @FXML
    public void prettyPrint(){
        // make sure tab is saved before continuing
        Tab currentTab = tabHelper.getCurrentTab();
        if(textHasChangedMap.get(currentTab) || tabFileMap.get(currentTab) == null){
            Optional<ButtonType> result = dialogHelper.saveDialog();
            if (result.get().getText().equals("Yes")){
                handleSave();
            } else {
                this.alertHandler.showErrorAlert("File must be saved to proceed" +
                        ".", "Warning");
                return;
            }
        }

        // tell printer to return the output and get the output
        this.printer.setReturnOutput(true);
        String filePath = tabFileMap.get(tabHelper.getCurrentTab()).getPath();
        String output = this.printer.prettyPrint(filePath);

        // Handle parse errors
        if(output == null){
            this.alertHandler.showErrorAlert("File must be a syntactically valid Bantam " +
                            "Java program.  Printing errors to console.", "Warning");
            List<Error> errors = printer.getErrors();
            String conStyle = console.getStyle();
            console.append("Cannot PrettyPrint: Syntactic Errors Found\n", conStyle);
            for(Error e: errors){
                console.append(e.toString() + "\n", conStyle);
            }
            console.append("\n", conStyle);
            return;
        }

        // save necessary attributes for replace
        CodeArea area = tabHelper.getCurrentCodeArea();
        String style = area.getStyle();
        int end = area.getLength();

        // Replace the text with the PrettyPrinted version
        area.replace(0, end,output,style);
    }
}