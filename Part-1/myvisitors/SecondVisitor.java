package myvisitors;
import visitor.GJDepthFirst;
import types.*;
import syntaxtree.*;
import java.util.*; 

public class SecondVisitor extends GJDepthFirst<String,SymbTable> {
    private Vector<SymbTable> v;
    private boolean checkScope;
    private Vector<String> paramsBuffer;
    public SecondVisitor(Vector<SymbTable> vv){
      v = vv;
      checkScope = true;
    }
    private SymbTable getClass(String name){
      for(int i=0; i< v.size(); i++)
            if (v.get(i).getName().equals(name))
                return v.get(i);
      return null;
    }
    private String dereference(String type){
      if (type.equals("int[]"))
        return "int";
      else if (type.equals("boolean[]"))
        return "boolean";
      error("Type "+type+" cannot be dereferenced");
      return null;
    }
    private boolean isSub(String sub, String up){
      if (sub.equals(up))
        return true;
      SymbTable ll = getClass(sub);
      while (ll!=null && !ll.getName().equals(up))
        ll = ll.getParent();
      if (ll==null)
        return false;
      else
        return true;
    }
    private boolean areSubParams(Vector<String> vSub,Vector<String> vUp){
      if (vSub.size()!=vUp.size())
        return false;
      for (int i=0; i<vSub.size();i++)
        if (!isSub(vSub.get(i),vUp.get(i)))
          return false;
      return true;
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
      checkScope = false;
      String name = n.f1.accept(this, argu);
      SymbTable st = getClass(name+".main");
      n.f15.accept(this, st);
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
      checkScope = false;
      String name = n.f1.accept(this, argu);
      SymbTable st = getClass(name);
      if (st == null)
        error("Caused by: "+name);
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
      checkScope = false;
      String name = n.f1.accept(this, argu);
      SymbTable st = getClass(name);
      if (st == null)
        error("Caused by: "+name);
      n.f6.accept(this, st);
      return _ret;
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
      checkScope = false;
      String id = n.f2.accept(this, argu);
      SymbTable st = getClass(argu.getName()+"."+id);
      n.f8.accept(this, st);
      String type1 = st.getParent().searchMethod(id).getType();
      String type2 = n.f10.accept(this, st);
      if (!isSub(type2,type1))
        error("Type "+type1+" of method "+id+" does not match return type: "+type2);
      return _ret;
    }
    
    public String visit(NodeToken n, SymbTable argu) { return n.toString(); }
    /**
    * f0 -> Identifier()
    * f1 -> "="
    * f2 -> Expression()
    * f3 -> ";"
    */
   public String visit(AssignmentStatement n, SymbTable argu) {
      String _ret=null;
      String type1 = n.f0.accept(this, argu);
      String type2 = n.f2.accept(this, argu);
      if (!isSub(type2,type1))
        error("Type "+type1+" does not match: "+type2);
      return _ret;
   }

    /**
    * f0 -> Identifier()
    * f1 -> "["
    * f2 -> Expression()
    * f3 -> "]"
    * f4 -> "="
    * f5 -> Expression()
    * f6 -> ";"
    */
   public String visit(ArrayAssignmentStatement n, SymbTable argu) {
      String _ret=null;
      String type1 = n.f0.accept(this, argu);
      type1 = dereference(type1);
      String type2 = n.f2.accept(this, argu);
      if (!type2.equals("int"))
        error("[_] expects int, not "+type2);
      String type3 = n.f5.accept(this, argu);
      if (!type1.equals(type3))
        error("Type "+type1+" does not match: "+type3);
      return _ret;
   }

   /**
    * f0 -> "if"
    * f1 -> "("
    * f2 -> Expression()
    * f3 -> ")"
    * f4 -> Statement()
    * f5 -> "else"
    * f6 -> Statement()
    */
   public String visit(IfStatement n, SymbTable argu) {
      String _ret=null;
      String type = n.f2.accept(this, argu);
      if (!type.equals("boolean"))
        error("If-expression expects boolean, not "+type);
      n.f4.accept(this, argu);
      n.f6.accept(this, argu);
      return _ret;
   }

