package bachelorThesisPlayground;

public class Vertex {
	public int id;
	public double x = -1;
	public double y = -1;
	public boolean fixed = false;
	
	public Vertex(int id) {
		this.id = id;			
	}
	
	public Vertex(int id, double x, double y) {
		this.id = id;	
		this.x = x;
		this.y = y;
	}
	
	public boolean equals(Object o){
		if (!(o instanceof Vertex)) return false; 
		return ((Vertex)o).id == this.id;
	}
	
	public int hashCode(){
		return id;
	}
}