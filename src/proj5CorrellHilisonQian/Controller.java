/*
 * File: proj5CorrellHilisonQian.Controller.java
 * Names: Leo Qian, Cassidy Correl, Nico Hillison
 * Class: CS361
 * Project 5
 * Date: 3/7/2022
 */

package proj5CorrellHilisonQian;

import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import org.fxmisc.richtext.StyleClassedTextArea;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Optional;
import java.util.Scanner;


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
    private StyleClassedTextArea styleclassedtextarea;
    @FXML
    private Button compile;
    @FXML
    private Button compileRun;
    @FXML
    private Button stop;

    private final HashMap<Tab, File> filenameFileMap = new HashMap<>();
    private final HashMap<Tab, Boolean> textHasChangedMap = new HashMap<>();
    private CodeAreaHelpers tabHelper;
    private FileHelpers fileHelper;
    private DialogHelpers dialogHelper;

    private Process process;
    /**
     *
     * Loads initial content on launch.
     */
    @FXML
    public void initialize() {
        tabHelper = new CodeAreaHelpers(tabPane, textHasChangedMap);
        fileHelper = new FileHelpers(tabHelper);
        dialogHelper = new DialogHelpers(tabPane, tabHelper, fileHelper, filenameFileMap,
                textHasChangedMap);
        tabHelper.createCodeAreaForTab(tabHelper.getCurrentTab());
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
    private boolean handleSaveAs(){
        return dialogHelper.saveAsDialog();

    }

    /**
     * Saves a file if saved previously, else prompts to save as.
     *
     * @return return whether the file is saved successfully
     */
    @FXML
    private boolean handleSave(){
        Tab currentTab = tabHelper.getCurrentTab();
        if(!filenameFileMap.containsKey(currentTab)){
            return handleSaveAs();
        }else{
            boolean isSaved = fileHelper.saveCurrentFile(filenameFileMap.get(currentTab));
            // if file is saved correctly, set the changed record to false
            if(isSaved) {
                textHasChangedMap.put(currentTab, false);
            }
            return isSaved;
        }
    }

    /**
     * Opens a new Tab with a CodeArea.
     */
    @FXML
    private void handleNew(){
        // initialize the simple date format and stick to this format for the default new tab names
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MM/dd/yy-hh:mm:ss.SSS");
        Tab newTab = new Tab((simpleDateFormat.format(new Date())));

        // add event handler to new tabs
        newTab.setOnCloseRequest(this::handleClose);


        tabPane.getTabs().add(newTab);
        tabPane.getSelectionModel().select(newTab);

        tabHelper.createCodeAreaForTab(newTab);

        if(tabHelper.getCurrentTab() != null){
            close.setDisable(false);
            save.setDisable(false);
            saveAs.setDisable(false);
            compile.setDisable(false);
            compileRun.setDisable(false);
            stop.setDisable(true);
            for (MenuItem item : edit.getItems())
            {
                item.setDisable(false);
            }
        }

    }

    /**
     * Closes a tab if all changes have been saved, or prompts the
     * user to save progress before closing.
     *
     * @param event The close event
     * @return whether the current tab is closed
     */
    @FXML
    private boolean handleClose(Event event){
        event.consume();
        Tab targetTab = tabHelper.getCurrentTab();

        if(!textHasChangedMap.get(targetTab)){
            tabPane.getTabs().remove(targetTab);

        }else{
            Optional<ButtonType> result = dialogHelper.closeDialog(targetTab.getText());
            if (result.get().getText().equals("Yes")){

                // if the file is saved successfully, remove the tab from the tabPane
                if(handleSave()) {
                    textHasChangedMap.remove(targetTab);
                    filenameFileMap.remove(targetTab);
                    tabPane.getTabs().remove(targetTab);
                }
                // otherwise, return false
                else {
                    return false;
                }
                // if the user select no, discard any changes and close the tab
            } else if (result.get().getText().equals("No")){
                textHasChangedMap.remove(targetTab);
                filenameFileMap.remove(targetTab);
                tabPane.getTabs().remove(targetTab);
            } else {
                return false; // user pressed cancel
            }
        }
        if(tabHelper.getCurrentTab() == null){
            close.setDisable(true);
            save.setDisable(true);
            saveAs.setDisable(true);
            compile.setDisable(true);
            compileRun.setDisable(true);
            stop.setDisable(true);
            for (MenuItem item : edit.getItems())
            {
                item.setDisable(true);
            }
        }

        // return true if tab closed
        return true;
    }

    /**
     * Opens a file into a new Tab and CodeArea.
     */
    @FXML
    private void handleOpen(){
        dialogHelper.openDialog();
        tabHelper.getCurrentTab().setOnCloseRequest(this::handleClose);

        if(tabHelper.getCurrentTab() != null){
            close.setDisable(false);
            save.setDisable(false);
            saveAs.setDisable(false);
            for (MenuItem item : edit.getItems())
            {
                item.setDisable(false);
            }

        }
    }



    /**
     * Closes each tab and the Application after checking whether unsaved changes exist.
     */
    @FXML
    public void handleExit(Event event){
        boolean closed = true;
        // if current selected tab is closed, continue to call
        // handle close until tag is not closed or no tab is left
        while (closed && tabHelper.getCurrentTab() != null){
            closed = handleClose(event);
        }
        if (tabHelper.getCurrentTab() == null) {
            System.exit(0);
        }
    }


    /**
     * Undoes latest action on GUI CodeArea
     */
    @FXML
    private void handleUndo(){ tabHelper.getCurrentCodeArea().undo(); }

    /**
     * Redoes an undone action within the GUI CodeArea
     */
    @FXML
    private void handleRedo(){
        tabHelper.getCurrentCodeArea().redo();
    }

    /**
     * Cuts selected text from GUI CodeArea
     */
    @FXML
    private void handleCut(){
        tabHelper.getCurrentCodeArea().cut();
    }

    /**
     * Copies selected text from GUI CodeArea
     */
    @FXML
    private void handleCopy(){
        tabHelper.getCurrentCodeArea().copy();
    }

    /**
     * Pastes test from clipboard into GUI CodeArea
     */
    @FXML
    private void handlePaste(){
        tabHelper.getCurrentCodeArea().paste();
    }

    /**
     * Selects all text within GUI CodeArea
     */
    @FXML
    private void handleSelectAll(){
        tabHelper.getCurrentCodeArea().selectAll();
    }

    /**
     * Helper method for compile button, replace text in the text area
     * with compilation result unless exception were thrown
     *
     * @return returns true if compilation is successful, returns false if any issue or exception were met
     */
    @FXML
    private boolean handleCompileButton(){
        // if the current tab is ever saved
        if (filenameFileMap.get(tabHelper.getCurrentTab()) != null)  {
            String file = "" + filenameFileMap.get(tabHelper.getCurrentTab());

            //Check if unsaved changes
            if(textHasChangedMap.get(tabHelper.getCurrentTab())){
                Optional<ButtonType> result = dialogHelper.CompileDialog();
                // if the user selects yes, call the save handler
                if (result.get().getText().equals("Yes")){
                    handleSave();
                }
                // if the user select cancel, then stop the compiling process and return false
                else if (result.get().getText().equals("Cancel")) {
                    return false;
                }
            }
            // compile the file
            ProcessBuilder pb = new ProcessBuilder("javac", file);
            pb.redirectErrorStream(true);



            try {
                process = pb.start();
                InputStream inStream = process.getInputStream();

                //Turn output stream to string
                Scanner s = new Scanner(inStream).useDelimiter("\\A");
                String inResult = s.hasNext() ? s.next() : "";

                // if the compilation is successful, replace the text in the console
                if(inResult.equals("")){
                    styleclassedtextarea.replaceText("Compilation was successful.\n");
                    return true;
                }
                // otherwise, replace the text in the console with the error message
                else{
                    styleclassedtextarea.replaceText(inResult);
                    return false;
                }

            }
            catch(Exception e){
                fileHelper.exceptionAlert(e);
                return false;
            }
        }
        // otherwise, tell the user to save the tab first
        else {
            dialogHelper.nullFileError();
            return false;
        }



    }

    /**
     * Compiles and Runs the open file in GUI
     */
    @FXML
    private void handleCompileRunButton(){
        if(handleCompileButton()){
            Thread thread = new Thread(() -> {
                File file = filenameFileMap.get(tabHelper.getCurrentTab());

                ProcessBuilder pb = new ProcessBuilder("java", file.getName().replace(".java",""));
                pb.directory(file.getParentFile());
                pb.redirectErrorStream(true);
                try{
                    process = pb.start();

                    while(process.isAlive()) {
                        InputStream inputStream = process.getInputStream();
                        //Turn output stream to string
                        Scanner s = new Scanner(inputStream).useDelimiter("\\A");
                        String inResult = s.hasNext() ? s.next() : "";
                        styleclassedtextarea.appendText(inResult);
                        //Disable C, CR, enable Stop
                        compile.setDisable(true);
                        compileRun.setDisable(true);
                        stop.setDisable(false);
                    }
                    compile.setDisable(false);
                    compileRun.setDisable(false);
                    stop.setDisable(true);
                }
                catch (Exception e){
                    System.out.println(e.getMessage());
                }
            });
            thread.run();

        }
    }

    /**
     * Stops Running Code
     */
    @FXML
    private void handleStopButton(){
        process.destroy();
    }
}
