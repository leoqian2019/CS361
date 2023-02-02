# cs361-Object Oriented Programming
### Bantam Java IDE
This is the semester long project from CS361 Object Oriented Programming class.
In this project, we implemented a new IDE using JavaFx.

## IDE Design
This program is designed to simulate the user experience of using a real IDE. Since real Java has a complex grammar, we used a fake language called Bantam Java which contains features that java has. 

After the user opens a java file or save their input as a java file, they will be able to run the program. 
The app compiles Bantam Java code using 3 steps:
1. analyze if the spelling is correct
2. tokenize the code into AST nodes
3. analyze if the grammar is correct

Then the code will be turn into real java code and use javac to compile it into .class file for execution.

## How to run the project?

Within the source folder, open ```proj10EnglishHillisonQian``` which has a Main.java file. 
Using Java 8, you can run it directly and you will see the app window open.
