package bachelorThesisPlayground;

import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Arrays;
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

import org.educationalProject.surfacePathfinder.visualization.DisplayMode;
import org.educationalProject.surfacePathfinder.visualization.NetworkVisualizer;
import org.educationalProject.surfacePathfinder.visualization.Screenshooter;
import org.educationalProject.surfacePathfinder.visualization.SwingWindow;
import org.jgrapht.UndirectedGraph;
import org.jgrapht.WeightedGraph;
import org.jgrapht.alg.CycleDetector;
import org.jgrapht.alg.interfaces.SpanningTreeAlgorithm.SpanningTree;
import org.jgrapht.alg.spanning.KruskalMinimumSpanningTree;
import org.jgrapht.alg.spanning.PrimMinimumSpanningTree;
import org.jgrapht.alg.util.Pair;
import org.jgrapht.graph.DefaultDirectedWeightedGraph;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleDirectedGraph;
import org.jgrapht.graph.SimpleGraph;
import org.jgrapht.graph.SimpleWeightedGraph;

import bachelorThesisPlayground.normalizers.ConvergenceImprover;
import bachelorThesisPlayground.normalizers.Normalizer;
import bachelorThesisPlayground.readers.CSVGraphReader;
import bachelorThesisPlayground.readers.DBReader;
import bachelorThesisPlayground.readers.ExcelGraphReader;
import bachelorThesisPlayground.readers.JsonGraphReader;
import bachelorThesisPlayground.writers.JSONWriter;

public class Main {

	static DBReader dbReader = new DBReader();
	public static void main(String[] args) {		
		//writeJSONGraphForD3();
		//we do layout in d3 and get FINAL_ALL_POINTS.json
		dbReader.init();
		
		SimpleWeightedGraph<Vertex,Edge> graph = getColoredGraph();
		
		List<SimpleWeightedGraph<Vertex,Edge>> components = coloredGraphSplit(graph);
			
		List<SimpleWeightedGraph<Vertex,Edge>> spanningTrees = getSpanningTrees(components);

		setSensorsWithPreferredPointCount(spanningTrees, 550, 300);
		
		drawGraph(graph, graphJoin(spanningTrees));
		//drawGraph(graph, null);
		
		//denormalizeCoordsAndWriteIntoFiles();
	}
	
