package bachelorThesisPlayground;

import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.jgrapht.graph.SimpleWeightedGraph;

public class Utils {

	public static List<SimpleWeightedGraph<Vertex,Edge>> coloredGraphSplit(SimpleWeightedGraph<Vertex,Edge> graph) {
		HashMap<Color, SimpleWeightedGraph<Vertex,Edge>> result = new HashMap<>();			
		for (Edge e: graph.edgeSet()) {
			if (e.a.r==e.b.r&&e.a.g==e.b.g&&e.a.b==e.b.b&&e.a.r!=-1) {
				Color key = new Color(e.a.r, e.a.g, e.a.b);
				if (!result.containsKey(key)) {
					result.put(key, new SimpleWeightedGraph<Vertex,Edge>(Edge.class));
				}
				result.get(key).addVertex(e.a);
				result.get(key).addVertex(e.b);
				result.get(key).addEdge(e.a, e.b, e);				
			}
		}
		return new ArrayList<>(result.values());
	}
	
	public static class MutableVertexPair{
	    Vertex first;
	    Vertex second;
	    public MutableVertexPair(Vertex a, Vertex b){
	        this.first = a;
	        this.second = b;
	    }		
	}
}
