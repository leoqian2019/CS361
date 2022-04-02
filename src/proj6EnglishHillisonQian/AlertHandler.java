/*
 * File: proj6EnglishHillisonQian.AlertHandler.java
 * Names: Nick English, Nico Hillison, Leo Qian
 * Class: CS361
 * Project 6
 * Date: 3/18/2022
 */
package proj6EnglishHillisonQian;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;

import java.util.Optional;

/**
 * Handles Alerts that appear for the user to see.
 */
public class AlertHandler {

    /**
     * Constructor, empty.
     */
    public AlertHandler(){}

    /**
     * Shows an alert about an error.
     * @param message the message to display in the alert
     * @param title the title of the alert
     */
    public void showErrorAlert(String message, String title) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * Shows an alert for confirmation from the user.
     * @param title the title of the alert
     * @return the button clicked by the user.
     */
    public Optional<ButtonType> showConfirmationAlert(String title) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setHeaderText(title);
        ButtonType yes = new ButtonType("Yes");
        ButtonType no = new ButtonType("No");
        ButtonType cancel = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
        alert.getButtonTypes().setAll(yes, no, cancel);
        return alert.showAndWait();
    }

}
