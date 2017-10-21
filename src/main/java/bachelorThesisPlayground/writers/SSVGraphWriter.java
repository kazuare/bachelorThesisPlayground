package bachelorThesisPlayground.writers;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

import bachelorThesisPlayground.Edge;

public class SSVGraphWriter {
	public static void writeGraphForLayout(Map<Integer, Edge> edges){
		try {

			PrintWriter writer = new PrintWriter("C:\\Users\\test\\Desktop\\диплом\\graph_for_layout.ssv", "UTF-8");
						
			boolean first = true;
			for (Edge e : edges.values()) {
				if (first) {
					first = false;
				} else {
					writer.println();
				}
				writer.print(e.id + " " + e.length + " ");
				writer.print(e.a.id + " " + e.a.x + " " + e.a.y + " ");
				writer.print(e.b.id + " " + e.b.x + " " + e.b.y);
			}
			
			System.out.println("finished");
			
			writer.close();			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
