/*
 * File: MainMethodFinder.java
 * Authors: Nick English, Nico Hillison, Leo Qian
 * Date: 4/23/22
 *
 */
package proj10EnglishHillisonQian.bantam.semant;

import proj10EnglishHillisonQian.bantam.ast.*;
import proj10EnglishHillisonQian.bantam.visitor.Visitor;

public class MainMethodFinder extends Visitor {
    private boolean hasMain = false;

    @Override
    public Object visit(Method methodNode){
        // if the method has a name "main"
        if("main".equals(methodNode.getName()) &&
                // if the method has a return type "void"
                "void".equals(methodNode.getReturnType()) &&
                // if the method has no parameter
                methodNode.getFormalList().getSize() == 0) {
            hasMain = true;
        }
        return null;
    }

    @Override
    public Object visit(Field fieldNode){
        return null;
    }

    @Override
    public Object visit(Class_ classNode){
        // advance if the class has a name "Main"
        if(classNode.getName().equals("Main")){
            classNode.getMemberList().accept(this);
        }
        return null;
    }

    public boolean hasMain(ASTNode rootNode){
        rootNode.accept(this);
        return hasMain;
    }
}
