/*
 * File: proj10EnglishHillisonQian.TabHelper.java
 * Names: Nick English, Nico Hillison, Leo Qian
 * Class: CS361
 * Project 10
 * Date: 5/5/2022
 */

package proj10EnglishHillisonQian;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.ListView;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Popup;
import org.fxmisc.flowless.VirtualizedScrollPane;
import org.fxmisc.richtext.CodeArea;

import java.util.HashMap;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Contains helper methods for text areas.
 */
public class TabHelper {
    private final TabPane tabPane;
    private HashMap<Tab, Boolean> textHasChangedMap;
    private ContextMenu codeContextMenu;

    public TabHelper(TabPane pane, HashMap<Tab, Boolean> textHasChangedMap,
                     ContextMenu contextMenu){
        this.tabPane = pane;
        this.textHasChangedMap = textHasChangedMap;
        this.codeContextMenu = contextMenu;
    }

    /**
     * Returns the current Tab object.
     *
     * @return the currently selected Tab.
     */
    public Tab getCurrentTab() {
        return tabPane.getSelectionModel().getSelectedItem();
    }

    /**
     * Returns the title on the current Tab object.
     *
     * @return the name of the current Tab.
     */
    public String getCurrentTabTitle() {
        return tabPane.getSelectionModel().getSelectedItem().getText();
    }

    /**
     * Gets the CodeArea on the currently selected Tab.
     *
     * @return the currently selected Tab's CodeArea.
     */
    public CodeArea getCurrentCodeArea() {
        return (CodeArea) ((VirtualizedScrollPane) ((AnchorPane) (getCurrentTab())
                .getContent()).getChildren().get(0)).getContent();
    }

    /**
     * Constructs a new CodeArea on a Tab and anchors it to the corners.
     *
     * @param tab the Tab to build the CodeArea on.
     */
    public void createCodeAreaForTab(Tab tab) {
        CodeArea codeArea = new CodeArea();
        codeArea.getStyleClass().add("codearea");


        // add the other half of bracket when user types in "(", "{", or "["
        codeArea.setOnKeyTyped(event -> {
            String charTyped = event.getCharacter();
            String secondToLastCharBeforeCaret = codeArea.getText();
            int position = codeArea.getCaretPosition();
                // avoid index out of bound issue
                if(position-2 > 0 && position-1>0){
                    secondToLastCharBeforeCaret = codeArea.getText(codeArea.
                            getCaretPosition()-2,position-1);
                }

            if(charTyped.equals("(")){
                codeArea.insertText(position, ")");
                codeArea.displaceCaret(position-1);
                codeArea.moveTo(position);
            }
            else if(charTyped.equals("{")){
                codeArea.insertText(position, "}");
                codeArea.displaceCaret(position-1);
                codeArea.moveTo(position);
            }
            else if(charTyped.equals("[")){
                codeArea.insertText(position, "]");
                codeArea.displaceCaret(position-1);
                codeArea.moveTo(position);
            }
            // if the user types the beginning of a keyword in the new line
            // or after a space, a popup will show
            else if((secondToLastCharBeforeCaret.equals(" ") ||
                    secondToLastCharBeforeCaret.equals("\n"))){
                if(charTyped.equals("i")){
                    displayPopup("i");
                }
                else if(charTyped.equals("e")){
                    displayPopup("e");
                }
                else if(charTyped.equals("b")){
                    displayPopup("b");
                }
                else if(charTyped.equals("f")){
                    displayPopup("f");
                }
                else if(charTyped.equals("w")){
                    displayPopup("w");
                }

            }

        });
        VirtualizedScrollPane<CodeArea> newPane = new
                VirtualizedScrollPane<>(codeArea);
        AnchorPane ap = new AnchorPane();
        ap.getChildren().add(newPane);
        tab.setContent(ap);
        BantamHighlighter keywordColors= new BantamHighlighter(this.getCurrentCodeArea());
        this.getCurrentCodeArea().replaceText("class");
        configureDirtyTracking(tab);

        AnchorPane.setBottomAnchor(newPane, 0.0);
        AnchorPane.setTopAnchor(newPane, 0.0);
        AnchorPane.setLeftAnchor(newPane, 0.0);
        AnchorPane.setRightAnchor(newPane, 0.0);
        tab.setContent(ap);

        ((VirtualizedScrollPane<CodeArea>) ((AnchorPane) tab.getContent()).getChildren()
                .get(0)).getContent().setContextMenu(codeContextMenu);
    }

    /**
     * Configures tracking changes to a CodeArea.
     *
     * @param tab the current Tab.
     */
    public void configureDirtyTracking(Tab tab) {
        textHasChangedMap.put(tab, false);
        ((VirtualizedScrollPane<CodeArea>) (((AnchorPane) tab.getContent())
                .getChildren()).get(0)).getContent().textProperty()
                .addListener(new ChangeListener<>() {

            /**
             * Updates the change tracker when a change is detected.
             *
             * @param observable the observed object.
             * @param oldValue the previous value of the object.
             * @param newValue the current value of the object.
             */
            @Override
            public void changed(ObservableValue<? extends String> observable,
                                String oldValue, String newValue) {
                textHasChangedMap.put(tab, true);
            }
        });
    }

    /**
     * display popup after the first letter of a keyword is typed
     * @param keyWord starting letter of a keyword
     */
    public void displayPopup(String keyWord){
        Popup popup = new Popup();
        popup.setAutoHide(true);
        ListView<String> list = new ListView<>();
        list.setPrefSize(150,100);
        AtomicReference<String> textToInsert = new AtomicReference<>("");

        // insert text into the code area once the user selects the text
        list.setOnKeyPressed(event -> {
            if(event.getCode() == KeyCode.ENTER){
                textToInsert.set(list.getSelectionModel().getSelectedItem());
                popup.hide();
                getCurrentCodeArea().appendText(String.valueOf(textToInsert)
                        .substring(1));
            }
            else{
                popup.hide();
            }
        });
        list.setOnMouseClicked(event -> {
            textToInsert.set(list.getSelectionModel().getSelectedItem());
            popup.hide();
            getCurrentCodeArea().appendText(String.valueOf(textToInsert).substring(1));
        });

        if(keyWord == "i"){
            list.getItems().addAll("if","implements","import","int","interface");
            popup.getContent().setAll(list);
            popup.show(tabPane.getScene().getWindow());
        }
        else if(keyWord == "e"){
            list.getItems().addAll("else","extends");
            popup.getContent().setAll(list);
            popup.show(tabPane.getScene().getWindow());
        }
        else if (keyWord == "b"){
            list.getItems().addAll("boolean","break");
            popup.getContent().setAll(list);
            popup.show(tabPane.getScene().getWindow());
        }
        else if (keyWord == "f"){
            list.getItems().addAll("for","float","final");
            popup.getContent().setAll(list);
            popup.show(tabPane.getScene().getWindow());
        }
        else if (keyWord == "w"){
            list.getItems().addAll("while");
            popup.getContent().setAll(list);
            popup.show(tabPane.getScene().getWindow());
        }
    }
}
