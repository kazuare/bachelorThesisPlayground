package bachelorThesisPlayground.readers;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONObject;

import bachelorThesisPlayground.Edge;
import bachelorThesisPlayground.Vertex;

public class JsonGraphReader {
	public static Map<Integer, Edge> readGraph(String file) {
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
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		JSONObject obj = new JSONObject("{\"name\": \"John\"}");

		System.out.println(obj.getString("name")); //John
		
		return null;	
	}
	public static Map<Integer, Edge> readGraphInfo(String file) {
		return null;	
	}
}
