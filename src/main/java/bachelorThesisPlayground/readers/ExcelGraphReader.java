package bachelorThesisPlayground.readers;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import bachelorThesisPlayground.Edge;
import bachelorThesisPlayground.Vertex;

public class ExcelGraphReader {	
	public static Map<Integer, Edge> getEdgeSkeletons(String file){
		ExcelReader reader = new ExcelReader();
		try {
			reader.init(file);
			reader.hasNext();reader.next(); //skip first line
			
			Map<Integer, Edge> result = new HashMap<>();
			int i = 0;
			while(reader.hasNext()){
				List<Integer> line = reader.next()
						.stream()
						.map(s -> (int)Double.parseDouble(s.substring(0, s.length())))
						.collect(Collectors.toList());
				Edge e = new Edge(line.get(1), line.get(2), line.get(3));		
				result.put(line.get(1), e);
				System.out.println(i++ + " " + e.id + " " + e.a.id + " " + e.b.id);
			}
			reader.close();
			return result;
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}	

	public static void populatePointsWithParameters(Map<Integer, Edge> edges, String file){
		ExcelReader reader = new ExcelReader();
		try {
			reader.init(file);			
			reader.hasNext();reader.next(); //skip first line

			int i = 0 ;
			Map<Integer, String> pointIdToPointType = new HashMap<>();
			while(reader.hasNext()){
				List<String> line = reader.next();
				pointIdToPointType.put((int)Double.parseDouble(line.get(1)), line.get(2));		
				System.out.println(i++);
			}
			reader.close();
			
			i = 0; 
			for (Edge e : edges.values()) {
				e.a.type = pointIdToPointType.get(e.a.id);
				e.b.type = pointIdToPointType.get(e.b.id);
			
				e.a.oldId = e.a.id;
				e.b.oldId = e.b.id;
				
				System.out.println(i++);
			}
			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void populateEdgeParameters(Map<Integer, Edge> edges, String file){
		ExcelReader reader = new ExcelReader();
		try {
			reader.init(file);			
			reader.hasNext();reader.next(); //skip first line

			int i = 0 ;
			while(reader.hasNext()){
				List<String> line = reader.next();
				Edge e = edges.get((int)Double.parseDouble(line.get(1)));
				e.material = line.get(2);
				e.diameter = Double.parseDouble(line.get(4)==null? "0" : line.get(4));
				e.length = Double.parseDouble(line.get(5));
				System.out.println(i++ + " " + e.id + " " + e.length + " " + e.a.id + " " + e.b.id);
			}
			reader.close();
			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void filterUnusedEdges(Map<Integer, Edge> edges, String file){
		ExcelReader reader = new ExcelReader();
		try {
			reader.init(file);
			reader.hasNext();reader.next(); //skip first line
			
			int i = 0;
			while(reader.hasNext()){
				List<String> line = reader.next();
				int id = (int)Double.parseDouble(line.get(1));
				if (!line.get(7).equals("В ЭКСПЛУАТАЦИИ")
					//|| "г.Санкт-Петербург, Промышленная улица".equals(line.get(3))
					//|| !line.get(9).equals("Сектор 31")
				) {
					edges.remove(id);
					System.out.println(i++ + " id " + id + " filtered - " + line.get(7) + " " + line.get(9));
				} else {
					System.out.println(i++ + " id " + id + " passed");
				}
			}
			reader.close();
			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
