/*
 * File: proj6EnglishHillisonQian.DialogHelper.java
 * Names: Nick English, Nico Hillison, Leo Qian
 * Class: CS361
 * Project 6
 * Date: 3/18/2022
 */

package proj6EnglishHillisonQian;

import javafx.scene.control.*;
import javafx.stage.FileChooser;
import java.io.File;
import java.util.HashMap;
import java.util.Optional;

/**
 * Contains helper methods for creating dialogs.
 */
public class DialogHelper {
    private final TabHelper tabHelper;
    private final FileController fileHelper;
    private final TabPane tabPane;
    private HashMap<Tab, File> filenameFileMap;
    private HashMap<Tab, Boolean> textHasChangedMap;
    private final FileChooser chooser = new FileChooser();
    private AlertHandler alertHandler;
    private ContextMenu tabContextMenu;

    public DialogHelper(TabPane tabPane, TabHelper tabHelper, FileController fileHelper,
                        HashMap<Tab, File> filenameFileMap,
                        HashMap<Tab, Boolean> textHasChangedMap, AlertHandler alertHandler,
                        ContextMenu tabContextMenu){
        this.tabHelper = tabHelper;
        this.fileHelper = fileHelper;
        this.tabPane = tabPane;
        this.filenameFileMap = filenameFileMap;
        this.textHasChangedMap = textHasChangedMap;
        this.alertHandler = alertHandler;
        this.tabContextMenu = tabContextMenu;
    }

    /**
     * Constructs and manages the About Dialog.
     */
    public void aboutDialog(){
        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle("About...");
        dialog.setContentText("This is project 5 by Nick, Nico, and Leo");

        ButtonType ok = new ButtonType("OK", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().add(ok);

        dialog.showAndWait();
    }

    /**
     * Builds and manages the Save As Dialog box.  Saves a Tab's contents
     * to a file path.
     *
     * @return true if file is saved, false if process is cancelled
     */
    public boolean saveAsDialog(String tabName)
    {
        String currentTabTitle = tabHelper.getCurrentTabTitle();
        chooser.setTitle("Save as...");
        // add default file extension to the file name
        chooser.setInitialFileName(tabName+".java");
        File currentFile = chooser.showSaveDialog(null);
        if (currentFile != null) {
            this.filenameFileMap.put(tabHelper.getCurrentTab(), currentFile);
            boolean saved = fileHelper.saveCurrentFile(currentFile);
            if(saved){
                this.textHasChangedMap.put(tabHelper.getCurrentTab(), false);
                tabHelper.getCurrentTab().setTooltip(new Tooltip(currentFile.toString()));
            }
            return true;
        }
        else{
            return false;
        }
    }

    /**
     * Builds and manages the Open Dialog.  Opens a file into a Tab.
     *
     */
    public void openDialog()
    {
        chooser.setTitle("Open file...");
        File openedFile = chooser.showOpenDialog(null);
        if (openedFile != null){
            for(Tab tab:tabPane.getTabs()){
                if(openedFile.equals(filenameFileMap.getOrDefault(tab, null))){
                    tabPane.getSelectionModel().select(tab);
                    return;
                }
            }
            Tab newTab = new Tab(openedFile.getName());
            tabPane.getTabs().add(newTab);
            tabPane.getSelectionModel().select(newTab);
            tabHelper.createCodeAreaForTab(newTab);
            fileHelper.writeFileToCodeArea(openedFile);
            // remember path when saving
            filenameFileMap.put(tabHelper.getCurrentTab(), openedFile);
            textHasChangedMap.put(tabHelper.getCurrentTab(), false);
            newTab.setTooltip(new Tooltip(openedFile.toString()));
            newTab.setContextMenu(tabContextMenu);
        }
    }

    /**
     * Constructs the Close Dialog and returns user selection.
     *
     * @return which type of Button was clicked within the dialog.
     */
    public Optional<ButtonType> closeDialog(String tabName){
       return alertHandler.showConfirmationAlert(String.format("Do you want to save "
               + "your progress on %s " + "before closing?", tabName));
    }

    /**
     * Constructs the Save Dialog and returns user selection.
     *
     * @return which type of Button was clicked within the dialog.
     */
    public Optional<ButtonType> saveDialog() {
        return alertHandler.showConfirmationAlert("Do you want to save your " +
                "changes before compiling?");
    }
}
