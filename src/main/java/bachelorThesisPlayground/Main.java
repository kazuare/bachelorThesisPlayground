package bachelorThesisPlayground;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
		
		int targetPumpStation = 185933001;
		
		SimpleWeightedGraph<Vertex,Edge> component = Utils.graphSplit(GraphBuilding.getColoredGraph()).stream().filter(
				e->e.vertexSet().stream().filter(v->v.pumpStationExit).findAny().get().oldId == targetPumpStation
		).findAny().get();
		
		WaterFlow.setFlowDirections(component);

		//DrawingUtils.saveGraph("yo_labels", graph, null, null, true, false);	
		//manual isolated zones
/*		IsolatedZone iz = new IsolatedZone(component, 
				Arrays.asList(findEdge(component, 106802401, 106802301)),
				Arrays.asList(findEdge(component, 106914101, 106887001)));
		
		IsolatedZone iz2 = new IsolatedZone(component, 
				Arrays.asList(
					findEdge(component, 106613101, 106811801),
					findEdge(component, 106690601, 106718301)					
				),
				Arrays.asList(findEdge(component, 106873601, 106916701)));
*/		
		ConsumptionCalculator.cleanNonLeafVertexesWithPlacecodes(component);
		
		
		//BackloggedCycleResolution.detectCycles(component);
		//Vertex focusPoint = breakSomething(component);
		//manual leaks
		
		//findEdge(component, 106806201, 106798301).leak = 0.5;
		//findEdge(component, 106878301, 106914101).leak = 0.5;
		
		List<Vertex> brokenPoints = new ArrayList<>();
		for (int i = 0; i < 3; i++){
			brokenPoints.add(reasonablyBreakSomething(component));
		}
		
		ConsumptionCalculator.recurrentSetWaterConsumption(component);	
		//DrawingUtils.drawGraphWithAttentionPoint(component, findVertex(component, 106878301));
		
