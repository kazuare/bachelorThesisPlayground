package bachelorThesisPlayground.graphBuilding;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.stream.Collectors;

import org.jgrapht.UndirectedGraph;
import org.jgrapht.WeightedGraph;
import org.jgrapht.alg.util.Pair;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleGraph;
import org.jgrapht.graph.SimpleWeightedGraph;

import bachelorThesisPlayground.Edge;
import bachelorThesisPlayground.Main;
import bachelorThesisPlayground.Vertex;
import bachelorThesisPlayground.normalizers.ConvergenceImprover;
import bachelorThesisPlayground.readers.CSVGraphReader;
import bachelorThesisPlayground.readers.ExcelGraphReader;
import bachelorThesisPlayground.readers.JsonGraphReader;

public class GraphBuilding {

	public static SimpleWeightedGraph<Vertex,Edge> getColoredGraph(){				
			ArrayList<Vertex> points = getPoints();
		
			//from here we get all needed data about edges
			ArrayList<Edge> edges = JsonGraphReader.readEdges("C:\\Users\\test\\Desktop\\‰ËÔÎÓÏ\\d3full.json");

			edges = filterUnneededEdges(edges);									
				
			Collections.sort(edges, (a,b)->Integer.compare(a.id, b.id));
				
			//there are some unneeded points after cleaning, lets remove them
			Set<Vertex> usedPoints = new HashSet<Vertex>(points.size());
			for (Edge e : edges){
				usedPoints.add(e.a);
				usedPoints.add(e.b);		
			}
			points.removeIf(v->!usedPoints.contains(v));
				
			//update valve statuses
			CSVGraphReader.updateStatusOfValves(points, "C:\\Users\\test\\Desktop\\‰ËÔÎÓÏ\\ÔÓÎÓÊÂÌËˇ_Í‡ÌÓ‚.csv");
			
			points.stream()
				.filter(x->x.oldId==185933001)
				.findFirst()
				.get()
				.pumpStationExit = true;
			
			points.stream()
			.filter(x->x.oldId==104773801 || x.oldId==104771001	)
			.forEach(x->{
				x.locked = true; 
				x.canBeLocked = true;
			}); 
			
			
			List<Integer> southernPumpStationNodes = Arrays.asList(new Integer[]{
				232987201,105053001,105054101,105056301,105054801,
				105054801,233387601,233387601,104907401,104907401,
				233122001,233417501,145360101,145361101,145361301 
			});
			
			points.stream()
			.filter(x->southernPumpStationNodes.contains(x.oldId))
			.forEach(x->x.southernPumpStation = true);
			
			//now we want to normalize points coordinates (normalization can be ruined after layouting)
			double minX = points.stream().mapToDouble(p->p.x).min().getAsDouble();
			double minY = points.stream().mapToDouble(p->p.y).min().getAsDouble();
			   
			points.stream()
				.forEach(v->{
				    v.x -= minX;
				    v.y -= minY;
				});			
			    	  
			//construct graph 
			SimpleWeightedGraph<Vertex,Edge> graph = new SimpleWeightedGraph<Vertex,Edge>(Edge.class);
			
			boolean filterByCoords = false;
			//System.out.println(points.stream().max((a,b)->Double.compare(a.x, b.x)).get());
			//System.out.println(points.stream().max((a,b)->Double.compare(a.y, b.y)).get());
			
			double lowX = 6000;
			double lowY = 0;
			double highX = 11000;
			double highY = 5000;
			
			List<Integer> bannedVertices = Arrays.asList(new Integer[]{144833201,144794101,144722801});
			
			HashMap<Integer, Vertex> idToVertex = new HashMap<Integer, Vertex>(points.size());
			for (Vertex p : points){
				if (filterByCoords)
					if (p.x > highX || p.y > highY || p.x < lowX || p.y < lowY) {
						continue;						
					} else {
						p.x -= lowX;
						p.y -= lowY;
					}
				
				if( bannedVertices.contains(p.oldId)) 
					continue;
				
				idToVertex.put(p.id, p);
				graph.addVertex(p);
			}
				
			for (Edge e: edges) {
				e.a = idToVertex.get(e.a.id);
				e.b = idToVertex.get(e.b.id);
								
				if((e.a==null || e.b == null))
					continue;
				
				if (e.a != e.b) {
					graph.addEdge(e.a, e.b, e);
					//System.out.println("filling graph with edge: " + e);
				}
			}
			
			System.out.println(graph.vertexSet().size());

			double edgeDeletingThreshold = 15;//8;
			
			Pair<Edge, Vertex> edgeAndPointToDelete = findEdgeAndPointToDelete(graph, edgeDeletingThreshold);
			while (edgeAndPointToDelete != null){
				Edge e = edgeAndPointToDelete.getFirst();
				Vertex pointToSave = (edgeAndPointToDelete.getSecond() == e.a) ? e.b : e.a;
				Vertex pointToDelete = (edgeAndPointToDelete.getSecond() == e.a) ? e.a : e.b;
				
				graph.removeEdge(e);
				Set<Edge> edgesToChange = new HashSet<>(graph.edgesOf(pointToDelete));
				graph.removeVertex(pointToDelete);		
				
				for (Edge edgeToChange : edgesToChange) {
					Edge edgeToChangeCopy = new Edge(edgeToChange.id);
					edgeToChangeCopy.diameter = edgeToChange.diameter;
					edgeToChangeCopy.material = edgeToChange.material;
					
					if (edgeToChange.a == pointToDelete) {
						edgeToChangeCopy.a = pointToSave;	
						edgeToChangeCopy.b = edgeToChange.b;
					} else {
						edgeToChangeCopy.a = edgeToChange.a;
						edgeToChangeCopy.b = pointToSave;						
					}	
					
					if (!graph.containsEdge(edgeToChangeCopy.a, edgeToChangeCopy.b)) {
						edgeToChangeCopy.length = edgeToChangeCopy.a.distance(edgeToChangeCopy.b);
						if(edgeToChangeCopy.a != edgeToChangeCopy.b){
							graph.addEdge(edgeToChangeCopy.a, edgeToChangeCopy.b, edgeToChangeCopy);
						}
					}		
				}				
							
				edgeAndPointToDelete = findEdgeAndPointToDelete(graph, edgeDeletingThreshold);
			}

			System.out.println(graph.vertexSet().size());
			
			List<Vertex> pumpStations = graph.vertexSet()
					.stream()
					.filter(p->p.pumpStationExit)
					.collect(Collectors.toList());
									
			colorizeGraph(graph, pumpStations);
			
			removeComponentWithPumpExit(graph, 145146201);		
			removeComponentWithPumpExit(graph, 106781801);	
			removeComponentWithPumpExit(graph, 106768001);
			removeComponentWithPumpExit(graph, 105666301);
			removeComponentWithPumpExit(graph, 107002701);
			removeComponentWithPumpExit(graph, 106843801);
			removeComponentWithPumpExit(graph, 204129801);
			
			//this one actually can be useful 
			removeComponentWithPumpExit(graph, 106784601);
			
			for (Vertex p: graph.vertexSet()) {
				p.colored = false;
				p.r = -1;
				p.g = -1;
				p.b = -1;
			}
			
			pumpStations = graph.vertexSet()
					.stream()
					.filter(p->p.pumpStationExit)
					.collect(Collectors.toList());
			
			colorizeGraph(graph, pumpStations);
			
			Map<Color, List<Vertex>> pumpExitAssignments = new HashMap<>();			
			for (Vertex p : pumpStations) {
				Color key = new Color(p.r, p.g, p.b);
				if (!pumpExitAssignments.containsKey(key)) {
					pumpExitAssignments.put(key, new ArrayList<Vertex>());
				} 
				pumpExitAssignments.get(key).add(p);
			}
			
			for (Color key: pumpExitAssignments.keySet()) {
				List<Vertex> res = pumpExitAssignments.get(key);
				Collections.sort(res, (a,b)->Integer.compare(a.oldId, b.oldId));
				System.out.println(key + " " + res);
			}
			System.out.println("====");

			Set<UndirectedGraph<Vertex, DefaultEdge>> componentsToJoin = new HashSet<>();
			for (List<Vertex> set : pumpExitAssignments.values()) {
				for (Vertex v1: set) {
					for (Vertex v2: set) {
						if (v1 != v2 && v1.distance(v2) < 300) {
							UndirectedGraph<Vertex, DefaultEdge> graphToJoin = componentsToJoin.stream()
								.filter(g->g.containsVertex(v1)||g.containsVertex(v2))
								.findFirst()
								.orElse(null);
							if (graphToJoin == null) {
								graphToJoin = new SimpleGraph<>(DefaultEdge.class);
								componentsToJoin.add(graphToJoin);
							}
							
							if (!graphToJoin.containsEdge(v1, v2)) {
								graphToJoin.addVertex(v1);
								graphToJoin.addVertex(v2);
								graphToJoin.addEdge(v1, v2);
							}
							
						}
					}
				}
			}
			
			List<ArrayList<Vertex>> listsToJoin = componentsToJoin.stream()
					.map(g->new ArrayList<>(g.vertexSet()))
					.collect(Collectors.toList());
			
			for (ArrayList<Vertex> list : listsToJoin) {
				Vertex v1 = list.get(0);
				for(int i = 1; i < list.size(); i++) {
					Vertex v2 = list.get(i); 
					v2.pumpStationExit = false;
					v2.southernPumpStation = false;
					if (!graph.containsEdge(v1, v2)) {
						Edge e = new Edge(-i, v1.id, v2.id);
						e.a = v1;
						e.b = v2;
						e.length = v1.distance(v2);
						e.material = "UTILITY";
						e.diameter = 10;
						graph.addEdge(v1, v2, e);
					}
				}
			}
			
			
			for (Vertex p: graph.vertexSet()) {
				p.colored = false;
				p.r = -1;
				p.g = -1;
				p.b = -1;
			}
			
			colorizeGraph(graph, pumpStations);
			
			pumpStations = graph.vertexSet()
					.stream()
					.filter(p->p.pumpStationExit)
					.collect(Collectors.toList());
			
			pumpExitAssignments = new HashMap<>();			
			for (Vertex p : pumpStations) {
				Color key = new Color(p.r, p.g, p.b);
				if (!pumpExitAssignments.containsKey(key)) {
					pumpExitAssignments.put(key, new ArrayList<Vertex>());
				} 
				pumpExitAssignments.get(key).add(p);
			}
			
			for (Color key: pumpExitAssignments.keySet()) {
				List<Vertex> res = pumpExitAssignments.get(key);
				Collections.sort(res, (a,b)->Integer.compare(a.oldId, b.oldId));
				System.out.println(key + " " + res);
			}
			return graph;
					
	}
	

