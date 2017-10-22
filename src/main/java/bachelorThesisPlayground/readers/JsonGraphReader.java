package bachelorThesisPlayground.readers;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;

import bachelorThesisPlayground.Edge;
import bachelorThesisPlayground.Vertex;

public class JsonGraphReader {
	public static ArrayList<Vertex> readNodes(String file) {
		try {
			BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file), "UTF-8"));
			//d3 output is one-liner
			String json = br.readLine();
			
			JSONArray graph = new JSONArray(json);
			
			ArrayList<Vertex> points = new ArrayList<>();
			for (Object node : graph) {
				JSONObject obj = (JSONObject)node;
				Vertex toAdd = new Vertex(obj.getInt("id"), obj.getDouble("x"), obj.getDouble("y"));
				if(obj.has("fx")){
					toAdd.fixed = true;
				}
				points.add(toAdd);
				System.out.println("parsing vertex: " + toAdd);
			}
			
			return points;
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;	
	}
	
	public static ArrayList<Edge> readEdges(String file) {
		try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file), "UTF-8"))) {
			String line = null;			
			while (!"\"links\":[".equals(line)) {
				line = br.readLine();
			}
			
			ArrayList<Edge> edges = new ArrayList<>();
			line = br.readLine();
			while (line.charAt(0)!=']') {
				JSONObject obj = new JSONObject(line.substring(0,line.indexOf("}")+1));
				Edge toAdd = new Edge(obj.getInt("edgeId"), obj.getInt("source"), obj.getInt("target"));
				toAdd.diameter = obj.getDouble("diameter");
				toAdd.length = obj.getDouble("value");
				toAdd.material = obj.getString("material");
				edges.add(toAdd);
				System.out.println("parsing edge: " + toAdd);
				line = br.readLine();
			}
			Collections.sort(edges, (a,b)->Integer.compare(a.id, b.id));
			return edges;
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return null;	
	}
}
