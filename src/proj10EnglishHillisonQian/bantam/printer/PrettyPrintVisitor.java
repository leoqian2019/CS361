/*
 * File: PrettyPrintVisitor.java
 * Authors: Nick English
 * Date: 5/5/22
 */

package proj10EnglishHillisonQian.bantam.printer;

import javafx.util.Pair;
import proj10EnglishHillisonQian.bantam.ast.*;
import proj10EnglishHillisonQian.bantam.visitor.Visitor;

import java.util.LinkedList;


/**
 * This visitor prints the program that generated a given AST
 * in a pretty format.
 */
public class PrettyPrintVisitor extends Visitor {
    private boolean returnOutput;
    private StringBuilder output;
    public int indentLevel;
    private LinkedList<Pair<Integer, String>> commentQueue;

    public PrettyPrintVisitor(boolean returnOutput){
        this.indentLevel = 0;
        this.returnOutput = returnOutput;
        this.output = new StringBuilder("");
    }

    public String getOutput(){
        return this.output.toString();
    }

    public void setCommentQueue(LinkedList<Pair<Integer, String>> map){
        this.commentQueue = map;
    }

    /**
     * Pretty prints any comments not yet printed up to the current line
     * @param line the line to stop at
     */
    public void printComments(int line){
        // check if comments to print
        if(this.commentQueue != null && this.commentQueue.peekFirst() != null){
            while(this.commentQueue.peekFirst() != null &&
                    this.commentQueue.peekFirst().getKey() <= line){
                // print and remove top comment, indent next line
                print(this.commentQueue.poll().getValue() + "\n");
                indentLine();
            }
        }
    }

    /**
     * Handles the output printing of strings.
     * @param s the string to output.
     */
    public void print(String s){
        if(returnOutput){
            output.append(s);
        } else {
            System.out.print(s);
        }
    }


    /**
     * Creates and returns a string builder with the necessary indentation.
     */
    void indentLine(){
        StringBuilder b = new StringBuilder();
        // add the necessary num tabs // NOTE - can't use String.repeat in this J version
        for(int i = 0; i < this.indentLevel; i++){
            b.append("\t");
        }
        // output the tabs
        print(b.toString());
    }


    /**
     * Creates and returns a string builder with the necessary indentation.
     */
    private void newLine(int line){
        indentLine();
        printComments(line);
    }

    /**
     * Visit a class node
     *
     * @param node the class node
     * @return result of the visit
     */
    public Object visit(Class_ node){
        // start class
        newLine(node.getLineNum());
        print("class " + node.getName());

        // add extends clause and {
        if(node.getParent() != null && !"Object".equals(node.getParent())){
            print(" extends " + node.getParent());
        }
        print(" {\n");

        // add indent level for class body, visit
        indentLevel++;
        node.getMemberList().accept(this);
        indentLevel--;

        // end class
        newLine(node.getLineNum());
        print("}\n\n");
        return null;
    }

    /**
     * Visit a field node
     *
     * @param node the field node
     * @return result of the visit
     */
    public Object visit(Field node) {
        // print field type and name
        newLine(node.getLineNum());
        print(node.getType() + " " + node.getName());

        // visit and print initialization
        Expr initExpr = node.getInit();
        if (initExpr != null) {
            print(" = ");
            initExpr.accept(this);
        }

        print(";\n");
        return null;
    }

    /**
     * Visit a method node
     *
     * @param node the method node
     * @return result of the visit
     */
    public Object visit(Method node) {
        // print method type and name
        print("\n");
        newLine(node.getLineNum());
        print(node.getReturnType() + " " + node.getName() + "(");

        // visit formal list
        node.getFormalList().accept(this);
        print(") {\n");

        // indent for method body, visit
        indentLevel++;
        node.getStmtList().accept(this);
        indentLevel--;

        newLine(node.getLineNum());
        print("}\n");
        return null;
    }

    /**
     * Visit a formal node
     *
     * @param node the formal node
     * @return result of the visit
     */
    public Object visit(Formal node) {
        print(node.getType() + " " + node.getName());
        return null;
    }

    /**
     * Visit a declaration statement node
     *
     * @param node the declaration statement node
     * @return result of the visit
     */
    public Object visit(DeclStmt node) {
        newLine(node.getLineNum());
        print("var " + node.getName() + " = ");

        // visit initial expr
        node.getInit().accept(this);
        print(";\n");
        return null;
    }

    /**
     * Visit an expression statement node
     *
     * @param node the expression statement node
     * @return result of the visit
     */
    public Object visit(ExprStmt node) {
        newLine(node.getLineNum());

        // visit expr
        node.getExpr().accept(this);

        print(";\n");
        return null;
    }