	public static void removeComponentWithPumpExit(SimpleWeightedGraph<Vertex,Edge> graph, int pumpExitId){
		Vertex pumpExitToDelete= graph.vertexSet().stream()
				.filter(x->x.oldId==pumpExitId)
				.findFirst()
				.get();
			Color componentToDelete = new Color(pumpExitToDelete.r, pumpExitToDelete.g, pumpExitToDelete.b);
			
			boolean modified = false;
			do {
				modified = false;
				
				Vertex toDelete = null;
				
				for (Vertex p : graph.vertexSet()) {
					boolean cannotBeDeleted = p.pumpStationEntry || p.pumpStationExit || p.betweenSectorBlock || p.southernPumpStation || p.locked;	
					if(!p.colored && !cannotBeDeleted || p.r==pumpExitToDelete.r && p.g==pumpExitToDelete.g && p.b==pumpExitToDelete.b) {
						toDelete = p;
						break;
					}
				}
				
				if (toDelete != null) {
					modified = true;
					graph.removeVertex(toDelete);
				}
			} while (modified);
			
			do {
				modified = false;
				
				Vertex toDelete = null;
				
				for (Vertex p : graph.vertexSet()) {	
					if(graph.edgesOf(p).size() == 0) {
						toDelete = p;
						break;
					}
				}
				
				if (toDelete != null) {
					modified = true;
					graph.removeVertex(toDelete);
				}
			} while (modified);
	}
	
