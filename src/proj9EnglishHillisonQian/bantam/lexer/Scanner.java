/*
 * File: Scanner.java
 * Authors: Nick English, Nico Hillison, Leo Qian
 * Date: 4/16/22
 */

package proj9EnglishHillisonQian.bantam.lexer;

import proj9EnglishHillisonQian.bantam.util.CompilationException;
import proj9EnglishHillisonQian.bantam.util.Error;
import proj9EnglishHillisonQian.bantam.util.ErrorHandler;

import java.io.IOException;
import java.io.Reader;

/**
 * This class reads characters from a file or a Reader
 * and breaks it into Tokens.
 */
public class Scanner
{
    /** the source of the characters to be broken into tokens */
    private SourceFile sourceFile;
    /** collector of all errors that occur */
    private ErrorHandler errorHandler;
    /** current char being read **/
    private char currentChar;
    /** char to hold pending characters **/
    private char pendingChar;

    /**
     * creates a new scanner for the given file
     * @param filename the name of the file to be scanned
     * @param handler the ErrorHandler that collects all the errors found
     */
    public Scanner(String filename, ErrorHandler handler) throws CompilationException {
        errorHandler = handler;
        currentChar = ' ';
        sourceFile = new SourceFile(filename);
        pendingChar = '\0';
    }

    /**
     * creates a new scanner for the given file
     * @param reader the Reader that will scan the file
     * @param handler the ErrorHandler that collects all the errors found
     */
    public Scanner(Reader reader, ErrorHandler handler) {
        errorHandler = handler;
        sourceFile = new SourceFile(reader);
    }

    /**
     * read characters and collect them into a Token.
     * It ignores white space unless it is inside a string or a comment.
     * It returns an EOF Token if all characters from the sourceFile have
     * already been read.
     * @return the Token containing the characters read
     */
    public Token scan() {
        try{
            // Get next non-whitespace char
            while (true) {
                // process chars in the pendingChar Field first
                if (pendingChar != '\0') {
                    currentChar = pendingChar;
                    pendingChar = '\0';
                } else {
                    currentChar = sourceFile.getNextChar();
                }

                // scan again if it is white space
                if (" \t\n".indexOf(currentChar) == -1) {
                    break;
                }
            }

            // when end of file reached, return EOF Token
            if ((currentChar + "").equals("\u0000")) {
                return new Token(Token.Kind.EOF, "\u0000",
                        sourceFile.getCurrentLineNumber());
            }
            if (currentChar == '{') {
                return new Token(Token.Kind.LCURLY, currentChar + "",
                        sourceFile.getCurrentLineNumber());
            }
            if (currentChar == '}') {
                return new Token(Token.Kind.RCURLY, currentChar + "",
                        sourceFile.getCurrentLineNumber());
            }
            if (currentChar == '(') {
                return new Token(Token.Kind.LPAREN, currentChar + "",
                        sourceFile.getCurrentLineNumber());
            }
            if (currentChar == ')') {
                return new Token(Token.Kind.RPAREN, currentChar + "",
                        sourceFile.getCurrentLineNumber());
            }
            if (currentChar == ',') {
                return new Token(Token.Kind.COMMA, currentChar + "",
                        sourceFile.getCurrentLineNumber());
            }
            if (currentChar == ';') {
                return new Token(Token.Kind.SEMICOLON, currentChar + "",
                        sourceFile.getCurrentLineNumber());
            }
            if (currentChar == ':') {
                return new Token(Token.Kind.COLON, currentChar + "",
                        sourceFile.getCurrentLineNumber());
            }
            if (currentChar == '.') {
                return new Token(Token.Kind.DOT, currentChar + "",
                        sourceFile.getCurrentLineNumber());
            }
            if (currentChar == '*'){
                return new Token(Token.Kind.MULDIV,"*",
                        sourceFile.getCurrentLineNumber());
            }
            if (currentChar == '%'){
                return new Token(Token.Kind.MULDIV,"%",
                        sourceFile.getCurrentLineNumber());
            }
            if (currentChar == '+') {
                return scanPlusSign();
            }
            if (currentChar == '-') {
                return scanMinusSign();
            }
            if (currentChar == '=') {
                return scanEqualSign();
            }
            if (currentChar == '/') {
                Token slashToken = scanSlash();

                // ignore comments
                if(slashToken.kind == Token.Kind.COMMENT){
                    return scan();
                }
                else{
                    return slashToken;
                }
            }
            if(currentChar == '&' || currentChar == '|'){
                return scanBoolOp();
            }
            if(currentChar == '!'){
                return scanExclamation();
            }
            if(currentChar == '<' || currentChar == '>'){
                return scanArrow(currentChar);
            }
            if (Character.isDigit(currentChar)) {
                return scanIntConst(currentChar);
            }
            if (Character.isLetter(currentChar)){
                return scanWord();
            }
            if (currentChar == '"') {
                return scanStringConst(currentChar);
            }

        // SourceFile failed to read
        } catch (IOException e) {
            System.out.println("IOException when reading from source.");
            return null;
        }

        // any symbol not mentioned will be treated as unsupported symbol
        errorHandler.register(Error.Kind.LEX_ERROR, sourceFile.getFilename(),
                sourceFile.getCurrentLineNumber(), "Unsupported char");
        return new Token(Token.Kind.ERROR, currentChar + "",
                sourceFile.getCurrentLineNumber());
    }

