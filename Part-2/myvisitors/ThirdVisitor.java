package myvisitors;
import visitor.GJDepthFirst;
import types.*;
import syntaxtree.*;
import java.util.*; 

public class ThirdVisitor extends GJDepthFirst<String,SymbTable> {
    private Vector<SymbTable> v;
    private Vector<String> vtableTypes;
    private boolean checkScope;
    private Vector<String> paramsBuffer;
    private int varC; /* keeps count of the current temp variable */
    private int labelC; /* keeps count of the current label */
    private String thisType;
    private Stack<Vector<String>> stk;
    public ThirdVisitor(Vector<SymbTable> vv,Vector<String> vevo){
      v = vv;
      vtableTypes = vevo;
      stk = new Stack<>();
    }
    private SymbTable getClass(String name){
      for(int i=0; i< v.size(); i++)
            if (v.get(i).getName().equals(name))
                return v.get(i);
      return null;
    }
    private void error(String msg) throws RuntimeException{
      throw new RuntimeException("Error:\n"+msg+"\n");
    }
    public String typeToIR(String type){
      if (type.equals("int"))
        return "i32";
      if (type.equals("boolean"))
        return "i8";
      if (type.equals("int[]"))
        return "i32*";
      if (type.equals("boolean[]"))
        return "i8*";
      return "i8*";
    }
    public String tmpVar(){
      String a = "%_" + varC;
      varC = varC + 1;
      return a;
    }
    public String label(){
      String a = "%_lb" + labelC;
      labelC = labelC + 1;
      return a;
    }
    public String seperateType(String typeIdType){ 
      String[] arr = typeIdType.split(","); 
      return arr[0];

    }
    public String seperateId(String typeIdType){
      String[] arr = typeIdType.split(","); 
      return arr[1];
    }
    public String seperateHLType(String typeIdType){
      String[] arr = typeIdType.split(","); 
      return arr[2];
    }
    public String getTypeOfVt(String classi){
      for(int i=0; i< vtableTypes.size(); i = i + 2)
            if (vtableTypes.get(i).equals(classi))
                return vtableTypes.get(i+1);
      return null;
    }

    //////////////////////////////
    ///////// Visitor GJ /////////
    //////////////////////////////

    public String visit(NodeToken n, SymbTable argu) { return n.toString(); }
    /**
    * f0 -> MainClass()
    * f1 -> ( TypeDeclaration() )*
    * f2 -> <EOF>
    */
    public String visit(Goal n, SymbTable argu) {
      String _ret=null;
      System.out.println("declare i8* @calloc(i32, i32)");
      System.out.println("declare i32 @printf(i8*, ...)");
      System.out.println("declare void @exit(i32)\n");
      System.out.println("@_cint = constant [4 x i8] c\"%d\\0a\\00\"");
      System.out.println("@_cOOB = constant [15 x i8] c\"Out of bounds\\0a\\00\"");
      System.out.println("@_cNSZ = constant [15 x i8] c\"Negative size\\0a\\00\"\n");
      System.out.println("define void @print_int(i32 %i) {");
      System.out.println("    %_str = bitcast [4 x i8]* @_cint to i8*");
      System.out.println("    call i32 (i8*, ...) @printf(i8* %_str, i32 %i)");
      System.out.println("    ret void");
      System.out.println("}\n");
      System.out.println("define void @throw_oob() {");
      System.out.println("    %_str = bitcast [15 x i8]* @_cOOB to i8*");
      System.out.println("    call i32 (i8*, ...) @printf(i8* %_str)");
      System.out.println("    call void @exit(i32 1)");
      System.out.println("    ret void");
      System.out.println("}\n");
      System.out.println("define void @throw_nsz() {");
      System.out.println("    %_str = bitcast [15 x i8]* @_cNSZ to i8*");
      System.out.println("    call i32 (i8*, ...) @printf(i8* %_str)");
      System.out.println("    call void @exit(i32 1)");
      System.out.println("    ret void");
      System.out.println("}\n");
      n.f0.accept(this, argu);
      n.f1.accept(this, argu);
      n.f2.accept(this, argu);
      return _ret;
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
      System.out.println("define i32 @main() {");
      String className = n.f1.accept(this, argu);
      thisType = className;
      n.f14.accept(this, argu);
      SymbTable st = getClass(className+".main");
      n.f15.accept(this, st);
      System.out.println("    ret i32 0\n}\n");
      return _ret;
    }