	public static List<SimpleWeightedGraph<Vertex, Edge>> decolorizeComponents(List<SimpleWeightedGraph<Vertex, Edge>> components) {
		for (UndirectedGraph<Vertex, Edge> component : components)
			for (Vertex v : component.vertexSet()) {
				v.r = -1;
				v.b = -1; 
				v.g = -1;
				v.colored = false;
			}
		return components;
	}
	
	public static void colorizeGraph(UndirectedGraph<Vertex, Edge> graph, List<Vertex> pumpStations) {
		for (int i = 0; i < pumpStations.size(); i++) {			
			Queue<Vertex> queue = new LinkedList<Vertex>();
			Set<Vertex> visited = new HashSet<>();
			//Vertex root = graph.vertexSet().stream().filter(p->p.oldId==106781901).findFirst().get();
			Vertex root = pumpStations.get(i);
			queue.add(root);
			visited.add(root);
			colorize(root, i);
			while(!queue.isEmpty()) {
				Vertex node = queue.remove();
				Iterator<Edge> it = graph.edgesOf(node).iterator();
				while (it.hasNext()) {
					Edge e = it.next();
					Vertex child = e.a.equals(node)?e.b:e.a;
					if (!visited.contains(child) && !child.locked && !child.betweenSectorBlock) {
						queue.add(child);
						visited.add(child);	
						colorize(child, i);
					}
				}
			}	
		}
	}
	public static List<Color> colors = Arrays.asList(
			Color.GREEN,
			Color.BLUE,
			Color.DARK_GRAY,
			Color.RED,
			Color.YELLOW,
			Color.CYAN,
			Color.MAGENTA,
			Color.LIGHT_GRAY,
			Color.ORANGE,
			Color.PINK
			);
	