   /**
    * f0 -> "while"
    * f1 -> "("
    * f2 -> Expression()
    * f3 -> ")"
    * f4 -> Statement()
    */
   public String visit(WhileStatement n, SymbTable argu) {
      String _ret=null;
      String type = n.f2.accept(this, argu);
      if (!type.equals("boolean"))
        error("While-expression expects boolean, not "+type);
      n.f4.accept(this, argu);
      return _ret;
   }

   /**
    * f0 -> "System.out.println"
    * f1 -> "("
    * f2 -> Expression()
    * f3 -> ")"
    * f4 -> ";"
    */
   public String visit(PrintStatement n, SymbTable argu) {
      String _ret=null;
      String type = n.f2.accept(this, argu);
      if (!type.equals("int"))
        error("Println expects int, not "+type);
      return _ret;
   }

   /**
    * f0 -> Clause()
    * f1 -> "&&"
    * f2 -> Clause()
    */
   public String visit(AndExpression n, SymbTable argu) {
      String type1 = n.f0.accept(this, argu);
      String type2 = n.f2.accept(this, argu);
      if (!type1.equals("boolean") || !type2.equals("boolean"))
        error("&& expects boolean");
      return "boolean";
   }

   /**
    * f0 -> PrimaryExpression()
    * f1 -> "<"
    * f2 -> PrimaryExpression()
    */
   public String visit(CompareExpression n, SymbTable argu) {
      String type1 = n.f0.accept(this, argu);
      String type2 = n.f2.accept(this, argu);
      if (!type1.equals("int") || !type2.equals("int"))
        error("< expects int");
      return "boolean";
   }

   /**
    * f0 -> PrimaryExpression()
    * f1 -> "+"
    * f2 -> PrimaryExpression()
    */
   public String visit(PlusExpression n, SymbTable argu) {
      String type1 = n.f0.accept(this, argu);
      String type2 = n.f2.accept(this, argu);
      if (!type1.equals("int") || !type2.equals("int"))
        error("+ expects int");
      return "int";
   }

   /**
    * f0 -> PrimaryExpression()
    * f1 -> "-"
    * f2 -> PrimaryExpression()
    */
   public String visit(MinusExpression n, SymbTable argu) {
      String type1 = n.f0.accept(this, argu);
      String type2 = n.f2.accept(this, argu);
      if (!type1.equals("int") || !type2.equals("int"))
        error("- expects int");
      return "int";
   }

   /**
    * f0 -> PrimaryExpression()
    * f1 -> "*"
    * f2 -> PrimaryExpression()
    */
   public String visit(TimesExpression n, SymbTable argu) {
      String type1 = n.f0.accept(this, argu);
      String type2 = n.f2.accept(this, argu);
      if (!type1.equals("int") || !type2.equals("int"))
        error("* expects int");
      return "int";
   }

   /**
    * f0 -> PrimaryExpression()
    * f1 -> "["
    * f2 -> PrimaryExpression()
    * f3 -> "]"
    */
   public String visit(ArrayLookup n, SymbTable argu) {
      String type1 = n.f0.accept(this, argu);
      String type2 = n.f2.accept(this, argu);
      if (!type2.equals("int"))
        error("[_] expects int, not "+type2);
      return dereference(type1);
   }

   /**
    * f0 -> PrimaryExpression()
    * f1 -> "."
    * f2 -> "length"
    */
   public String visit(ArrayLength n, SymbTable argu) {
      String type1 = n.f0.accept(this, argu);
      if (dereference(type1) == null)
        return null;
      return "int";
   }

