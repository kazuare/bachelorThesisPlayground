package org.educationalProject.surfacePathfinder.visualization;

import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.ByteBuffer;
import java.util.List;

import javax.imageio.ImageIO;

import org.jgrapht.graph.DefaultDirectedWeightedGraph;
import org.jgrapht.graph.DefaultWeightedEdge;

import com.jogamp.common.nio.Buffers;
import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL2;

import bachelorThesisPlayground.Edge;
import bachelorThesisPlayground.Vertex;
import io.github.jdiemke.triangulation.Triangle2D;

public class NetworkVisualizer extends Visualizer{
	protected DefaultDirectedWeightedGraph<Vertex, Edge> graph;
	protected int width = 1000;
	protected int height = 0;
	protected double maxX = 0;
	protected double maxY = 0;	    
	protected double scaling = 0;
	public NetworkVisualizer setDefaultWidth(int width){
		this.width = width;
		return this;
	}
	
	public NetworkVisualizer setData(DefaultDirectedWeightedGraph<Vertex, Edge> graph){
		this.graph = graph;	
		dataSet = true;
		return this;
	}
	
	protected void drawContent( GL2 gl2 ){
		gl2.glColor3f(1f,1f,1f);	
		gl2.glRectd(0,0,maxX,maxY);
		
		for (Edge e : graph.edgeSet()) {
			gl2.glLineWidth((float)DisplayMode.getStrokeWidth());
		    gl2.glBegin( GL2.GL_LINES );   
	    	drawColoredPoint(gl2, e.a, 0, 1, 1);   
	    	drawColoredPoint(gl2, e.b, 1, 0, 1);
		      
		    gl2.glEnd(); 
		}
		
		
		gl2.glPointSize((float)DisplayMode.getBigPointSize());
		gl2.glBegin(GL.GL_POINTS);        	
		for (Vertex v : graph.vertexSet()) {
			if(v.fixed)
				drawColoredPoint(gl2, v, 0f, 0f, 0f);
		}		
		gl2.glEnd();
		
		gl2.glPointSize((float)DisplayMode.getSmallPointSize());
		gl2.glBegin(GL.GL_POINTS);        	
		for (Vertex v : graph.vertexSet()) {
			if(!v.fixed)
				drawColoredPoint(gl2, v, 1f, 0.5f, 0f);
		}		
		gl2.glEnd();
		
	}

	public NetworkVisualizer calculateWeightAndHeight(){
		maxX = graph.vertexSet().stream().mapToDouble(p->p.x).max().getAsDouble();
	    maxY = graph.vertexSet().stream().mapToDouble(p->p.y).max().getAsDouble();
	    
	    scaling = width/maxX;
	    height = (int) (scaling * maxY);
	    return this;
	}
	
	public int getHeight(){
		return height;
	}
	
	public int getWidth(){
		return width;
	}
	
	@Override
	protected void display(GL2 gl2) {
		gl2.glClear( GL.GL_COLOR_BUFFER_BIT );
	    gl2.glLoadIdentity();	
		
	    if(height == 0)
	    	calculateWeightAndHeight();
	    
		drawContent(gl2);	
		
	}
	
	public double normalizeX(double data){
		return width*data/maxX; 
	}
	
	public double normalizeY(double data){
		return height*data/maxY; 
	}
	
	public void drawPoint(GL2 gl2, Vertex v){
		gl2.glVertex2d(normalizeX(v.x), normalizeY(v.y));
	}
	
	public void drawColoredPoint(GL2 gl2, Vertex v, float r, float g, float b){
		gl2.glColor3f(r,g,b);	
		drawPoint(gl2, v);
	}
	
}