    /**
    * f0 -> "class"
    * f1 -> Identifier()
    * f2 -> "{"
    * f3 -> ( VarDeclaration() )*   //Unuseddddd
    * f4 -> ( MethodDeclaration() )*
    * f5 -> "}"
    */
    public String visit(ClassDeclaration n, SymbTable argu) {
      String _ret=null;
      String name = n.f1.accept(this, argu);
      thisType = name;
      SymbTable st = getClass(name);
      n.f4.accept(this, st);
      return _ret;
    }

    /**
    * f0 -> "class"
    * f1 -> Identifier()
    * f2 -> "extends"
    * f3 -> Identifier()
    * f4 -> "{"
    * f5 -> ( VarDeclaration() )*   //Unuseddddd
    * f6 -> ( MethodDeclaration() )*
    * f7 -> "}"
    */
    public String visit(ClassExtendsDeclaration n, SymbTable argu) {
      String _ret=null;
      String name = n.f1.accept(this, argu);
      thisType = name;
      SymbTable st = getClass(name);
      n.f3.accept(this, argu);
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
      String type = n.f0.accept(this, argu);
      String id = n.f1.accept(this, argu);
      System.out.println("    %"+id+" = alloca "+typeToIR(type));
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
      varC = 0;
      labelC = 0;
      String className = argu.getName();
      String type = n.f1.accept(this, argu);
      String id = n.f2.accept(this, argu);
      System.out.print("define "+ typeToIR(type) +" @"+className+"."+id+"(i8* %this");
      n.f4.accept(this, argu);
      System.out.println(") {");
      /* going to iterate parameters */
      Vector<String> params = argu.searchMethod(id).getParams();
      Vector<Triplet> varTable = getClass(className+"."+id).getVarTable();
      for (int i=0; i < params.size(); i++){
        String paramType = params.get(i);
        Triplet curParam = varTable.get(i);
        if (!paramType.equals(curParam.getType()))
          System.out.println("Something is wrong");
        String paramId = curParam.getId();
        System.out.println("    %"+paramId+" = alloca "+typeToIR(paramType));
        System.out.print("    store "+typeToIR(paramType)+" %."+paramId);
        System.out.println(", "+typeToIR(paramType)+"* %"+paramId);
      }
      n.f7.accept(this, argu);
      SymbTable st = getClass(className+"."+id);
      n.f8.accept(this, st);
      String typeIdTypeR = n.f10.accept(this, st); 
      String typeR = seperateType(typeIdTypeR);
      String idR = seperateId(typeIdTypeR); 
      System.out.println("    ret "+ typeR +" "+idR+"\n}\n");
      return _ret;
    }

