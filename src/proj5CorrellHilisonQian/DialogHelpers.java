/*
 * File: proj5CorrellHilisonQian.DialogHelpers.java
 * Names: Leo Qian, Cassidy Correl, Nico Hillison
 * Class: CS361
 * Project 5
 * Date: 3/7/2022
 */

package proj5CorrellHilisonQian;

import javafx.scene.control.*;
import javafx.stage.FileChooser;
import java.io.File;
import java.util.HashMap;
import java.util.Optional;

/**
 * Contains helper methods for creating dialogs.
 */
public class DialogHelpers {
    private final CodeAreaHelpers tabHelper;
    private final FileHelpers fileHelper;
    private final TabPane tabPane;
    private HashMap<Tab, File> filenameFileMap;
    private HashMap<Tab, Boolean> textHasChangedMap;
    private final FileChooser chooser = new FileChooser();

    public DialogHelpers(TabPane tabPane, CodeAreaHelpers tabHelper,
                         FileHelpers fileHelper, HashMap<Tab, File> filenameFileMap,
                         HashMap<Tab, Boolean> textHasChangedMap){
        this.tabHelper = tabHelper;
        this.fileHelper = fileHelper;
        this.tabPane = tabPane;
        this.filenameFileMap = filenameFileMap;
        this.textHasChangedMap = textHasChangedMap;

        // restrict the type of files that our file chooser can handle
        chooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Java Files","*.java")
        );
    }

    /**
     * Constructs and manages the About Dialog.
     */
    public void aboutDialog(){
        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle("About...");
        dialog.setContentText("This is project 5 by Cassidy, Nico, and Leo");

        ButtonType ok = new ButtonType("OK", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().add(ok);

        dialog.showAndWait();
    }

    /**
     * Builds and manages the Save As Dialog box.  Saves a Tab's contents
     * to a file path.
     * @return return whether the file is saved
     */
    public boolean saveAsDialog()
    {
        String currentTabTitle = tabHelper.getCurrentTabTitle();
        chooser.setTitle("Save as...");
        chooser.setInitialFileName(currentTabTitle);
        File currentFile = chooser.showSaveDialog(null);
        if (currentFile != null) {
            this.filenameFileMap.put(tabHelper.getCurrentTab(), currentFile);

            // add tool tip for new files saved
            tabHelper.getCurrentTab().setTooltip(new Tooltip(currentFile.getPath()));
            boolean isSaved = fileHelper.saveCurrentFile(currentFile);
            // if the file is saved successfully, change the changed filed to false
            if (isSaved) {
                this.textHasChangedMap.put(tabHelper.getCurrentTab(), false);
            }
            return isSaved;
        }
        else {
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
            // if the file is already opened, select that tab and exit the method
            for(Tab tab:tabPane.getTabs()){
                if(openedFile.equals(filenameFileMap.getOrDefault(tab, null))){
                    tabPane.getSelectionModel().select(tab);
                    return;
                }
            }

            // get ready for the new tab
            Tab newTab = new Tab(openedFile.getName());
            // set tooltip of the new tab to the path of the opened file
            newTab.setTooltip(new Tooltip(openedFile.getPath()));
            tabPane.getTabs().add(newTab);
            tabPane.getSelectionModel().select(newTab);
            tabHelper.createCodeAreaForTab(newTab);
            fileHelper.writeFileToCodeArea(openedFile);
            // remember path when saving
            filenameFileMap.put(tabHelper.getCurrentTab(), openedFile);
            textHasChangedMap.put(tabHelper.getCurrentTab(), false);
        }
    }

    /**
     * Constructs the Close Dialog and returns user selection.
     *
     * @return which type of Button was clicked within the dialog.
     */
    public Optional<ButtonType> closeDialog(String tabName){
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setHeaderText(String.format("Do you want to save " + "your progress on %s " +
                "before closing?", tabName));
        ButtonType yes = new ButtonType("Yes");
        ButtonType no = new ButtonType("No");
        ButtonType cancel = new ButtonType("Cancel",
                ButtonBar.ButtonData.CANCEL_CLOSE);
        alert.getButtonTypes().setAll(yes, no, cancel);
        return alert.showAndWait();
    }

    /**
     * Constructs the dialog asking user whether to save progress before compiling
     *
     * @return which type of Button was clicked within the dialog.
     */
    public Optional<ButtonType> CompileDialog(){
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setHeaderText(String.format("Do you want to save " + "your changes to %s " +
                "before compiling?", tabHelper.getCurrentTab().getText()));
        ButtonType yes = new ButtonType("Yes");
        ButtonType no = new ButtonType("No");
        ButtonType cancel = new ButtonType("Cancel",
                ButtonBar.ButtonData.CANCEL_CLOSE);
        alert.getButtonTypes().setAll(yes, no, cancel);
        return alert.showAndWait();
    }

    /**
     * alert the user to save before compiling the file if the tab is never saved
     */
    public void nullFileError(){
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setHeaderText("Your current tab is never saved\nPlease Save before compiling");
        alert.setContentText(null);
        alert.show();
    }

}

