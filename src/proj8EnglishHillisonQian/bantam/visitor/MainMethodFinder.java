package proj8EnglishHillisonQian.bantam.visitor;

import proj8EnglishHillisonQian.bantam.ast.*;

public class MainMethodFinder extends Visitor{
    private boolean hasMain = false;

    @Override
    public Object visit(Method methodNode){
        if(methodNode.getName().equals("main")) {
            hasMain = true;
        }
        return null;
    }

    @Override
    public Object visit(Field fieldNode){
        return null;
    }

    public boolean hasMain(ASTNode rootNode){
        rootNode.accept(this);
        return hasMain;
    }

    public static void main(String[] args) {
        Method method = new Method(0,"int","main",null,null);
        Field field = new Field(0,"int","else",null);
        MemberList members = new MemberList(0);
        members.addElement(field);
        members.addElement(method);
        Class_ thisClass = new Class_(0,null,null,null,members);
        MainMethodFinder find = new MainMethodFinder();
        System.out.println(find.hasMain(thisClass));
    }
}
