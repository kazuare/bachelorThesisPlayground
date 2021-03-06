package bachelorThesisPlayground;

import java.util.StringJoiner;

public class Vertex {
	public int id;
	public int oldId = -1;
	public double x = -1;
	public double y = -1;
	public boolean fixed = false;
	public boolean southernPumpStation = false;
	public boolean pumpStationEntry = false;
	public boolean pumpStationExit = false;
	public boolean canBeLocked = false;
	public boolean locked = false;
	public boolean betweenSectorBlock = false;
	public boolean pressureTransferCandidate = false;
	public String type = null;
	
	public double cycleInput = -1;
	public boolean inCycle = false;
	
	public int placecode = -1;
	public double consumption = -Double.MAX_VALUE;	
	public boolean sensorPlaced = false;
	
	public boolean colored = false;
	public float r = -1;
	public float g = -1;
	public float b = -1;
	public int zoneIndex = -1;
	
	public String address = null;
	
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
				.add(""+oldId)
				.add(""+x)
				.add(""+y)
				.toString();
	}
}