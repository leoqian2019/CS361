/*
 * File: proj5CorrellHilisonQian.CodeAreaHelpers.java
 * Names: Leo Qian, Cassidy Correl, Nico Hillison
 * Class: CS361
 * Project 5
 * Date: 3/7/2022
 */

package proj5CorrellHilisonQian;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.AnchorPane;
import org.fxmisc.flowless.VirtualizedScrollPane;
import org.fxmisc.richtext.CodeArea;

import java.util.HashMap;

/**
 * Contains helper methods for text areas.
 */
public class CodeAreaHelpers {
    private final TabPane tabPane;
    private HashMap<Tab, Boolean> textHasChangedMap;

    public CodeAreaHelpers(TabPane pane,HashMap<Tab, Boolean> textHasChangedMap){
        this.tabPane = pane;
        this.textHasChangedMap = textHasChangedMap;
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
        return getCurrentTab().getText();
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
        VirtualizedScrollPane<CodeArea> newPane = new
                VirtualizedScrollPane<>(new CodeArea());
        AnchorPane ap = new AnchorPane();
        ap.getChildren().add(newPane);
        tab.setContent(ap);
        JavaKeywordsAsync keywordColors= new JavaKeywordsAsync(this.getCurrentCodeArea());
        this.getCurrentCodeArea().replaceText("class");
        configureDirtyTracking(tab);

        AnchorPane.setBottomAnchor(newPane, 0.0);
        AnchorPane.setTopAnchor(newPane, 0.0);
        AnchorPane.setLeftAnchor(newPane, 0.0);
        AnchorPane.setRightAnchor(newPane, 0.0);
        tab.setContent(ap);

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
}