   /**
    * f0 -> PrimaryExpression()
    * f1 -> "."
    * f2 -> Identifier()
    * f3 -> "("
    * f4 -> ( ExpressionList() )?
    * f5 -> ")"
    */
   public String visit(MessageSend n, SymbTable argu) {
      String _ret=null;
      String type1 = n.f0.accept(this, argu);
      if (type1.equals("int")||type1.equals("boolean")||type1.equals("int[]")||type1.equals("boolean[]"))
        error("Type "+type1+" does not have methods");
      checkScope = false;
      String id = n.f2.accept(this, argu);
      SymbTable st = getClass(type1);
      if(st == null)
        error("Unknown type: "+type1);
      Quad q = st.deepSearchMethod(id);
      if (q == null)
        error("Method "+id+" not in scope");
      paramsBuffer = new Vector<>();
      n.f4.accept(this, argu);
      Vector<String> params = paramsBuffer;
      if (!areSubParams(params,q.getParams()))
        error("Wrong param types for method: "+id+"\nExpected: "+q.getParams()+" Got: "+params);
      return q.getType();
   }

   /**
    * f0 -> Expression()
    * f1 -> ExpressionTail()
    */
   public String visit(ExpressionList n, SymbTable argu) {
      String _ret=null;
      String type1 = n.f0.accept(this, argu);
      if (type1 != null)
        paramsBuffer.add(type1);
      n.f1.accept(this, argu);
      return _ret;
   }

   /**
    * f0 -> ","
    * f1 -> Expression()
    */
   public String visit(ExpressionTerm n, SymbTable argu) {
      String type1 = n.f1.accept(this, argu);
      if (type1 != null)
        paramsBuffer.add(type1);
      return type1;
   }

   /**
    * f0 -> <INTEGER_LITERAL>
    */
   public String visit(IntegerLiteral n, SymbTable argu) {
      return "int";
   }

   /**
    * f0 -> "true"
    */
   public String visit(TrueLiteral n, SymbTable argu) {
      return "boolean";
   }

   /**
    * f0 -> "false"
    */
   public String visit(FalseLiteral n, SymbTable argu) {
      return "boolean";
   }

   /**
    * f0 -> <IDENTIFIER>
    */
   public String visit(Identifier n, SymbTable argu) {
      String _ret=null;
      String id = n.f0.accept(this, argu);
      if (checkScope){
        String type = argu.getVarType(id);
        if (type == null)
          error("Variable not in scope: "+id);
        return type;
      }
      else{
        checkScope = true;
        return id;
      }
   }

   /**
    * f0 -> "this"
    */
   public String visit(ThisExpression n, SymbTable argu) {
      return argu.getParent().getName();
   }

   /**
    * f0 -> "new"
    * f1 -> "boolean"
    * f2 -> "["
    * f3 -> Expression()
    * f4 -> "]"
    */
   public String visit(BooleanArrayAllocationExpression n, SymbTable argu) {
      String type = n.f3.accept(this, argu);
      if (!type.equals("int"))
        error("[_] expects int, not "+type);
      return "boolean[]";
   }

   /**
    * f0 -> "new"
    * f1 -> "int"
    * f2 -> "["
    * f3 -> Expression()
    * f4 -> "]"
    */
   public String visit(IntegerArrayAllocationExpression n, SymbTable argu) {
      String type = n.f3.accept(this, argu);
      if (!type.equals("int"))
        error("[_] expects int, not "+type);
      return "int[]";
   }

   /**
    * f0 -> "new"
    * f1 -> Identifier()
    * f2 -> "("
    * f3 -> ")"
    */
   public String visit(AllocationExpression n, SymbTable argu) {
      String _ret=null;
      checkScope = false;
      String id = n.f1.accept(this, argu);
      if (getClass(id) == null)
        error("Unknown type: "+id);
      return id;
   }

   /**
    * f0 -> "!"
    * f1 -> Clause()
    */
   public String visit(NotExpression n, SymbTable argu) {
      String type = n.f1.accept(this, argu);
      if (!type.equals("boolean"))
        error("!_ expects boolean, not "+type);
      return "boolean";
   }

   /**
    * f0 -> "("
    * f1 -> Expression()
    * f2 -> ")"
    */
   public String visit(BracketExpression n, SymbTable argu) {
      return n.f1.accept(this, argu);
   }

}