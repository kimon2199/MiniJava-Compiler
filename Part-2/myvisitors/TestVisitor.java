package myvisitors;
import visitor.GJDepthFirst;
import types.*;
import syntaxtree.*;
import java.util.*; 

public class TestVisitor extends GJDepthFirst<String,SymbTable> {
    private Vector<SymbTable> v = new Vector<>();
    private Vector<String> vtableTypes;
    private Vector<String> paramsBuffer;
    public TestVisitor(){
        v = new Vector<>();
        vtableTypes = new Vector<>();
    }
    private SymbTable getClass(String name){
      for(int i=0; i< v.size(); i++){
            SymbTable parSt = v.get(i); //looking for class that is being expanded
            if (parSt.getName().equals(name)){  //if the class being expanded was found
                return parSt;
            }
      }
      error("Class that is being extended does not exist!");
      return null;
    }
    private boolean classExists(String name){
      for(int i=0; i< v.size(); i++)
            if (v.get(i).getName().equals(name))
                return true;
      return false;
    }
    public void printVtable(){
        for(int i=0; i< v.size(); i++){
            if (!v.get(i).getName().contains(".")){
              v.get(i).printVtable();
              vtableTypes.add(v.get(i).getName());
              vtableTypes.add("["+v.get(i).deepNumMethods()+" x i8*]");
            }
        }
    }
    public void print(){
        System.out.println("Vector is :"); 
        for(int i=0; i< v.size(); i++){
            v.get(i).print();
        }
    }
    public void output(){
      for(int i=1; i< v.size(); i++)
        v.get(i).output();
    }
    public void checkInvalidTypes(){
      for(int i=0; i< v.size(); i++){
        SymbTable st = v.get(i);
        Vector<Triplet> vt = st.getVarTable();
        Vector<Quad> mt = st.getMethTable();
        for(int j=0; j<vt.size();j++){
          if (i==1 && j==0)
            continue;
          String type = vt.get(j).getType();
          if (type.equals("int") || type.equals("int[]") || type.equals("boolean") || type.equals("boolean[]"))
            continue;
          else
            if (classExists(type))
              continue;
          error("Unknown type: "+type);
        }
        for(int j=0; j<mt.size();j++){
          String type = mt.get(j).getType();
          if (type.equals("int") || type.equals("int[]") || type.equals("boolean") || type.equals("boolean[]"))
            continue;
          else
            if (classExists(type))
              continue;
          error("Unknown type: "+type);
        }
      }
    }
    public Vector<SymbTable> getV(){
      return v;
    }
    public Vector<String> getVtableTypes(){
      return vtableTypes;
    }
    private void error(String msg) throws RuntimeException{
      throw new RuntimeException("Error:\n"+msg+"\n");
    }
    /**
    * f0 -> "class"
    * f1 -> Identifier()
    * f2-10->"{","public","static","void","main","(","String","[","]"
    * f11 -> Identifier()
    * f12,f13 -> ")","{"
    * f14 -> ( VarDeclaration() )*
    * f15 -> ( Statement() )*
    * f16,f17 -> "}","}"
    */
    public String visit(MainClass n, SymbTable argu) {
      String _ret=null;
      String name;
      name = n.f1.accept(this, argu);
      SymbTable st = new SymbTable(name);
      v.add(st);
      SymbTable st1 = new SymbTable(name+".main",st,false);
      v.add(st1);
      String id = n.f11.accept(this, argu);
      st1.addVar("String[]",id);
      n.f14.accept(this, st1);
      //n.f15.accept(this, argu);
      return _ret;
    }
    /**
    * f0 -> "class"
    * f1 -> Identifier()
    * f2 -> "{"
    * f3 -> ( VarDeclaration() )*
    * f4 -> ( MethodDeclaration() )*
    * f5 -> "}"
    */
    public String visit(ClassDeclaration n, SymbTable argu) {
        String _ret=null;
        String name;
        name = n.f1.accept(this, argu);
        if (classExists(name))
          error("Duplicate class declaration: "+name);
        SymbTable st = new SymbTable(name);
        v.add(st);
        n.f3.accept(this, st);
        n.f4.accept(this, st);
        return _ret;
    }
    /**
    * f0 -> "class"
    * f1 -> Identifier()
    * f2 -> "extends"
    * f3 -> Identifier()
    * f4 -> "{"
    * f5 -> ( VarDeclaration() )*
    * f6 -> ( MethodDeclaration() )*
    * f7 -> "}"
    */
    public String visit(ClassExtendsDeclaration n, SymbTable argu) {
        String _ret=null;
        String name,nameOfParent;
        name = n.f1.accept(this, argu);
        if (classExists(name))
          error("Duplicate class declaration: "+name);
        nameOfParent = n.f3.accept(this, argu);
        SymbTable st = null;
        st = new SymbTable(name,getClass(nameOfParent),true);
        v.add(st);
        n.f5.accept(this, st);
        n.f6.accept(this, st);
        return _ret;
    }
	  /**
    * f0 -> Type()
    * f1 -> Identifier()
    * f2 -> ";"
    */
    public String visit(VarDeclaration n, SymbTable argu) {
     	  String _ret=null;
      	String type, id;
      	type = n.f0.accept(this, argu);
      	id = n.f1.accept(this, argu);
      	argu.addVar(type,id);
      	return _ret;
    }

    public String visit(NodeToken n, SymbTable argu) { return n.toString(); }
    /**
    * f0 -> "int"
    * f1 -> "["
    * f2 -> "]"
    */
    public String visit(IntegerArrayType n, SymbTable argu) {
        return "int[]";
    }
    /**
    * f0 -> "boolean"
    * f1 -> "["
    * f2 -> "]"
    */
    public String visit(BooleanArrayType n, SymbTable argu) {
        return "boolean[]";
    }
    /**
    * f0 -> "public"
    * f1 -> Type()
    * f2 -> Identifier()
    * f3 -> "("
    * f4 -> ( FormalParameterList() )?
    * f5 -> ")"
    * f6 -> "{"
    * f7 -> ( VarDeclaration() )*
    * f8 -> ( Statement() )*
    * f9 -> "return"
    * f10 -> Expression()
    * f11 -> ";"
    * f12 -> "}"
    */
   	public String visit(MethodDeclaration n, SymbTable argu) {
      	String _ret=null;
      	String type, id;
     	  type = n.f1.accept(this, argu);
      	id = n.f2.accept(this, argu);
        SymbTable st = new SymbTable(argu.getName()+"."+id,argu,false);
        v.add(st);
        paramsBuffer = new Vector<>();
      	n.f4.accept(this, st);
        boolean con = argu.shouldPlaceMethod(type,id,paramsBuffer);
        if (con)
            argu.addMeth(type,id,paramsBuffer);
        //else = method is just beeing overridden so dont count
      	n.f7.accept(this, st);
      	//n.f8.accept(this, argu);
      	//n.f10.accept(this, argu);
      	return _ret;
    }
    /**
    * f0 -> Type()
    * f1 -> Identifier()
    */
    public String visit(FormalParameter n, SymbTable argu) {
        String _ret=null;
        String type = n.f0.accept(this, argu);
        paramsBuffer.add(type);
        String id = n.f1.accept(this, argu); //add this to some list later
        argu.addVar(type,id);
        return _ret;
    }


}