    /**
    * f0 -> Type()
    * f1 -> Identifier()
    */
    public String visit(FormalParameter n, SymbTable argu) {
      String _ret=null;
      String type = n.f0.accept(this, argu);
      String id = n.f1.accept(this, argu);
      System.out.print(", "+typeToIR(type)+" %."+id);
      return _ret;
    }

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
    * f0 -> Identifier()
    * f1 -> "="
    * f2 -> Expression()  maybe expresion can return a sting of type+id
    * f3 -> ";"
    */
    public String visit(AssignmentStatement n, SymbTable argu) {
      String _ret=null;
      String id1 = n.f0.accept(this, argu);
      String typeIdType = n.f2.accept(this, argu);
      String type2 = seperateType(typeIdType);
      String id2 = seperateId(typeIdType);
      String tmpVarFin;
      if (argu.belongsToMethod(id1)){
        tmpVarFin = "%" + id1;
      }
      else{
        String tmpVar1 = tmpVar();
        int off = argu.getOffsetVV(id1);
        System.out.println("    "+tmpVar1+" = getelementptr i8, i8* %this, i32 "+off);

        tmpVarFin = tmpVar();
        System.out.println("    "+tmpVarFin+" = bitcast i8* "+tmpVar1+" to "+type2+"*");
      }
      System.out.println("    store "+type2+" "+id2+", "+type2+"* "+tmpVarFin);
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
      String id1 = n.f0.accept(this, argu);
      String typeIdType = n.f5.accept(this, argu);
      String type2 = seperateType(typeIdType);
      String id2 = seperateId(typeIdType);
      String tmpVarFin;
      if (argu.belongsToMethod(id1)){
        tmpVarFin = "%" + id1;
      }
      else{
        String tmpVar1 = tmpVar();
        int off = argu.getOffsetVV(id1);
        System.out.println("    "+tmpVar1+" = getelementptr i8, i8* %this, i32 "+off);

        tmpVarFin = tmpVar();
        System.out.println("    "+tmpVarFin+" = bitcast i8* "+tmpVar1+" to "+type2+"**");
      }
      String tmpVar2 = tmpVar();
      System.out.println("    "+tmpVar2+" = load "+type2+"*, "+type2+"** "+tmpVarFin);
      String zika = tmpVar2;
      if(type2.equals("i8")){
        String tmpVarif = tmpVar();
        System.out.println("    "+tmpVarif+" = bitcast i8* "+tmpVar2+" to i32*");
        zika = tmpVarif;
      }
      String tmpVar3 = tmpVar();
      System.out.println("    "+tmpVar3+" = load i32, i32* "+zika);
      String typeIdTypeIndex = n.f2.accept(this, argu);
      String idIndex = seperateId(typeIdTypeIndex);
      String tmpVar4 = tmpVar();
      System.out.println("    "+tmpVar4+" = icmp sge i32 "+idIndex+", 0");
      String tmpVar5 = tmpVar();
      System.out.println("    "+tmpVar5+" = icmp slt i32 "+idIndex+", "+tmpVar3);
      String tmpVar6 = tmpVar();
      System.out.println("    "+tmpVar6+" = and i1 "+tmpVar4+", "+tmpVar5);
      String okLabel = label();
      String errLabel = label();
      System.out.println("    br i1 "+tmpVar6+", label "+okLabel+", label "+errLabel);
      System.out.println(errLabel.substring(1)+":");
      System.out.println("    call void @throw_oob()");
      System.out.println("    br label "+okLabel);
      System.out.println(okLabel.substring(1)+":");
      String tmpVar7 = tmpVar();
      if (type2.equals("i8"))
        System.out.println("    "+tmpVar7+" = add i32 4, "+idIndex);
      else
        System.out.println("    "+tmpVar7+" = add i32 1, "+idIndex);
      String tmpVar8 = tmpVar();
      System.out.println("    "+tmpVar8+" = getelementptr "+type2+", "+type2+"* "+tmpVar2+", i32 "+tmpVar7);
      System.out.println("    store "+type2+" "+id2+", "+type2+"* "+tmpVar8);
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
      String typeIdType = n.f2.accept(this, argu);
      String type = seperateType(typeIdType);
      String id = seperateId(typeIdType);
      String ifLabel = label();
      String elseLabel = label();
      String endLabel = label();
      if (type.equals("i8")){
        String tmpVar8 = tmpVar();
        System.out.println("    "+tmpVar8+" = trunc i8 "+id+" to i1");
        id = tmpVar8;
      }
      System.out.println("    br i1 "+id+", label "+ifLabel+", label "+elseLabel);
      System.out.println(ifLabel.substring(1)+":");
      n.f4.accept(this, argu);
      System.out.println("    br label "+endLabel);
      System.out.println(elseLabel.substring(1)+":");
      n.f6.accept(this, argu);
      System.out.println("    br label "+endLabel);
      System.out.println(endLabel.substring(1)+":");
      return null;
    }