    /**
     * Visit an if statement node
     *
     * @param node the if statement node
     * @return result of the visit
     */
    public Object visit(IfStmt node) {
        newLine(node.getLineNum());
        print("if (");

        // visit predicate
        node.getPredExpr().accept(this);
        print(") {\n");

        // indent for the body, visit
        indentLevel++;
        node.getThenStmt().accept(this);
        indentLevel--;

        newLine(node.getLineNum());
        print("}\n");

        // handle/visit else statement if present
        if (node.getElseStmt() != null) {
            newLine(node.getLineNum());
            print("else {\n");

            // indent the body, visit
            indentLevel++;
            node.getElseStmt().accept(this);
            indentLevel--;

            newLine(node.getLineNum());
            print("}\n\n");

        } else { // no else, end
            print("\n");
        }
        return null;
    }

    /**
     * Visit a while statement node
     *
     * @param node the while statement node
     * @return result of the visit
     */
    public Object visit(WhileStmt node) {
        newLine(node.getLineNum());
        print("while (");

        // visit predicate
        node.getPredExpr().accept(this);
        print(") {\n");

        // indent the body, visit
        indentLevel++;
        node.getBodyStmt().accept(this);
        indentLevel--;

        newLine(node.getLineNum());
        print("}\n\n");
        return null;
    }

    /**
     * Visit a for statement node
     *
     * @param node the for statement node
     * @return result of the visit
     */
    public Object visit(ForStmt node) {
        newLine(node.getLineNum());
        print("for(");

        // Init
        if(node.getInitExpr()!= null) {
            node.getInitExpr().accept(this);
        }
        print("; ");

        // Predicate
        if(node.getPredExpr()!=null) {
            node.getPredExpr().accept(this);
        }
        print("; ");

        // Update
        if(node.getUpdateExpr() != null) {
            node.getUpdateExpr().accept(this);
        }
        print(") {\n");

        // indent body, visit
        indentLevel++;
        node.getBodyStmt().accept(this);
        indentLevel--;

        newLine(node.getLineNum());
        print("}\n\n");
        return null;
    }

    /**
     * Visit a break statement node
     *
     * @param node the break statement node
     * @return result of the visit
     */
    public Object visit(BreakStmt node) {
        newLine(node.getLineNum());
        print("break;\n");
        return null;
    }

    /**
     * Visit a return statement node
     *
     * @param node the return statement node
     * @return result of the visit
     */
    public Object visit(ReturnStmt node) {
        newLine(node.getLineNum());
        print("return");

        // handle/visit expr
        if (node.getExpr() != null) {
            print(" ");
            node.getExpr().accept(this);
        }
        print(";\n");
        return null;
    }

    /**
     * Visit a dispatch expression node
     *
     * @param node the dispatch expression node
     * @return the type of the expression
     */
    public Object visit(DispatchExpr node) {
        // handle/visit reference
        if(node.getRefExpr() != null){
            node.getRefExpr().accept(this);
            print(".");
        }

        print(node.getMethodName() + "(");
        // handle/visit parameters
        if(node.getActualList() != null){
            node.getActualList().accept(this);
        }

        print(")");
        return null;
    }

    /**
     * Visit a list node of expressions
     *
     * @param node the expression list node
     * @return the result of the visit
     */
    public Object visit(ExprList node) {
        boolean comma = false;

        for (ASTNode expr : node) {
            // Place a comma if appropriate
            if(comma){
                print(", ");
            } else { comma = true;}

            // visit the expr
            expr.accept(this);
        }
        return null;
    }

    /**
     * Visit a new expression node
     *
     * @param node the new expression node
     * @return the type of the expression
     */
    public Object visit(NewExpr node) {
        print("new " + node.getType() + "()");
        return null;
    }

    /**
     * Visit an instanceof expression node
     *
     * @param node the instanceof expression node
     * @return the type of the expression
     */
    public Object visit(InstanceofExpr node) {
        node.getExpr().accept(this);
        print(" instanceof " + node.getType());
        return null;
    }

    /**
     * Visit a cast expression node
     *
     * @param node the cast expression node
     * @return the type of the expression
     */
    public Object visit(CastExpr node) {
        print("cast(" + node.getType() + ", ");
        node.getExpr().accept(this);
        print(")");
        return null;
    }

    /**
     * Visit an assignment expression node
     *
     * @param node the assignment expression node
     * @return the type of the expression
     */
    public Object visit(AssignExpr node) {
        // handle/visit reference
        if(node.getRefName() != null){
            print(node.getRefName() + ".");
        }

        print(node.getName() + " = ");
        node.getExpr().accept(this);
        return null;
    }


    /**
     * Visit a variable expression node
     *
     * @param node the variable expression node
     * @return the type of the expression
     */
    public Object visit(VarExpr node) {
        // handle/visit reference
        if(node.getRef() != null){
            node.getRef().accept(this);
            print(".");
        }

        print(node.getName());
        return null;
    }

    /**
     * visits and prints binaryExpr inorder
     * @param e the binary expression to visit inorder
     */
    private void visitInorder(BinaryExpr e){
        e.getLeftExpr().accept(this);
        print(" " + e.getOpName() + " ");
        e.getRightExpr().accept(this);
    }

