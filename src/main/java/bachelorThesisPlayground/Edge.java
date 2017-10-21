package bachelorThesisPlayground;

public class Edge {
	public int id;
	public double length;
	public Vertex a;
	public Vertex b;
	public String material;
	public double diameter;
	
	public Edge(int id) {
		this.id = id;
	}

	public Edge(int eid, int aid, int bid) {
		id = eid;
		a = new Vertex(aid);
		b = new Vertex(bid);
	}
}
