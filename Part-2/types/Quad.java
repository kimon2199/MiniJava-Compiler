package types;
import java.util.*; 

public class Quad{
	private String type;
	private String id;
	private Vector<String> paramTypes;
	private int offset;
	public Quad(String t,String n,Vector<String> p,int off){
		type = t;
		id = n;
		paramTypes = p;
		offset = off;
	}
	public String toString(){
		return "(" + type + "," + id + "," + paramTypes + "," + offset + ")";
	}
	public String getId(){
		return id;
	}
	public String getType(){
		return type;
	}
	public Vector<String> getParams(){
		return paramTypes;
	}
	public int getOffset(){
		return offset;
	}
}