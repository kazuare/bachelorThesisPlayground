package bachelorThesisPlayground;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.educationalProject.surfacePathfinder.visualization.DisplayMode;
import org.educationalProject.surfacePathfinder.visualization.NetworkVisualizer;
import org.educationalProject.surfacePathfinder.visualization.Screenshooter;
import org.educationalProject.surfacePathfinder.visualization.SwingWindow;
import org.jgrapht.graph.DefaultDirectedWeightedGraph;
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
		
		ArrayList<Vertex> points = JsonGraphReader.readNodes("C:\\Users\\test\\Desktop\\диплом\\FINAL_ALL_POINTS.json");
		ArrayList<Edge> edges = JsonGraphReader.readEdges("C:\\Users\\test\\Desktop\\диплом\\d3full.json");

		HashMap<Integer,Edge> idToEdge = new HashMap<>(edges.size());
		for(Edge e : edges)
			idToEdge.put(e.id, e);
		
		ExcelGraphReader.filterUnusedEdges(idToEdge);
		ConvergenceImprover.removeSmallConnectedComponents(idToEdge, true, 16);
		
		edges = new ArrayList<Edge>(idToEdge.values());
		Collections.sort(edges, (a,b)->Integer.compare(a.id, b.id));
		
		Set<Vertex> usedPoints = new HashSet<Vertex>(points.size());
		for (Edge e : edges){
			usedPoints.add(e.a);
			usedPoints.add(e.b);		
		}
		
		points.removeIf(v->!usedPoints.contains(v));
		
	    double minX = points.stream().mapToDouble(p->p.x).min().getAsDouble();
	    double minY = points.stream().mapToDouble(p->p.y).min().getAsDouble();
	   
	    points.stream()
		    .forEach(v->{
		    	v.x -= minX;
		    	v.y -= minY;
		    });
		
		DefaultDirectedWeightedGraph<Vertex,Edge> graph = new DefaultDirectedWeightedGraph<Vertex,Edge>(Edge.class);
		
		HashMap<Integer, Vertex> idToVertex = new HashMap<Integer, Vertex>(points.size());
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
		
		//DefaultDirectedWeightedGraph<Vertex,Edge> graph = new DefaultDirectedWeightedGraph<Vertex,Edge>(Edge.class);
		//graph.addVertex(new Vertex(2,3,4));
		
		DisplayMode.setMode("screenshot");
		
		NetworkVisualizer visScreenshot = new NetworkVisualizer()
				.setData(graph)
				.setDefaultWidth(10000)
				.calculateWeightAndHeight();
		Screenshooter.start(visScreenshot, visScreenshot.getWidth() + 50, visScreenshot.getHeight()+ 50);

		DisplayMode.setMode("screen");
		
		NetworkVisualizer vis = new NetworkVisualizer()
				.setData(graph)
				.setDefaultWidth(800)
				.calculateWeightAndHeight();
		SwingWindow.start(vis, vis.getWidth() + 50, vis.getHeight()+ 50, "pipes");
		
	}
	
	//парсим файлы, пишем в джсон файл для d3 force v4
	static void writeJSONGraphForD3() {
		//тянем ребра графа и идентификаторы их точек-концов
		Map<Integer, Edge> edges = ExcelGraphReader.getEdgeSkeletons("C:\\Users\\test\\Desktop\\диплом\\2016_07_25_связи_дуг_и_узлов_4_секторов.xlsx");
		//тянем дополнительные параметры ребер - длину, диаметр и тд
		ExcelGraphReader.populateEdgeParameters(edges, "C:\\Users\\test\\Desktop\\диплом\\2016_07_25_дуги_4_секторов_Южной.xlsx");
		//тянем координаты точек-концов ребер
		CSVGraphReader.populateVertexCoordinates(edges, "C:\\Users\\test\\Desktop\\диплом\\Южная зона точки с координатами.csv");
		//GraphReader.filterUnusedEdges(edges);
		
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
	
}
