/*
 * File: Converter.java
 * Authors: Nick English, Nico Hillison, Leo Qian
 * Date: 5/5/22
 *
 */

package proj10EnglishHillisonQian.bantam.printer;

import proj10EnglishHillisonQian.bantam.ast.*;
import proj10EnglishHillisonQian.bantam.parser.Parser;
import proj10EnglishHillisonQian.bantam.semant.SemanticAnalyzer;
import proj10EnglishHillisonQian.bantam.util.ClassTreeNode;
import proj10EnglishHillisonQian.bantam.util.CompilationException;
import proj10EnglishHillisonQian.bantam.util.Error;
import proj10EnglishHillisonQian.bantam.util.ErrorHandler;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

/**
 * class converting bantam java file into java file
 * and save it as Main.java in the tmp folder
 */
public class Converter extends PrettyPrintVisitor{
    /**
     * constructor that sets returnOutput to true
     */
    public Converter() {
        super(true);
    }

    /**
     * the convert method that takes in a program node and writes the converted java file
     * @param program the program AST node to be converted
     * @return the string of the converted java file
     */
    public String convert(Program program){
        // after visiting the program node, store the converted string in a variable
        this.visit(program);
        String output = this.getOutput();

        try{
            // write the string into a java file
            BufferedWriter writer = new BufferedWriter(new FileWriter("src/tmp/Main.java"));
            writer.write(output);
            writer.close();
        }
        catch(IOException e){
            System.out.println(e.getMessage());
            return null;
        }

        return output;
    }

    /**
     * convert method that takes in a file name
     * @param fileName bantam Java file name to be converted
     * @return the string of the converted java file
     */
    public String convert(String fileName){
        ErrorHandler errorHandler = new ErrorHandler();
        Parser parser = new Parser(errorHandler);
        SemanticAnalyzer analyzer = new SemanticAnalyzer(errorHandler);
        Program program;
        try {
            // try to check for syntax and semantic errors
            program = parser.parse(fileName);
            ClassTreeNode node = analyzer.analyze(program);
            // add the two built in classes to the class list of the program node
            Class_ textIONode = node.getClassMap().get("TextIO").getASTNode();
            Class_ sysNode = node.getClassMap().get("Sys").getASTNode();
            program.getClassList().addElement(textIONode);
            program.getClassList().addElement(sysNode);

            System.out.println("Analyzed Successfully, start converting");
            return convert(program);
        } catch (CompilationException ex) {
            System.out.println(ex.getMessage());
            System.out.println("  There were errors:");
            List<Error> errors = errorHandler.getErrorList();
            for (Error error : errors) {
                System.out.println("\t" + error.toString());
            }
            return null;
        }
    }

    /**
     * visit the filed node and make them ready to run as java file
     * @param node the field node
     * @return
     */
    @Override
    public Object visit(Field node){
        indentLine();
        // keyword "protected" need to be before all fields
        print("protected "+node.getType() + " " + node.getName());
        Expr initExpr = node.getInit();
        if (initExpr != null) {
            print(" = ");
            initExpr.accept(this);
        }
        print(";\n");
        return null;
    }

    /**
     * visit method node and make them ready to run as java file
     * @param node the method node
     * @return
     */
    @Override
    public Object visit(Method node){
        indentLine();
        // keyword "public" need to be before all methods
        print("public "+node.getReturnType() + " " + node.getName() + "(");

        node.getFormalList().accept(this);
        print(") {\n");

        indentLevel++;
        node.getStmtList().accept(this);
        indentLevel--;
        indentLine();
        print("}\n\n");

        return null;
    }

    /**
     * visit class_ node and add convert
     * @param node the class node
     * @return
     */
    @Override
    public Object visit(Class_ node){
        indentLine();
        if(node.getName().equals("Main")){
            // keyword "public" need to be before the "Main" class
            print("public ");
        }
        print("class " + node.getName());
        if(node.getParent() != null && !"Object".equals(node.getParent())){
            print(" extends " + node.getParent());
        }
        print(" {\n");
        indentLevel++;
        node.getMemberList().accept(this);
        if(node.getName().equals("Main")){
            // method to be included with the "Main" class
            print(
                "\tpublic static void main(String[] args) {\n" +
                        "\t\t(new Main()).main();\n" +
                        "\t}\n"
            );
        }
        indentLevel--;
        indentLine();
        print("}\n\n");
        return null;
    }

    public static void main(String[] args) {
        Converter converter = new Converter();
        String[] files;
        if(args.length < 1){
            files = new String[1];
            files[0] = ("Main.btm");
        } else {
            files = args;
        }

        for (String inFile : files) {
            System.out.println("\n========== Results for " + inFile + " =============");
            String output = converter.convert(inFile);
            System.out.println(output);

        }
    }

}
