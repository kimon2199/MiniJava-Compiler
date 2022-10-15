package types;

public class Triplet{
	private String type;
	private String id;
	private int offset;
	public Triplet(String t,String n,int off){
		type = t;
		id = n;
		offset = off;
	}
	public String toString(){
		return "(" + type + "," + id + "," + offset + ")";
	}
	public String getId(){
		return id;
	}
	public String getType(){
		return type;
	}
	public int getOffset(){
		return offset;
	}
}