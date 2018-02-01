package bachelorThesisPlayground;

import java.util.List;
import org.educationalProject.surfacePathfinder.visualization.DrawingUtils;
import org.jgrapht.graph.SimpleWeightedGraph;

import bachelorThesisPlayground.graphBuilding.GraphBuilding;
import bachelorThesisPlayground.readers.DBReader;

public class Main {

	public static DBReader dbReader = new DBReader();
	public static void main(String[] args) {		
		//writeJSONGraphForD3();
		//we do layout in d3 and get FINAL_ALL_POINTS.json
		dbReader.init();
		
		SimpleWeightedGraph<Vertex,Edge> graph = GraphBuilding.getColoredGraph();		
		
		List<SimpleWeightedGraph<Vertex,Edge>> components = Utils.coloredGraphSplit(graph);
				 
		DrawingUtils.drawGraphWithAttentionPoint(components.get(0), components.get(0).vertexSet().stream().findAny().get());
		DrawingUtils.drawGraphWithAttentionPoint(components.get(1), components.get(1).vertexSet().stream().findAny().get());
		DrawingUtils.drawGraphWithAttentionPoint(components.get(2), components.get(2).vertexSet().stream().findAny().get());
		DrawingUtils.drawGraphWithAttentionPoint(components.get(3), components.get(3).vertexSet().stream().findAny().get());
		DrawingUtils.drawGraphWithAttentionPoint(components.get(4), components.get(4).vertexSet().stream().findAny().get());
		
		DrawingUtils.drawGraph(graph, null);
	}

}