    /**
    * f0 -> "while"
    * f1 -> "("
    * f2 -> Expression()
    * f3 -> ")"
    * f4 -> Statement()
    */
    public String visit(WhileStatement n, SymbTable argu) {
      String loop = label();
      String continu = label();
      String end = label();
      System.out.println("    br label "+loop);
      System.out.println(loop.substring(1)+":");
      String typeIdType = n.f2.accept(this, argu);
      String type = seperateType(typeIdType);
      String id = seperateId(typeIdType);
      if (type.equals("i8")){
        String tmpVar8 = tmpVar();
        System.out.println("    "+tmpVar8+" = trunc i8 "+id+" to i1");
        id = tmpVar8;
      }
      System.out.println("    br i1 "+id+", label "+continu+", label "+end);
      System.out.println(continu.substring(1)+":");
      n.f4.accept(this, argu);
      System.out.println("    br label "+loop);
      System.out.println(end.substring(1)+":");
      return null;
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
      String typeIdType = n.f2.accept(this, argu);
      String type = seperateType(typeIdType);
      String id = seperateId(typeIdType);
      System.out.println("    call void (i32) @print_int(i32 "+id+")");
      return _ret;
    }

    /**
    * f0 -> Clause()
    * f1 -> "&&"
    * f2 -> Clause()
    */
    public String visit(AndExpression n, SymbTable argu) {
      String typeIdType0 = n.f0.accept(this, argu);
      String type0 = seperateType(typeIdType0);
      String id0 = seperateId(typeIdType0);
      if (type0.equals("i8")){
        String tmpVar8 = tmpVar();
        System.out.println("    "+tmpVar8+" = trunc i8 "+id0+" to i1");
        id0 = tmpVar8;
      }
      String exp_res_0 = label();
      String exp_res_1 = label();
      String exp_res_2 = label();
      String exp_res_3 = label();
      System.out.println("    br i1 "+id0+", label "+exp_res_1+", label "+exp_res_0);
      System.out.println(exp_res_0.substring(1)+":");
      System.out.println("    br label "+exp_res_3);
      System.out.println(exp_res_1.substring(1)+":");
      String typeIdType1 = n.f2.accept(this, argu);
      String type1 = seperateType(typeIdType1);
      String id1 = seperateId(typeIdType1);
      if (type1.equals("i8")){
        String tmpVar9 = tmpVar();
        System.out.println("    "+tmpVar9+" = trunc i8 "+id1+" to i1");
        id1 = tmpVar9;
      }
      System.out.println("    br label "+exp_res_2);
      System.out.println(exp_res_2.substring(1)+":");
      System.out.println("    br label "+exp_res_3);
      String tmpVar10 = tmpVar();
      System.out.println(exp_res_3.substring(1)+":");
      System.out.println("    "+tmpVar10+" = phi i1 [ 0, "+exp_res_0+" ], [ "+id1+", "+exp_res_2+" ]");
      String tmpVar11 = tmpVar();
      System.out.println("    "+tmpVar11+" = zext i1 "+tmpVar10+" to i8");
      return "i8,"+tmpVar11+",boolean";
    }

    /**
    * f0 -> PrimaryExpression()
    * f1 -> "<"
    * f2 -> PrimaryExpression()
    */
    public String visit(CompareExpression n, SymbTable argu) {
      String _ret=null;
      String typeIdType0 = n.f0.accept(this, argu);
      String id0 = seperateId(typeIdType0);
      String typeIdType1 = n.f2.accept(this, argu);
      String id1 = seperateId(typeIdType1);
      String tmpVar = tmpVar();
      System.out.println("    "+tmpVar+" = icmp slt i32 "+id0+", "+id1);
      String tmpVar11 = tmpVar();
      System.out.println("    "+tmpVar11+" = zext i1 "+tmpVar+" to i8");
      return "i8,"+tmpVar11+",boolean";
    }


