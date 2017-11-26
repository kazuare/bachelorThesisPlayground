package bachelorThesisPlayground.readers;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import bachelorThesisPlayground.Edge;
import bachelorThesisPlayground.Vertex;

public class CSVGraphReader {
	public static void populateVertexCoordinates(Map<Integer, Edge> edges, String file){
		try (BufferedReader br = new BufferedReader(new FileReader(file))) {
			String sCurrentLine;
			
			Map<Integer, Vertex> vertexes = new HashMap<>();
			
			br.readLine();
			while ((sCurrentLine = br.readLine()) != null) {
				int id = Integer.parseInt(sCurrentLine.substring(0, sCurrentLine.indexOf(';')));
				sCurrentLine = sCurrentLine.substring(sCurrentLine.indexOf(';') + 1);
				
				double x = Double.parseDouble(sCurrentLine.substring(0, sCurrentLine.indexOf(';')));
				sCurrentLine = sCurrentLine.substring(sCurrentLine.indexOf(';') + 1);
				
				double y = Double.parseDouble(sCurrentLine);
				
				vertexes.put(id, new Vertex(id, x, y));
			}
			
			for (Edge e : edges.values()) {
				if (vertexes.containsKey(e.a.id)) {
					e.a.x = vertexes.get(e.a.id).x;
					e.a.y = vertexes.get(e.a.id).y;
				}
				if (vertexes.containsKey(e.b.id)) {
					e.b.x = vertexes.get(e.b.id).x;
					e.b.y = vertexes.get(e.b.id).y;
				}
			}
			int i = 0; 
			for (Edge e : edges.values()) {
				System.out.println(i++ + " id:" + e.id + " len:" + e.length + 
						" a:" + e.a.id + " " + e.a.x + " " + e.a.y + 
						" b:" + e.b.id + " " + e.b.x + " " + e.b.y);
			}
			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static Vertex getPointWithId(String file, int idToFind){
		try (BufferedReader br = new BufferedReader(new FileReader(file))) {
			String sCurrentLine;
	
			br.readLine();
			while ((sCurrentLine = br.readLine()) != null) {
				int id = Integer.parseInt(sCurrentLine.substring(0, sCurrentLine.indexOf(';')));
				
				if (id == idToFind) {

					sCurrentLine = sCurrentLine.substring(sCurrentLine.indexOf(';') + 1);
					
					double x = Double.parseDouble(sCurrentLine.substring(0, sCurrentLine.indexOf(';')));
					sCurrentLine = sCurrentLine.substring(sCurrentLine.indexOf(';') + 1);
					
					double y = Double.parseDouble(sCurrentLine);
					
					return new Vertex(id, x, y);
				} else continue;
				
			}
			
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
}
