import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.io.FileInputStream;
import syntaxtree.*;
import visitor.*;
import myvisitors.*;
import java.io.*;

public class Main {
    public static void main (String [] args){
        if(args.length == 0){
            System.err.println("Usage: java Main <file1.java> <file2.java> ... <fileN.java>");
            System.exit(1);
        }
        FileInputStream fis = null;
        for(int i=0; i<args.length;i++){
            try{
                fis = new FileInputStream(args[i]);

                File file = new File("./"+args[i].replace(".java", "")+".ll");
                PrintStream stream = new PrintStream(file);
                System.setOut(stream);

                MiniJavaParser parser = new MiniJavaParser(fis);
                Goal root = parser.Goal();

                TestVisitor eval = new TestVisitor();
                root.accept(eval, null);

                //eval.print();
                eval.checkInvalidTypes();

                // Following two lines do what Part-1 does, if program is 
                // guaranteed to be syntactically correct they can be commented out
                SecondVisitor eval1 = new SecondVisitor(eval.getV());
                root.accept(eval1, null);

                //eval.print();
                eval.printVtable();

                ThirdVisitor eval2 = new ThirdVisitor(eval.getV(),eval.getVtableTypes());
                root.accept(eval2, null);
            }
            catch(ParseException ex){
                System.out.println(ex.getMessage());
            }
            catch(FileNotFoundException ex){
                System.err.println(ex.getMessage());
            }
            catch(RuntimeException ex){
                System.err.println(ex.getMessage());
            }
            finally{
                try{
                    if(fis != null) fis.close();
                }
                catch(IOException ex){
                    System.err.println(ex.getMessage());
                }
            }
        }
    }
}
