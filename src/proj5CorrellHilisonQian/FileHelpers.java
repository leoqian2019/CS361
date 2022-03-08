/*
 * File: proj5CorrellHilisonQian.FileHelpers.java
 * Names: Leo Qian, Cassidy Correl, Nico Hillison
 * Class: CS361
 * Project 5
 * Date: 3/7/2022
 */

package proj5CorrellHilisonQian;

import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import org.fxmisc.richtext.CodeArea;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Scanner;

/**
 * Contains helper methods for saving and opening files.
 */
public class FileHelpers {
    private final CodeAreaHelpers tabHelper;

    public FileHelpers(CodeAreaHelpers tabHelper){
        this.tabHelper = tabHelper;
    }

    /**
     * Writes the content of a file to the new CodeArea.
     *
     * @param openedFile the file to write into the CodeArea.
     */
    public void writeFileToCodeArea(File openedFile){
        CodeArea currentArea = this.tabHelper.getCurrentCodeArea();
        currentArea.replaceText(""); // clear default "Sample text" message
        try {
            Scanner scan = new Scanner(new File(String
                    .valueOf(openedFile))).useDelimiter("\\s+");
            while (scan.hasNextLine()) {
                currentArea.appendText(scan.nextLine() + "\n");
            }
        }catch (FileNotFoundException ex) {
            exceptionAlert(ex);
        }
    }

    /**
     * Writes the content of a Tab to a given file path.
     *
     * @param currentFile the File currently referenced by the Tab.
     * @return return true if file saved, return false if file not saved
     */
    public boolean saveCurrentFile(File currentFile){
        try {
            PrintWriter outFile = new PrintWriter(String.valueOf(currentFile));
            outFile.println(this.tabHelper.getCurrentCodeArea().getText());
            outFile.close();
            this.tabHelper.getCurrentTab().setText(currentFile.getName());

            return true;
        } catch (FileNotFoundException e) {
            exceptionAlert(e);
            return false;
        }

    }

    /**
     * Helper method to display error message to user when an exception is thrown
     *
     * this type is default since main need to use it to print possible exception message
     */
    void exceptionAlert(Exception ex){
        Alert alert = new Alert(Alert.AlertType.ERROR);
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
}
