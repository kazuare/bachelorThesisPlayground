package bachelorThesisPlayground.deprecated;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.jgrapht.alg.interfaces.SpanningTreeAlgorithm.SpanningTree;
import org.jgrapht.alg.spanning.PrimMinimumSpanningTree;
import org.jgrapht.graph.SimpleWeightedGraph;

import bachelorThesisPlayground.Edge;
import bachelorThesisPlayground.Vertex;

public class DeprecatedSpanningTrees {
	public static void setSensorsWithPreferredPointCountWithSpanningTrees(List<SimpleWeightedGraph<Vertex,Edge>> components, int preferredPointCount, int itCount) {
		double step = 10000;
		double currentPosition = step;
		int pointCount = 0;
		int i;
		for (i = 0; i < itCount; i++) {
			for (SimpleWeightedGraph<Vertex,Edge> component : components)
				for (Vertex p : component.vertexSet())
					p.sensorPlaced = false;
			
			pointCount = setSensorsWithMinimumPath(components, currentPosition);
			System.out.println("got " + pointCount + "points ("+ preferredPointCount +" preferred) with " + i + " iterations and length " + currentPosition);	
			if (pointCount != preferredPointCount) {
				step /= 2;
				currentPosition += pointCount>preferredPointCount? step : -step;
			} else break;
		}	
		
	} 
	
	public static int setSensorsWithMinimumPath(List<SimpleWeightedGraph<Vertex,Edge>> components, double minimumPath) {
		int markedCounter = 0;
		for (SimpleWeightedGraph<Vertex,Edge> component : components) {
			Set<Vertex> visited = new HashSet<>(); 
			Vertex pumpStationExit = component.vertexSet()
					.stream()
					.filter(p->p.pumpStationExit)
					.findFirst()
					.get();
			dfsWithMarking(component, pumpStationExit, visited, minimumPath, minimumPath);
			
			for (Vertex p : component.vertexSet())
				if (p.sensorPlaced)
					markedCounter++;
		}	
		return markedCounter;
	} 

	public static List<SimpleWeightedGraph<Vertex,Edge>> getSpanningTrees(List<SimpleWeightedGraph<Vertex,Edge>> components) {
		List<SimpleWeightedGraph<Vertex,Edge>> result = new ArrayList<>();
		for (SimpleWeightedGraph<Vertex,Edge> component : components) {
			SimpleWeightedGraph<Vertex,Edge> tree = new SimpleWeightedGraph<Vertex,Edge>(Edge.class);
			
			Vertex pumpStationExit = component.vertexSet().stream().filter(x->x.pumpStationExit).findFirst().get();
			
			tree.addVertex(pumpStationExit);
			
			List<Edge> queue = new ArrayList<>();
			
			while (true) {
				queue.clear();
				for (Edge e : component.edgeSet()) 
					if (
						tree.containsVertex(e.a)&&!tree.containsVertex(e.b)
							||
						!tree.containsVertex(e.a)&&tree.containsVertex(e.b)
					)
						queue.add(e);
				
				if(queue.isEmpty())
					break;
				
				Collections.sort(queue, (a,b)->edgeComparatorForSpanningTreeAlg(tree,pumpStationExit,a,b));
				//Collections.sort(queue, (a,b)->Double.compare(a.length, b.length));
				
				tree.addVertex(queue.get(0).a);
				tree.addVertex(queue.get(0).b);
				tree.addEdge(queue.get(0).a, queue.get(0).b, queue.get(0));
			}
			result.add(tree);
			System.out.println("spanning tree check: " + (checkSpanningTree(component, tree) ? "probably ok" : "fail"));
		}
		return result;
	}
	

	public static void dfsWithMarking(SimpleWeightedGraph<Vertex,Edge> graph, Vertex currentPoint, Set<Vertex> visited, double minimumPath, double remainingPath) {
		visited.add(currentPoint);
	    //System.out.println("Visiting vertex " + currentPoint);
		if (graph.edgesOf(currentPoint).size() > 1)
		    if (remainingPath <= 0) {
		    	remainingPath = minimumPath;
		    	currentPoint.sensorPlaced = true;
		    } else {	
			    for (Edge e : graph.edgesOf(currentPoint)) {
			    	Vertex p = currentPoint.equals(e.a) ? e.b : e.a;
			    	if (!visited.contains(p) && remainingPath-e.length <= 0){ 
				    	remainingPath = minimumPath;
				    	currentPoint.sensorPlaced = true;
				    	break;
			    	}
			    }	    	
		    }	    
	    
	    for (Edge e : graph.edgesOf(currentPoint)) {
	    	Vertex p = currentPoint.equals(e.a) ? e.b : e.a;
	    	if (!visited.contains(p)){ 
	    		dfsWithMarking(graph, p, visited, minimumPath,remainingPath-e.length);
	    	}
	    }
	}

	
	public static boolean checkSpanningTree(SimpleWeightedGraph<Vertex,Edge> graph, SimpleWeightedGraph<Vertex,Edge> tree){
		if (!graph.vertexSet().containsAll(tree.vertexSet()) || !tree.vertexSet().containsAll(graph.vertexSet()))
			return false;
		
		
		PrimMinimumSpanningTree<Vertex,Edge>  alg = new PrimMinimumSpanningTree<Vertex,Edge> (tree);
		
		SpanningTree<Edge> algTree = alg.getSpanningTree();
		
		if (!algTree.getEdges().containsAll(tree.edgeSet()) || !tree.edgeSet().containsAll(algTree.getEdges()))
			return false;
		
		return true;			
	}
	
	public static int edgeComparatorForSpanningTreeAlg(SimpleWeightedGraph<Vertex,Edge> graph, Vertex pumpStationExit, Edge a, Edge b){
		double distanceDeltaWeight = 1;
		double diameterWeight = 0.3;
		double consumptionWeight = 1;
		
		double left = 0;
		left += getDistanceDelta(graph, a, pumpStationExit) * distanceDeltaWeight;
		left += a.diameter * diameterWeight;
		left += Math.max(Math.max(a.a.consumption, a.b.consumption), 0) * consumptionWeight;
		
		double right = 0;
		right += getDistanceDelta(graph, b, pumpStationExit) * distanceDeltaWeight;
		right += b.diameter * diameterWeight;
		right += Math.max(Math.max(b.a.consumption, b.b.consumption), 0) * consumptionWeight;
		
		return -Double.compare(left, right);
	}

	
	public static double getDistanceDelta(SimpleWeightedGraph<Vertex,Edge> graph, Edge a, Vertex pumpStationExit) {
		if (graph.containsVertex(a.a)) {
			return pumpStationExit.distance(a.b) - pumpStationExit.distance(a.a);
		} else {
			return pumpStationExit.distance(a.a) - pumpStationExit.distance(a.b);			
		}
	}

}
