package types;
import java.util.*; 

public class SymbTable{
  private String name;
  private Vector<Triplet> varTable;
  private Vector<Quad> methTable;
  private SymbTable extensionOf; //class beeing extended
  private int currVarOffset;
  private int currMethOffset;
  private boolean classs;
  public SymbTable(String name) {
    varTable = new Vector<>();  
    methTable = new Vector<>();  
    extensionOf = null;
    this.name = name;
    currVarOffset = 0;
    currMethOffset = 0;
    classs = true;
  }
  public SymbTable(String name,SymbTable par,boolean cl) {
    varTable = new Vector<>();  
    methTable = new Vector<>();  
    extensionOf = par;
    this.name = name;
    currVarOffset = par.getCurrVarOffset();
    currMethOffset = par.getCurrMethOffset();
    classs = cl;
  }
  public void print(){
  	System.out.println("SymbTable " + name + " is :"); 
  	System.out.println("  varTable:");
    for(int i=0; i< varTable.size(); i++){
      System.out.println("    " + varTable.get(i).toString());
    }
    System.out.println("  methTable:");
    for(int i=0; i< methTable.size(); i++){
      System.out.println("    " + methTable.get(i).toString());
    }
    System.out.println();
  }
  public void output(){
    if (isClass()){
      System.out.println("-----------Class "+name+"-----------");
      System.out.println("--Variables---");
      for(int i=0; i< varTable.size(); i++)
        System.out.println(name+"."+varTable.get(i).getId()+" : "+varTable.get(i).getOffset());
      System.out.println("---Mathods---");
      for(int i=0; i< methTable.size(); i++)
        if (methTable.get(i).getOffset() >= 0)
          System.out.println(name+"."+methTable.get(i).getId()+" : "+methTable.get(i).getOffset());
      System.out.println("");
    }
  }
  public void addVar(String type,String name){
    searchVar(name);
  	Triplet gIn = new Triplet(type,name,currVarOffset);
  	varTable.add(gIn);
    incrCurrVarOffset(type);
  }
  public void addMeth(String type,String name,Vector<String> params){
  	Quad gIn = new Quad(type,name,params,currMethOffset);
  	methTable.add(gIn);
    incrCurrMethOffset();
  }
  public String getName(){
    return name;
  }
  public SymbTable getParent(){
    return extensionOf;
  }
  public int getCurrVarOffset(){
    return currVarOffset;
  }
  public int getCurrMethOffset(){
    return currMethOffset;
  }
  public Vector<Triplet> getVarTable(){
    return varTable;
  }
  public Vector<Quad> getMethTable(){
    return methTable;
  }
  public String getVarType(String id){
    for (int i=0; i<varTable.size();i++)
      if (id.equals(varTable.get(i).getId()))
        return varTable.get(i).getType();
    if (extensionOf != null)
      return extensionOf.getVarType(id);
    else
      return null;
  }
  public void incrCurrVarOffset(String type){
    if (type.equals("int"))
      currVarOffset = currVarOffset + 4;
    else if (type.equals("boolean"))
      currVarOffset = currVarOffset + 1;
    else
      currVarOffset = currVarOffset + 8;
  }
  public void incrCurrMethOffset(){
    currMethOffset = currMethOffset + 8;
  }
  public boolean isClass(){
    return classs;
  }
  public Quad searchMethod(String id){
    Quad _ret = null;
    for(int i=0; i< methTable.size(); i++){
            Quad inQ = methTable.get(i);
            if (inQ.getId().equals(id))
              return inQ;
        }
    return _ret;
  }
  public Quad deepSearchMethod(String id){
    for(int i=0; i< methTable.size(); i++){
            Quad inQ = methTable.get(i);
            if (inQ.getId().equals(id))
              return inQ;
        }
    if (extensionOf != null)
      return extensionOf.deepSearchMethod(id);
    else
      return null;
  }
  public void searchVar(String id) throws RuntimeException{
    for(int i=0; i< varTable.size(); i++)
      if (varTable.get(i).getId().equals(id))
        throw new RuntimeException("Error:\nVariable "+id+" is already defined in scope\n");
  }
  public boolean shouldPlaceMethod(String type,String id,Vector<String> params) throws RuntimeException{
    if (this.searchMethod(id) != null)
      throw new RuntimeException("Error:\nMethod already declared in this scope: "+id+"\n");
    for(SymbTable i = extensionOf; i!=null; i = i.getParent()){
      Quad q = null;
      q = i.searchMethod(id);
      if (q != null){
        if (q.getType().equals(type) && q.getParams().equals(params)){
          Quad gIn = new Quad(type,id,params,-1);
          methTable.add(gIn);
          return false;
        }
        else
          throw new RuntimeException("Error:\ndue to ilegal overiding or overloading: "+id+"\n");
      }
    }
    return true;
  }
  public boolean eqParams(Vector<String> v1,Vector<String> v2){
    if (v1.size()!=v2.size())
      return false;
    for (int i=0; i<v1.size();i++)
      if (!v1.get(i).equals(v2.get(i)))
        return false;
    return true;
  }
}