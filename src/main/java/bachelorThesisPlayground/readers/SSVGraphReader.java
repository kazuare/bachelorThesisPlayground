package bachelorThesisPlayground.readers;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Scanner;

import bachelorThesisPlayground.Edge;
import bachelorThesisPlayground.Vertex;

public class SSVGraphReader {
	public static Map<Integer, Edge> readEdges(){
		try {
			File file = new File("C:\\Users\\test\\Desktop\\диплом\\graph_for_layout.ssv");
			FileInputStream fis  = new FileInputStream(file);

			Scanner scanner = new Scanner(fis).useLocale(Locale.US);
			Map<Integer, Edge> edges = new HashMap<>();
			
			while(scanner.hasNext()){
				Edge e = new Edge(scanner.nextInt());
				e.length = scanner.nextDouble();
				e.a = new Vertex(scanner.nextInt(), scanner.nextDouble(), scanner.nextDouble());
				e.b = new Vertex(scanner.nextInt(), scanner.nextDouble(), scanner.nextDouble());
				edges.put(e.id, e);
			}			
			scanner.close();
			fis.close();
			return edges;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
	public static Map<Integer, Edge> replaceCoordinates(Map<Integer, Edge> edges){
		try {
			File file = new File("C:\\Users\\test\\Desktop\\диплом\\positioned_points.ssv");
			FileInputStream fis  = new FileInputStream(file);

			Scanner scanner = new Scanner(fis).useLocale(Locale.US);
			ArrayList<Vertex> points = new ArrayList<>();
			while(scanner.hasNext())
				points.add(new Vertex(scanner.nextInt(), scanner.nextDouble(), scanner.nextDouble()));
			
			scanner.close();
			fis.close();		
			
			for (Edge e : edges.values()) {
				e.a.x = points.get(e.a.id).x;
				e.a.y = points.get(e.a.id).y;
				
				e.b.x = points.get(e.b.id).x;
				e.b.y = points.get(e.b.id).y;
			}
							
			return edges;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
}