	//парсим файлы, пишем в джсон файл для d3 force v4
	static void writeJSONGraphForD3() {
		//тянем ребра графа и идентификаторы их точек-концов
		Map<Integer, Edge> edges = ExcelGraphReader.getEdgeSkeletons("C:\\Users\\test\\Desktop\\диплом\\связи_дуг_и_узлов.xlsx");
		//тянем дополнительные параметры ребер - длину, диаметр и тд
		ExcelGraphReader.populateEdgeParameters(edges, "C:\\Users\\test\\Desktop\\диплом\\дуги.xlsx");
		//тянем координаты точек-концов ребер
		CSVGraphReader.populateVertexCoordinates(edges, "C:\\Users\\test\\Desktop\\диплом\\точки с координатами.csv");
		//GraphReader.filterUnusedEdges(edges);
		
		ExcelGraphReader.populatePointsWithParameters(edges, "C:\\Users\\test\\Desktop\\диплом\\узлы.xlsx");
		
		//в графе есть несколько некорректно заданных ребер, выкидываем
		Iterator<Integer> it = edges.keySet().iterator();
		while(it.hasNext()){
			Integer id = it.next();
			if(edges.get(id).diameter == 0)
				it.remove();
		}		
		
		//убираем компоненты связности, где нет ни одной фиксированной точки
		//убираем также слишком маленькие компоненты
		ConvergenceImprover.removeSmallConnectedComponents(edges, false, 50);
		
		//приводим идентификаторы точек к виду 0...n-1
		Normalizer.normalizeIds(edges);
		
		//приводим координаты к диапазону [0,m_x/m_y],
		//проводим масштабирование координат
		Normalizer.normalizeCoords(edges);
		
		//улучшаем сходимость d3 с помощью преобразования вида
		//<<все нификсированные точки кидаем в окрестность смежных зафиксированных>>
		//побочный эффект: задает параметр fixed у вершин
		ConvergenceImprover.assignCoordsOld(edges);
		
		/* обрезает все точки, у которых любая координата больше трешхолда
		int threshold = 3000;
		Iterator<Map.Entry<Integer, Edge>> iter = edges.entrySet().iterator();
		while(iter.hasNext()){
			Map.Entry<Integer, Edge> entry = iter.next();
			if(entry.getValue().a.x > threshold || entry.getValue().b.x > threshold 
					|| entry.getValue().a.y > threshold || entry.getValue().b.y > threshold
					)		
				iter.remove();	
		}
		*/
		
		//укороченная версия файла, для д3
		JSONWriter.write(edges, "C:\\Users\\test\\Desktop\\диплом\\d3.json");
		//более полная версия графа, ребра хранят доп свойства
		JSONWriter.writeFull(edges, "C:\\Users\\test\\Desktop\\диплом\\d3full.json");
		
		System.out.println(edges.values().stream().mapToDouble(e->Math.max(e.a.x, e.b.x)).max().getAsDouble());
		System.out.println(edges.values().stream().mapToDouble(e->Math.max(e.a.y, e.b.y)).max().getAsDouble());
		
	}
	
	
	public static SimpleWeightedGraph<Vertex,Edge> getColoredGraph(){				
			ArrayList<Vertex> points = getPoints();
		
			//from here we get all needed data about edges
			ArrayList<Edge> edges = JsonGraphReader.readEdges("C:\\Users\\test\\Desktop\\диплом\\d3full.json");

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
			CSVGraphReader.updateStatusOfValves(points, "C:\\Users\\test\\Desktop\\диплом\\положения_кранов.csv");
			
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
			
			
			HashMap<Integer, Vertex> idToVertex = new HashMap<Integer, Vertex>(points.size());
			for (Vertex p : points){
				if (filterByCoords)
					if (p.x > highX || p.y > highY || p.x < lowX || p.y < lowY) {
						continue;						
					} else {
						p.x -= lowX;
						p.y -= lowY;
					}
				idToVertex.put(p.id, p);
				graph.addVertex(p);
			}
				
			for (Edge e: edges) {
				e.a = idToVertex.get(e.a.id);
				e.b = idToVertex.get(e.b.id);
				
				if(filterByCoords && (e.a==null || e.b == null))
					continue;
				
				if (e.a != e.b) {
					graph.addEdge(e.a, e.b, e);
					System.out.println("filling graph with edge: " + e);
				}
			}
			
			System.out.println(graph.vertexSet().size());

			double edgeDeletingThreshold = 20;//8;
			
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
		if (currentPoint.sensorCanBePlaced && graph.edgesOf(currentPoint).size() > 1)
		    if (remainingPath <= 0) {
		    	remainingPath = minimumPath;
		    	currentPoint.mainSensorPlaced = true;
		    } else {	
			    for (Edge e : graph.edgesOf(currentPoint)) {
			    	Vertex p = currentPoint.equals(e.a) ? e.b : e.a;
			    	if (!visited.contains(p) && remainingPath-e.length <= 0 && !p.sensorCanBePlaced){ 
				    	remainingPath = minimumPath;
				    	currentPoint.mainSensorPlaced = true;
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
	

	public static void setSensorsWithPreferredPointCount(List<SimpleWeightedGraph<Vertex,Edge>> components, int preferredPointCount, int itCount) {
		double step = 10000;
		double currentPosition = step;
		int pointCount = 0;
		int i;
		for (i = 0; i < itCount; i++) {
			for (SimpleWeightedGraph<Vertex,Edge> component : components)
				for (Vertex p : component.vertexSet())
					p.mainSensorPlaced = false;
			
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
				if (p.mainSensorPlaced)
					markedCounter++;
		}	
		return markedCounter;
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
	
	
	public static void drawGraph(SimpleWeightedGraph<Vertex,Edge> graph, SimpleWeightedGraph<Vertex,Edge> overlayGraph){
		//save as png file

		DisplayMode.setMode("screenshot");
			
		NetworkVisualizer visScreenshot = new NetworkVisualizer()
				.setData(graph)
				.setDefaultWidth(10000)
				.setLabelDrawing(true)
				.setOverlayData(overlayGraph)
				.calculateWeightAndHeight();
		Screenshooter.start(visScreenshot, visScreenshot.getWidth() + 50, visScreenshot.getHeight()+ 50);

		Path source = FileSystems.getDefault().getPath("c:\\users\\test\\desktop\\yo.png");
		Path out = FileSystems.getDefault().getPath("yo.png");
		try {
		    Files.copy(source, out, StandardCopyOption.REPLACE_EXISTING);
		} catch (IOException e) {
		    e.printStackTrace();
		}

		//draw on screen
		DisplayMode.setMode("screen");
		
		NetworkVisualizer vis = new NetworkVisualizer()
				.setData(graph)
				.setDefaultWidth(800)
				.setOverlayData(overlayGraph)
				.calculateWeightAndHeight();
		SwingWindow.start(vis, vis.getWidth() + 50, vis.getHeight()+ 50, "pipes");
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
	
	public static SimpleWeightedGraph<Vertex,Edge> graphJoin(List<SimpleWeightedGraph<Vertex,Edge>> graphs) {
		SimpleWeightedGraph<Vertex,Edge> result = new SimpleWeightedGraph<Vertex,Edge>(Edge.class);
		for (SimpleWeightedGraph<Vertex,Edge> graph : graphs) {
			for(Vertex v : graph.vertexSet())
				result.addVertex(v);
			for(Edge e : graph.edgeSet())
				result.addEdge(e.a, e.b, e);
		}
		return result;
	}

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
	
	public static void colorize(Vertex node, int i) {
		List<Color> colors = Arrays.asList(
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
				
				boolean aCannotBeDeleted = e.a.fixed || graph.edgesOf(e.a).size() == 1 || e.a.pumpStationEntry || e.a.pumpStationExit || e.a.betweenSectorBlock || e.a.southernPumpStation;
				boolean bCannotBeDeleted = e.b.fixed || graph.edgesOf(e.b).size() == 1 || e.b.pumpStationEntry || e.b.pumpStationExit || e.b.betweenSectorBlock || e.b.southernPumpStation;				
				
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
		/*
		Iterator<Vertex> pointIterator = graph.vertexSet().iterator();
				
		while (pointIterator.hasNext()) {
			Vertex p = pointIterator.next();
			boolean cannotBeDeleted = p.fixed || p.pumpStationEntry || p.pumpStationExit || p.locked;
			Set<Edge> edges = graph.edgesOf(p);
			if (edges.size() == 2 && !cannotBeDeleted) {
				Iterator<Edge> edgeRetriever = edges.iterator();
				edgeRetriever.hasNext();
				Edge edgeA = edgeRetriever.next();
				edgeRetriever.hasNext();
				Edge edgeB = edgeRetriever.next();
				
				Vertex targetA = edgeA.a.equals(p)?edgeA.b:edgeA.a;
				Vertex targetB = edgeB.a.equals(p)?edgeB.b:edgeB.a;
				double angle = Math.abs(
						Math.toDegrees(
								Math.atan2(targetB.y-p.y, targetB.x-p.x) - Math.atan2(targetA.y-p.y, targetA.x-p.x)
								)
						);
				
				if (angle < 90 || (angle > 360 && angle < 450)) {
					return new Pair<>(edgeA.length>edgeB.length?edgeB:edgeA, p);
				}
				
			}
		}
		*/
		return null;
	}
	
	public static ArrayList<Vertex> getPoints() {
		//layouted graph
		ArrayList<Vertex> points = JsonGraphReader.readNodes("C:\\Users\\test\\Desktop\\диплом\\FINAL_ALL_POINTS.json");
		
		JsonGraphReader.populatePointsWithParameters(points, "C:\\Users\\test\\Desktop\\диплом\\d3full.json");	
		
		// now we want to resolve parameters of points from their subtypes
		for (Vertex p : points) {
			String type = p.type.toUpperCase();
			p.canBeLocked = type.contains("С ЗАДВИЖКОЙ") ||
					    	type.contains("ЗАГЛУШКА") ||
					    	type.contains("ЗАДВИЖКА") ||
					    	type.contains("С ЗАГЛУШКОЙ");
			
			p.betweenSectorBlock = type.contains("МЕЖСЕКТОРНЫЙ РАСХОДОМЕР");
			
			p.pumpStationEntry = type.contains("ТОЧКА ВХОДА НА ПНС");
			p.pumpStationExit = type.contains("ТОЧКА ВЫХОДА С ПНС");
		}
				
		CSVGraphReader.populatePlacecodes(points, "C:\\Users\\test\\Desktop\\диплом\\placecode2vert_id.csv");
		
		Map<Integer, Double> consumption = dbReader.readConsumption();
		for (Vertex p : points) 
			if (consumption.get(p.placecode)!=null){
				p.consumption = consumption.get(p.placecode);
				p.sensorCanBePlaced = true;
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
						
		ExcelGraphReader.filterUnusedEdges(idToEdge, "C:\\Users\\test\\Desktop\\диплом\\дуги.xlsx");
		ConvergenceImprover.removeSmallConnectedComponents(idToEdge, true, 16);
		
		return new ArrayList<Edge>(idToEdge.values());
	}
	
	public static void filterEdgesWithInvalidPoints(ArrayList<Edge> edges) {
		//убираем неиспользующиеся точки
		Iterator<Edge> iter = edges.iterator();
		while(iter.hasNext()){
			Edge e = iter.next();
			if("НЕТ ТОЧКИ".equals(e.a.type) || "НЕТ ТОЧКИ".equals(e.b.type))	
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
	
	public static class MutableVertexPair{
	    Vertex first;
	    Vertex second;
	    public MutableVertexPair(Vertex a, Vertex b){
	        this.first = a;
	        this.second = b;
	    }		
	}
}









/* 100% dead code:
  
  
 
	public static void denormalizeCoordsAndWriteIntoFiles(){

		ArrayList<Vertex> points = getPoints();
		
		//from here we get all needed data about edges
		ArrayList<Edge> edges = JsonGraphReader.readEdges("C:\\Users\\test\\Desktop\\диплом\\d3full.json");
				
		filterEdgesWithInvalidPoints(edges);
		
		//here we get rid of unneeded edges and graph components
		HashMap<Integer,Edge> idToEdge = new HashMap<>(edges.size());
		for(Edge e : edges)
			idToEdge.put(e.id, e);
		
		ExcelGraphReader.filterUnusedEdges(idToEdge, "C:\\Users\\test\\Desktop\\диплом\\дуги.xlsx");
		ConvergenceImprover.removeSmallConnectedComponents(idToEdge, true, 16);
		
		edges = new ArrayList<Edge>(idToEdge.values());
		Collections.sort(edges, (a,b)->Integer.compare(a.id, b.id));
		
		//there are some unneeded points after cleaning, lets remove them
		Set<Vertex> usedPoints = new HashSet<Vertex>(points.size());
		for (Edge e : edges){
			usedPoints.add(e.a);
			usedPoints.add(e.b);		
		}
		
		points.removeIf(v->!usedPoints.contains(v));
		
		// now we want to resolve parameters of points from their subtypes
	    for (Vertex p : points) {
	    	p.canBeLocked = p.type.toUpperCase().contains("С ЗАДВИЖКОЙ") ||
			    			p.type.toUpperCase().contains("ЗАГЛУШКА") ||
			    			p.type.toUpperCase().contains("ЗАДВИЖКА") ||
	    					p.type.toUpperCase().contains("С ЗАГЛУШКОЙ");
	    	
	    	p.pumpStationEntry = p.type.toUpperCase().contains("ТОЧКА ВХОДА НА ПНС");
	    	p.pumpStationExit = p.type.toUpperCase().contains("ТОЧКА ВЫХОДА С ПНС");
	    }
	   
		HashMap<Integer, Vertex> idToVertex = new HashMap<Integer, Vertex>(points.size());
		for (Vertex p : points){
			idToVertex.put(p.id, p);
		}
		
		for (Edge e: edges) {
			e.a = idToVertex.get(e.a.id);
			e.b = idToVertex.get(e.b.id);
			System.out.println("filling graph with edge: " + e);
		}
		
		int idOfFixedNode = -1;
		int oldIdOfFixedNode = -1;
		for (Vertex p : points) {
			if (p.fixed) {
				idOfFixedNode = p.id;
				oldIdOfFixedNode = p.oldId;
				break;
			}				
		}
				
		Vertex denormalizedVertex = CSVGraphReader.getPointWithId("C:\\Users\\test\\Desktop\\диплом\\точки с координатами.csv", oldIdOfFixedNode);
		
		double shiftX = idToVertex.get(idOfFixedNode).x*1000 - denormalizedVertex.x;
		double shiftY = idToVertex.get(idOfFixedNode).y*1000 - denormalizedVertex.y;
		
		DecimalFormat df = new DecimalFormat("#");
        df.setMaximumFractionDigits(8);
		
		for (Vertex p : points) {
			p.x = p.x * 1000 - shiftX;
			p.y = p.y * 1000 - shiftY;
			
			if (p.fixed)
				System.out.println(p.oldId + " " + df.format(p.x) + " " + df.format(p.y));
		}
		
		CSVGraphWriter.writeEdgesForAnalyst(edges);
		CSVGraphWriter.writeNodesForAnalyst(points);
		
		
		//check everything is fine
		
		//now we want to normalize points coordinates (normalization can be ruined after layouting)
	    double minX = points.stream().mapToDouble(p->p.x).min().getAsDouble();
	    double minY = points.stream().mapToDouble(p->p.y).min().getAsDouble();
	   
	    points.stream()
		    .forEach(v->{
		    	v.x -= minX;
		    	v.y -= minY;
		    });
		
	    	    
	    //construct graph 
		DefaultDirectedWeightedGraph<Vertex,Edge> graph = new DefaultDirectedWeightedGraph<Vertex,Edge>(Edge.class);
		
		idToVertex = new HashMap<Integer, Vertex>(points.size());
		for (Vertex p : points){
			idToVertex.put(p.id, p);
			graph.addVertex(p);
		}
		
		for (Edge e: edges) {
			e.a = idToVertex.get(e.a.id);
			e.b = idToVertex.get(e.b.id);
			graph.addEdge(e.a, e.b, e);
			System.out.println("filling graph with edge: " + e);
		}
		//display 
		
		//save as png file
		DisplayMode.setMode("screenshot");
		
		NetworkVisualizer visScreenshot = new NetworkVisualizer()
				.setData(graph)
				.setDefaultWidth(10000)
				.calculateWeightAndHeight();
		Screenshooter.start(visScreenshot, visScreenshot.getWidth() + 50, visScreenshot.getHeight()+ 50);

		//draw on screen
		DisplayMode.setMode("screen");
		
		NetworkVisualizer vis = new NetworkVisualizer()
				.setData(graph)
				.setDefaultWidth(800)
				.calculateWeightAndHeight();
		SwingWindow.start(vis, vis.getWidth() + 50, vis.getHeight()+ 50, "pipes");
	}
 
 
 
 
 */