package bachelorThesisPlayground;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.educationalProject.surfacePathfinder.visualization.DrawingUtils;
import org.jgrapht.graph.SimpleWeightedGraph;

import bachelorThesisPlayground.graphBuilding.GraphBuilding;
import bachelorThesisPlayground.readers.DBReader;
import bachelorThesisPlayground.water.flow.ConsumptionCalculator;
import bachelorThesisPlayground.water.flow.WaterFlow;

public class Main {

	public static DBReader dbReader = new DBReader();
	public static void main(String[] args) {		
		//writeJSONGraphForD3();
		//we do layout in d3 and get FINAL_ALL_POINTS.json
		dbReader.init();
		
		SimpleWeightedGraph<Vertex,Edge> graph = GraphBuilding.getColoredGraph();		
		
		List<SimpleWeightedGraph<Vertex,Edge>> components = Utils.graphSplit(graph);
		
		WaterFlow.setFlowDirections(components);
		
		for(int i = 0; i < 5; i++) {			
			SimpleWeightedGraph<Vertex,Edge> currentComponent = components.get(i);
			//BackloggedCycleResolution.detectCycles(currentComponent);
			ConsumptionCalculator.cleanNonLeafVertexesWithPlacecodes(currentComponent);
			Vertex focusPoint = breakSomething(currentComponent);
			ConsumptionCalculator.recurrentSetWaterConsumption(currentComponent);	
			DrawingUtils.drawGraphWithAttentionPoint(currentComponent, focusPoint);
			
		}

		//DrawingUtils.saveGraph(graph, null, null);
	}
	
	
	public static Vertex breakSomething(SimpleWeightedGraph<Vertex,Edge> graph) {
		List<Vertex> points = new ArrayList<>(graph.vertexSet());
		Collections.shuffle(points);
				
		Vertex pointToBreak = points.get(0);		
		List<Edge> edges = new ArrayList<>(graph.edgesOf(pointToBreak));
		Collections.shuffle(edges);
		
		edges.get(0).leak = Math.random();
		
		return pointToBreak;
	}
	
}


