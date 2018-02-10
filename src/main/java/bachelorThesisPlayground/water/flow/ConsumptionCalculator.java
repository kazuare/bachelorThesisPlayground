package bachelorThesisPlayground.water.flow;

import java.util.List;
import java.util.stream.Collectors;

import org.jgrapht.graph.SimpleWeightedGraph;

import bachelorThesisPlayground.Edge;
import bachelorThesisPlayground.Vertex;

public class ConsumptionCalculator {

	public static List<Vertex> findStartsOfInputEdges(SimpleWeightedGraph<Vertex,Edge> graph, Vertex destination) {
		return graph.edgesOf(destination)
				.stream()
				.filter(e->e.b.equals(destination))
				.map(e->e.a)
				.collect(Collectors.toList());
	}

	public static void cleanNonLeafVertexesWithPlacecodes(SimpleWeightedGraph<Vertex,Edge> graph){
		graph.vertexSet()
				.stream()
				.filter(v -> v.placecode != -1)
				.filter(v -> graph.edgesOf(v).stream().filter(edge->edge.a.equals(v)).findAny().isPresent())
				.forEach(v->{
					v.placecode = -1;
					v.consumption = - Double.MAX_VALUE;
				});
	}
	
	public static double recurrentSetWaterConsumption(SimpleWeightedGraph<Vertex,Edge> graph){
		Vertex start = graph.vertexSet()
				.stream()
				.filter(v->v.pumpStationExit)
				.findFirst()
				.get();
		return recurrentSetWaterConsumption(graph,start);
	}
	
	public static void recurrentSetWaterConsumptionSanityCheck(SimpleWeightedGraph<Vertex,Edge> graph){
		Vertex v = graph.vertexSet()
				.stream()
				.filter(e->findStartsOfInputEdges(graph, e).size()==2)
				.findAny()
				.get();
		System.out.println(v);
		System.out.println(v.consumption);
		
		for (Vertex input: findStartsOfInputEdges(graph, v)) {
			System.out.println("=====-----=====");
			System.out.println(input.consumption);
			System.out.println(graph.edgesOf(input)
				.stream()
				.filter(e->e.a.equals(input))
				.map(e->e.b)
				.collect(Collectors.toList()));
		}
	}
	
	public static double recurrentSetWaterConsumption(SimpleWeightedGraph<Vertex,Edge> graph, Vertex start){
		if (start.consumption == -Double.MAX_VALUE) {
			double consumption = 0;
			System.out.println("Vertex " + start + ": ");
			for (Edge e : graph.edgesOf(start)) 
				if (e.a.equals(start)) {
					System.out.println("output " + e.b + ": ");
					double childConsumption = recurrentSetWaterConsumption(graph, e.b) 
							/ findStartsOfInputEdges(graph, e.b).size() 
							* (1 + e.leak);
					System.out.println("" + recurrentSetWaterConsumption(graph, e.b) 
									 + " " + findStartsOfInputEdges(graph, e.b).size() 
									 + " " + (1 + e.leak)
									 + " ---> " + childConsumption);
					consumption += childConsumption;
				}
			System.out.println("total: " + consumption);
			start.consumption = consumption;
		}
		return start.consumption;		
	}
}
