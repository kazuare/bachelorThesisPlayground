package bachelorThesisPlayground;

import java.util.StringJoiner;

public class Vertex {
	public int id;
	public int oldId = -1;
	public double x = -1;
	public double y = -1;
	public boolean fixed = false;
	public boolean pumpStationEntry = false;
	public boolean pumpStationExit = false;
	public boolean canBeLocked = false;
	public String type = null;
	
	
	public Vertex(int id) {
		this.id = id;			
	}
	
	public Vertex(int id, double x, double y) {
		this.id = id;	
		this.x = x;
		this.y = y;
	}

	@Override
	public boolean equals(Object o) {
		if (!(o instanceof Vertex)) return false; 
		return ((Vertex)o).id == this.id;
	}
	
	public double distance(Vertex o) {
		double dx = this.x - o.x;
		double dy = this.y - o.y;
		return Math.sqrt(dx*dx + dy*dy) ;
	}

	@Override
	public int hashCode() {
		return id;
	}

	@Override
	public String toString() {
		return new StringJoiner(", ","[","]")
				.add(""+id)
				.add(""+x)
				.add(""+y)
				.toString();
	}
}