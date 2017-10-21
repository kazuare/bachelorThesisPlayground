package bachelorThesisPlayground;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.educationalProject.surfacePathfinder.visualization.DecolorizedMapVisualizer;
import org.educationalProject.surfacePathfinder.visualization.Point;
import org.educationalProject.surfacePathfinder.visualization.SwingWindow;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleWeightedGraph;

import bachelorThesisPlayground.normalizers.ConvergenceImprover;
import bachelorThesisPlayground.normalizers.Normalizer;
import bachelorThesisPlayground.readers.CSVGraphReader;
import bachelorThesisPlayground.readers.ExcelGraphReader;
import bachelorThesisPlayground.writers.JSONWriter;
import bachelorThesisPlayground.writers.SSVGraphWriter;
import bachelorThesisPlayground.writers.WriterForGephy;

public class Main {

	public static void main(String[] args) {
		//writeJSONGraphForD3();
		
		
		/*
		edges 
		
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
				System.out.println(points.get(e.a.id) + " " + points.get(e.b.id));
				graph.addEdge(points.get(e.a.id), points.get(e.b.id));
			}
		
		
		DecolorizedMapVisualizer vis = new DecolorizedMapVisualizer();
		vis.setData(graph, null);
		SwingWindow.start(vis, 700, 700, "pipes");*/
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
		ConvergenceImprover.removeSmallConnectedComponents(edges);
		
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
