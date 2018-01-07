package org.educationalProject.surfacePathfinder.visualization;

import java.awt.Color;
import java.awt.Font;
import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.ByteBuffer;
import java.util.List;

import javax.imageio.ImageIO;

import org.jgrapht.WeightedGraph;
import org.jgrapht.graph.DefaultDirectedWeightedGraph;
import org.jgrapht.graph.DefaultWeightedEdge;

import com.jogamp.common.nio.Buffers;
import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL2;
import com.jogamp.opengl.util.awt.TextRenderer;

import bachelorThesisPlayground.Edge;
import bachelorThesisPlayground.Vertex;
import io.github.jdiemke.triangulation.Triangle2D;

public class NetworkVisualizer extends Visualizer{
	protected WeightedGraph<Vertex, Edge> graph;
	protected WeightedGraph<Vertex, Edge> overlayGraph;
	protected int width = 1000;
	protected int height = 0;
	protected double maxX = 0;
	protected double maxY = 0;	    
	protected double scaling = 0;
	protected boolean drawLabel = false;
	public NetworkVisualizer setDefaultWidth(int width){
		this.width = width;
		return this;
	}
	
	public NetworkVisualizer setData(WeightedGraph<Vertex, Edge> graph){
		this.graph = graph;	
		dataSet = true;
		return this;
	}
	
	public NetworkVisualizer setOverlayData(WeightedGraph<Vertex, Edge> graph){
		this.overlayGraph = graph;	
		return this;
	}
	
	public NetworkVisualizer setLabelDrawing(boolean drawLabel){
		this.drawLabel = drawLabel;
		return this;
	}
	
	protected void drawContent( GL2 gl2 ){
		System.out.println("drawing start");
	  	gl2.glColor3f(1f,1f,1f);	
		gl2.glRectd(0,0,width+50,height+50);

		gl2.glPointSize(100);
		gl2.glBegin(GL.GL_POINTS);  
    	drawColoredPoint(gl2, new Vertex(1,0,0), 1, 0, 0);      
	    gl2.glEnd(); 
		

		System.out.println("drawing main lines");
		gl2.glLineWidth((float)DisplayMode.getStrokeWidth());
	    gl2.glBegin( GL2.GL_LINES );   
		for (Edge e : graph.edgeSet()) {
		    if (!e.a.colored) {
		    	drawColoredPoint(gl2, e.a, 0, 1, 1);   		    	
		    } else {
		    	drawColoredPoint(gl2, e.a, e.a.r, e.a.g, e.a.b); 
		    }
		    if (!e.b.colored) {
		    	drawColoredPoint(gl2, e.b, 1, 0, 1); 		    	
		    } else {
		    	drawColoredPoint(gl2, e.b, e.b.r, e.b.g, e.b.b); 
		    }
	    	
		      
		}
	    gl2.glEnd(); 

		System.out.println("drawing points");
		/*
		gl2.glPointSize((float)DisplayMode.getBigPointSize()*2f);
		gl2.glBegin(GL.GL_POINTS);        	
		for (Vertex v : graph.vertexSet()) {
			if(v.pumpStationEntry)
				drawColoredPoint(gl2, v, 0f, 0f, 1f);
		}		
		gl2.glEnd();
		*/
		gl2.glPointSize((float)DisplayMode.getBigPointSize()*2f);
		gl2.glBegin(GL.GL_POINTS);        	
		for (Vertex v : graph.vertexSet()) {
			if(v.mainSensorPlaced)
				drawColoredPoint(gl2, v, 0f, 0f, 1f);
		}		
		gl2.glEnd();
		
		gl2.glPointSize((float)DisplayMode.getBigPointSize()*2f);
		gl2.glBegin(GL.GL_POINTS);        	
		for (Vertex v : graph.vertexSet()) {
			if(v.pumpStationExit)
				drawColoredPoint(gl2, v, 0f, 1f, 0f);
		}		
		gl2.glEnd();
		
		gl2.glPointSize((float)DisplayMode.getBigPointSize()*3f);
		gl2.glBegin(GL.GL_POINTS);        	
		for (Vertex v : graph.vertexSet()) {
			if(v.betweenSectorBlock)
				drawColoredPoint(gl2, v, 1f, 0f, 0.5f);
		}		
		gl2.glEnd();
		
		gl2.glPointSize((float)DisplayMode.getBigPointSize()*3f);
		gl2.glBegin(GL.GL_POINTS);        	
		for (Vertex v : graph.vertexSet()) {
			if(v.southernPumpStation)
				drawColoredPoint(gl2, v, 0.5f, 0.5f, 0.5f);
		}		
		gl2.glEnd();
		
		gl2.glPointSize((float)DisplayMode.getBigPointSize()*1.5f);
		gl2.glBegin(GL.GL_POINTS);        	
		for (Vertex v : graph.vertexSet()) {
			if(v.locked)
				drawColoredPoint(gl2, v, 1f, 0f, 0f);
		}		
		gl2.glEnd();
		
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

		System.out.println("drawing overlay");
		if (overlayGraph != null) {
			gl2.glLineWidth((float)DisplayMode.getStrokeWidth());
		    gl2.glBegin( GL2.GL_LINES );   
			for (Edge e : overlayGraph.edgeSet()) {
			    drawColoredPoint(gl2, e.a, 0, 0, 0); 
			    drawColoredPoint(gl2, e.b, 0, 0, 0);  
			}
		    gl2.glEnd(); 
		}

		System.out.println("drawing labels");
		if (drawLabel) {
			TextRenderer textRenderer = new TextRenderer(new Font("Verdana", Font.BOLD, 12));
			textRenderer.begin3DRendering();
			textRenderer.setColor(Color.BLACK);

			for (Vertex v : graph.vertexSet()) {
				if (!v.pumpStationExit && !v.betweenSectorBlock)
					continue;
				
			    gl2.glPushMatrix(); 
			    gl2.glTranslated((int)normalizeX(v.x), (int)normalizeY(v.y), 0.0); 
				textRenderer.draw(""+v.oldId, 0, 0);
			    textRenderer.flush(); 
			    gl2.glPopMatrix(); 
			}
			textRenderer.end3DRendering();
		}
		
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
	/* works incorrectly
	public void drawRhombus(GL2 gl2, Vertex v, float r, float g, float b){
		gl2.glColor3f(r,g,b);	
		double radius = DisplayMode.getPolygonDiameter()/2;
		gl2.glBegin(GL.GL_LINES);
		gl2.glVertex2d(normalizeX(v.x+radius), normalizeY(v.y));
		gl2.glVertex2d(normalizeX(v.x), normalizeY(v.y-radius));
		gl2.glVertex2d(normalizeX(v.x-radius), normalizeY(v.y));
		gl2.glVertex2d(normalizeX(v.x), normalizeY(v.y+radius));		
		gl2.glEnd();	
	}
	
	public void drawTriangle(GL2 gl2, Vertex v, float r, float g, float b){
		gl2.glColor3f(r,g,b);	
		double radius = DisplayMode.getPolygonDiameter()/2;
		gl2.glBegin(GL.GL_LINES);
		gl2.glVertex2d(normalizeX(v.x), normalizeY(v.y+radius));
		gl2.glVertex2d(normalizeX(v.x-radius), normalizeY(v.y-radius));
		gl2.glVertex2d(normalizeX(v.x+radius), normalizeY(v.y-radius));
		gl2.glEnd();	
	}
	*/
}