	public static void colorize(Vertex node, int i) {		
		node.colored = true;
		if (i < colors.size()) {
			node.r = (float) (colors.get(i).getRed()/256.0);
			node.g = (float) (colors.get(i).getGreen()/256.0);
			node.b = (float) (colors.get(i).getBlue()/256.0);
		} else if (i < colors.size()* 2) {
			node.r = (float) (colors.get(i - colors.size()).getRed()/2/256.0);
			node.g = (float) (colors.get(i - colors.size()).getGreen()/2/256.0);
			node.b = (float) (colors.get(i - colors.size()).getBlue()/2/256.0);								
		} else {
			node.r = (float) (colors.get(i - colors.size()*2).getRed()/3/256.0);
			node.g = (float) (colors.get(i - colors.size()*2).getGreen()/3/256.0);
			node.b = (float) (colors.get(i - colors.size()*2).getBlue()/3/256.0);								
		}
	}
	
	public static Pair<Edge, Vertex> findEdgeAndPointToDelete(WeightedGraph<Vertex,Edge> graph, double edgeDeletingThreshold) {
		Iterator<Edge> edgeIterator = graph.edgeSet().iterator();
		
		while (edgeIterator.hasNext()) {
			Edge e = edgeIterator.next();
			if (e.length < edgeDeletingThreshold) {
				//avoiding lock transfer
				if (e.a.locked || e.b.locked ) {
					continue;
				}
				
				if (e.a.fixed || e.b.fixed ) {
					continue;
				}
				
				if (e.a.placecode > 0 || e.b.placecode > 0 ) {
					continue;
				}
				
				boolean aCannotBeDeleted = graph.edgesOf(e.a).size() == 1 || e.a.pumpStationEntry || e.a.pumpStationExit || e.a.betweenSectorBlock || e.a.southernPumpStation;
				boolean bCannotBeDeleted = graph.edgesOf(e.b).size() == 1 || e.b.pumpStationEntry || e.b.pumpStationExit || e.b.betweenSectorBlock || e.b.southernPumpStation;				
				
				Vertex pointToDelete;
				if (aCannotBeDeleted && bCannotBeDeleted) {
					continue;
				} else if (aCannotBeDeleted) {
					pointToDelete = e.b;
				} else if (bCannotBeDeleted) {
					pointToDelete = e.a;
				} else {
					pointToDelete = (graph.edgesOf(e.a).size() < graph.edgesOf(e.b).size()) ? e.a : e.b;
				}
				return new Pair<>(e, pointToDelete);
			}
		}
		
		edgeIterator = graph.edgeSet().iterator();
		
		List<Integer> bannedVertices = Arrays.asList(new Integer[]{
				178370501, 177782701, 104960501, 188659001, 104780601, 188658601, 104834601, 178381001, 104863801,
				104923001, 104780201, 104780301, 104781001, 181274401, 104946801, 104960801, 104949101, 104961201,
				104923101, 178937101, 104881901, 188658801, 104948201, 104957101, 104881901, 104959901, 104918001, 
				104916501, 178936901, 104863701, 199743001, 178930601, 104958001, 104956001, 104958101, 104923601, 
				104863701, 104958701, 104834401, 104948901, 104845201, 104961301, 104957401, 104958501, 104881501,
				180105501, 177172701, 104924001, 178936801, 104947401, 104959701, 104925101, 104924401, 104949501, 
				104948401, 104959101, 104923301, 104959601, 104958601, 104911201, 203199401, 104893701, 104845301, 
				178934101, 181271701, 104958801, 104960401, 188658901, 104947301, 104947501, 104947601, 104961901, 
				104924601, 104947901, 104956301, 104947201, 104961401, 180683201, 104845101, 104866401, 104881601, 
				104956901, 189236701, 104864101, 104949001, 104893901, 104865901, 104957301, 104956601, 104958201, 
				104960101, 203199501, 203199301, 104962101, 104924901, 104865801, 104957501, 104959401, 104959301, 
				104959501, 104949601, 104948501, 104863401, 104863301, 
				104934701//dublicate pump station exit		
				});
		
		while (edgeIterator.hasNext()) {
			Edge e = edgeIterator.next();
			if (bannedVertices.contains(new Integer(e.a.oldId))) {				
				return new Pair<>(e, e.a);
			}
			if (bannedVertices.contains(new Integer(e.b.oldId))) {				
				return new Pair<>(e, e.b);
			}
		}
		
		edgeIterator = graph.edgeSet().iterator();
		
		return null;
	}
	
