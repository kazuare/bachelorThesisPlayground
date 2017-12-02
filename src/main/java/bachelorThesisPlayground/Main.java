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
import org.jgrapht.WeightedGraph;
import org.jgrapht.alg.util.Pair;
import org.jgrapht.graph.DefaultDirectedWeightedGraph;
import org.jgrapht.graph.SimpleWeightedGraph;

import bachelorThesisPlayground.normalizers.ConvergenceImprover;
import bachelorThesisPlayground.normalizers.Normalizer;
import bachelorThesisPlayground.readers.CSVGraphReader;
import bachelorThesisPlayground.readers.ExcelGraphReader;
import bachelorThesisPlayground.readers.JsonGraphReader;
import bachelorThesisPlayground.writers.JSONWriter;

public class Main {

	public static void main(String[] args) {
		//writeJSONGraphForD3();
		//we do layout in d3 and get FINAL_ALL_POINTS.json
		
		drawGraph();
		
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
	
	
	public static void drawGraph(){				
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
			
			double lowX = 5000;
			double lowY = 4950;
			double highX = 5500;
			double highY = 5450;
			
			
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
/*
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
					
					if (!graph.containsEdge(edgeToChangeCopy.a, edgeToChangeCopy.b)
							&&
						!graph.containsEdge(edgeToChangeCopy.b, edgeToChangeCopy.a)) {
						edgeToChangeCopy.length = edgeToChangeCopy.a.distance(edgeToChangeCopy.b);
						if(edgeToChangeCopy.a != edgeToChangeCopy.b){
							graph.addEdge(edgeToChangeCopy.a, edgeToChangeCopy.b, edgeToChangeCopy);
						}
					}		
				}				
							
				edgeAndPointToDelete = findEdgeAndPointToDelete(graph, edgeDeletingThreshold);
			}
*/
			System.out.println(graph.vertexSet().size());
			
			List<Vertex> pumpStations = graph.vertexSet()
					.stream()
					.filter(p->p.pumpStationExit)
					.collect(Collectors.toList());
			
			List<Color> colors = Arrays.asList(
					Color.MAGENTA,
					Color.BLUE,
					Color.DARK_GRAY,
					Color.RED,
					Color.YELLOW,
					Color.CYAN,
					Color.GREEN,
					Color.LIGHT_GRAY,
					Color.ORANGE,
					Color.PINK
					);
			
			for (int i = 0; i < pumpStations.size(); i++) {			
				Queue<Vertex> queue = new LinkedList<Vertex>();
				Set<Vertex> visited = new HashSet<>();
				//Vertex root = graph.vertexSet().stream().filter(p->p.oldId==106781901).findFirst().get();
				Vertex root = pumpStations.get(i);
				queue.add(root);
				visited.add(root);
				while(!queue.isEmpty()) {
					Vertex node = queue.remove();
					Iterator<Edge> it = graph.edgesOf(node).iterator();
					while (it.hasNext()) {
						Edge e = it.next();
						Vertex child = e.a.equals(node)?e.b:e.a;
						if (!visited.contains(child) && !child.locked && !child.betweenSectorBlock) {
							queue.add(child);
							visited.add(child);		
							child.colored = true;
							if (i < colors.size()) {
								child.r = (float) (colors.get(i).getRed()/256.0);
								child.g = (float) (colors.get(i).getGreen()/256.0);
								child.b = (float) (colors.get(i).getBlue()/256.0);
							} else if (i < colors.size()* 2) {
								child.r = (float) (colors.get(i - colors.size()).getRed()/2/256.0);
								child.g = (float) (colors.get(i - colors.size()).getGreen()/2/256.0);
								child.b = (float) (colors.get(i - colors.size()).getBlue()/2/256.0);								
							} else {
								child.r = (float) (colors.get(i - colors.size()*2).getRed()/3/256.0);
								child.g = (float) (colors.get(i - colors.size()*2).getGreen()/3/256.0);
								child.b = (float) (colors.get(i - colors.size()*2).getBlue()/3/256.0);								
							}
						}
					}
				}	
			}
			//display 
				
			//save as png file
			DisplayMode.setMode("screenshot");
				
			NetworkVisualizer visScreenshot = new NetworkVisualizer()
					.setData(graph)
					.setDefaultWidth(10000)
					.setLabelDrawing(true)
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
					.calculateWeightAndHeight();
			SwingWindow.start(vis, vis.getWidth() + 50, vis.getHeight()+ 50, "pipes");
						
	}
	
	public static Pair<Edge, Vertex> findEdgeAndPointToDelete(WeightedGraph<Vertex,Edge> graph, double edgeDeletingThreshold) {
		Iterator<Edge> edgeIterator = graph.edgeSet().iterator();
		
		while (edgeIterator.hasNext()) {
			Edge e = edgeIterator.next();
			if (e.length < edgeDeletingThreshold) {
				boolean aCannotBeDeleted = e.a.fixed || graph.edgesOf(e.a).size() == 1 || e.a.pumpStationEntry || e.a.pumpStationExit || e.a.locked || e.a.betweenSectorBlock;
				boolean bCannotBeDeleted = e.b.fixed || graph.edgesOf(e.b).size() == 1 || e.b.pumpStationEntry || e.b.pumpStationExit || e.b.locked || e.b.betweenSectorBlock;				
				
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
				104959501, 104949601, 104948501, 104863401, 104863301
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