    /**
    * f0 -> PrimaryExpression()
    * f1 -> "+"
    * f2 -> PrimaryExpression()
    */
    public String visit(PlusExpression n, SymbTable argu) {
      String typeIdType0 = n.f0.accept(this, argu);
      String id0 = seperateId(typeIdType0);
      String typeIdType1 = n.f2.accept(this, argu);
      String id1 = seperateId(typeIdType1);
      String tmpVar = tmpVar();
      System.out.println("    "+tmpVar+" = add i32 "+id0+", "+id1);
      return "i32,"+tmpVar+",int";
    }

    /**
    * f0 -> PrimaryExpression()
    * f1 -> "-"
    * f2 -> PrimaryExpression()
    */
    public String visit(MinusExpression n, SymbTable argu) {
      String typeIdType0 = n.f0.accept(this, argu);
      String id0 = seperateId(typeIdType0);
      String typeIdType1 = n.f2.accept(this, argu);
      String id1 = seperateId(typeIdType1);
      String tmpVar = tmpVar();
      System.out.println("    "+tmpVar+" = sub i32 "+id0+", "+id1);
      return "i32,"+tmpVar+",int";
    }

    /**
    * f0 -> PrimaryExpression()
    * f1 -> "*"
    * f2 -> PrimaryExpression()
    */
    public String visit(TimesExpression n, SymbTable argu) {
      String typeIdType0 = n.f0.accept(this, argu);
      String id0 = seperateId(typeIdType0);
      String typeIdType1 = n.f2.accept(this, argu);
      String id1 = seperateId(typeIdType1);
      String tmpVar = tmpVar();
      System.out.println("    "+tmpVar+" = mul i32 "+id0+", "+id1);
      return "i32,"+tmpVar+",int";    
    }

    /**
    * f0 -> PrimaryExpression()
    * f1 -> "["
    * f2 -> PrimaryExpression()
    * f3 -> "]"
    */
    public String visit(ArrayLookup n, SymbTable argu) {
      String _ret=null;
      String typeIdType = n.f0.accept(this, argu);
      String type1 = seperateType(typeIdType);
      String arrPtr = seperateId(typeIdType);
      String type2;
      String type2or;
      if (type1.equals("i8*")){
        type2 = "i8";
        type2or = "boolean";
      }
      else{
        type2 = "i32";
        type2or = "int";
      }
      String zika = arrPtr;
      if(type1.equals("i8*")){
        String tmpVarif = tmpVar();
        System.out.println("    "+tmpVarif+" = bitcast i8* "+arrPtr+" to i32*");
        zika = tmpVarif;
      }
      String tmpVar3 = tmpVar();
      System.out.println("    "+tmpVar3+" = load i32, i32* "+zika);
      String typeIdTypeIndex = n.f2.accept(this, argu);
      String idIndex = seperateId(typeIdTypeIndex);
      String tmpVar4 = tmpVar();
      System.out.println("    "+tmpVar4+" = icmp sge i32 "+idIndex+", 0");
      String tmpVar5 = tmpVar();
      System.out.println("    "+tmpVar5+" = icmp slt i32 "+idIndex+", "+tmpVar3);
      String tmpVar6 = tmpVar();
      System.out.println("    "+tmpVar6+" = and i1 "+tmpVar4+", "+tmpVar5);
      String okLabel = label();
      String errLabel = label();
      System.out.println("    br i1 "+tmpVar6+", label "+okLabel+", label "+errLabel);
      System.out.println(errLabel.substring(1)+":");
      System.out.println("    call void @throw_oob()");
      System.out.println("    br label "+okLabel);
      System.out.println(okLabel.substring(1)+":");
      String tmpVar7 = tmpVar();
      if (type1.equals("i8*"))
        System.out.println("    "+tmpVar7+" = add i32 4, "+idIndex);
      else
        System.out.println("    "+tmpVar7+" = add i32 1, "+idIndex);
      String tmpVar8 = tmpVar();
      System.out.println("    "+tmpVar8+" = getelementptr "+type2+", "+type1+" "+arrPtr+", i32 "+tmpVar7);
      String tmpVar9 = tmpVar();
      System.out.println("    "+tmpVar9+" = load "+type2+", "+type1+" "+tmpVar8);
      return type2+","+tmpVar9+","+type2or;
    }