    /**
     * scan the chars to form integer constant token and return it or an error
     * @param intChar the starting char of the INTCONST token
     * @return the INTCONST token formed
     * @throws IOException when SourceFile.getNextChar throws IOException
     */
    private Token scanIntConst(char intChar) throws IOException {
        String content = intChar + "";
        while(true){
            char next = sourceFile.getNextChar();
            if(!Character.isDigit(next)){
                // add char just scanned into the stack
                pendingChar = next;

                // Check size of Integer
                try{Integer.parseInt(content);}
                catch(Exception e){
                    errorHandler.register(Error.Kind.LEX_ERROR,sourceFile.getFilename(),
                            sourceFile.getCurrentLineNumber(),"Int constant too large");
                    return new Token(Token.Kind.ERROR,  content, sourceFile.getCurrentLineNumber());
                }

                return new Token(Token.Kind.INTCONST, content, sourceFile.getCurrentLineNumber());
            }
            content += next;


        }
    }

    /**
     * scan char stream for a string constant and return it or an error
     * @param prevChar the first char of the token
     * @return the string constant Token
     * @throws IOException when SourceFile.getNextChar() throws it
     */
    private Token scanStringConst(char prevChar) throws IOException {
        int startLine = sourceFile.getCurrentLineNumber();
        String content = prevChar + "";
        boolean errorFound = false;
        while(true){
            char next = sourceFile.getNextChar();

            // if we reach the end of the string, check for errors and return a token
            if(next == '"' && prevChar != '\\'){
                content += next;

                // return error token if the string is too long
                if(content.length() > 5000){
                    errorHandler.register(Error.Kind.LEX_ERROR,sourceFile.getFilename(),
                            sourceFile.getCurrentLineNumber(),"String too long");
                    return new Token(Token.Kind.ERROR, content,
                            sourceFile.getCurrentLineNumber());
                }

                // return error token if the string expands multiple lines
                if(startLine != sourceFile.getCurrentLineNumber()){
                    errorHandler.register(Error.Kind.LEX_ERROR,sourceFile.getFilename(),
                            sourceFile.getCurrentLineNumber(),"String on multiple lines");
                    return new Token(Token.Kind.ERROR, content,
                            sourceFile.getCurrentLineNumber());
                }

                // return error token if user used unsupported escape char
                if(errorFound){
                    return new Token(Token.Kind.ERROR, content,
                            sourceFile.getCurrentLineNumber());
                }
                return new Token(Token.Kind.STRCONST, content, sourceFile.getCurrentLineNumber());
            }

            // Check if unsupported escape char
            if(prevChar == '\\' && "nt\"\\f".indexOf(next) == -1){
                errorHandler.register(Error.Kind.LEX_ERROR,sourceFile.getFilename(),
                        sourceFile.getCurrentLineNumber(),"Invalid escape char in string");
                errorFound = true;
            }

            // return error token if user forget to terminate the string
            if((next + "").equals("\u0000")){
                pendingChar = next;
                errorHandler.register(Error.Kind.LEX_ERROR,sourceFile.getFilename(),
                        sourceFile.getCurrentLineNumber(),"Unterminated string constant");
                return new Token(Token.Kind.ERROR, content,
                        sourceFile.getCurrentLineNumber());
            }
            // update content
            content += next;
            prevChar = next;
        }
    }

