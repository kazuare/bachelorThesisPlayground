package bachelorThesisPlayground.graphBuilding;

import java.util.Iterator;
import java.util.Map;

import bachelorThesisPlayground.Edge;
import bachelorThesisPlayground.normalizers.ConvergenceImprover;
import bachelorThesisPlayground.normalizers.Normalizer;
import bachelorThesisPlayground.readers.CSVGraphReader;
import bachelorThesisPlayground.readers.ExcelGraphReader;
import bachelorThesisPlayground.writers.JSONWriter;

public class D3 {

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
}