/*		
		System.out.println(iz.zoneCheck());
		System.out.println(iz2.zoneCheck());
*/		
		setPossiblePressureTransferCandidates(component);
		
		setCanBeMagical(component);

		
		List<IsolatedZone> zones = new ArrayList<>();
		for (Set<Vertex> miniComponent : 
				getPointsOfIsolatedComponents(getComponentsIzolatedByPossiblyMagicalEdges(component))) {
			unmagicUnusableMagicEdges(component, miniComponent);
			
			if(miniComponent.stream().filter(e->e.oldId == targetPumpStation).findAny().isPresent())
				continue;				
				
			zones.add(new IsolatedZone(
					component, 
					getEntriesOfComponent(component, miniComponent),
					getExitsOfComponent(component, miniComponent)
					));
		}
		
		for (IsolatedZone zone : zones) {
			String result = zone.zoneCheck();
			if (!"OK".equals(result))
				System.out.println(result);
		}
			
		int shouldSeeNoMoreXErrors = 0;
		for (Edge e : component.edgeSet()) {
			if (e.leak > 0) 
				if (e.a.consumption == 0 && e.b.consumption == 0) {
					System.out.println("This leak cannot be seen");
				} else {
					shouldSeeNoMoreXErrors++;					
				}				
		} 
		
		System.out.println("Should see no more than " + shouldSeeNoMoreXErrors + " errors");
		
		for (int i = 0; i < 3; i++){
			DrawingUtils.drawGraphWithAttentionPoint(component, brokenPoints.get(i));			
		}
		
		DrawingUtils.saveGraph("yo", component, null, null, false, true);	
		
		
	}
	
	public static SimpleWeightedGraph<Vertex,Edge> getComponentsIzolatedByPossiblyMagicalEdges(SimpleWeightedGraph<Vertex,Edge> graph) {
		SimpleWeightedGraph<Vertex,Edge> components = new SimpleWeightedGraph<Vertex,Edge>(Edge.class);
		for (Vertex v : graph.vertexSet()) {
			components.addVertex(v);
		}
		
		for (Edge e : graph.edgeSet()) {
			if (!e.canBeMagical) {
				components.addEdge(e.a, e.b, e);
			}
		}
			
		return components;
	}
	
	public static void unmagicUnusableMagicEdges(SimpleWeightedGraph<Vertex,Edge> graph, Set<Vertex> component) {
		for (Edge e : graph.edgeSet()) 
			if (e.canBeMagical && component.contains(e.a) && component.contains(e.b)) 
				e.canBeMagical = false;	
	}
	
	public static List<Edge> getEntriesOfComponent(SimpleWeightedGraph<Vertex,Edge> graph, Set<Vertex> component) {
		List<Edge> edges = new ArrayList<>();
		for (Edge e : graph.edgeSet()) {
			if (e.canBeMagical && component.contains(e.b)) {
				if (component.contains(e.a))
					throw new RuntimeException("zone contains both ends of magical edge");			
				edges.add(e);
			}
		}	
		return edges;
	}

	public static List<Edge> getExitsOfComponent(SimpleWeightedGraph<Vertex,Edge> graph, Set<Vertex> component) {
		List<Edge> edges = new ArrayList<>();
		for (Edge e : graph.edgeSet()) {
			if (e.canBeMagical && component.contains(e.a)) {
				if (component.contains(e.b))
					throw new RuntimeException("zone contains both ends of magical edge");			
				edges.add(e);
			}
		}	
		return edges;
	}
	
	public static List<Set<Vertex>> getPointsOfIsolatedComponents(SimpleWeightedGraph<Vertex,Edge> components) {
		List<Set<Vertex>> pointsOfComponents = new ArrayList<>();
		for (Vertex v : components.vertexSet()) {
			Set<Vertex> neighbours = getNeighbourPoints(components, v);
			if (!pointsOfComponents.contains(neighbours))
				pointsOfComponents.add(neighbours);
		}
		return pointsOfComponents;
	}
	
	public static Set<Vertex> getNeighbourPoints(SimpleWeightedGraph<Vertex,Edge> components, Vertex start){
		List<Vertex> vertices = new ArrayList<>();
		vertices.add(start);
		
		for (int i = 0; i < vertices.size(); i++) 
			for (Vertex v : IsolatedZone.getNeighbours(components, vertices.get(i))) 
				if (!vertices.contains(v)) 
					vertices.add(v);
		
		return new HashSet<>(vertices);
	}
	
	public static void getRidOfSequencesOfMagicalEdges(SimpleWeightedGraph<Vertex,Edge> graph) {
		//твой код тут
		//цепочки можно находить простым обходом по направлению течения воды
		//проходишь по всей цепочке, убираешь все, кроме первого и последнего
		//или может вообще нельзя убирать волшебство из цепочек ребер?
		//это гипотетически можешь мешать оптимизировать размер зон в будущем
	}
	
	public static void setCanBeMagical(SimpleWeightedGraph<Vertex,Edge> graph) {
		Predicate<Vertex> canBeEndOfMagicalEdge = (Vertex p)-> p.pressureTransferCandidate || p.type.toLowerCase().contains("колодец");
		
		for (Edge e : graph.edgeSet())
			if (canBeEndOfMagicalEdge.test(e.a) && canBeEndOfMagicalEdge.test(e.b)) {
				e.canBeMagical = true;
			}
	}
	
	public static void setPossiblePressureTransferCandidates(SimpleWeightedGraph<Vertex,Edge> graph) {
		boolean changed = false;
		int iteration = 0;
		
		Predicate<Vertex> canTransferPressure = (Vertex p)-> p.pressureTransferCandidate || p.placecode > 0;
		//loop for advanced pressure transfer
		do {
			changed = false;
			for (Vertex v : graph.vertexSet()) {
				if(graph.edgesOf(v).size() == 2 && !canTransferPressure.test(v))
					for (Vertex p: getOutputNeighbours(graph, v)) {
						if (canTransferPressure.test(p)) {
							v.pressureTransferCandidate = true;
							changed = true;
							break;
						}
					}
			}
		} while(changed);
		
		List<Vertex> toMark = new ArrayList<>();
		for (Vertex v : graph.vertexSet()) {
			for (Vertex p: getOutputNeighbours(graph, v)) {
				if (canTransferPressure.test(p)) {
					toMark.add(v);
					break;
				}
			}	
		}
		
		for (Vertex v : toMark) 
			v.pressureTransferCandidate = true;
	}
	
	public static List<Vertex> getOutputNeighbours(SimpleWeightedGraph<Vertex,Edge> graph, Vertex v){
		return graph.edgesOf(v)
					.stream()
					.map(e->e.b)
					.filter(e->!e.equals(v))
					.collect(Collectors.toList());
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
		
		edges.get(0).leak = Math.random()*0.8 + 0.2;
		
		System.out.println("Breaking down edge " + edges.get(0));
		System.out.println("Consumptions of ends are equal to : " + edges.get(0).a.consumption +  ", " + edges.get(0).b.consumption);
		
		return pointToBreak;
	}
	
	public static Vertex reasonablyBreakSomething(SimpleWeightedGraph<Vertex,Edge> graph) {
		List<Vertex> points = new ArrayList<>(graph.vertexSet());
		Collections.shuffle(points);
				
		Vertex pointToBreak = points.get(0);		
		List<Edge> edges = new ArrayList<>(graph.edgesOf(pointToBreak));
		Collections.shuffle(edges);
		
		Edge e = edges.get(0);
		
		if (e.length < 20) {
			return reasonablyBreakSomething(graph);
		}
		
		e.leak = Math.random()*0.8 + 0.2;
		
		System.out.println("Breaking down edge " + e);
		System.out.println("Consumptions of ends are equal to : " + e.a.consumption +  ", " + e.b.consumption);
		
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

