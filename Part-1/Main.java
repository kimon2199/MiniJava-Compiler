import syntaxtree.*;
import visitor.*;
import myvisitors.*;
import java.io.*;

public class Main {
    public static void main (String [] args){
        if(args.length == 0){
            System.err.println("Usage: java Main <file1> <file2> ... <fileN>");
            System.exit(1);
        }
        FileInputStream fis = null;
        for(int i=0; i<args.length;i++){
            try{
                fis = new FileInputStream(args[i]);
                introduce(args[i]);
                MiniJavaParser parser = new MiniJavaParser(fis);
                Goal root = parser.Goal();

                TestVisitor eval = new TestVisitor();
                root.accept(eval, null);

                //eval.print();
                eval.checkInvalidTypes();

                SecondVisitor eval1 = new SecondVisitor(eval.getV());
                root.accept(eval1, null);

                eval.output();
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
    public static void introduce(String nm){
        System.out.println("-----------------------------------------");
        System.out.println("   "+nm);
        System.out.println("-----------------------------------------\n");
    }
}
