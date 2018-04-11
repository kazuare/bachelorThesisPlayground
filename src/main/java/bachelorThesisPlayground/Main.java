package bachelorThesisPlayground;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import org.educationalProject.surfacePathfinder.visualization.DrawingUtils;
import org.jgrapht.graph.SimpleWeightedGraph;

import bachelorThesisPlayground.deprecated.BackloggedCycleResolution;
import bachelorThesisPlayground.graphBuilding.GraphBuilding;
import bachelorThesisPlayground.readers.DBReader;
import bachelorThesisPlayground.water.flow.ConsumptionCalculator;
import bachelorThesisPlayground.water.flow.WaterFlow;
import google.map.api.MapsApi;

public class Main {
	public static boolean cycleFlow = false;
	
	public static DBReader dbReader = new DBReader();
	@SuppressWarnings("serial")
	public static void main(String[] args) {		
		//writeJSONGraphForD3();
		//we do layout in d3 and get FINAL_ALL_POINTS.json
		dbReader.init();
		
		int targetPumpStation = 185933001;
		
		SimpleWeightedGraph<Vertex,Edge> component = Utils.graphSplit(GraphBuilding.getColoredGraph()).stream().filter(
				e->e.vertexSet().stream().filter(v->v.pumpStationExit).findAny().get().oldId == targetPumpStation
		).findAny().get();
		
		WaterFlow.setFlowDirections(component);

		if (cycleFlow) {
			BackloggedCycleResolution.detectCycles(component);
			DrawingUtils.saveGraph("yo", component, null, null, false, true);	
			System.exit(0);
		}
		
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
	
		//Vertex pointToBreak = component.vertexSet().stream().filter(e->e.oldId==106894001).findFirst().get();		
		//new ArrayList<>(component.edgesOf(pointToBreak)).get(0).leak = Math.random()*0.8 + 0.2;
		
		List<Vertex> brokenPoints = new ArrayList<>();
		for (int i = 0; i < 3; i++){
			//brokenPoints.add(reasonablyBreakSomething(component));
		}
		
		ConsumptionCalculator.recurrentSetWaterConsumption(component);	
		
		setCanBeMagical(component);

		List<Set<Vertex>> miniComponents = getPointsOfIsolatedComponents(getComponentsIzolatedByPossiblyMagicalEdges(component));
		
		for (Set<Vertex> miniComponent : miniComponents) 
			unmagicUnusableMagicEdges(component, miniComponent);
		
		miniComponents = getPointsOfIsolatedComponents(getComponentsIzolatedByPossiblyMagicalEdges(component));
				
		//план:
		// находим зону с наименьшим весом
		// последовательно убираем у волшебные ребра (одно за раз), считаем метрику по графу
		// находим ребро, при уборке которого метрика по всему графу - лучшая
		// убираем это ребро
		// метрика - сумма квадратов
		 		
		int magicalEdgeCount = component.edgeSet()
				.stream()
				.mapToInt(e->e.canBeMagical ? 1 : 0)
				.sum();
		
		System.out.println("Maximum magical edge count: " + magicalEdgeCount);
		System.out.println("Initial metric value is " + getSquareSum(component, miniComponents));
		System.out.println("Initial mean weight is " + getAverageWeight(component, miniComponents));
		System.out.println("Initial minimal weight is " + getWeightOfSmallestComponent(component, miniComponents));
		
		Set<Vertex> smallestComponent = miniComponents.stream()
				.min((a,b)->
					Double.compare(getMiniComponentWeight(component, a), getMiniComponentWeight(component, b)))
				.get();
		
		Set<Edge> magicalEdgesOfSmallestComponent = new HashSet<Edge>();
		magicalEdgesOfSmallestComponent.addAll(getEntriesOfComponent(component, smallestComponent));
		magicalEdgesOfSmallestComponent.addAll(getExitsOfComponent(component, smallestComponent));
		
		while (magicalEdgeCount > 350) {
			
			Edge bestEdgeForDeletion = null;
			double bestMetricValue = Double.MAX_VALUE;
			
			for (Edge edge : magicalEdgesOfSmallestComponent) {
				SimpleWeightedGraph<Vertex,Edge> componentCopy = new SimpleWeightedGraph<Vertex,Edge>(Edge.class);
				component.vertexSet().forEach(v->componentCopy.addVertex(v));
				for (Edge e : component.edgeSet()) {				
					Edge copy = new Edge(e.id, e.a, e.b);
					copy.canBeMagical = e.equals(edge) ? false : e.canBeMagical;
					copy.length = e.length;
					copy.diameter = e.diameter;
					componentCopy.addEdge(e.a, e.b, copy);
				}
				
				List<Set<Vertex>> componentsWithOneUnmagickedEdge = getPointsOfIsolatedComponents(getComponentsIzolatedByPossiblyMagicalEdges(componentCopy));
				
				for (Set<Vertex> miniComponent : componentsWithOneUnmagickedEdge) 
					unmagicUnusableMagicEdges(componentCopy, miniComponent);
				
				componentsWithOneUnmagickedEdge = getPointsOfIsolatedComponents(getComponentsIzolatedByPossiblyMagicalEdges(componentCopy));
				
				magicalEdgeCount = componentCopy.edgeSet()
						.stream()
						.mapToInt(e->e.canBeMagical ? 1 : 0)
						.sum();
				
				double metricValue = getSquareSum(componentCopy, componentsWithOneUnmagickedEdge);				
				if (bestMetricValue > metricValue) {
					bestMetricValue = metricValue;
					bestEdgeForDeletion = edge;
				}			
			}
			
			final Edge best = bestEdgeForDeletion;		
			component.edgeSet().stream().filter(a->a.equals(best)).findFirst().get().canBeMagical = false;
			
			for (Set<Vertex> miniComponent : miniComponents) 
				unmagicUnusableMagicEdges(component, miniComponent);
			
			miniComponents = getPointsOfIsolatedComponents(getComponentsIzolatedByPossiblyMagicalEdges(component));
			
			magicalEdgeCount = component.edgeSet()
					.stream()
					.mapToInt(e->e.canBeMagical ? 1 : 0)
					.sum();
			
			System.out.println("Unmagicked " + best.id + ", " + magicalEdgeCount + " edges left");
			double metricValue = getSquareSum(component, miniComponents);
			System.out.println("Metric value is " + metricValue);
			System.out.println("Mean weight is " + getAverageWeight(component, miniComponents));
			System.out.println("Minimal weight is " + getWeightOfSmallestComponent(component, miniComponents));
			System.out.println();
			
			smallestComponent = miniComponents.stream()
					.min((a,b)->
						Double.compare(getMiniComponentWeight(component, a), getMiniComponentWeight(component, b)))
					.get();
			
			magicalEdgesOfSmallestComponent = new HashSet<Edge>();
			magicalEdgesOfSmallestComponent.addAll(getEntriesOfComponent(component, smallestComponent));
			magicalEdgesOfSmallestComponent.addAll(getExitsOfComponent(component, smallestComponent));
			
		}
		
		List<IsolatedZone> zones = new ArrayList<>();
		for (Set<Vertex> miniComponent : miniComponents) {			
			if(miniComponent.stream().filter(e->e.oldId == targetPumpStation).findAny().isPresent())
				continue;				
				
			zones.add(new IsolatedZone(
					component, 
					getEntriesOfComponent(component, miniComponent),
					getExitsOfComponent(component, miniComponent)
					));
		}
		
		List<Vertex> affectedConsumers = new ArrayList<>();
		
		for (IsolatedZone zone : zones) {
			String result = zone.zoneCheck();
			if (!"OK".equals(result)) {
				System.out.println(result);
				affectedConsumers.addAll(zone.getConsumers());
			}
		}
		
		System.out.println("Affected: " + affectedConsumers
				.stream()
				.map(p->MapsApi.prepareForGoogleStatics(p.address))
				.collect(Collectors.toList()));
					
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
		
		for (Vertex point : brokenPoints){
			DrawingUtils.drawGraphWithAttentionPoint(component, point);			
		}
		
		DrawingUtils.saveGraph("yo", component, null, null, false, true);	
		
		try {
			MapsApi.renderAffectedConsumers(affectedConsumers);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static double getMiniComponentWeight(SimpleWeightedGraph<Vertex,Edge> graph, Set<Vertex> component) {
		return component.stream()
				.flatMap(v->graph.edgesOf(v).stream())
				.distinct()
				.mapToDouble(e->e.canBeMagical || e.magical ? 0 : e.diameter)
				.sum();
	} 

	public static double getWeightOfSmallestComponent(SimpleWeightedGraph<Vertex,Edge> graph, List<Set<Vertex>> components) {
		return components.stream()
				.mapToDouble(component->getMiniComponentWeight(graph,component))
				.min()
				.getAsDouble();
	} 
	
	public static double getSquareSum(SimpleWeightedGraph<Vertex,Edge> graph, List<Set<Vertex>> components) {
		return components.stream()
				.mapToDouble(component->{
					double a = getMiniComponentWeight(graph,component);
					return a*a;
				})
				.sum();
	} 

	public static double getAverageWeight(SimpleWeightedGraph<Vertex,Edge> graph, List<Set<Vertex>> components) {
		return components.stream()
				.mapToDouble(component->getMiniComponentWeight(graph,component))
				.average()
				.getAsDouble();
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



