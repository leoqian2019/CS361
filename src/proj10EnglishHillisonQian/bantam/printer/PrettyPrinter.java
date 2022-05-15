/*
 * File: PrettyPrinter.java
 * Authors: Nick English, Nico Hillison, Leo Qian
 * Date: 5/5/22
 *
 */

package proj10EnglishHillisonQian.bantam.printer;

import javafx.util.Pair;
import proj10EnglishHillisonQian.bantam.ast.*;
import proj10EnglishHillisonQian.bantam.parser.Parser;
import proj10EnglishHillisonQian.bantam.util.CompilationException;
import proj10EnglishHillisonQian.bantam.util.Error;
import proj10EnglishHillisonQian.bantam.util.ErrorHandler;

import java.util.LinkedList;
import java.util.List;

/**
 * Prints a program or file in a pretty format from its AST.
 * Uses the PrettyPrintVisitor to print.
 */
public class PrettyPrinter {
    private List<Error> errors;
    private boolean returnOutput;
    private LinkedList<Pair<Integer, String>> commentQueue;

    public PrettyPrinter(){
    }

    public void setReturnOutput(boolean returnOutput){
        this.returnOutput = returnOutput;
    }

    public List<Error> getErrors(){
        return this.errors;
    }

    /**
     * Creates and runs a visitor to pretty print a program.
     * @param program the program to pretty print
     * @return the output string or null depending on returnOutput field
     */
    private String prettyPrint(Program program){
        PrettyPrintVisitor visitor = new PrettyPrintVisitor(returnOutput);

        // pass the comments to the visitor
        if(this.commentQueue != null){
            visitor.setCommentQueue(commentQueue);
        }
        // start printing
        visitor.visit(program);

        // get the output if necessary
        if(returnOutput){
            return visitor.getOutput();
        }
        return null;
    }

    /**
     * Generates a program from a file and calls prettyPrint for the program.
     * @param filename the file to pretty print
     * @return the output string or null depending on returnOutput field
     */
    public String prettyPrint(String filename){
        ErrorHandler errorHandler = new ErrorHandler();
        Parser parser = new Parser(errorHandler);
        Program program;
        try {
            // generate the program from the file and get the comments
            program = parser.parse(filename);
            this.commentQueue = parser.getCommentQueue();

            // pretty print the program
            return prettyPrint(program);
        } catch (CompilationException ex) {
            // if exception thrown, save the errors
            this.errors = errorHandler.getErrorList();
            return null;
        }
    }

    /**
     * main method to test the pretty printer on files
     * @param args the files to pretty print
     */
    public static void main(String[] args){
        PrettyPrinter printer = new PrettyPrinter();
        String[] files;
        if(args.length < 1){
            files = new String[1];
            files[0] = ("ParserTestEnglishHillisonQian.btm");
        } else {
            files = args;
        }

        // pretty print provided file(s)
        for (String inFile : files) {
            System.out.println("\n========== Results for " + inFile + " =============");
            try {
                printer.prettyPrint(inFile);

            } catch (CompilationException ex) {
                // print exception and found errors
                System.out.println(ex.getMessage());
                System.out.println("  There were errors:");
                for (Error error : printer.errors) {
                    System.out.println("\t" + error.toString());
                }
            }

        }
    }
}
