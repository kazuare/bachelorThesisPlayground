package org.educationalProject.surfacePathfinder.visualization;

import java.util.ArrayList;
import io.github.jdiemke.triangulation.Vector2D;
/**
* extends Vector2D in order to use altitude
*/
public class Point extends Vector2D {
	public double alt;
	public int id = 0;
	public ArrayList<Integer> domains;
	
	public Point(double x, double y, double alt, int id){
		super(x, y);
		this.alt = alt;
		this.id = id;
	}
	public Point(double x, double y, double alt){
		super(x, y);
		this.alt = alt;
	}
	public boolean equals(Point b){
		if(b == null)
			return false;
		if (this == b)
			return true;
		return (x == b.x) && (y == b.y) && (id == b.id);
	}
	public void setPoint(Point b) {
		this.x = b.x;
		this.y = b.y;
		this.alt = b.alt;
	}
	@Override
	public boolean equals(Object obj){
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Point b = (Point)obj;
		return (x == b.x) && (y == b.y) && (id == b.id);
	}
	@Override
	public String toString(){
		return "point: " + x + " " + y + " " + alt + " " + id; 
	}
	
}
