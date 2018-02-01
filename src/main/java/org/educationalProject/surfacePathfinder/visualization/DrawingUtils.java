package org.educationalProject.surfacePathfinder.visualization;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;

import org.jgrapht.graph.SimpleWeightedGraph;

import bachelorThesisPlayground.Edge;
import bachelorThesisPlayground.Vertex;

public class DrawingUtils {

	public static void saveGraph(SimpleWeightedGraph<Vertex,Edge> graph, SimpleWeightedGraph<Vertex,Edge> overlayGraph){
		//save as png file
		DisplayMode.setMode("screenshot");
			
		NetworkVisualizer visScreenshot = new NetworkVisualizer()
				.setData(graph)
				.setDefaultWidth(10000)
				.setLabelDrawing(true)
				.setOverlayData(overlayGraph)
				.setLabelDrawing(false)
				.calculateWeightAndHeight();
		Screenshooter.start(visScreenshot, visScreenshot.getWidth() + 50, visScreenshot.getHeight()+ 50);

		Path source = FileSystems.getDefault().getPath("c:\\users\\test\\desktop\\yo.png");
		Path out = FileSystems.getDefault().getPath("yo.png");
		try {
		    Files.copy(source, out, StandardCopyOption.REPLACE_EXISTING);
		} catch (IOException e) {
		    e.printStackTrace();
		}
	}
	
	public static void drawGraph(SimpleWeightedGraph<Vertex,Edge> graph, SimpleWeightedGraph<Vertex,Edge> overlayGraph){
		//draw on screen
		DisplayMode.setMode("screen");
		
		NetworkVisualizer vis = new NetworkVisualizer()
				.setData(graph)
				.setDefaultWidth(650)
				.setOverlayData(overlayGraph)
				.setOffset(true)
				.setLabelDrawing(false)
				.calculateWeightAndHeight();
		SwingWindow.start(vis, vis.getWidth() + 50, vis.getHeight()+ 50, "pipes");
	}
	
	public static void drawGraphWithAttentionPoint(SimpleWeightedGraph<Vertex,Edge> graph, Vertex point){
		//draw on screen
		DisplayMode.setMode("screen");
		
		NetworkVisualizer vis = new NetworkVisualizer()
				.setData(graph)
				.setDefaultWidth(650)
				.setAttentionPoint(point)
				.setLabelDrawing(false)
				.calculateWeightAndHeight();
		SwingWindow.start(vis, vis.getWidth() + 50, vis.getHeight()+ 50, "pipes");
	}
	
	public static SimpleWeightedGraph<Vertex,Edge> graphJoin(List<SimpleWeightedGraph<Vertex,Edge>> graphs) {
		SimpleWeightedGraph<Vertex,Edge> result = new SimpleWeightedGraph<Vertex,Edge>(Edge.class);
		for (SimpleWeightedGraph<Vertex,Edge> graph : graphs) {
			for(Vertex v : graph.vertexSet())
				result.addVertex(v);
			for(Edge e : graph.edgeSet())
				result.addEdge(e.a, e.b, e);
		}
		return result;
	}
}
