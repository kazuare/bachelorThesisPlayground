package bachelorThesisPlayground.water.flow;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.jgrapht.graph.SimpleWeightedGraph;

import bachelorThesisPlayground.Edge;
import bachelorThesisPlayground.Vertex;

public class WaterFlow {
	
	public static void setFlowDirections(SimpleWeightedGraph<Vertex,Edge> component) {
		List<Vertex> errors = new ArrayList<>();
		
		WaterFlow.setWaterFlowDirections(component);
		errors.addAll(WaterFlow.waterFlowSanityCheck(component));
		
		System.out.println("errors: " + errors);
		if (errors.size() > 0 )
			throw new RuntimeException("water flow sanity check");
		
	}
	
	public static void setFlowDirections(List<SimpleWeightedGraph<Vertex,Edge>> components) {
		List<Vertex> errors = new ArrayList<>();
		
		WaterFlow.setWaterFlowDirections(components.get(0));
		errors.addAll(WaterFlow.waterFlowSanityCheck(components.get(0)));
		WaterFlow.setWaterFlowDirections(components.get(1));
		errors.addAll(WaterFlow.waterFlowSanityCheck(components.get(1)));
		WaterFlow.setWaterFlowDirections(components.get(2));
		errors.addAll(WaterFlow.waterFlowSanityCheck(components.get(2)));
		WaterFlow.setWaterFlowDirections(components.get(3));
		errors.addAll(WaterFlow.waterFlowSanityCheck(components.get(3)));
		WaterFlow.setWaterFlowDirections(components.get(4));
		errors.addAll(WaterFlow.waterFlowSanityCheck(components.get(4)));
		
		System.out.println("errors: " + errors);
		if (errors.size() > 0 )
			throw new RuntimeException("water flow sanity check");
		
	}
	
	public static void setWaterFlowDirections(SimpleWeightedGraph<Vertex,Edge> graph){
		Vertex start = graph.vertexSet()
				.stream()
				.filter(v->v.pumpStationExit)
				.findFirst()
				.get();
		setWaterFlowDirections(start, graph);
	}
	
	
	public static List<Vertex> waterFlowSanityCheck(SimpleWeightedGraph<Vertex,Edge> graph) {
		List<Vertex> errors = new ArrayList<>();
		
		Set<Vertex> from = new HashSet<>();
		Set<Vertex> to = new HashSet<>();
		
		for (Edge e : graph.edgeSet()) {
			from.add(e.a);
			to.add(e.b);
		}
		
		for (Vertex v : from) {
			if (!to.contains(v) && !v.pumpStationExit)
				errors.add(v);
		}
		
		return errors;
	}
	public static void setWaterFlowDirections(Vertex start, SimpleWeightedGraph<Vertex,Edge> graph){
		Set<Edge> processedEdges = new HashSet<>();
		
		for (Edge e : graph.edgesOf(start)) {
			if (e.b.equals(start)) {
				swapEnds(e);
			}
		}
		
		processedEdges.addAll(graph.edgesOf(start));
		
		List<Edge> graphEdges = new ArrayList<>(graph.edgeSet());
		
		Collections.sort(graphEdges, (a,b) -> -Double.compare(a.diameter, b.diameter));
		
		while (processedEdges.size() < graph.edgeSet().size()) {
			if (processedEdges.size() % 100 == 0) {
				System.out.println(processedEdges.size() + " / " + graph.edgeSet().size());
			}
			
			Edge toProcess = null;
			for (int i = 0; i < graphEdges.size(); i++) {
				Edge e = graphEdges.get(i);
				if (!processedEdges.contains(e) && findCollision(processedEdges, e) != null) {
					toProcess = e;
					graphEdges.remove(i);
					break;
				}
			}
			
			if (toProcess.b.equals(findCollision(processedEdges, toProcess))) {
				swapEnds(toProcess);
			}
			
			processedEdges.add(toProcess);
		}
	}
	
	public static void swapEnds(Edge e) {
		Vertex temp = e.a;
		e.a = e.b;
		e.b = temp;		
	}
	
	public static Vertex findCollision(Set<Edge> edges, Edge e) {
		for (Edge edgeFromSet : edges) {
			if (e.a.equals(edgeFromSet.a) || e.a.equals(edgeFromSet.b))
				return e.a;
			if (e.b.equals(edgeFromSet.a) || e.b.equals(edgeFromSet.b))
				return e.b;
		}		
		return null;
	}
}
