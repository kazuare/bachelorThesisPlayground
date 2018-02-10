package bachelorThesisPlayground;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.educationalProject.surfacePathfinder.visualization.DrawingUtils;
import org.jgrapht.graph.SimpleWeightedGraph;

import bachelorThesisPlayground.deprecated.IsolatedZoneWithSingletonInOut;
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
		
		SimpleWeightedGraph<Vertex,Edge> component = Utils.graphSplit(GraphBuilding.getColoredGraph()).stream().filter(
				e->e.vertexSet().stream().filter(v->v.pumpStationExit).findAny().get().oldId == 185933001
		).findAny().get();
		
		WaterFlow.setFlowDirections(component);

		//DrawingUtils.saveGraph("yo_labels", graph, null, null, true, false);	
		
		IsolatedZoneWithSingletonInOut iz = new IsolatedZoneWithSingletonInOut(component, findEdge(component, 106802401, 106802301),
													findEdge(component, 106914101, 106887001));
		
		ConsumptionCalculator.cleanNonLeafVertexesWithPlacecodes(component);
		//BackloggedCycleResolution.detectCycles(component);
		//Vertex focusPoint = breakSomething(component);
		findEdge(component, 106878301, 106914101).leak = 0.5;
		ConsumptionCalculator.recurrentSetWaterConsumption(component);	
		DrawingUtils.drawGraphWithAttentionPoint(component, findVertex(component, 106878301));
		
		
		
		System.out.println(iz.zoneCheck());
		
		DrawingUtils.saveGraph("yo", component, null, null, false, true);	
		
		
	}
	
	public static Edge findEdge(SimpleWeightedGraph<Vertex,Edge> graph, int id1, int id2) {
		return graph.getEdge(findVertex(graph, id1), findVertex(graph, id2));
	}
	
	public static Vertex findVertex(SimpleWeightedGraph<Vertex,Edge> graph, int id) {
		return graph.vertexSet().stream().filter(v->v.oldId == id).findAny().get();
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
/*
 dbReader.init();
		
		SimpleWeightedGraph<Vertex,Edge> graph = GraphBuilding.getColoredGraph();		
		
		List<SimpleWeightedGraph<Vertex,Edge>> components = Utils.graphSplit(graph);
		
		//SimpleWeightedGraph<Vertex,Edge> chosenComponent = components.stream().filter(
		//		e->e.vertexSet().stream().filter(v->v.pumpStationExit).findAny().get().oldId == 185933001
		//).findAny().get();
		
		WaterFlow.setFlowDirections(components);

		//DrawingUtils.saveGraph("yo_labels", graph, null, null, true, false);	
		
		IsolatedZone iz = new IsolatedZone(graph, findEdge(graph, 106454301, 106547001),
													findEdge(graph, 106771601, 106771401));
		
		for(int i = 0; i < 5; i++) {			
			SimpleWeightedGraph<Vertex,Edge> currentComponent = components.get(i);
			//BackloggedCycleResolution.detectCycles(currentComponent);
			ConsumptionCalculator.cleanNonLeafVertexesWithPlacecodes(currentComponent);
			Vertex focusPoint = breakSomething(currentComponent);
			ConsumptionCalculator.recurrentSetWaterConsumption(currentComponent);	
			//DrawingUtils.drawGraphWithAttentionPoint(currentComponent, focusPoint);			
		}
		
		//DrawingUtils.saveGraph("yo", graph, null, null, false, true);	
		
 * */