    /**
     * Visit a binary comparison equals expression node
     *
     * @param node the binary comparison equals expression node
     * @return the type of the expression
     */
    public Object visit(BinaryCompEqExpr node) {
        visitInorder(node);
        return null;
    }

    /**
     * Visit a binary comparison not equals expression node
     *
     * @param node the binary comparison not equals expression node
     * @return the type of the expression
     */
    public Object visit(BinaryCompNeExpr node) {
        visitInorder(node);
        return null;
    }

    /**
     * Visit a binary comparison less than expression node
     *
     * @param node the binary comparison less than expression node
     * @return the type of the expression
     */
    public Object visit(BinaryCompLtExpr node) {
        visitInorder(node);
        return null;
    }

    /**
     * Visit a binary comparison less than or equal to expression node
     *
     * @param node the binary comparison less than or equal to expression node
     * @return the type of the expression
     */
    public Object visit(BinaryCompLeqExpr node) {
        visitInorder(node);
        return null;
    }

    /**
     * Visit a binary comparison greater than expression node
     *
     * @param node the binary comparison greater than expression node
     * @return the type of the expression
     */
    public Object visit(BinaryCompGtExpr node) {
        visitInorder(node);
        return null;
    }

    /**
     * Visit a binary comparison greater than or equal to expression node
     *
     * @param node the binary comparison greater to or equal to expression node
     * @return the type of the expression
     */
    public Object visit(BinaryCompGeqExpr node) {
        visitInorder(node);
        return null;
    }

    /**
     * Visit a binary arithmetic plus expression node
     *
     * @param node the binary arithmetic plus expression node
     * @return the type of the expression
     */
    public Object visit(BinaryArithPlusExpr node) {
        visitInorder(node);
        return null;
    }

    /**
     * Visit a binary arithmetic minus expression node
     *
     * @param node the binary arithmetic minus expression node
     * @return the type of the expression
     */
    public Object visit(BinaryArithMinusExpr node) {
        visitInorder(node);
        return null;
    }

    /**
     * Visit a binary arithmetic times expression node
     *
     * @param node the binary arithmetic times expression node
     * @return the type of the expression
     */
    public Object visit(BinaryArithTimesExpr node) {
        visitInorder(node);
        return null;
    }

    /**
     * Visit a binary arithmetic divide expression node
     *
     * @param node the binary arithmetic divide expression node
     * @return the type of the expression
     */
    public Object visit(BinaryArithDivideExpr node) {
        visitInorder(node);
        return null;
    }

    /**
     * Visit a binary arithmetic modulus expression node
     *
     * @param node the binary arithmetic modulus expression node
     * @return the type of the expression
     */
    public Object visit(BinaryArithModulusExpr node) {
        visitInorder(node);
        return null;
    }

    /**
     * Visit a binary logical AND expression node
     *
     * @param node the binary logical AND expression node
     * @return the type of the expression
     */
    public Object visit(BinaryLogicAndExpr node) {
        visitInorder(node);
        return null;
    }

    /**
     * Visit a binary logical OR expression node
     *
     * @param node the binary logical OR expression node
     * @return the type of the expression
     */
    public Object visit(BinaryLogicOrExpr node) {
        visitInorder(node);
        return null;
    }

    /**
     * Visit a unary negation expression node
     *
     * @param node the unary negation expression node
     * @return the type of the expression
     */
    public Object visit(UnaryNegExpr node) {
        print("-");
        node.getExpr().accept(this);
        return null;
    }

    /**
     * Visit a unary NOT expression node
     *
     * @param node the unary NOT expression node
     * @return the type of the expression
     */
    public Object visit(UnaryNotExpr node) {
        print("!");
        node.getExpr().accept(this);
        return null;
    }

    /**
     * Visit a unary increment expression node
     *
     * @param node the unary increment expression node
     * @return the type of the expression
     */
    public Object visit(UnaryIncrExpr node) {
        if(node.isPostfix()){
            node.getExpr().accept(this);
            print("++");
        } else {
            print("++");
            node.getExpr().accept(this);
        }
        return null;
    }

    /**
     * Visit a unary decrement expression node
     *
     * @param node the unary decrement expression node
     * @return the type of the expression
     */
    public Object visit(UnaryDecrExpr node) {
        if(node.isPostfix()){
            node.getExpr().accept(this);
            print("--");
        } else {
            print("--");
            node.getExpr().accept(this);
        }
        return null;
    }

    /**
     * Visit an int constant expression node
     *
     * @param node the int constant expression node
     * @return the type of the expression
     */
    public Object visit(ConstIntExpr node) {
        print(node.getConstant());
        return null;
    }

    /**
     * Visit a boolean constant expression node
     *
     * @param node the boolean constant expression node
     * @return the type of the expression
     */
    public Object visit(ConstBooleanExpr node) {
        print(node.getConstant());
        return null;
    }

    /**
     * Visit a string constant expression node
     *
     * @param node the string constant expression node
     * @return the type of the expression
     */
    public Object visit(ConstStringExpr node) {
        print(node.getConstant());
        return null;
    }
}