    /**
    * f0 -> PrimaryExpression()
    * f1 -> "."
    * f2 -> "length"
    */
    public String visit(ArrayLength n, SymbTable argu) {
      String typeIdType = n.f0.accept(this, argu);
      String type1 = seperateType(typeIdType);
      String arrPtr = seperateId(typeIdType);
      String type2;
      String type2or;
      if (type1.equals("i8*")){
        type2 = "i8";
        type2or = "boolean";
      }
      else{
        type2 = "i32";
        type2or = "int";
      }
      String zika = arrPtr;
      if(type1.equals("i8*")){
        String tmpVarif = tmpVar();
        System.out.println("    "+tmpVarif+" = bitcast i8* "+arrPtr+" to i32*");
        zika = tmpVarif;
      }
      String tmpVar3 = tmpVar();
      System.out.println("    "+tmpVar3+" = load i32, i32* "+zika);
      return "i32,"+tmpVar3+",int";
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
      String typeIdType0 = n.f0.accept(this, argu);
      String type0 = seperateType(typeIdType0);
      String id0 = seperateId(typeIdType0);
      String tmpVar0 = tmpVar();
      System.out.println("    "+tmpVar0+" = bitcast i8* "+id0+" to i8***");
      String tmpVar1 = tmpVar();
      System.out.println("    "+tmpVar1+" = load i8**, i8*** "+tmpVar0);
      String methodId = n.f2.accept(this, argu);
      SymbTable originalClass = getClass(seperateHLType(typeIdType0));
      Quad method = originalClass.deepSearchOriginalMethod(methodId);
      String type1 = typeToIR(method.getType());
      String tmpVar2 = tmpVar();
      System.out.println("    "+tmpVar2+" = getelementptr i8*, i8** "+tmpVar1+", i32 "+method.getOffset()/8);
      String tmpVar3 = tmpVar();
      System.out.println("    "+tmpVar3+" = load i8*, i8** "+tmpVar2);
      String tmpVar4 = tmpVar();
      System.out.print("    "+tmpVar4+" = bitcast i8* "+tmpVar3+" to "+type1+" (i8*");
      Vector<String> paramTypes = method.getParams();
      for(int i=0; i< paramTypes.size(); i++)
        System.out.print(","+typeToIR(paramTypes.get(i)));
      System.out.println(")*");

      stk.push(new Vector<>());
      n.f4.accept(this, argu);

      String tmpVar5 = tmpVar();
      Vector<String> prms = stk.pop();
      System.out.print("    "+tmpVar5+" = call "+type1+" "+tmpVar4+"(i8* "+id0);
      for (int i=0; i< prms.size(); i++){
        String typeIdTypez = prms.get(i);
        String typez = seperateType(typeIdTypez);
        String idz = seperateId(typeIdTypez);
        System.out.print("," + typez + " " + idz);
      }
      System.out.println(")");
      return type1+","+tmpVar5+","+method.getType();
    }

    /**
    * f0 -> Expression()
    * f1 -> ExpressionTail()
    */
    public String visit(ExpressionList n, SymbTable argu) {
      String _ret=null;
      String typeIdType0 = n.f0.accept(this, argu);
      Vector<String> prms = stk.pop();
      prms.add(typeIdType0);
      stk.push(prms);
      n.f1.accept(this, argu);
      return _ret;
    }

