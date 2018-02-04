package bachelorThesisPlayground;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import java.util.stream.Collectors;

import org.educationalProject.surfacePathfinder.visualization.DisplayMode;
import org.educationalProject.surfacePathfinder.visualization.DrawingUtils;
import org.jgrapht.DirectedGraph;
import org.jgrapht.alg.shortestpath.FloydWarshallShortestPaths;
import org.jgrapht.graph.SimpleDirectedGraph;
import org.jgrapht.graph.SimpleWeightedGraph;

import bachelorThesisPlayground.graphBuilding.GraphBuilding;
import bachelorThesisPlayground.readers.DBReader;
import bachelorThesisPlayground.water.flow.WaterFlow;

public class Main {

	public static DBReader dbReader = new DBReader();
	public static void main(String[] args) {		
		//writeJSONGraphForD3();
		//we do layout in d3 and get FINAL_ALL_POINTS.json
		dbReader.init();
		
		SimpleWeightedGraph<Vertex,Edge> graph = GraphBuilding.getColoredGraph();		
		
		List<SimpleWeightedGraph<Vertex,Edge>> components = Utils.graphSplit(graph);
		/*
		DrawingUtils.drawGraphWithAttentionPoint(components.get(0), breakSomething(components.get(0)));
		DrawingUtils.drawGraphWithAttentionPoint(components.get(1), breakSomething(components.get(1)));
		DrawingUtils.drawGraphWithAttentionPoint(components.get(2), breakSomething(components.get(2)));
		DrawingUtils.drawGraphWithAttentionPoint(components.get(3), breakSomething(components.get(3)));
		DrawingUtils.drawGraphWithAttentionPoint(components.get(4), breakSomething(components.get(4)));
		*/
		
		WaterFlow.setFlowDirections(components);
		
		for(int i = 0; i < 5; i++) {
			SimpleWeightedGraph<Vertex,Edge> currentComponent = components.get(i);
			detectCycles(components.get(i));
			//for(Vertex v : components.get(i).vertexSet())
			//	recurrentSetWaterConsumptionWithoutCycles(components.get(i), v);		
		}
		
		DrawingUtils.saveGraph(graph, null, null);
		DrawingUtils.drawGraph(graph, null, null);
	}
	
	public static SimpleWeightedGraph<Vertex,Edge> getUnhandledCycle(SimpleWeightedGraph<Vertex,Edge> graph) {
		Vertex cycledVertex = graph.vertexSet()
				.stream()
				.filter(v->v.inCycle && v.consumption < 0)
				.findAny()
				.orElse(null);
		
		if (cycledVertex == null)
			return null;
		
		SimpleWeightedGraph<Vertex,Edge> cycle = new SimpleWeightedGraph<Vertex,Edge>(Edge.class);
		cycle.addVertex(cycledVertex);
		
		int size = -Integer.MIN_VALUE;
		while (size != cycle.edgeSet().size()) {
			size = cycle.edgeSet().size();
			for (Vertex v : cycle.vertexSet())
				for (Edge e : graph.edgesOf(v)) {
					if (!cycle.containsVertex(e.a) && e.a.inCycle
							|| !cycle.containsVertex(e.b) && e.b.inCycle) {
						cycle.addVertex(e.a);
						cycle.addVertex(e.b);
						cycle.addEdge(e.a, e.b, e);
					}
				}
		}
		
		return cycle;
	}
	
	public static void detectCycles(SimpleWeightedGraph<Vertex,Edge> graph){
		Vertex start = graph.vertexSet()
				.stream()
				.filter(v->v.pumpStationExit)
				.findFirst()
				.get();
		detectCycles(graph, start);
	}

	public static void detectCycles(SimpleWeightedGraph<Vertex,Edge> graph, Vertex start){
		System.out.println("Started dependency calculation");
		SimpleDirectedGraph<Vertex, Edge> directedGraph = new SimpleDirectedGraph<>(Edge.class); 
		for (Edge e : graph.edgeSet()) {
			directedGraph.addVertex(e.a);
			directedGraph.addVertex(e.b);
			directedGraph.addEdge(e.a, e.b, e);
		}
		FloydWarshallShortestPaths<Vertex, Edge> alg = new FloydWarshallShortestPaths<>(directedGraph);
		
		HashMap<Vertex, ArrayList<Vertex>> dependencies = new HashMap<>();
		
		int i = 0;
		for (Vertex v : directedGraph.vertexSet()) {
			if (++i % 100 == 0)
				System.out.println(i + " / " + directedGraph.vertexSet().size());
			dependencies.put(v, new ArrayList<>());
			for (Vertex connectivityCandidate : directedGraph.vertexSet())
				if (!v.equals(connectivityCandidate) && alg.getPath(v, connectivityCandidate) != null)
					dependencies.get(v).add(connectivityCandidate);
		}
		
		for (Vertex v : directedGraph.vertexSet()) {
			for (Vertex dependency : dependencies.get(v)) {
				if (alg.getPath(dependency, v) != null) {
					v.inCycle = true;
				}
			}
		}
		
		System.out.println("Finished dependency calculation");
	}
	
	public static double recurrentSetWaterConsumption(SimpleWeightedGraph<Vertex,Edge> graph){
		Vertex start = graph.vertexSet()
				.stream()
				.filter(v->v.pumpStationExit)
				.findFirst()
				.get();
		return recurrentSetWaterConsumption(graph,start);
	}
	
	public static double recurrentSetWaterConsumption(SimpleWeightedGraph<Vertex,Edge> graph, Vertex start){
		if (start.consumption < 0) {
			double consumption = 0;
			for (Edge e : graph.edgesOf(start)) 
				if (e.a.equals(start)) {
					double childConsumption = recurrentSetWaterConsumptionWithoutCycles(graph, e.b);
					consumption += childConsumption;
				}
			start.consumption = consumption;
		}
		return start.consumption;		
	}
	
	//nested call chain doesnt set anything if meets cycled node
	public static double recurrentSetWaterConsumptionWithoutCycles(SimpleWeightedGraph<Vertex,Edge> graph, Vertex start){
		if (start.inCycle)
			return -Double.MAX_VALUE;
		if (start.consumption < 0) {
			double consumption = 0;
			for (Edge e : graph.edgesOf(start)) 
				if (e.a.equals(start)) {
					double childConsumption = recurrentSetWaterConsumptionWithoutCycles(graph, e.b);
					if (childConsumption == -Double.MAX_VALUE) {
						return -Double.MAX_VALUE;
					}
					consumption += childConsumption;
				}
			start.consumption = consumption;
		}
		return start.consumption;		
	}

	
	public static Vertex breakSomething(SimpleWeightedGraph<Vertex,Edge> graph) {
		List<Vertex> points = new ArrayList<>(graph.vertexSet());
		Collections.shuffle(points);
				
		List<Edge> edges = new ArrayList<>(graph.edgesOf(points.get(0)));
		Collections.shuffle(edges);
		
		edges.get(0).leak = Math.random() * 30;
		
		return points.get(0);
	}
	
}


