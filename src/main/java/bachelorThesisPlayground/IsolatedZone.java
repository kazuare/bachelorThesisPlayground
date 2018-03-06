package bachelorThesisPlayground;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.jgrapht.graph.SimpleWeightedGraph;

import bachelorThesisPlayground.water.flow.ConsumptionCalculator;

public class IsolatedZone {
	public List<Vertex> consumers = new ArrayList<>();
	public List<Vertex> vertices = new ArrayList<>();
	SimpleWeightedGraph<Vertex,Edge> hostGraph;
	public List<Edge> entries;
	public List<Edge> exits;
	
	//contract: entries are incoming edges, exits are outcoming, 
	//so you can travel from entry.b->exit.a through the zone
	//(all entries and exits can be reached from each other)
	public IsolatedZone(SimpleWeightedGraph<Vertex,Edge> graph, List<Edge> entries, List<Edge> exits) {
		hostGraph = graph;
		
		List<Vertex> startsOfEntries = entries.stream().map(e->e.a).collect(Collectors.toList());
		List<Vertex> endsOfEntries = entries.stream().map(e->e.b).collect(Collectors.toList());		
		List<Vertex> startsOfExits = exits.stream().map(e->e.a).collect(Collectors.toList());
		List<Vertex> endsOfExits = exits.stream().map(e->e.b).collect(Collectors.toList());

		for (Vertex v : endsOfEntries) {
			registerVertex(v);
		}
		
		for (Vertex v : startsOfExits) {
			registerVertex(v);
		}
		
		for (int i = 0; i < vertices.size(); i++) {
			for (Vertex v : getNeighbours(graph, vertices.get(i))) {
				System.out.println("traversing " + v);
				if (startsOfEntries.contains(v) || endsOfExits.contains(v)) {
					System.out.println("startsOfEntries.contains(v) || endsOfExits.contains(v)");
				} else if ( !(startsOfExits.contains(v) || endsOfEntries.contains(v) || vertices.contains(v)) ) {
					registerVertex(v);
				}
			}
		}
		
		//add all ends of entries and starts of exits if they are not present
		List<Vertex> a = new ArrayList<>();
		for (Vertex v : endsOfEntries)
			if (!vertices.contains(v))
				registerVertex(v);
		

		for (Vertex v : startsOfExits)
			if (!vertices.contains(v))
				registerVertex(v);
		
		for (Edge e: exits) {
			e.magical = true;
		}

		for (Edge e: entries) {
			e.magical = true;
		}
		this.entries = new ArrayList<>(entries);
		this.exits = new ArrayList<>(exits);
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
		if (Math.abs(getEntryFlow() - getInnerConsumption() - getExitFlow()) < 0.000001)
			return "OK";
		else {
			for (Vertex v : vertices) {
				v.colored = true;
				v.r = 1;
				v.b = 0; 
				v.g = 0;
			}
			return "Leak event: " + getEntryFlow() + " " + getInnerConsumption() + " " + getExitFlow();
		}
	}
}




//has bug

//contract: entries are incoming edges, exits are outcoming, 
	//so you can travel from entry.b->exit.a through the zone
	//(all entries and exits can be reached from each other)
	/*
	public IsolatedZone(SimpleWeightedGraph<Vertex,Edge> graph, List<Edge> entries, List<Edge> exits) {
		hostGraph = graph;
		
		List<Vertex> startsOfEntries = entries.stream().map(e->e.a).collect(Collectors.toList());
		List<Vertex> endsOfEntries = entries.stream().map(e->e.b).collect(Collectors.toList());		
		List<Vertex> startsOfExits = exits.stream().map(e->e.a).collect(Collectors.toList());
		List<Vertex> endsOfExits = exits.stream().map(e->e.b).collect(Collectors.toList());

		registerVertex(endsOfEntries.get(0));
		
		for (int i = 0; i < vertices.size(); i++) {
			for (Vertex v : getNeighbours(graph, vertices.get(i))) {
				System.out.println("traversing " + v);
				if (startsOfEntries.contains(v) || endsOfExits.contains(v)) {
					//traversion algo came from the backdoor and struck us, zone is not isolated
					//but this is expected on the first iteration
					if(i != 0)
						throw new RuntimeException("not isolated");
				} else if ( !(startsOfExits.contains(v) || endsOfEntries.contains(v) || vertices.contains(v)) ) {
					registerVertex(v);
				}
			}
		}
		
		//add all ends of entries and starts of exits if they are not present
		List<Vertex> a = new ArrayList<>();
		for (Vertex v : endsOfEntries)
			if (!vertices.contains(v))
				registerVertex(v);
		

		for (Vertex v : startsOfExits)
			if (!vertices.contains(v))
				registerVertex(v);
		
		for (Edge e: exits) {
			e.magical = true;
		}

		for (Edge e: entries) {
			e.magical = true;
		}
		this.entries = new ArrayList<>(entries);
		this.exits = new ArrayList<>(exits);
	}
	*/