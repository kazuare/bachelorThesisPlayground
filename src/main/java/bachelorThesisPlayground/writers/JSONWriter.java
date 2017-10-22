package bachelorThesisPlayground.writers;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import bachelorThesisPlayground.Edge;
import bachelorThesisPlayground.Vertex;

public class JSONWriter {
	public static void write(Map<Integer, Edge> edges, String file){
		Set<Vertex> points = new HashSet<>();
		for (Edge e: edges.values()) {
			if(!points.contains(e.a)) points.add(e.a);
			if(!points.contains(e.b)) points.add(e.b);
		}
		Vertex[] array = points.toArray(new Vertex[points.size()]);

		try {

			PrintWriter writer = new PrintWriter(file, "UTF-8");
			
			writer.println("{");
			writer.println("\"nodes\":[");
			for (int i = 0; i < array.length; i++) {
				writer.print("{\"id\": \"" + array[i].id + "\",");
				if (!array[i].fixed) {
					writer.print("\"x\": " + array[i].x + ", ");
					writer.print("\"y\": " + array[i].y);
				} else {
					writer.print("\"fx\": " + array[i].x + ", ");
					writer.print("\"fy\": " + array[i].y);
				}				
				writer.print(" }");			
				
				if (i != array.length - 1) {
					writer.println(",");
				} else {
					writer.println();
				}
			}
			writer.println("],");
			
			writer.println("\"links\":[");
			Edge[] edgeArray = edges.values().toArray(new Edge[edges.values().size()]);
			for (int i = 0; i < edgeArray.length; i++) {
				writer.print("{\"source\": \""
								+ edgeArray[i].a.id 
								+"\", \"target\": \""
								+ edgeArray[i].b.id 
								+"\", \"value\": "
								+ edgeArray[i].length 
								+"}");
				if (i != edgeArray.length - 1) {
					writer.println(",");
				} else {
					writer.println();
				}
			}
			writer.println("]");
			
			writer.print("}");
			writer.close();			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	public static void writeFull(Map<Integer, Edge> edges, String file){
		Set<Vertex> points = new HashSet<>();
		for (Edge e: edges.values()) {
			if(!points.contains(e.a)) points.add(e.a);
			if(!points.contains(e.b)) points.add(e.b);
		}
		Vertex[] array = points.toArray(new Vertex[points.size()]);

		try {

			PrintWriter writer = new PrintWriter(file, "UTF-8");
			
			writer.println("{");
			writer.println("\"nodes\":[");
			for (int i = 0; i < array.length; i++) {
				writer.print("{\"id\": \"" + array[i].id + "\",");
				if (!array[i].fixed) {
					writer.print("\"x\": " + array[i].x + ", ");
					writer.print("\"y\": " + array[i].y);
				} else {
					writer.print("\"fx\": " + array[i].x + ", ");
					writer.print("\"fy\": " + array[i].y);
				}				
				writer.print(" }");			
				
				if (i != array.length - 1) {
					writer.println(",");
				} else {
					writer.println();
				}
			}
			writer.println("],");
			
			writer.println("\"links\":[");
			Edge[] edgeArray = edges.values().toArray(new Edge[edges.values().size()]);
			for (int i = 0; i < edgeArray.length; i++) {
				writer.print("{ "
								+"\"material\":\"" 
								+edgeArray[i].material.replace("\"", "\\\"")
								+"\", "
								+"\"diameter\":" 
								+edgeArray[i].diameter
								+" , "
								+"\"edgeId\":\"" 
								+edgeArray[i].id
								+"\", "
								+"\"source\": \""
								+ edgeArray[i].a.id 
								+"\", \"target\": \""
								+ edgeArray[i].b.id 
								+"\", \"value\": "
								+ edgeArray[i].length 
								+"}");
				if (i != edgeArray.length - 1) {
					writer.println(",");
				} else {
					writer.println();
				}
			}
			writer.println("]");
			
			writer.print("}");
			writer.close();			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
