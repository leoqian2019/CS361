/*
* File: proj6EnglishHillisonQian.Run.java
 * Names: Nick English, Nico Hillison, Leo Qian
 * Class: CS361
 * Project 6
 * Date: 3/18/2022
*/

package proj6EnglishHillisonQian;

import javafx.application.Platform;
import org.fxmisc.richtext.StyleClassedTextArea;

import java.io.*;

public class Run extends Thread{
    private File fileToCompile;
    private AlertHandler alertHandler;
    private StyleClassedTextArea console;
    private String errorMessage;

    public Run(File fileToCompile, StyleClassedTextArea console) {
        this.fileToCompile = fileToCompile;
        this.alertHandler = new AlertHandler();
        this.console = console;
        this.errorMessage = null;
    }

    /**
     * Runs a compiled java file and redirects the output to the console
     * */
    public void run() {

        ProcessBuilder pb = new ProcessBuilder();
        try {
            pb.command("java", "-cp", this.fileToCompile.getCanonicalPath()
                    .replace("/"+ this.fileToCompile.getName(), ""),
                    this.fileToCompile.getName().replace(".java", ""));
            pb.redirectErrorStream(true);
            Process p = pb.start();
            Controller.outputStream = p.getOutputStream();

            InputStream processInputStream = p.getInputStream();
            int byteValueFromRead;
            String terminalOutput = "";
            int time = 0;

            while(p.isAlive()) {

                while (processInputStream.available() != 0 &&  (byteValueFromRead =
                        processInputStream.read()) != -1) {
                    terminalOutput += (char) byteValueFromRead;
                    if (byteValueFromRead == 10)
                        break;
                }

                if (terminalOutput != ""){
                    // reassign it to a new variable to avoid error in lambda
                    // try swapping output with terminalOutput in Platform.runLater()
                    // for demonstration
                    if(time >= 100) {
                        String output = terminalOutput;
                        Platform.runLater(() -> {
                            console.append(output, "");
                            console.requestFollowCaret();
                        });
                        terminalOutput = "";
                        time = 0;
                    }else{
                        time++;
                    }

                }
            }

            p.waitFor();

            if (p.exitValue() != 0) {
                BufferedReader r = new BufferedReader(
                                   new InputStreamReader(p.getErrorStream()));
                String readLine;
                String errorMessage = "";

                while ((readLine = r.readLine()) != null) {
                    errorMessage += readLine + "\n";
                }

                this.errorMessage = errorMessage;
            }
        } catch (IOException e) {
            Platform.runLater(() -> {
                this.alertHandler.showErrorAlert("File not found or not accessible. "+
                        "Ensure a valid path is specified and try again.",
                        "Failed to Open File");
            });

            this.errorMessage = "File could not be found.\n";

            return;
        } catch (InterruptedException e) {
            Platform.runLater(() -> {
                this.alertHandler.showErrorAlert("User interrupted the process, exiting.",
                        "Process interrupted");
            });
            this.errorMessage = "Process interrupted.\n";
            return;
        }
    }

    /**
     * @return a boolean that tells if an Error Message exists
     * */
    public boolean hasErrorMessage(){
        return errorMessage != null;
    }

    /**
     * @return the error message
     * */
    public String getErrorMessage() {
        return errorMessage;
    }

}