    /**
     * scan words to determine if they are keyword/identifier, return appropriate token
     * @return return the correct keyword token or identifier token
     * @throws IOException if SourceFile fails to read
     */
    private Token scanWord() throws IOException {
        String content = currentChar + "";
        while(true){
            char next = sourceFile.getNextChar();

            // Check if word has ended (next char not valid) and return token
            if(!Character.isLetter(next) && !Character.isDigit(next) && next != '_'){
                pendingChar = next;
                return new Token(Token.Kind.IDENTIFIER, content,
                        sourceFile.getCurrentLineNumber());
            }
            content += next;

        }
    }

    /**
     * scan the chars following the plus sign and return appropriate token
     * @return return the appropriate token formed with the plus sign
     * @throws IOException when SourceFile.getNextChar throws IOException
     */
    private Token scanPlusSign() throws  IOException{
        char next = sourceFile.getNextChar();
        if(next != '+'){
            pendingChar = next;
            return new Token(Token.Kind.PLUSMINUS, "+", sourceFile.getCurrentLineNumber());
        }
        else {
            return new Token(Token.Kind.UNARYINCR, "++", sourceFile.getCurrentLineNumber());
        }
    }

    /**
     * scan the chars following the minus sign and return appropriate token
     * @return return the appropriate token formed with the minus sign
     * @throws IOException when SourceFile.getNextChar throws IOException
     */
    private Token scanMinusSign() throws  IOException{
        char next = sourceFile.getNextChar();
        if(next != '-'){
            pendingChar = next;
            return new Token(Token.Kind.PLUSMINUS, "-", sourceFile.getCurrentLineNumber());
        }
        else {
            return new Token(Token.Kind.UNARYDECR, "--", sourceFile.getCurrentLineNumber());
        }
    }

    /**
     * scan the chars following the equal sign and return appropriate token
     * @return return the appropriate token formed with the equal sign
     * @throws IOException when SourceFile.getNextChar throws IOException
     */
    private Token scanEqualSign() throws  IOException{
        char next = sourceFile.getNextChar();
        if(next != '='){
            pendingChar = next;
            return new Token(Token.Kind.ASSIGN, "=", sourceFile.getCurrentLineNumber());
        }
        else {
            return new Token(Token.Kind.COMPARE, "==", sourceFile.getCurrentLineNumber());
        }
    }

    /**
     * return the appropriate token for things that start with /
     * @return  return the appropriate token start with /
     * @throws IOException when SourceFile.getNextChar throws IOException
     */
    private Token scanSlash() throws IOException {
        char next = sourceFile.getNextChar();
        if(next == '/'){
            return scanLineComment();
        } else if (next == '*') {
            return scanBlockComment();
        } else {
            pendingChar = next;
            return new Token(Token.Kind.MULDIV, "/", sourceFile.getCurrentLineNumber());
        }
    }

    /**
     * scan for && or || and return appropriate token.
     * @return the appropriate BinaryLogic or Error Token
     * @throws IOException when SourceFile.getNextChar throws IOException
     */
    private Token scanBoolOp() throws IOException{
        char next = sourceFile.getNextChar();
        if(next == currentChar){
            return new Token(Token.Kind.BINARYLOGIC, currentChar + "" + next,
                    sourceFile.getCurrentLineNumber());
        }
        pendingChar = next;
        errorHandler.register(Error.Kind.LEX_ERROR,sourceFile.getFilename(),
                sourceFile.getCurrentLineNumber(),"Logical Op | or &");
        return new Token(Token.Kind.ERROR, currentChar+"",
                sourceFile.getCurrentLineNumber());
    }