    /**
    * f0 -> ","
    * f1 -> Expression()
    */
    public String visit(ExpressionTerm n, SymbTable argu) {
      String _ret=null;
      String typeIdType0 = n.f1.accept(this, argu);
      Vector<String> prms = stk.pop();
      prms.add(typeIdType0);
      stk.push(prms);
      n.f1.accept(this, argu);
      return _ret;
    }

    /**
    * f0 -> IntegerLiteral()
    *       | TrueLiteral()
    *       | FalseLiteral()
    *       | Identifier()
    *       | ThisExpression()
    *       | ArrayAllocationExpression()
    *       | AllocationExpression()
    *       | BracketExpression()
    */
    public String visit(PrimaryExpression n, SymbTable argu) {
      String _ret = n.f0.accept(this, argu);
      if (argu.belongsToMethod(_ret)){
        String tmpVar = tmpVar();
        String typeIR = typeToIR(argu.getVarType(_ret));
        System.out.println("    "+tmpVar+" = load "+typeIR+", "+typeIR+"* %"+_ret);
        return typeIR + "," + tmpVar+","+argu.getVarType(_ret);
      }
      if (argu.isFieldOfClass(_ret)){
        String tmpVar1 = tmpVar();
        int off = argu.getOffsetVV(_ret);
        System.out.println("    "+tmpVar1+" = getelementptr i8, i8* %this, i32 "+off);

        String tmpVarFin = tmpVar();
        String typeIR = typeToIR(argu.getVarType(_ret));
        System.out.println("    "+tmpVarFin+" = bitcast i8* "+tmpVar1+" to "+typeIR+"*");
        String tmpVarR = tmpVar();
        System.out.println("    "+ tmpVarR +" = load "+ typeIR +", "+ typeIR + "* " +tmpVarFin);
        return typeIR+","+tmpVarR+","+argu.getVarType(_ret);

      }
      return _ret;
    }

    /**
    * f0 -> "true"
    */
    public String visit(TrueLiteral n, SymbTable argu) {
      String tmpVarR = tmpVar();
      System.out.println("    "+tmpVarR+" = zext i1 1 to i8");
      return "i8,"+tmpVarR+",boolean";
    }

    /**
    * f0 -> "false"
    */
    public String visit(FalseLiteral n, SymbTable argu) {
      String tmpVarR = tmpVar();
      System.out.println("    "+tmpVarR+" = zext i1 0 to i8");
      return "i8,"+tmpVarR+",boolean";
    }

    /**
    * f0 -> <INTEGER_LITERAL>
    */
    public String visit(IntegerLiteral n, SymbTable argu) {
      return "i32," + n.f0.accept(this, argu) + ",int";
    }

    /**
    * f0 -> "this"
    */
    public String visit(ThisExpression n, SymbTable argu) {
      return "i8*,%this,"+thisType;
    }

    /**
    * f0 -> "new"
    * f1 -> "boolean"
    * f2 -> "["
    * f3 -> Expression()
    * f4 -> "]"
    */
    public String visit(BooleanArrayAllocationExpression n, SymbTable argu) {
      String typeIdType = n.f3.accept(this, argu);
      String id = seperateId(typeIdType);
      String tmpVar0 = tmpVar();
      System.out.println("    "+tmpVar0+" = add i32 4, "+id);
      String tmpVar1 = tmpVar();
      System.out.println("    "+tmpVar1+" = icmp sge i32 "+tmpVar0+", 4");
      String okLabel = label();
      String errLabel = label();
      System.out.println("    br i1 "+tmpVar1+", label "+okLabel+", label "+errLabel);
      System.out.println(errLabel.substring(1)+":");
      System.out.println("    call void @throw_nsz()");
      System.out.println("    br label "+okLabel);
      System.out.println(okLabel.substring(1)+":");
      String tmpVar2 = tmpVar();
      System.out.println("    "+tmpVar2+" = call i8* @calloc(i32 1, i32 "+tmpVar0+")");
      String tmpVar3 = tmpVar();
      System.out.println("    "+tmpVar3+" = bitcast i8* "+tmpVar2+" to i32*");
      System.out.println("    store i32 "+id+", i32* "+tmpVar3);
      return "i8*,"+tmpVar2+",boolean[]";
    }

