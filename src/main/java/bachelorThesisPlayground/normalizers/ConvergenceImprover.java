package bachelorThesisPlayground.normalizers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.educationalProject.surfacePathfinder.visualization.Point;
import org.jgrapht.alg.ConnectivityInspector;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleWeightedGraph;

import bachelorThesisPlayground.Edge;

public class ConvergenceImprover {
	
	public static Map<Integer, Edge> removeSmallConnectedComponents(Map<Integer, Edge> edges, boolean ignoreFixedCoordsCondition, int cuttingSizeThreshold) {
		HashMap<Integer, Point> points = new HashMap<>();
		for (Edge e : edges.values()) {
			if(!points.containsKey(e.a.id))
				points.put(e.a.id, new Point(e.a.x, e.a.y, 0, e.a.id));
			if(!points.containsKey(e.b.id))
				points.put(e.b.id, new Point(e.b.x, e.b.y, 0, e.b.id));
		}
		
		SimpleWeightedGraph<Point,DefaultWeightedEdge> graph = new SimpleWeightedGraph<Point,DefaultWeightedEdge>(DefaultWeightedEdge.class);
		
		for(Point p : points.values())
			graph.addVertex(p);
		
		for(Edge e : edges.values())
			if(!graph.containsEdge(points.get(e.a.id), points.get(e.b.id)) && !points.get(e.a.id).equals(points.get(e.b.id))){
				graph.addEdge(points.get(e.a.id), points.get(e.b.id));
			}
		ConnectivityInspector<Point,DefaultWeightedEdge> inspector = new ConnectivityInspector<Point,DefaultWeightedEdge>(graph);
		List<Set<Point>> connectedComponents = inspector.connectedSets();
		Set<Set<Point>> acceptedComponents = connectedComponents.stream()
				.filter(set->set.size()>cuttingSizeThreshold)
				.filter(set->hasFixedCoords(set) || ignoreFixedCoordsCondition)
				.collect(Collectors.toSet());
		Set<Integer> acceptedPoints = new HashSet<>();
		for (Set<Point> acceptedComponent : acceptedComponents) {
			acceptedPoints.addAll(acceptedComponent.stream().map(p->p.id).collect(Collectors.toSet()));
		}		
		/*
		Collections.sort(connectedComponents, (a,b)->Integer.compare(a.size(),b.size()));

		for(Set<Point> set : connectedComponents){
			System.out.println("size: " + set.size() + ", hasFixed: " + hasFixedCoords(set));
		}
		*/
		int count = 0;
		Iterator<Integer> it = edges.keySet().iterator();
		while (it.hasNext()) {
			Integer id = it.next();
			Edge e = edges.get(id);
			if ( !(acceptedPoints.contains(e.a.id) && acceptedPoints.contains(e.b.id)) ) {
				System.out.println("Edge " + e.id + " is removed, total removed: " + ++count);
				it.remove();
			}
		}
		return edges;
		
	}
	
	public static boolean hasFixedCoords(Set<Point> component){
		for (Point p : component) {
			if (p.x >= 0 && p.y >= 0) {
				return true;
			}
		}
		return false;
	}
	
	public static void assignCoordsOld(Map<Integer, Edge> edges){
		ArrayList<Integer> idToTier = new ArrayList<>();
		ArrayList<Double> idToX = new ArrayList<>();
		ArrayList<Double> idToY = new ArrayList<>();
		for(int i = 0; i < 35000; i++){
			idToTier.add(null);
			idToX.add(-1.0);
			idToY.add(-1.0);
		}
		for (Edge e : edges.values()) {
			if (e.a.x >= 0) {
				e.a.fixed = true;
				idToTier.set(e.a.id, 0);
				idToX.set(e.a.id, e.a.x);
				idToY.set(e.a.id, e.a.y);
			}
			if (e.b.x >= 0) {
				e.b.fixed = true;
				idToTier.set(e.b.id, 0);
				idToX.set(e.b.id, e.b.x);
				idToY.set(e.b.id, e.b.y);
			}
		}
		int currentTier = 1;
		boolean changed = false;
		while (currentTier == 1 || changed) {
			changed = false;
			for (Edge e : edges.values()) {
				if (idToTier.get(e.a.id)==null && idToTier.get(e.b.id)!=null) {
					changed = true;
					idToTier.set(e.a.id, currentTier);
					idToX.set(e.a.id, idToX.get(e.b.id));
					idToY.set(e.a.id, idToY.get(e.b.id));// + 20 - 40 * Math.random());
				}
				if (idToTier.get(e.b.id)==null && idToTier.get(e.a.id)!=null) {
					changed = true;
					idToTier.set(e.b.id, currentTier);
					idToX.set(e.b.id, idToX.get(e.a.id));
					idToY.set(e.b.id, idToX.get(e.a.id));
				}
			}
			currentTier++;
		}
		
		for (Edge e : edges.values()) {
			if (!e.a.fixed) {
				e.a.x = idToX.get(e.a.id);
				e.a.y = idToY.get(e.a.id);
			}	
			if (!e.b.fixed) {
				e.b.x = idToX.get(e.b.id);
				e.b.y = idToY.get(e.b.id);
			}	
		}
		
	}
}
