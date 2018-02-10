package bachelorThesisPlayground;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.jgrapht.graph.SimpleWeightedGraph;

import bachelorThesisPlayground.water.flow.ConsumptionCalculator;

public class IsolatedZone {/*
	public List<Vertex> consumers = new ArrayList<>();
	public List<Vertex> vertices = new ArrayList<>();
	SimpleWeightedGraph<Vertex,Edge> hostGraph;
	public List<Edge> entries;
	public List<Edge> exits;
	
	//contract: e1 is incoming edge, e2 is outcoming, so you can travel from e1.b->e1.a through the zone
	public IsolatedZone(SimpleWeightedGraph<Vertex,Edge> graph, List<Edge> entries, List<Edge> exits) {
		hostGraph = graph;
		registerVertex(e1.b);
		for (int i = 0; i < vertices.size(); i++) {
			for (Vertex v : getNeighbours(graph, vertices.get(i))) {
				System.out.println("traversing " + v);
				if (v.equals(e1.a) || v.equals(e2.b)) {
					//traversion algo came from the backdoor and struck us, zone is not isolated
					if(i != 0 && v.equals(e1.a))
						throw new RuntimeException("not isolated");
				} else if ( !(v.equals(e2.a) || vertices.contains(v)) ) {
					registerVertex(v);
				}
			}
		}
		registerVertex(e2.a);
		
		for (Edge e: exits) {
			e.magical = true;
		}

		for (Edge e: entries) {
			e.magical = true;
		}
		
		e1.magical = true;
		e2.magical = true;
		entry = e1;
		exit = e2;
	}
	
	public void registerVertex(Vertex v){
		System.out.println("registering " + v);
		vertices.add(v);
		if (v.placecode != -1)
			consumers.add(v);
	}
	
	public static List<Vertex> getNeighbours(SimpleWeightedGraph<Vertex,Edge> graph, Vertex v){
		return graph.edgesOf(v)
					.stream()
					.flatMap(e->Stream.of(e.a, e.b))
					.filter(e->!e.equals(v))
					.collect(Collectors.toList());
	}	
	
	public double getInnerConsumption(){
		return consumers.stream()
				.mapToDouble(e->e.consumption)
				.sum();
	}

	public double getEntryFlow(){
		double sum = 0;
		for (Edge entry : entries)
			sum +=  entry.b.consumption
				/ ConsumptionCalculator.findStartsOfInputEdges(hostGraph, entry.b).size() 
				* (1 + entry.leak);
		return sum;
	}

	public double getExitFlow(){
		double sum = 0;
		for (Edge exit : exits)
			sum += exit.b.consumption
				/ ConsumptionCalculator.findStartsOfInputEdges(hostGraph, exit.b).size() 
				* (1 + exit.leak);
		return sum;
	}
	
	public String zoneCheck(){
		if (getEntryFlow() - getInnerConsumption() - getExitFlow() == 0)
			return "OK";
		else {
			for (Vertex v : vertices) {
				v.colored = true;
				v.r = 1;
				v.b = 0; 
				v.g = 0;
			}
			return "" + getEntryFlow() + " " + getInnerConsumption() + " " + getExitFlow();
		}
	}*/
}