package bachelorThesisPlayground.writers;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import bachelorThesisPlayground.Edge;
import bachelorThesisPlayground.Vertex;

public class CSVGraphWriter {
	public static void writeEdgesForAnalyst(List<Edge> edges){
		try {
			PrintWriter writer = new PrintWriter("C:\\Users\\test\\Desktop\\диплом\\edges_for_analyst.csv", "UTF-8");
						
			boolean first = true;
			for (Edge e : edges) {
				if (first) {
					first = false;
					writer.print("edgeId;aId;bId");
				}
				writer.println();
				writer.print(e.id + ";");
				writer.print(e.a.oldId + ";");
				writer.print(e.b.oldId);
			}
			
			System.out.println("finished");
			
			writer.close();			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void writeNodesForAnalyst(List<Vertex> points){
		try {
			PrintWriter writer = new PrintWriter("C:\\Users\\test\\Desktop\\диплом\\nodes_for_analyst.csv", "UTF-8");
						
			DecimalFormat df = new DecimalFormat("#");
	        df.setMaximumFractionDigits(8);
			
			
			boolean first = true;
			for (Vertex p : points) {
				if (first) {
					first = false;
					writer.print("id;x;y;fixedNode;type;canBeLocked;pumpStationEntry;pumpStationExit");
				}
				writer.println();
				writer.print(p.oldId + ";" + df.format(p.x) + ";" + df.format(p.y) + ";" + p.fixed 
						+ ";" + p.type + ";" + p.canBeLocked + ";" + p.pumpStationEntry + ";" + p.pumpStationExit);
			}			
			System.out.println("finished");
			
			writer.close();			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