    /**
     * scan a line comment and return a token.
     * @return the appropriate Comment Token
     * @throws IOException when SourceFile.getNextChar throws IOException
     */
    private Token scanLineComment() throws IOException {
        int startLine = sourceFile.getCurrentLineNumber();
        String content = "//";
        while(true){
            char next = sourceFile.getNextChar();
            // Return when comment token ended
            if(startLine != sourceFile.getCurrentLineNumber()
                    | (next + "").equals("\u0000")){
                pendingChar = next ;
                return new Token(Token.Kind.COMMENT, content,
                        sourceFile.getCurrentLineNumber());
            }
            content += next;
        }
    }

    /**
     * scan a block comment and return a token.
     * @return the appropriate Comment Token
     * @throws IOException when SourceFile.getNextChar throws IOException
     */
    private Token scanBlockComment() throws IOException {
        String content = "/*";
        while(true){
            char next = sourceFile.getNextChar();
            // check if block comment ends
            if(next == '*'){
                content += next;
                next = sourceFile.getNextChar();
                if(next  == '/'){
                    content += next;
                    return new Token(Token.Kind.COMMENT, content, sourceFile.getCurrentLineNumber());
                }
            }
            // check for unterminated comment
            if((next + "").equals("\u0000")){
                pendingChar = next;
                errorHandler.register(Error.Kind.LEX_ERROR,sourceFile.getFilename(),
                        sourceFile.getCurrentLineNumber(),"Unterminated block comment");
                return new Token(Token.Kind.ERROR, content + "",
                        sourceFile.getCurrentLineNumber());
            }
            content += next;
        }
    }

    /**
     * scan chars following ! symbol and return a token.
     * @return the appropriate Unary or Compare Token
     * @throws IOException when SourceFile.getNextChar throws IOException
     */
    private Token scanExclamation() throws IOException{
        while(true){
            char next = sourceFile.getNextChar();
            if(next != '='){
                pendingChar = next;
                return new Token(Token.Kind.UNARYNOT, "!",
                        sourceFile.getCurrentLineNumber());
            }
            else {
                return new Token(Token.Kind.COMPARE, "!=",
                        sourceFile.getCurrentLineNumber());
            }
        }
    }

    /**
     * scan for a comparison with < or >
     * @param currentChar the first char in the token
     * @return an appropriate Compare Token
     * @throws IOException when SourceFile.getNextChar throws IOException
     */
    private Token scanArrow(char currentChar) throws IOException{
        while(true){
            char next = sourceFile.getNextChar();
            if(next != '='){
                pendingChar = next;
                return new Token(Token.Kind.COMPARE, currentChar+"",
                        sourceFile.getCurrentLineNumber());
            }
            else {
                return new Token(Token.Kind.COMPARE, currentChar+"=",
                        sourceFile.getCurrentLineNumber());
            }
        }
    }

    /**
     * The main method to run test files through the scanner and print
     * generated tokens to the console.
     * @param args the files to test on.
     */
    public static void main(String[] args){
        String[] files;
        if(args.length < 1){
            files = new String[1];
            files[0] = ("ParserTestEnglishHillisonQian.btm");
        } else {
            files = args;
        }

        ErrorHandler errorHandler = new ErrorHandler();
        Token token;
        int errors = 0;

        for(String file: files){
            System.out.println(file);
            try{
                Scanner scanner = new Scanner(file, errorHandler);
                while(true){
                    token = scanner.scan();
                    if(token == null){
                        System.out.println("SourceFile failed to read, stopping.");
                        break;
                    }
                    if(token.kind == Token.Kind.EOF){
                        System.out.println(token.toString());
                        break;
                    }
                    if(token.kind == Token.Kind.ERROR){
                        errors++;
                    }

                    System.out.println(token.toString());
                }

                if(errors > 0){
                    System.out.println(errors + " Error Tokens Found");
                } else {
                    System.out.println("Scanning Successful");
                }
            }
            catch (CompilationException e){
                System.out.println("File "+file+" is not found!");
            }

        }
    }

}
