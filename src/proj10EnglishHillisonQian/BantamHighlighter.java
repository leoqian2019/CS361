/*
 * File: proj10EnglishHillisonQian.JavaKeywordsAsync.java
 * Names: Nick English, Nico Hillison, Leo Qian
 * Class: CS361
 * Project 10
 * Date: 5/5/2022
 */

package proj10EnglishHillisonQian;

import java.time.Duration;
import java.util.Collection;
import java.util.Collections;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javafx.concurrent.Task;
import org.fxmisc.richtext.CodeArea;
import org.fxmisc.richtext.LineNumberFactory;
import org.fxmisc.richtext.model.StyleSpans;
import org.fxmisc.richtext.model.StyleSpansBuilder;
import org.reactfx.Subscription;

/**
 * Runs keyword highlighting for a CodeArea.
 */
public class BantamHighlighter {

    private static final String[] KEYWORDS = new String[] {
            "break", "boolean", "int", "void", "cast", "class", "var", "extends", "this",
            "super", "else", "extends", "for", "if", "instanceof", "new", "return", "while"
    };

    private static final String KEYWORD_PATTERN = "\\b(" + String.join("|", KEYWORDS) + ")\\b";
    private static final String PAREN_PATTERN = "\\(|\\)";
    private static final String BRACE_PATTERN = "\\{|\\}";
    private static final String BRACKET_PATTERN = "\\[|\\]";
    private static final String SEMICOLON_PATTERN = "\\;";
    private static final String STRING_PATTERN = "\"([^\"\\\\]|\\\\.)*\"";
    private static final String COMMENT_PATTERN = "//[^\n]*" + "|" + "/\\*(.|\\R)*?\\*/";
    private static final String FINAL_INT_PATTERN =
            "(?<=\\s|[\\[\\]\\(\\){};*+\\-\\/=!<>%^]|^)[-+]?\\d+" +
                    "(?=\\s|[\\[\\]\\(\\){};*+\\-\\/=!<>%^]|$)";

    private static final Pattern PATTERN = Pattern.compile(
            "(?<FINALINT>" + FINAL_INT_PATTERN + ")" + "|(?<KEYWORD>" + KEYWORD_PATTERN + ")"
                    + "|(?<PAREN>" + PAREN_PATTERN + ")"
                    + "|(?<BRACE>" + BRACE_PATTERN + ")"
                    + "|(?<BRACKET>" + BRACKET_PATTERN + ")"
                    + "|(?<SEMICOLON>" + SEMICOLON_PATTERN + ")"
                    + "|(?<STRING>" + STRING_PATTERN + ")"
                    + "|(?<COMMENT>" + COMMENT_PATTERN + ")"
    );

    /**
     * Creates a new keyword highlighter for a codeArea.
     *
     * @param codeArea the CodeArea to highlight in.
     */
    public BantamHighlighter(CodeArea codeArea){
            this.codeArea = codeArea;
            start();
    }

    private final CodeArea codeArea;
    private ExecutorService executor;

    /**
     * Performs startup of the keyword highlighter.
     */
    public void start() {
        executor = Executors.newSingleThreadExecutor();
        codeArea.setParagraphGraphicFactory(LineNumberFactory.get(codeArea));
        Subscription cleanupWhenDone = codeArea.multiPlainChanges()
                .successionEnds(Duration.ofMillis(500))
                .retainLatestUntilLater(executor)
                .supplyTask(this::computeHighlightingAsync)
                .awaitLatest(codeArea.multiPlainChanges())
                .filterMap(t -> {
                    if(t.isSuccess()) {
                        return Optional.of(t.get());
                    } else {
                        t.getFailure().printStackTrace();
                        return Optional.empty();
                    }
                })
                .subscribe(this::applyHighlighting);

    }

    /**
     * Creates and runs an asynchronous highlighting task on the CodeArea.
     *
     * @return the created and executed task.
     */
    private Task<StyleSpans<Collection<String>>> computeHighlightingAsync() {
        String text = codeArea.getText();
        Task<StyleSpans<Collection<String>>> task = new Task<StyleSpans<Collection<String>>>() {
            @Override
            protected StyleSpans<Collection<String>> call() throws Exception {
                return computeHighlighting(text);
            }
        };
        executor.execute(task);
        return task;
    }

    /**
     * Applies the computed highlighting to the CodeArea.
     *
     * @param highlighting the highlighting styles to apply.
     */
    private void applyHighlighting(StyleSpans<Collection<String>> highlighting) {
        codeArea.setStyleSpans(0, highlighting);
    }

    /**
     * Calculates what to highlight in the CodeArea.
     *
     * @param text the text to check.
     * @return the styles to apply.
     */
    private StyleSpans<Collection<String>> computeHighlighting(String text) {
        Matcher matcher = PATTERN.matcher(text);
        int lastKwEnd = 0;
        StyleSpansBuilder<Collection<String>> spansBuilder
                = new StyleSpansBuilder<>();
        while(matcher.find()) {
            String styleClass =
                    matcher.group("FINALINT") != null ? "finalInt" :
                        matcher.group("KEYWORD") != null ? "keyword" :
                           matcher.group("PAREN") != null ? "paren" :
                              matcher.group("BRACE") != null ? "brace" :
                                 matcher.group("BRACKET") != null ? "bracket" :
                                    matcher.group("SEMICOLON") != null ? "semicolon" :
                                       matcher.group("STRING") != null ? "string" :
                                          matcher.group("COMMENT") != null ? "comment" :
                                             null; /* never */ assert styleClass != null;
            spansBuilder.add(Collections.emptyList(), matcher.start() - lastKwEnd);
            spansBuilder.add(Collections.singleton(styleClass), matcher.end() - matcher.start());
            lastKwEnd = matcher.end();
        }
        spansBuilder.add(Collections.emptyList(), text.length() - lastKwEnd);
        return spansBuilder.create();
    }
}