	public static ArrayList<Vertex> getPoints() {
		//layouted graph
		ArrayList<Vertex> points = JsonGraphReader.readNodes("C:\\Users\\test\\Desktop\\‰ËÔÎÓÏ\\FINAL_ALL_POINTS.json");
		
		JsonGraphReader.populatePointsWithParameters(points, "C:\\Users\\test\\Desktop\\‰ËÔÎÓÏ\\d3full.json");	
		
		// now we want to resolve parameters of points from their subtypes
		for (Vertex p : points) {
			String type = p.type.toUpperCase();
			p.canBeLocked = type.contains("— «¿ƒ¬»∆ Œ…") ||
					    	type.contains("«¿√À”ÿ ¿") ||
					    	type.contains("«¿ƒ¬»∆ ¿") ||
					    	type.contains("— «¿√À”ÿ Œ…");
			
			p.betweenSectorBlock = type.contains("Ã≈∆—≈ “Œ–Õ€… –¿—’ŒƒŒÃ≈–");
			
			p.pumpStationEntry = type.contains("“Œ◊ ¿ ¬’Œƒ¿ Õ¿ œÕ—");
			p.pumpStationExit = type.contains("“Œ◊ ¿ ¬€’Œƒ¿ — œÕ—");
		}
				
		CSVGraphReader.populatePlacecodes(points, "C:\\Users\\test\\Desktop\\‰ËÔÎÓÏ\\placecode2vert_id.csv");
		
		Map<Integer, Double> consumption = Main.dbReader.readConsumption();
		for (Vertex p : points) 
			if (consumption.get(p.placecode)!=null){
				p.consumption = consumption.get(p.placecode);
				System.out.println("Vertex " + p + " has consumption level " + p.consumption);
			}
		
		return points;
	}
	
	public static ArrayList<Edge> filterUnneededEdges(ArrayList<Edge> edges) {
		edges = new ArrayList<Edge>(edges);
		
		filterEdgesWithInvalidPoints(edges);
		
		//here we get rid of unneeded edges and graph components
		//map is used to be compatible with old API
		HashMap<Integer,Edge> idToEdge = new HashMap<>(edges.size());
		for(Edge e : edges)
			idToEdge.put(e.id, e);
						
		ExcelGraphReader.filterUnusedEdges(idToEdge, "C:\\Users\\test\\Desktop\\‰ËÔÎÓÏ\\‰Û„Ë.xlsx");
		ConvergenceImprover.removeSmallConnectedComponents(idToEdge, true, 16);
		
		return new ArrayList<Edge>(idToEdge.values());
	}
	
	public static void filterEdgesWithInvalidPoints(ArrayList<Edge> edges) {
		//Û·Ë‡ÂÏ ÌÂËÒÔÓÎ¸ÁÛ˛˘ËÂÒˇ ÚÓ˜ÍË
		Iterator<Edge> iter = edges.iterator();
		while(iter.hasNext()){
			Edge e = iter.next();
			if("Õ≈“ “Œ◊ »".equals(e.a.type) || "Õ≈“ “Œ◊ »".equals(e.b.type))	
				iter.remove();	
			/*
			else if ( 
					(e.a.fixed && (e.a.x < 10000 || e.a.y < 10000))
					|| 
					(e.b.fixed && (e.b.x < 10000 || e.b.y < 10000))
					)
				iter.remove();	
			else {
				System.out.print("===.>");
				System.out.println(e.a);
				System.out.print("===..>");
				System.out.println(e.b);
			}
			*/	
		}
	}
	
}