    /**
    * f0 -> "new"
    * f1 -> "int"
    * f2 -> "["
    * f3 -> Expression()
    * f4 -> "]"
    */
    public String visit(IntegerArrayAllocationExpression n, SymbTable argu) {
      String typeIdType = n.f3.accept(this, argu);
      String id = seperateId(typeIdType);
      String tmpVar0 = tmpVar();
      System.out.println("    "+tmpVar0+" = add i32 1, "+id);
      String tmpVar1 = tmpVar();
      System.out.println("    "+tmpVar1+" = icmp sge i32 "+tmpVar0+", 1");
      String okLabel = label();
      String errLabel = label();
      System.out.println("    br i1 "+tmpVar1+", label "+okLabel+", label "+errLabel);
      System.out.println(errLabel.substring(1)+":");
      System.out.println("    call void @throw_nsz()");
      System.out.println("    br label "+okLabel);
      System.out.println(okLabel.substring(1)+":");
      String tmpVar2 = tmpVar();
      System.out.println("    "+tmpVar2+" = call i8* @calloc(i32 "+tmpVar0+", i32 4)");
      String tmpVar3 = tmpVar();
      System.out.println("    "+tmpVar3+" = bitcast i8* "+tmpVar2+" to i32*");
      System.out.println("    store i32 "+id+", i32* "+tmpVar3);
      return "i32*,"+tmpVar3+",int[]";
    }

    /**
    * f0 -> "new"
    * f1 -> Identifier()
    * f2 -> "("
    * f3 -> ")"
    */
    public String visit(AllocationExpression n, SymbTable argu) {
      String _ret=null;
      String classi = n.f1.accept(this, argu);
      String tmpVar0 = tmpVar();
      System.out.println("    "+tmpVar0+" = call i8* @calloc(i32 1, i32 "+ (getClass(classi).getObjSize()+8) +")");
      String tmpVar1 = tmpVar();
      System.out.println("    "+tmpVar1+" = bitcast i8* "+tmpVar0+" to i8***");
      String tmpVar2 = tmpVar();
      String typeOfVt = getTypeOfVt(classi);
      System.out.println("    "+tmpVar2+" = getelementptr "+typeOfVt+", "+typeOfVt+"* @."+classi+"_vtable, i32 0, i32 0");
      System.out.println("    store i8** "+tmpVar2+", i8*** "+tmpVar1);
      return "i8*,"+tmpVar0+","+classi;
    }

    /**
    * f0 -> "!"
    * f1 -> Clause()
    */
    public String visit(NotExpression n, SymbTable argu) {
      String typeIdType =  n.f1.accept(this, argu);
      String id = seperateId(typeIdType);
      String type = seperateType(typeIdType);
      String tmpVar = tmpVar();
      String tmpVar0 = tmpVar();
      String tmpVar1 = tmpVar();
      String tmpVar2 = tmpVar();
      String lab0 = label();
      String lab1 = label();
      String lab2 = label();
      System.out.println("    "+tmpVar+" = icmp eq "+type+" 0, "+id);
      System.out.println("    br i1 "+tmpVar+", label "+lab0+", label "+lab1);
      System.out.println(lab0.substring(1)+":");
      System.out.println("    "+tmpVar0+" = zext i1 1 to i8");
      System.out.println("    br label "+lab2);
      System.out.println(lab1.substring(1)+":");
      System.out.println("    "+tmpVar1+" = zext i1 0 to i8");
      System.out.println("    br label "+lab2);
      System.out.println(lab2.substring(1)+":");
      System.out.println("    "+tmpVar2+" = phi i8 ["+tmpVar0+", "+lab0+"], ["+tmpVar1+", "+lab1+"]");
      return "i8,"+tmpVar2+",boolean";
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