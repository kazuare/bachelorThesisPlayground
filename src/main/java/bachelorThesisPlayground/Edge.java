package bachelorThesisPlayground;

import java.util.StringJoiner;

public class Edge {
	public int id;
	public double length;
	public Vertex a;
	public Vertex b;
	public String material;
	public double diameter;
	//from 0 to 1
	public double leak = 0;
	
	public Edge(int id) {
		this.id = id;
	}

	public Edge(int eid, int aid, int bid) {
		id = eid;
		a = new Vertex(aid);
		b = new Vertex(bid);
	}

	@Override
	public boolean equals(Object o){
		if (!(o instanceof Edge)) return false; 
		return ((Edge)o).id == this.id;
	}

	@Override
	public int hashCode(){
		return id;
	}
	
	@Override
	public String toString() {
		return new StringJoiner(", ","[","]")
				.add(""+id)
				.add(""+a)
				.add(""+b)
				.add(""+length)
				.add(""+diameter)
				.add(material)
				.toString();
	}
}
