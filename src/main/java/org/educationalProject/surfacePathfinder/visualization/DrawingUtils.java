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

	public static void saveGraph(String name, SimpleWeightedGraph<Vertex,Edge> graph, SimpleWeightedGraph<Vertex,Edge> overlayGraph, List<Vertex> pointsToLabel, boolean drawLabels, boolean drawConsumption){
		//save as png file			
		NetworkVisualizer visScreenshot = new NetworkVisualizer()
				.setMode("screenshot")
				.setData(graph)
				.setDefaultWidth(6000)
				.setOverlayData(overlayGraph)
				.setPointsToLabel(pointsToLabel)
				.setLabelDrawing(drawLabels)
				.setConsumptionDrawing(drawConsumption)
				.calculateWeightAndHeight();
		
		visScreenshot.name = name;
		
		Screenshooter.start(visScreenshot, visScreenshot.getWidth() + 50, visScreenshot.getHeight()+ 50);

		Path source = FileSystems.getDefault().getPath("c:\\users\\test\\desktop\\" + name + ".png");
		Path out = FileSystems.getDefault().getPath(name + ".png");
		try {
		    Files.copy(source, out, StandardCopyOption.REPLACE_EXISTING);
		} catch (IOException e) {
		    e.printStackTrace();
		}
	}
	
	public static void drawGraph(SimpleWeightedGraph<Vertex,Edge> graph, SimpleWeightedGraph<Vertex,Edge> overlayGraph, List<Vertex> pointsToLabel){
		//draw on screen
		NetworkVisualizer vis = new NetworkVisualizer()
				.setMode("screen")
				.setData(graph)
				.setDefaultWidth(800)
				.setOverlayData(overlayGraph)
				.setPointsToLabel(pointsToLabel)
				.setOffset(true)
				.setLabelDrawing(true)
				.setConsumptionDrawing(false)
				.calculateWeightAndHeight();
		SwingWindow.start(vis, vis.getWidth() + 50, vis.getHeight()+ 50, "pipes");
	}
	
	public static void drawGraphWithAttentionPoint(SimpleWeightedGraph<Vertex,Edge> graph, Vertex point){
		//draw on screen
		NetworkVisualizer vis = new NetworkVisualizer()
				.setMode("focused screen")
				.setData(graph)
				.setDefaultWidth(650)
				.setAttentionPoint(point)
				.setLabelDrawing(false)
				.setConsumptionDrawing(true)
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
